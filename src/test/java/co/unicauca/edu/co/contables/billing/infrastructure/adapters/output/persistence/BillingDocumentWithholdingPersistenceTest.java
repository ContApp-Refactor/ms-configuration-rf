package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.Withholding;
import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.mapper.BillingDocumentPersistenceMapper;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import java.util.List;
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
 * Persistencia de las retenciones: columnas nuevas NULABLES en {@code billing_documents} (JSON +
 * total de retenciones), ida y vuelta por el adaptador real, total neto y documentos anteriores sin
 * retenciones.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billing_jpa_wh;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = BillingJpaTestConfig.class)
class BillingDocumentWithholdingPersistenceTest {

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

    private static Withholding w(WithholdingCode code, long taxId, String name, String pct) {
        return new Withholding(code, taxId, name, rate(pct));
    }

    /** Subtotal 20.000, IVA 3.000, retenciones 4.000 + 2.000, neto 17.000. */
    private static BillingDocument exampleSale() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(readySaleDraft().counterparty(), TODAY, null,
                List.of(lineOf(1, product(1L, "10000.00", "15"), "2")), methodTerms(1L),
                List.of(w(WithholdingCode.RETE_ICA, 3L, "c", "20"), w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")),
                doc.updatedAt(), doc.updatedBy());
        return doc;
    }

    // ---------- mapper (sin base de datos) ----------

    @Test
    void the_mapper_stores_json_the_withholding_total_and_the_net_total() {
        BillingDocumentEntity entity = mapper.newEntity(exampleSale());

        assertThat(entity.getWithholdingTotal()).isEqualByComparingTo("6000.00");
        assertThat(entity.getTotal()).isEqualByComparingTo("17000.00");
        assertThat(entity.getWithholdings()).contains("RETE_ICA", "RETE_FUENTE", "\"taxName\":\"c\"",
                "\"amountCents\":400000", "\"amountCents\":200000");
    }

    @Test
    void the_mapper_restores_the_withholdings_in_order_and_the_totals() {
        BillingDocument doc = exampleSale();

        BillingDocument restored = mapper.toDomain(mapper.newEntity(doc));

        assertThat(restored.withholdings()).isEqualTo(doc.withholdings());
        assertThat(restored.totals()).isEqualTo(doc.totals());
    }

    @Test
    void a_document_without_withholdings_maps_both_new_columns_to_null_or_zero() {
        BillingDocumentEntity entity = mapper.newEntity(readySaleDraft());

        assertThat(entity.getWithholdings()).isNull();
        assertThat(entity.getWithholdingTotal()).isEqualByComparingTo("0");
    }

    @Test
    void a_row_from_before_this_change_restores_without_withholdings() {
        BillingDocumentEntity entity = mapper.newEntity(readySaleDraft());
        entity.setWithholdings(null);
        entity.setWithholdingTotal(null);

        BillingDocument restored = mapper.toDomain(entity);

        assertThat(restored.withholdings()).isEmpty();
        assertThat(restored.totals().withholdings().toCents()).isZero();
        assertThat(mapper.toSummary(entity).totals().withholdings().toCents()).isZero();
    }

    @Test
    void the_summary_exposes_the_withholding_total_and_the_net_total() {
        BillingDocumentEntity entity = mapper.newEntity(exampleSale());

        assertThat(mapper.toSummary(entity).totals().withholdings().toCents()).isEqualTo(600_000L);
        assertThat(mapper.toSummary(entity).totals().total().toCents()).isEqualTo(1_700_000L);
    }

    // ---------- adaptador real (H2) ----------

    @Test
    void a_draft_with_withholdings_survives_a_round_trip() {
        BillingDocument saved = adapter.save(exampleSale());
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.withholdings()).containsExactly(
                w(WithholdingCode.RETE_ICA, 3L, "c", "20"), w(WithholdingCode.RETE_FUENTE, 2L, "b", "10"));
        assertThat(found.totals().total().toCents()).isEqualTo(1_700_000L);
        assertThat(jdbc.queryForObject("select total from billing_documents where id = ?",
                java.math.BigDecimal.class, saved.id())).isEqualByComparingTo("17000.00");
        assertThat(jdbc.queryForObject("select withholding_total from billing_documents where id = ?",
                java.math.BigDecimal.class, saved.id())).isEqualByComparingTo("6000.00");
    }

    @Test
    void the_frozen_snapshot_of_an_emitted_invoice_survives_a_round_trip() {
        BillingDocument doc = exampleSale();
        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 9L), issuer(), null, LATER, OTHER_USER);
        BillingDocument saved = adapter.save(doc);
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.withholdings()).isEqualTo(doc.withholdings());
        assertThat(found.totals().total().toCents()).isEqualTo(1_700_000L);
    }

    @Test
    void editing_the_draft_replaces_the_stored_withholdings_in_place() {
        BillingDocument saved = adapter.save(exampleSale());
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(loaded.counterparty(), TODAY, null, loaded.lines(), loaded.payment(),
                List.of(w(WithholdingCode.AUTO_RENTA, 1L, "a", "5")), loaded.updatedAt().plusSeconds(5), "otro");
        adapter.save(loaded);
        flushAndClear();

        BillingDocument reloaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        assertThat(reloaded.withholdings()).containsExactly(w(WithholdingCode.AUTO_RENTA, 1L, "a", "5"));
        assertThat(reloaded.totals().total().toCents()).isEqualTo(2_300_000L - 100_000L);
    }

    @Test
    void clearing_the_withholdings_nulls_the_json_column() {
        BillingDocument saved = adapter.save(exampleSale());
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(loaded.counterparty(), TODAY, null, loaded.lines(), loaded.payment(), List.of(),
                loaded.updatedAt().plusSeconds(5), "otro");
        adapter.save(loaded);
        flushAndClear();

        assertThat(jdbc.queryForObject("select withholdings from billing_documents where id = ?", String.class,
                saved.id())).isNull();
        assertThat(adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow().withholdings()).isEmpty();
    }
}
