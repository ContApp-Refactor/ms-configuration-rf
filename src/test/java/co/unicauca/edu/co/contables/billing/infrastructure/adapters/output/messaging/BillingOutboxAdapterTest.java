package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_B;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.application.ports.output.model.OutboxClaim;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentAnnulled;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentEmitted;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.BillingDocumentPersistenceAdapter;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.BillingJpaTestConfig;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingOutboxEventEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingOutboxEventJpaRepository;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Outbox transaccional (EVT-1, EVT-6, TEN-1): el append escribe una fila PENDING en la misma
 * transaccion del documento, el rollback no deja fila, UNIQUE(tenant, event_id) y aislamiento por
 * tenant (H4b), y el lado relay de H6: reclamo nativo entre tenants, bloqueo por fila bajo el
 * tenant de la fila y marcado de publicado / intento fallido / fallo definitivo.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billing_outbox;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = BillingJpaTestConfig.class)
class BillingOutboxAdapterTest {

    @Autowired
    private BillingOutboxAdapter adapter;
    @Autowired
    private BillingOutboxEventJpaRepository repository;
    @Autowired
    private BillingDocumentPersistenceAdapter documents;
    @Autowired
    private BillingEventSerializer serializer;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PlatformTransactionManager transactionManager;

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
        jdbc.update("delete from billing_outbox_events");
        jdbc.update("delete from billing_document_lines");
        jdbc.update("delete from billing_documents");
    }

    private long count(String table) {
        Long value = jdbc.queryForObject("select count(*) from " + table, Long.class);
        return value == null ? 0L : value;
    }

    private <T> T asTenant(String tenant, Supplier<T> action) {
        TenantContext.setTenantId(tenant);
        try {
            return new TransactionTemplate(transactionManager).execute(status -> action.get());
        } finally {
            TenantContext.clear();
        }
    }

    private static BillingDocumentEmitted emittedEvent(int eventNo) {
        return new BillingDocumentEmitted(uuid(eventNo), LATER, withVersion(emittedSale(), 1L), null);
    }

    // ---------- append ----------

    @Test
    void EVT_1_append_stores_a_pending_row_with_attempts_zero_and_catalog_routing() {
        BillingDocumentEmitted event = emittedEvent(50);

        adapter.append(event);

        List<BillingOutboxEventEntity> rows = repository.findAll();
        assertThat(rows).hasSize(1);
        BillingOutboxEventEntity row = rows.get(0);
        assertThat(row.getEventId()).isEqualTo(uuid(50).toString());
        assertThat(row.getEventType()).isEqualTo("FacturaVentaEmitida");
        assertThat(row.getEventVersion()).isEqualTo(1);
        assertThat(row.getAggregateType()).isEqualTo("SALE_INVOICE");
        assertThat(row.getAggregateId()).isEqualTo(event.document().id());
        assertThat(row.getEnterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(row.getExchangeName()).isEqualTo("billing.documents.exchange");
        assertThat(row.getRoutingKey()).isEqualTo("sale-invoice.emitted.v1");
        assertThat(row.getStatus()).isEqualTo("PENDING");
        assertThat(row.getAttempts()).isZero();
        assertThat(row.getLastError()).isNull();
        assertThat(row.getPublishedAt()).isNull();
        assertThat(row.getCreatedAt()).isNotNull();
        assertThat(row.getNextAttemptAt()).isEqualTo(row.getCreatedAt());
        assertThat(row.getTenantId()).isEqualTo(TENANT_A);
    }

    @Test
    void EVT_3_payload_is_the_serialized_envelope_for_the_current_tenant() {
        BillingDocumentEmitted event = emittedEvent(51);

        adapter.append(event);

        BillingOutboxEventEntity row = repository.findAll().get(0);
        assertThat(row.getPayload()).isEqualTo(serializer.serialize(event, TENANT_A));
        assertThat(row.getPayload()).contains("\"tenantId\":\"" + TENANT_A + "\"");
        assertThat(row.getPayload().length()).isGreaterThan(1000);
    }

    @Test
    void EVT_2_annulled_event_uses_the_annulled_pair() {
        adapter.append(new BillingDocumentAnnulled(uuid(52), LATER, withVersion(annulledSale(), 2L), null));

        BillingOutboxEventEntity row = repository.findAll().get(0);
        assertThat(row.getEventType()).isEqualTo("FacturaVentaAnulada");
        assertThat(row.getRoutingKey()).isEqualTo("sale-invoice.annulled.v1");
        assertThat(row.getStatus()).isEqualTo("PENDING");
    }

    // ---------- atomicidad con el documento ----------

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_1_rollback_leaves_neither_document_nor_outbox_row() {
        BillingDocument doc = emittedSale();
        TenantContext.setTenantId(TENANT_A);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                BillingDocument saved = documents.save(doc);
                adapter.append(new BillingDocumentEmitted(uuid(53), NOW, saved, null));
                status.setRollbackOnly();
            });
        } finally {
            TenantContext.clear();
        }

        assertThat(count("billing_outbox_events")).isZero();
        assertThat(count("billing_documents")).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_1_commit_persists_document_and_outbox_row_together() {
        BillingDocument doc = emittedSale();
        TenantContext.setTenantId(TENANT_A);
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                BillingDocument saved = documents.save(doc);
                adapter.append(new BillingDocumentEmitted(uuid(54), NOW, saved, null));
            });
        } finally {
            TenantContext.clear();
        }

        assertThat(count("billing_outbox_events")).isEqualTo(1);
        assertThat(count("billing_documents")).isEqualTo(1);
    }

    // ---------- unicidad y tenant ----------

    @Test
    void EVT_6_same_event_id_twice_in_the_same_tenant_violates_the_unique_constraint() {
        BillingDocumentEmitted event = emittedEvent(55);
        adapter.append(event);

        assertThatThrownBy(() -> adapter.append(event)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_6_same_event_id_is_allowed_in_different_tenants() {
        BillingDocumentEmitted event = emittedEvent(56);

        asTenant(TENANT_A, () -> {
            adapter.append(event);
            return null;
        });
        asTenant(TENANT_B, () -> {
            adapter.append(event);
            return null;
        });

        assertThat(count("billing_outbox_events")).isEqualTo(2);
        assertThat(jdbc.queryForList("select distinct tenant_id from billing_outbox_events order by tenant_id",
                String.class)).containsExactly(TENANT_A, TENANT_B);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_rows_of_another_tenant_are_invisible_through_the_repository() {
        asTenant(TENANT_A, () -> {
            adapter.append(emittedEvent(57));
            return null;
        });

        assertThat((long) asTenant(TENANT_A, repository::count)).isEqualTo(1L);
        assertThat((long) asTenant(TENANT_B, repository::count)).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_5_append_without_tenant_in_the_context_fails() {
        BillingDocumentEmitted event = emittedEvent(58);
        TenantContext.clear();

        assertThatThrownBy(() -> adapter.append(event)).isInstanceOf(RuntimeException.class);
        assertThat(count("billing_outbox_events")).isZero();
    }

    // ---------- H6: lado relay (reclamo entre tenants, bloqueo, marcado) ----------

    private static final Instant BASE = Instant.parse("2026-10-22T15:00:00Z");

    /** Inserta una fila cruda del outbox bajo el tenant dado y devuelve su id. */
    private long insertRow(String tenant, String eventId, String status, Instant nextAttemptAt, int attempts) {
        return asTenant(tenant, () -> {
            BillingOutboxEventEntity row = new BillingOutboxEventEntity();
            row.setEventId(eventId);
            row.setEventType("FacturaVentaEmitida");
            row.setEventVersion(1);
            row.setAggregateType("SALE_INVOICE");
            row.setAggregateId(uuid(1));
            row.setEnterpriseId(ENTERPRISE_ID);
            row.setExchangeName("billing.documents.exchange");
            row.setRoutingKey("sale-invoice.emitted.v1");
            row.setPayload("{\"eventId\":\"" + eventId + "\"}");
            row.setStatus(status);
            row.setAttempts(attempts);
            row.setNextAttemptAt(nextAttemptAt);
            row.setCreatedAt(BASE);
            return repository.saveAndFlush(row).getId();
        });
    }

    private BillingOutboxEventEntity reload(String tenant, long id) {
        return asTenant(tenant, () -> repository.findById(id).orElseThrow());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_native_claim_sees_due_pending_rows_of_every_tenant_without_tenant_in_context() {
        long a = insertRow(TENANT_A, "ea-1", "PENDING", BASE.minusSeconds(30), 0);
        long b = insertRow(TENANT_B, "eb-1", "PENDING", BASE.minusSeconds(10), 2);
        TenantContext.clear();

        List<OutboxClaim> claims = adapter.findDueClaims(BASE, 50);

        assertThat(claims).containsExactly(new OutboxClaim(a, TENANT_A), new OutboxClaim(b, TENANT_B));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_native_claim_orders_by_next_attempt_then_id_and_honors_the_limit() {
        long late = insertRow(TENANT_A, "e-late", "PENDING", BASE.minusSeconds(5), 0);
        long early1 = insertRow(TENANT_B, "e-early1", "PENDING", BASE.minusSeconds(60), 0);
        long early2 = insertRow(TENANT_A, "e-early2", "PENDING", BASE.minusSeconds(60), 0);
        TenantContext.clear();

        assertThat(adapter.findDueClaims(BASE, 50)).extracting(OutboxClaim::id)
                .containsExactly(early1, early2, late);
        assertThat(adapter.findDueClaims(BASE, 2)).extracting(OutboxClaim::id).containsExactly(early1, early2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_native_claim_excludes_published_failed_and_not_yet_due_rows() {
        insertRow(TENANT_A, "e-pub", "PUBLISHED", BASE.minusSeconds(60), 1);
        insertRow(TENANT_A, "e-fail", "FAILED", BASE.minusSeconds(60), 10);
        insertRow(TENANT_B, "e-future", "PENDING", BASE.plusSeconds(60), 1);
        long due = insertRow(TENANT_B, "e-due", "PENDING", BASE, 0);
        TenantContext.clear();

        assertThat(adapter.findDueClaims(BASE, 50)).containsExactly(new OutboxClaim(due, TENANT_B));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_jpql_cannot_see_other_tenants_but_the_native_claim_can() {
        insertRow(TENANT_A, "ea-2", "PENDING", BASE.minusSeconds(30), 0);
        insertRow(TENANT_B, "eb-2", "PENDING", BASE.minusSeconds(30), 0);

        assertThat((long) asTenant(TENANT_A, repository::count)).isEqualTo(1L);
        TenantContext.clear();
        assertThat(adapter.findDueClaims(BASE, 50)).hasSize(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_lockPending_returns_the_whole_message_under_the_tenant_of_the_row() {
        long id = insertRow(TENANT_A, "ea-3", "PENDING", Instant.now().minusSeconds(60), 3);

        Optional<PendingOutboxMessage> locked = asTenant(TENANT_A, () -> adapter.lockPending(id));

        assertThat(locked).isPresent();
        PendingOutboxMessage message = locked.get();
        assertThat(message.id()).isEqualTo(id);
        assertThat(message.eventId()).isEqualTo("ea-3");
        assertThat(message.eventType()).isEqualTo("FacturaVentaEmitida");
        assertThat(message.tenantId()).isEqualTo(TENANT_A);
        assertThat(message.exchange()).isEqualTo("billing.documents.exchange");
        assertThat(message.routingKey()).isEqualTo("sale-invoice.emitted.v1");
        assertThat(message.payloadJson()).isEqualTo("{\"eventId\":\"ea-3\"}");
        assertThat(message.attempts()).isEqualTo(3);
        assertThat(message.nextAttemptAt()).isNotNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_lockPending_of_a_row_of_another_tenant_is_empty() {
        long bRow = insertRow(TENANT_B, "eb-3", "PENDING", Instant.now().minusSeconds(60), 0);

        assertThat(asTenant(TENANT_A, () -> adapter.lockPending(bRow))).isEmpty();
        assertThat(asTenant(TENANT_B, () -> adapter.lockPending(bRow))).isPresent();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_lockPending_is_empty_for_published_failed_future_and_unknown_rows() {
        long published = insertRow(TENANT_A, "e-p", "PUBLISHED", Instant.now().minusSeconds(60), 1);
        long failed = insertRow(TENANT_A, "e-f", "FAILED", Instant.now().minusSeconds(60), 10);
        long future = insertRow(TENANT_A, "e-t", "PENDING", Instant.now().plusSeconds(3600), 1);

        assertThat(asTenant(TENANT_A, () -> adapter.lockPending(published))).isEmpty();
        assertThat(asTenant(TENANT_A, () -> adapter.lockPending(failed))).isEmpty();
        assertThat(asTenant(TENANT_A, () -> adapter.lockPending(future))).isEmpty();
        assertThat(asTenant(TENANT_A, () -> adapter.lockPending(987_654L))).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_markPublished_sets_status_timestamp_and_clears_the_error() {
        long id = insertRow(TENANT_A, "ea-4", "PENDING", Instant.now().minusSeconds(60), 2);
        asTenant(TENANT_A, () -> {
            adapter.markAttemptFailed(id, 3, BASE, false, "previo");
            return null;
        });
        Instant at = Instant.parse("2026-10-22T16:30:00Z");

        asTenant(TENANT_A, () -> {
            adapter.markPublished(id, at);
            return null;
        });

        BillingOutboxEventEntity row = reload(TENANT_A, id);
        assertThat(row.getStatus()).isEqualTo("PUBLISHED");
        assertThat(row.getPublishedAt()).isEqualTo(at);
        assertThat(row.getLastError()).isNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_markAttemptFailed_keeps_the_row_pending_with_the_new_schedule() {
        long id = insertRow(TENANT_A, "ea-5", "PENDING", Instant.now().minusSeconds(60), 0);
        Instant next = Instant.parse("2026-10-22T15:00:05Z");

        asTenant(TENANT_A, () -> {
            adapter.markAttemptFailed(id, 1, next, false, "nack del broker");
            return null;
        });

        BillingOutboxEventEntity row = reload(TENANT_A, id);
        assertThat(row.getStatus()).isEqualTo("PENDING");
        assertThat(row.getAttempts()).isEqualTo(1);
        assertThat(row.getNextAttemptAt()).isEqualTo(next);
        assertThat(row.getLastError()).isEqualTo("nack del broker");
        assertThat(row.getPublishedAt()).isNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_markAttemptFailed_exhausted_makes_the_failure_definitive() {
        long id = insertRow(TENANT_A, "ea-6", "PENDING", Instant.now().minusSeconds(60), 9);

        asTenant(TENANT_A, () -> {
            adapter.markAttemptFailed(id, 10, BASE, true, "sigue caido");
            return null;
        });

        BillingOutboxEventEntity row = reload(TENANT_A, id);
        assertThat(row.getStatus()).isEqualTo("FAILED");
        assertThat(row.getAttempts()).isEqualTo(10);
        TenantContext.clear();
        assertThat(adapter.findDueClaims(Instant.now().plusSeconds(86_400), 50)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_a_failed_attempt_with_a_future_schedule_leaves_the_due_set() {
        long id = insertRow(TENANT_A, "ea-7", "PENDING", BASE.minusSeconds(60), 0);
        asTenant(TENANT_A, () -> {
            adapter.markAttemptFailed(id, 1, BASE.plusSeconds(5), false, "x");
            return null;
        });
        TenantContext.clear();

        assertThat(adapter.findDueClaims(BASE, 50)).isEmpty();
        assertThat(adapter.findDueClaims(BASE.plusSeconds(5), 50)).extracting(OutboxClaim::id).containsExactly(id);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_a_very_long_error_is_truncated_to_the_column_length() {
        long id = insertRow(TENANT_A, "ea-8", "PENDING", Instant.now().minusSeconds(60), 0);
        String longError = "x".repeat(5000);

        asTenant(TENANT_A, () -> {
            adapter.markAttemptFailed(id, 1, BASE, false, longError);
            return null;
        });

        assertThat(reload(TENANT_A, id).getLastError()).hasSize(1000);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_marking_a_row_that_does_not_exist_for_the_tenant_fails() {
        long bRow = insertRow(TENANT_B, "eb-4", "PENDING", Instant.now().minusSeconds(60), 0);

        assertThatThrownBy(() -> asTenant(TENANT_A, () -> {
            adapter.markPublished(bRow, BASE);
            return null;
        })).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> asTenant(TENANT_A, () -> {
            adapter.markAttemptFailed(bRow, 1, BASE, false, "x");
            return null;
        })).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void EVT_4_a_row_appended_by_the_adapter_is_picked_up_by_the_claim() {
        asTenant(TENANT_A, () -> {
            adapter.append(emittedEvent(70));
            return null;
        });
        TenantContext.clear();

        assertThat(adapter.findDueClaims(Instant.now().plusSeconds(60), 50)).hasSize(1);
    }
}
