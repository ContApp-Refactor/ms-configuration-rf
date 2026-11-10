package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.mapper.BillingDocumentPersistenceMapper;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

/**
 * Persistencia de las condiciones de pago: columnas nuevas NULABLES en {@code billing_documents},
 * ida y vuelta por el adaptador real y documentos sin forma de pago (compras, historicas).
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billing_jpa_pay;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = BillingJpaTestConfig.class)
class BillingDocumentPaymentPersistenceTest {

    private final BillingDocumentPersistenceMapper mapper = new BillingDocumentPersistenceMapper();

    @Autowired
    private BillingDocumentPersistenceAdapter adapter;
    @Autowired
    private EntityManager em;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeTransaction
    void bindTenantBeforeTheSessionOpens() {
        TenantContext.setTenantId(TENANT_A);
    }

    @AfterTransaction
    void clearTenantAfterTransaction() {
        TenantContext.clear();
    }

    @AfterEach
    void cleanUp() {
        TenantContext.clear();
        jdbc.update("delete from billing_document_lines");
        jdbc.update("delete from billing_documents");
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private static BillingDocument saleWith(PaymentTerms payment) {
        BillingDocument doc = readySaleDraft();
        doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(), payment,
                doc.updatedAt(), doc.updatedBy());
        return doc;
    }

    // ---------- mapper (sin base de datos) ----------

    @Test
    void the_mapper_copies_method_id_and_the_frozen_name_and_restores_them() {
        BillingDocument doc = saleWith(PaymentTerms.draft(7L));
        doc.freezePayment("Efectivo");

        BillingDocumentEntity entity = mapper.newEntity(doc);

        assertThat(entity.getPaymentMethodId()).isEqualTo(7L);
        assertThat(entity.getPaymentMethodName()).isEqualTo("Efectivo");
        assertThat(mapper.toDomain(entity).payment()).isEqualTo(new PaymentTerms(7L, "Efectivo"));
    }

    @Test
    void the_entity_has_no_bank_account_fields() {
        assertThat(java.util.Arrays.stream(BillingDocumentEntity.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName))
                .noneMatch(name -> name.toLowerCase().contains("bankaccount"));
    }

    @Test
    void a_sale_without_payment_maps_all_new_columns_to_null() {
        BillingDocumentEntity sale = mapper.newEntity(saleWith(null));

        assertThat(sale.getPaymentMethodId()).isNull();
        assertThat(sale.getPaymentMethodName()).isNull();
    }

    @Test
    void a_purchase_invoice_round_trips_its_payment() {
        BillingDocument purchase = emittedPurchase();
        purchase.payment();
        BillingDocumentEntity entity = mapper.newEntity(purchase);

        assertThat(entity.getPaymentMethodId()).isEqualTo(1L);
        assertThat(mapper.toDomain(entity).payment().paymentMethodId()).isEqualTo(1L);
    }

    @Test
    void a_row_from_before_this_change_restores_without_payment() {
        BillingDocumentEntity entity = mapper.newEntity(saleWith(null));

        assertThat(mapper.toDomain(entity).payment()).isNull();
    }

    // ---------- adaptador real (H2) ----------

    @Test
    void the_method_with_the_frozen_name_survives_a_round_trip() {
        BillingDocument doc = saleWith(methodTerms(7L));
        doc.freezePaymentMethodName("Transferencia bancaria");
        BillingDocument saved = adapter.save(doc);
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.payment().paymentMethodId()).isEqualTo(7L);
        assertThat(found.payment().paymentMethodName()).isEqualTo("Transferencia bancaria");
    }

    @Test
    void a_frozen_method_name_of_a_non_bank_method_survives_a_round_trip() {
        BillingDocument cashOnly = saleWith(methodTerms(8L));
        cashOnly.freezePayment("Efectivo");
        BillingDocument savedCash = adapter.save(cashOnly);
        flushAndClear();

        assertThat(adapter.findById(ENTERPRISE_ID, savedCash.id()).orElseThrow().payment())
                .isEqualTo(new PaymentTerms(8L, "Efectivo"));
    }

    @Test
    void a_draft_without_payment_stores_null_columns_and_reads_back_without_payment() {
        BillingDocument saved = adapter.save(draftSale());
        flushAndClear();

        assertThat(adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow().payment()).isNull();
        assertThat(jdbc.queryForObject("select payment_method_id from billing_documents where id = ?", Long.class,
                saved.id())).isNull();
    }

    @Test
    void editing_the_draft_changes_the_payment_in_place() {
        BillingDocument saved = adapter.save(saleWith(methodTerms(1L)));
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(loaded.counterparty(), TODAY, null, loaded.lines(), methodTerms(9L),
                loaded.updatedAt().plusSeconds(5), "otro");
        adapter.save(loaded);
        flushAndClear();

        PaymentTerms payment = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow().payment();
        assertThat(payment.paymentMethodId()).isEqualTo(9L);
    }
}
