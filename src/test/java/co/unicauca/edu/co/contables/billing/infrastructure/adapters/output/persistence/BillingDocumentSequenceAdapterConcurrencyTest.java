package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * EMT-3 / RN-16 / D11 con bloqueos reales (H2 en modo PostgreSQL): consecutivos sin huecos ni
 * duplicados con hilos concurrentes, reuso del numero tras rollback, independencia por tipo /
 * empresa / tenant, carrera al crear la fila y espera acotada del bloqueo. Cada operacion corre en su
 * propia transaccion (sin transaccion de prueba) y el tenant se fija ANTES de abrirla.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billing_seq;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = BillingJpaTestConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BillingDocumentSequenceAdapterConcurrencyTest {

    private static final DocumentType SALE = DocumentType.SALE_INVOICE;

    @Autowired
    private BillingDocumentSequenceAdapter adapter;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() {
        TenantContext.clear();
        jdbc.update("delete from billing_document_sequences");
    }

    // ---------- helpers ----------

    private <T> T inTenantTx(String tenant, Supplier<T> action) {
        TenantContext.setTenantId(tenant);
        try {
            return new TransactionTemplate(transactionManager).execute(status -> action.get());
        } finally {
            TenantContext.clear();
        }
    }

    private long next(String tenant, String enterpriseId, DocumentType type) {
        return inTenantTx(tenant, () -> adapter.nextValue(enterpriseId, type));
    }

    private Long lastNumber(String tenant, String enterpriseId, DocumentType type) {
        return jdbc.queryForObject("select last_number from billing_document_sequences "
                + "where tenant_id = ? and enterprise_id = ? and document_type = ?", Long.class,
                tenant, enterpriseId, type.name());
    }

    private long rowCount() {
        Long value = jdbc.queryForObject("select count(*) from billing_document_sequences", Long.class);
        return value == null ? 0L : value;
    }

    private void seedUpTo(long last) {
        for (long i = 1; i <= last; i++) {
            assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(i);
        }
    }

    /** Lanza {@code threads} hilos que arrancan a la vez; cada uno obtiene un numero y lo retiene {@code holdMs}. */
    private List<Long> concurrentNext(int threads, long holdMs) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Callable<Long> task = () -> {
                    ready.countDown();
                    go.await();
                    return inTenantTx(TENANT_A, () -> {
                        long value = adapter.nextValue(ENTERPRISE_ID, SALE);
                        sleep(holdMs);
                        return value;
                    });
                };
                futures.add(pool.submit(task));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            go.countDown();
            List<Long> values = new ArrayList<>();
            for (Future<Long> future : futures) {
                values.add(future.get(30, TimeUnit.SECONDS));
            }
            return values;
        } finally {
            pool.shutdownNow();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- consecutivos ----------

    @Test
    void EMT_3_the_first_value_is_one_and_the_following_are_consecutive() {
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(1L);
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(2L);
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(3L);

        assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(3L);
        String prefix = jdbc.queryForObject("select prefix from billing_document_sequences", String.class);
        assertThat(prefix).isEqualTo("FV");
    }

    @Test
    void EMT_3_sequences_are_independent_per_type_enterprise_and_tenant() {
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(1L);
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(2L);

        assertThat(next(TENANT_A, ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE)).isEqualTo(1L);
        assertThat(next(TENANT_A, ENTERPRISE_ID, DocumentType.SALE_RETURN)).isEqualTo(1L);
        assertThat(next(TENANT_A, OTHER_ENTERPRISE_ID, SALE)).isEqualTo(1L);
        assertThat(next(TENANT_B, ENTERPRISE_ID, SALE)).isEqualTo(1L);

        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(3L);
        assertThat(rowCount()).isEqualTo(5L);
        String purchasePrefix = jdbc.queryForObject(
                "select prefix from billing_document_sequences where document_type = 'PURCHASE_INVOICE'", String.class);
        String returnPrefix = jdbc.queryForObject(
                "select prefix from billing_document_sequences where document_type = 'SALE_RETURN'", String.class);
        assertThat(purchasePrefix).isEqualTo("FC");
        assertThat(returnPrefix).isEqualTo("NC");
    }

    // ---------- rollback ----------

    @Test
    void EMT_3_a_rolled_back_transaction_does_not_consume_the_number() {
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(1L);

        TenantContext.setTenantId(TENANT_A);
        long rolledBack;
        try {
            rolledBack = new TransactionTemplate(transactionManager).execute(status -> {
                long value = adapter.nextValue(ENTERPRISE_ID, SALE);
                status.setRollbackOnly();
                return value;
            });
        } finally {
            TenantContext.clear();
        }

        assertThat(rolledBack).isEqualTo(2L);
        assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(1L);
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(2L);
    }

    @Test
    void EMT_3_rolling_back_the_very_first_emission_still_leaves_the_sequence_ready_for_one() {
        TenantContext.setTenantId(TENANT_A);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                assertThat(adapter.nextValue(ENTERPRISE_ID, SALE)).isEqualTo(1L);
                status.setRollbackOnly();
            });
        } finally {
            TenantContext.clear();
        }

        assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isZero();
        assertThat(next(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(1L);
    }

    // ---------- concurrencia ----------

    @Test
    @Timeout(60)
    void EMT_3_two_concurrent_emissions_after_seven_get_eight_and_nine() throws Exception {
        seedUpTo(7);

        List<Long> values = concurrentNext(2, 300);

        assertThat(values).containsExactlyInAnyOrder(8L, 9L);
        assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(9L);
    }

    @Test
    @Timeout(60)
    void EMT_3_many_concurrent_emissions_get_distinct_numbers_without_gaps() throws Exception {
        seedUpTo(7);

        List<Long> values = concurrentNext(5, 50);

        assertThat(values).containsExactlyInAnyOrder(8L, 9L, 10L, 11L, 12L);
        assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(12L);
    }

    @Test
    @Timeout(60)
    void EMT_3_concurrent_first_emissions_race_to_create_the_row_and_still_get_consecutive_numbers()
            throws Exception {
        List<Long> values = concurrentNext(3, 50);

        assertThat(values).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(rowCount()).isEqualTo(1L);
        assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(3L);
    }

    @Test
    @Timeout(90)
    void EMT_3_a_waiter_that_cannot_obtain_the_lock_in_time_fails_with_sequence_busy() throws Exception {
        seedUpTo(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch holderHasLock = new CountDownLatch(1);
        CountDownLatch releaseHolder = new CountDownLatch(1);
        try {
            Future<Long> holder = pool.submit(() -> inTenantTx(TENANT_A, () -> {
                long value = adapter.nextValue(ENTERPRISE_ID, SALE);
                holderHasLock.countDown();
                await(releaseHolder);
                return value;
            }));
            assertThat(holderHasLock.await(10, TimeUnit.SECONDS)).isTrue();

            Future<Throwable> waiter = pool.submit(() -> catchThrowable(() -> next(TENANT_A, ENTERPRISE_ID, SALE)));
            Throwable failure = waiter.get(60, TimeUnit.SECONDS);
            releaseHolder.countDown();

            assertThat(failure).isInstanceOf(BillingException.class);
            assertThat(((BillingException) failure).errorCode()).isEqualTo(BillingErrorCode.BILLING_SEQUENCE_BUSY);
            assertThat(holder.get(10, TimeUnit.SECONDS)).isEqualTo(2L);
            assertThat(lastNumber(TENANT_A, ENTERPRISE_ID, SALE)).isEqualTo(2L);
        } finally {
            releaseHolder.countDown();
            pool.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
