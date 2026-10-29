package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_B;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedInvoice;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSaleWithLines;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.InvoiceReturnAggregate;
import co.unicauca.edu.co.contables.billing.application.service.BillingDocumentLoader;
import co.unicauca.edu.co.contables.billing.application.service.BillingDocumentQueryService;
import co.unicauca.edu.co.contables.billing.application.service.BillingReferenceResolver;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocumentState;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.OriginalDocumentRef;
import co.unicauca.edu.co.contables.billing.domain.model.Quantity;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnLineAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
 * Persistencia de documentos (TEN-1, TEN-3, TEN-4, DRF-2/3) sobre H2 en modo PostgreSQL con una
 * configuracion de prueba exclusiva de billing (D18). El tenant se fija con {@code @BeforeTransaction}
 * porque la sesion de Hibernate toma el tenant al abrirse, es decir al iniciar la transaccion del test.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billing_jpa;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = BillingJpaTestConfig.class)
class BillingDocumentPersistenceAdapterTest {

    @Autowired
    private BillingDocumentPersistenceAdapter adapter;
    @Autowired
    private EntityManager em;
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
        jdbc.update("delete from billing_document_lines");
        jdbc.update("delete from billing_documents");
    }

    // ---------- helpers ----------

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private <T> T asTenant(String tenant, Supplier<T> action) {
        TenantContext.setTenantId(tenant);
        try {
            return new TransactionTemplate(transactionManager).execute(status -> action.get());
        } finally {
            TenantContext.clear();
        }
    }

    private static BillingDocument draftWithLines(int count) {
        BillingDocument doc = draftSale();
        List<DocumentLine> lines = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            lines.add(lineOf(i, product(i), String.valueOf(i)));
        }
        doc.replaceInvoiceDraft(customer(), TODAY, null, lines, NOW, USER);
        return doc;
    }

    /** Copia del documento con otra identidad y empresa (para unicidad y aislamiento). */
    private static BillingDocument relocated(BillingDocument doc, UUID id, String enterpriseId) {
        BillingDocumentState s = doc.state();
        return BillingDocument.restore(new BillingDocumentState(id, enterpriseId, s.type(), s.status(), s.number(),
                s.issueDate(), s.counterparty(), s.supplierInvoiceNumber(), s.issuer(), s.original(),
                s.returnReason(), s.lines(), s.createdAt(), s.createdBy(), s.updatedAt(), s.updatedBy(),
                s.emittedAt(), s.emittedBy(), s.annulledAt(), s.annulledBy(), s.annulReason(), null));
    }

    private long count(String table) {
        Long value = jdbc.queryForObject("select count(*) from " + table, Long.class);
        return value == null ? 0L : value;
    }

    private static BillingErrorCode codeOf(Throwable thrown) {
        return ((BillingException) thrown).errorCode();
    }

    // ---------- round trip ----------

    @Test
    void DRF_2_round_trip_of_a_partial_draft_without_counterparty_or_lines() {
        BillingDocument saved = adapter.save(draftSale());
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.state()).isEqualTo(saved.state());
        assertThat(found.counterparty()).isNull();
        assertThat(found.lines()).isEmpty();
        assertThat(found.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(found.number()).isNull();
    }

    @Test
    void DRF_2_round_trip_of_a_full_draft_with_overrides() {
        BillingDocument doc = draftSale();
        DocumentLine overridden = DocumentLine.forInvoice(1, product(1L, "100.00", "19"),
                Quantity.of(new BigDecimal("2.5")), money("150.00"), rate("5"));
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(overridden, lineOf(2, product(2L), "1")), NOW, USER);
        BillingDocument saved = adapter.save(doc);
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.state()).isEqualTo(saved.state());
        assertThat(found.lines()).hasSize(2);
        assertThat(found.lines().get(0).priceOverridden()).isTrue();
        assertThat(found.lines().get(0).taxOverridden()).isTrue();
        assertThat(found.lines().get(1).priceOverridden()).isFalse();
        assertThat(found.totals()).isEqualTo(saved.totals());
    }

    @Test
    void D6_numeric_total_columns_are_persisted_from_the_derived_totals() {
        BillingDocument saved = adapter.save(readySaleDraft());
        flushAndClear();

        BigDecimal total = jdbc.queryForObject("select total from billing_documents", BigDecimal.class);
        BigDecimal tax = jdbc.queryForObject("select tax_total from billing_documents", BigDecimal.class);

        assertThat(saved.totals().total().value()).isEqualByComparingTo("238.00");
        assertThat(total).isEqualByComparingTo("238.00");
        assertThat(tax).isEqualByComparingTo("38.00");
    }

    @Test
    void EMT_2_round_trip_of_an_emitted_sale_keeps_number_issuer_and_audit() {
        BillingDocument saved = adapter.save(emittedSale());
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.state()).isEqualTo(saved.state());
        assertThat(found.number().display()).isEqualTo("FV-000007");
        assertThat(found.issuer().name()).isEqualTo("Empresa Emisora SAS");
        assertThat(found.emittedBy()).isEqualTo("approver");
        String display = jdbc.queryForObject("select number_display from billing_documents", String.class);
        assertThat(display).isEqualTo("FV-000007");
    }

    @Test
    void ANL_1_round_trip_of_an_annulled_document_keeps_reason_and_number() {
        BillingDocument saved = adapter.save(annulledSale());
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.state()).isEqualTo(saved.state());
        assertThat(found.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(found.annulReason()).isEqualTo("error de digitacion");
        assertThat(found.number().display()).isEqualTo("FV-000007");
    }

    @Test
    void RET_round_trip_of_a_return_keeps_original_reference_and_source_line() {
        BillingDocument saved = adapter.save(BillingDocument.restore(draftSaleReturnState("producto danado")));
        flushAndClear();

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(found.state()).isEqualTo(saved.state());
        assertThat(found.type()).isEqualTo(DocumentType.SALE_RETURN);
        assertThat(found.original().id()).isEqualTo(uuid(1));
        assertThat(found.original().number().display()).isEqualTo("FV-000007");
        assertThat(found.lines().get(0).sourceLineNo()).isEqualTo(1);
    }

    @Test
    void D10_enum_columns_are_plain_strings_and_tenant_column_is_filled_by_hibernate() {
        adapter.save(readySaleDraft());
        flushAndClear();

        String type = jdbc.queryForObject("select document_type from billing_documents", String.class);
        String status = jdbc.queryForObject("select status from billing_documents", String.class);
        String tenant = jdbc.queryForObject("select tenant_id from billing_documents", String.class);
        String lineTenant = jdbc.queryForObject("select tenant_id from billing_document_lines", String.class);

        assertThat(type).isEqualTo("SALE_INVOICE");
        assertThat(status).isEqualTo("BORRADOR");
        assertThat(tenant).isEqualTo(TENANT_A);
        assertThat(lineTenant).isEqualTo(TENANT_A);
    }

    // ---------- version ----------

    @Test
    void DRF_3_first_save_gets_version_zero_and_each_change_increments_it() {
        BillingDocument first = adapter.save(readySaleDraft());
        assertThat(first.version()).isEqualTo(0L);
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, first.id()).orElseThrow();
        loaded.replaceInvoiceDraft(customer(), TODAY, null, List.of(lineOf(1, product(1L), "5")),
                NOW.plusSeconds(30), USER);
        BillingDocument second = adapter.save(loaded);

        assertThat(second.version()).isEqualTo(1L);
        assertThat(second.updatedAt()).isEqualTo(NOW.plusSeconds(30));
    }

    @Test
    void DRF_3_saving_a_document_with_a_stale_version_is_a_concurrent_modification() {
        BillingDocument first = adapter.save(readySaleDraft());
        flushAndClear();
        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, first.id()).orElseThrow();
        loaded.replaceInvoiceDraft(customer(), TODAY, null, List.of(lineOf(1, product(1L), "5")),
                NOW.plusSeconds(30), USER);
        adapter.save(loaded); // version 1
        flushAndClear();

        BillingDocument stale = withVersion(first, 0L);

        assertThatThrownBy(() -> adapter.save(stale))
                .isInstanceOf(BillingException.class)
                .satisfies(thrown -> assertThat(codeOf(thrown))
                        .isEqualTo(BillingErrorCode.BILLING_CONCURRENT_MODIFICATION));
    }

    @Test
    void DRF_3_saving_an_existing_document_without_version_is_a_concurrent_modification() {
        BillingDocument first = adapter.save(readySaleDraft());
        flushAndClear();

        BillingDocument noVersion = withVersion(first, null);

        assertThatThrownBy(() -> adapter.save(noVersion))
                .isInstanceOf(BillingException.class)
                .satisfies(thrown -> assertThat(codeOf(thrown))
                        .isEqualTo(BillingErrorCode.BILLING_CONCURRENT_MODIFICATION));
    }

    @Test
    void DRF_3_saving_a_versioned_document_that_no_longer_exists_is_a_concurrent_modification() {
        BillingDocument ghost = withVersion(readySaleDraft(), 3L);

        assertThatThrownBy(() -> adapter.save(ghost))
                .isInstanceOf(BillingException.class)
                .satisfies(thrown -> assertThat(codeOf(thrown))
                        .isEqualTo(BillingErrorCode.BILLING_CONCURRENT_MODIFICATION));
    }

    // ---------- fusion de lineas (D9) ----------

    @Test
    void D9_shrinking_lines_keeps_the_head_and_deletes_the_tail() {
        BillingDocument saved = adapter.save(draftWithLines(3));
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(lineOf(1, product(1L), "1")), NOW.plusSeconds(10), USER);
        BillingDocument afterShrink = adapter.save(loaded);
        flushAndClear();

        assertThat(count("billing_document_lines")).isEqualTo(1L);
        assertThat(afterShrink.lines()).hasSize(1);
        BillingDocument reloaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        assertThat(reloaded.lines()).extracting(DocumentLine::lineNo).containsExactly(1);
    }

    @Test
    void D9_growing_lines_adds_the_tail_without_violating_the_line_number_unique_key() {
        BillingDocument saved = adapter.save(draftWithLines(1));
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(customer(), TODAY, null, draftWithLines(4).lines(), NOW.plusSeconds(10), USER);
        adapter.save(loaded);
        flushAndClear();

        assertThat(count("billing_document_lines")).isEqualTo(4L);
        BillingDocument reloaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        assertThat(reloaded.lines()).extracting(DocumentLine::lineNo).containsExactly(1, 2, 3, 4);
    }

    @Test
    void D9_replacing_the_content_of_a_line_that_keeps_its_number_updates_it_in_place() {
        BillingDocument saved = adapter.save(draftWithLines(2));
        flushAndClear();
        Long firstLineRowId = jdbc.queryForObject(
                "select id from billing_document_lines where line_no = 1", Long.class);

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(lineOf(1, product(9L, "50.00", "0"), "3"), lineOf(2, product(2L), "2")),
                NOW.plusSeconds(10), USER);
        adapter.save(loaded);
        flushAndClear();

        Long sameRowId = jdbc.queryForObject("select id from billing_document_lines where line_no = 1", Long.class);
        Long productId = jdbc.queryForObject(
                "select product_id from billing_document_lines where line_no = 1", Long.class);
        assertThat(sameRowId).isEqualTo(firstLineRowId);
        assertThat(productId).isEqualTo(9L);
        assertThat(count("billing_document_lines")).isEqualTo(2L);
    }

    @Test
    void D9_clearing_all_lines_removes_every_row() {
        BillingDocument saved = adapter.save(draftWithLines(3));
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceInvoiceDraft(null, TODAY, null, List.of(), NOW.plusSeconds(10), USER);
        adapter.save(loaded);
        flushAndClear();

        assertThat(count("billing_document_lines")).isZero();
        assertThat(adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow().counterparty()).isNull();
    }

    // ---------- unicidad del numero ----------

    @Test
    void DRF_1_many_drafts_without_number_coexist_in_the_same_enterprise_and_type() {
        adapter.save(draftSale());
        adapter.save(relocated(draftSale(), uuid(2), ENTERPRISE_ID));
        adapter.save(relocated(draftSale(), uuid(3), ENTERPRISE_ID));
        flushAndClear();

        assertThat(count("billing_documents")).isEqualTo(3L);
    }

    @Test
    void EMT_3_the_same_number_twice_in_the_same_tenant_enterprise_and_type_is_rejected() {
        adapter.save(emittedSale());

        assertThatThrownBy(() -> adapter.save(relocated(emittedSale(), uuid(2), ENTERPRISE_ID)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void EMT_3_the_same_number_is_allowed_in_another_enterprise_or_another_type() {
        adapter.save(emittedSale());
        adapter.save(relocated(emittedSale(), uuid(2), OTHER_ENTERPRISE_ID));
        adapter.save(relocated(emittedPurchase(), uuid(3), ENTERPRISE_ID));
        flushAndClear();

        assertThat(count("billing_documents")).isEqualTo(3L);
    }

    // ---------- alcance por empresa ----------

    @Test
    void TEN_1_a_document_of_another_enterprise_is_not_visible() {
        BillingDocument saved = adapter.save(readySaleDraft());
        flushAndClear();

        assertThat(adapter.findById(OTHER_ENTERPRISE_ID, saved.id())).isEmpty();
        assertThat(adapter.findSummaries(OTHER_ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null)).isEmpty();
        assertThat(adapter.findById(ENTERPRISE_ID, saved.id())).isPresent();
    }

    // ---------- aislamiento por tenant (TEN-1, TEN-3) ----------

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_tenant_b_cannot_see_list_delete_or_overwrite_tenant_a_documents() {
        BillingDocument saved = asTenant(TENANT_A, () -> adapter.save(readySaleDraft()));

        assertThat(asTenant(TENANT_B, () -> adapter.findById(ENTERPRISE_ID, saved.id()))).isEmpty();
        assertThat(asTenant(TENANT_B,
                () -> adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null))).isEmpty();
        asTenant(TENANT_B, () -> {
            adapter.delete(saved);
            return null;
        });
        assertThatThrownBy(() -> asTenant(TENANT_B, () -> adapter.save(saved)))
                .isInstanceOf(BillingException.class);

        assertThat(asTenant(TENANT_A, () -> adapter.findById(ENTERPRISE_ID, saved.id()))).isPresent();
        assertThat(count("billing_documents")).isEqualTo(1L);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_3_two_tenants_can_hold_the_same_document_number() {
        asTenant(TENANT_A, () -> adapter.save(emittedSale()));
        asTenant(TENANT_B, () -> adapter.save(relocated(emittedSale(), uuid(2), ENTERPRISE_ID)));

        assertThat(count("billing_documents")).isEqualTo(2L);
        assertThat(asTenant(TENANT_A,
                () -> adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null))).hasSize(1);
        assertThat(asTenant(TENANT_B,
                () -> adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null))).hasSize(1);
    }

    // ---------- listados ----------

    private BillingDocument savedWithUpdatedAt(UUID id, DocumentType type, Instant updatedAt) {
        BillingDocument doc = type == DocumentType.PURCHASE_INVOICE ? draftPurchase() : draftSale();
        BillingDocument copy = relocated(doc, id, ENTERPRISE_ID);
        copy.replaceInvoiceDraft(null, TODAY, null, List.of(), updatedAt, USER);
        return adapter.save(copy);
    }

    @Test
    void QRY_2_summaries_are_ordered_by_updated_at_descending() {
        savedWithUpdatedAt(uuid(11), DocumentType.SALE_INVOICE, NOW);
        savedWithUpdatedAt(uuid(12), DocumentType.SALE_INVOICE, NOW.plusSeconds(120));
        savedWithUpdatedAt(uuid(13), DocumentType.SALE_INVOICE, NOW.plusSeconds(60));
        flushAndClear();

        List<BillingDocumentSummary> summaries =
                adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null);

        assertThat(summaries).extracting(BillingDocumentSummary::id).containsExactly(uuid(12), uuid(13), uuid(11));
    }

    @Test
    void QRY_2_summaries_filter_by_type() {
        savedWithUpdatedAt(uuid(11), DocumentType.SALE_INVOICE, NOW);
        savedWithUpdatedAt(uuid(12), DocumentType.PURCHASE_INVOICE, NOW);
        flushAndClear();

        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, null, null))
                .extracting(BillingDocumentSummary::id).containsExactly(uuid(12));
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, null, null)).isEmpty();
    }

    @Test
    void QRY_2_summaries_filter_by_status() {
        adapter.save(readySaleDraft());
        adapter.save(relocated(emittedSale(), uuid(2), ENTERPRISE_ID));
        flushAndClear();

        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, DocumentStatus.BORRADOR, null))
                .extracting(BillingDocumentSummary::id).containsExactly(uuid(1));
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, DocumentStatus.EMITIDA, null))
                .extracting(BillingDocumentSummary::id).containsExactly(uuid(2));
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, DocumentStatus.ANULADA, null))
                .isEmpty();
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null)).hasSize(2);
    }

    @Test
    void QRY_2_summaries_filter_by_original_document() {
        adapter.save(BillingDocument.restore(draftSaleReturnState("motivo")));
        adapter.save(readySaleDraft());
        flushAndClear();

        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, null, uuid(1)))
                .extracting(BillingDocumentSummary::id).containsExactly(uuid(2));
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, null, uuid(99))).isEmpty();
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, DocumentStatus.BORRADOR, uuid(1)))
                .hasSize(1);
        assertThat(adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, DocumentStatus.EMITIDA, uuid(1)))
                .isEmpty();
    }

    @Test
    void QRY_2_summary_carries_header_totals_number_and_version_without_lines() {
        BillingDocument saved = adapter.save(relocated(emittedSale(), uuid(5), ENTERPRISE_ID));
        flushAndClear();

        BillingDocumentSummary summary =
                adapter.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null).get(0);

        assertThat(summary.id()).isEqualTo(uuid(5));
        assertThat(summary.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(summary.number().display()).isEqualTo("FV-000007");
        assertThat(summary.counterpartyId()).isEqualTo(10L);
        assertThat(summary.counterpartyName()).isEqualTo("Cliente 10");
        assertThat(summary.totals()).isEqualTo(saved.totals());
        assertThat(summary.version()).isEqualTo(saved.version());
        assertThat(summary.emittedAt()).isEqualTo(saved.emittedAt());
    }

    // ---------- borrado ----------

    @Test
    void DRF_5_delete_removes_the_document_and_its_lines() {
        BillingDocument saved = adapter.save(draftWithLines(2));
        flushAndClear();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        adapter.delete(loaded);
        flushAndClear();

        assertThat(adapter.findById(ENTERPRISE_ID, saved.id())).isEmpty();
        assertThat(count("billing_documents")).isZero();
        assertThat(count("billing_document_lines")).isZero();
    }

    @Test
    void DRF_5_deleting_an_unknown_document_is_a_no_op() {
        adapter.delete(readySaleDraft());

        assertThat(count("billing_documents")).isZero();
    }

    // ---------- lectura con bloqueo (ANL-1, D12) ----------

    @Test
    void D12_find_by_id_for_update_returns_the_same_document_as_find_by_id_including_lines() {
        BillingDocument saved = adapter.save(draftWithLines(3));
        flushAndClear();

        BillingDocument locked = adapter.findByIdForUpdate(ENTERPRISE_ID, saved.id()).orElseThrow();

        assertThat(locked.state()).isEqualTo(saved.state());
        assertThat(locked.lines()).extracting(DocumentLine::lineNo).containsExactly(1, 2, 3);
    }

    @Test
    void D12_find_by_id_for_update_of_an_unknown_id_is_empty() {
        assertThat(adapter.findByIdForUpdate(ENTERPRISE_ID, uuid(99))).isEmpty();
    }

    @Test
    void TEN_1_find_by_id_for_update_does_not_see_a_document_of_another_enterprise() {
        BillingDocument saved = adapter.save(readySaleDraft());
        flushAndClear();

        assertThat(adapter.findByIdForUpdate(OTHER_ENTERPRISE_ID, saved.id())).isEmpty();
        assertThat(adapter.findByIdForUpdate(ENTERPRISE_ID, saved.id())).isPresent();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_find_by_id_for_update_does_not_see_a_document_of_another_tenant() {
        BillingDocument saved = asTenant(TENANT_A, () -> adapter.save(emittedSale()));

        assertThat(asTenant(TENANT_B, () -> adapter.findByIdForUpdate(ENTERPRISE_ID, saved.id()))).isEmpty();
        assertThat(asTenant(TENANT_A, () -> adapter.findByIdForUpdate(ENTERPRISE_ID, saved.id()))).isPresent();
    }

    @Test
    void ANL_1_a_document_loaded_for_update_can_be_annulled_and_saved_keeping_its_number() {
        BillingDocument saved = adapter.save(emittedSale());
        flushAndClear();

        BillingDocument locked = adapter.findByIdForUpdate(ENTERPRISE_ID, saved.id()).orElseThrow();
        locked.annul("error de digitacion", false, NOW.plusSeconds(120), USER);
        BillingDocument annulled = adapter.save(locked);
        flushAndClear();

        assertThat(annulled.version()).isEqualTo(1L);
        BillingDocument reloaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        assertThat(reloaded.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(reloaded.number().display()).isEqualTo("FV-000007");
        assertThat(reloaded.annulReason()).isEqualTo("error de digitacion");
        assertThat(reloaded.annulledBy()).isEqualTo(USER);
        assertThat(reloaded.annulledAt()).isEqualTo(NOW.plusSeconds(120));
    }

    // ---------- devoluciones por estado (ANL-2) ----------

    /** Devolucion de venta del original dado, con el estado y consecutivo indicados. */
    private static BillingDocument returnOf(UUID id, UUID originalId, DocumentStatus status, Long numberValue) {
        BillingDocumentState s = draftSaleReturnState("producto danado");
        OriginalDocumentRef original = new OriginalDocumentRef(originalId, DocumentType.SALE_INVOICE,
                DocumentNumber.of(DocumentType.SALE_INVOICE, 7L), TODAY);
        DocumentNumber number = numberValue == null ? null : DocumentNumber.of(DocumentType.SALE_RETURN, numberValue);
        return BillingDocument.restore(new BillingDocumentState(id, ENTERPRISE_ID, DocumentType.SALE_RETURN, status,
                number, s.issueDate(), s.counterparty(), s.supplierInvoiceNumber(), s.issuer(), original,
                s.returnReason(), s.lines(), s.createdAt(), s.createdBy(), s.updatedAt(), s.updatedBy(),
                s.emittedAt(), s.emittedBy(), s.annulledAt(), s.annulledBy(), s.annulReason(), null));
    }

    @Test
    void ANL_2_an_emitted_return_of_the_invoice_is_detected() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L));
        flushAndClear();

        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isTrue();
    }

    @Test
    void ANL_2_draft_and_annulled_returns_do_not_count_as_emitted() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.BORRADOR, null));
        adapter.save(returnOf(uuid(3), uuid(1), DocumentStatus.ANULADA, 1L));
        flushAndClear();

        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isFalse();
        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.BORRADOR)).isTrue();
        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.ANULADA)).isTrue();
    }

    @Test
    void ANL_2_returns_of_another_original_are_not_matched() {
        adapter.save(returnOf(uuid(2), uuid(50), DocumentStatus.EMITIDA, 1L));
        flushAndClear();

        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isFalse();
        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(50), DocumentStatus.EMITIDA)).isTrue();
    }

    @Test
    void ANL_2_without_any_return_the_answer_is_false() {
        adapter.save(emittedSale());
        flushAndClear();

        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isFalse();
    }

    @Test
    void TEN_1_returns_of_another_enterprise_are_not_matched() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L));
        flushAndClear();

        assertThat(adapter.existsReturnInStatus(OTHER_ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_returns_of_another_tenant_are_not_matched() {
        asTenant(TENANT_A, () -> adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L)));

        assertThat(asTenant(TENANT_B,
                () -> adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA))).isFalse();
        assertThat(asTenant(TENANT_A,
                () -> adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA))).isTrue();
    }

    // ---------- consultas de devoluciones (H8b) ----------

    @Test
    void RET_11_find_returns_of_returns_every_status_of_that_original_only_with_their_lines() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.BORRADOR, null));
        adapter.save(returnOf(uuid(3), uuid(1), DocumentStatus.EMITIDA, 1L));
        adapter.save(returnOf(uuid(4), uuid(1), DocumentStatus.ANULADA, 2L));
        adapter.save(returnOf(uuid(5), uuid(50), DocumentStatus.EMITIDA, 3L));
        flushAndClear();

        List<BillingDocument> result = adapter.findReturnsOf(ENTERPRISE_ID, uuid(1));

        assertThat(result).extracting(BillingDocument::id).containsExactlyInAnyOrder(uuid(2), uuid(3), uuid(4));
        assertThat(result).allSatisfy(r -> {
            assertThat(r.original().id()).isEqualTo(uuid(1));
            assertThat(r.type()).isEqualTo(DocumentType.SALE_RETURN);
            assertThat(r.lines()).hasSize(1);
        });
    }

    @Test
    void RET_11_find_returns_of_an_invoice_without_returns_is_empty() {
        adapter.save(returnOf(uuid(5), uuid(50), DocumentStatus.EMITIDA, 3L));
        flushAndClear();

        assertThat(adapter.findReturnsOf(ENTERPRISE_ID, uuid(1))).isEmpty();
    }

    @Test
    void TEN_1_find_returns_of_does_not_see_returns_of_another_enterprise() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L));
        flushAndClear();

        assertThat(adapter.findReturnsOf(OTHER_ENTERPRISE_ID, uuid(1))).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_find_returns_of_does_not_see_returns_of_another_tenant() {
        asTenant(TENANT_A, () -> adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L)));

        assertThat(asTenant(TENANT_B, () -> adapter.findReturnsOf(ENTERPRISE_ID, uuid(1)))).isEmpty();
        assertThat(asTenant(TENANT_A, () -> adapter.findReturnsOf(ENTERPRISE_ID, uuid(1)))).hasSize(1);
    }

    @Test
    void RET_11_find_summaries_by_ids_returns_the_requested_ones_ordered_by_updated_at_descending() {
        savedWithUpdatedAt(uuid(11), DocumentType.SALE_INVOICE, NOW);
        savedWithUpdatedAt(uuid(12), DocumentType.SALE_INVOICE, NOW.plusSeconds(120));
        savedWithUpdatedAt(uuid(13), DocumentType.SALE_INVOICE, NOW.plusSeconds(60));
        flushAndClear();

        List<BillingDocumentSummary> result =
                adapter.findSummariesByIds(ENTERPRISE_ID, List.of(uuid(11), uuid(12), uuid(99)));

        assertThat(result).extracting(BillingDocumentSummary::id).containsExactly(uuid(12), uuid(11));
    }

    @Test
    void RET_11_find_summaries_by_ids_with_no_ids_is_empty() {
        savedWithUpdatedAt(uuid(11), DocumentType.SALE_INVOICE, NOW);
        flushAndClear();

        assertThat(adapter.findSummariesByIds(ENTERPRISE_ID, List.of())).isEmpty();
    }

    @Test
    void TEN_1_find_summaries_by_ids_does_not_see_documents_of_another_enterprise() {
        savedWithUpdatedAt(uuid(11), DocumentType.SALE_INVOICE, NOW);
        flushAndClear();

        assertThat(adapter.findSummariesByIds(OTHER_ENTERPRISE_ID, List.of(uuid(11)))).isEmpty();
    }

    @Test
    void RET_11_aggregate_emitted_returns_counts_and_sums_only_emitted_returns_per_original() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L));
        adapter.save(returnOf(uuid(3), uuid(1), DocumentStatus.EMITIDA, 2L));
        adapter.save(returnOf(uuid(4), uuid(1), DocumentStatus.ANULADA, 3L));
        adapter.save(returnOf(uuid(5), uuid(1), DocumentStatus.BORRADOR, null));
        adapter.save(returnOf(uuid(6), uuid(50), DocumentStatus.EMITIDA, 4L));
        flushAndClear();

        List<InvoiceReturnAggregate> result =
                adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN);

        assertThat(result).hasSize(2);
        InvoiceReturnAggregate first = result.stream().filter(a -> a.originalId().equals(uuid(1))).findFirst()
                .orElseThrow();
        assertThat(first.count()).isEqualTo(2L);
        assertThat(first.total()).isEqualTo(money("238.00"));
        assertThat(first.quantity()).isEqualByComparingTo("2");
        InvoiceReturnAggregate second = result.stream().filter(a -> a.originalId().equals(uuid(50))).findFirst()
                .orElseThrow();
        assertThat(second.count()).isEqualTo(1L);
        assertThat(second.total()).isEqualTo(money("119.00"));
        assertThat(second.quantity()).isEqualByComparingTo("1");
    }

    @Test
    void RET_11_aggregate_emitted_returns_filters_by_return_type_and_ignores_when_none_is_emitted() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L));
        adapter.save(returnOf(uuid(3), uuid(60), DocumentStatus.BORRADOR, null));
        flushAndClear();

        assertThat(adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN)).isEmpty();
        assertThat(adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN))
                .extracting(InvoiceReturnAggregate::originalId).containsExactly(uuid(1));
    }

    @Test
    void TEN_1_aggregate_emitted_returns_is_scoped_to_enterprise() {
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L));
        flushAndClear();

        assertThat(adapter.aggregateEmittedReturns(OTHER_ENTERPRISE_ID, DocumentType.SALE_RETURN)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_aggregate_emitted_returns_is_scoped_to_tenant() {
        asTenant(TENANT_A, () -> adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 1L)));

        assertThat(asTenant(TENANT_B,
                () -> adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN))).isEmpty();
        assertThat(asTenant(TENANT_A,
                () -> adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN))).hasSize(1);
    }

    @Test
    void RET_11_sold_quantities_sum_the_line_quantities_of_each_requested_document() {
        adapter.save(relocated(draftWithLines(3), uuid(21), ENTERPRISE_ID));
        adapter.save(relocated(draftWithLines(2), uuid(22), ENTERPRISE_ID));
        adapter.save(relocated(draftWithLines(4), uuid(23), OTHER_ENTERPRISE_ID));
        flushAndClear();

        Map<UUID, BigDecimal> sold =
                adapter.soldQuantities(ENTERPRISE_ID, List.of(uuid(21), uuid(22), uuid(23), uuid(99)));

        assertThat(sold).containsOnlyKeys(uuid(21), uuid(22));
        assertThat(sold.get(uuid(21))).isEqualByComparingTo("6");
        assertThat(sold.get(uuid(22))).isEqualByComparingTo("3");
    }

    @Test
    void RET_11_sold_quantities_with_no_ids_is_empty() {
        adapter.save(relocated(draftWithLines(3), uuid(21), ENTERPRISE_ID));
        flushAndClear();

        assertThat(adapter.soldQuantities(ENTERPRISE_ID, List.of())).isEmpty();
    }

    // ---------- H8c: mapeo de devoluciones, agregados y listas ----------

    /** Factura de venta emitida con dos lineas: 10 x 100.00 (IVA 19) y 4 x 50.00 (IVA 0). */
    private static BillingDocument twoLineSale(UUID id, long numberValue) {
        return emittedSaleWithLines(id, numberValue,
                lineOf(1, product(1L), "10"), lineOf(2, product(2L, "50.00", "0"), "4"));
    }

    private static BillingDocument returnWithCreatedAt(UUID id, UUID originalId, Instant createdAt) {
        BillingDocumentState s = returnOf(id, originalId, DocumentStatus.BORRADOR, null).state();
        return BillingDocument.restore(new BillingDocumentState(s.id(), s.enterpriseId(), s.type(), s.status(),
                s.number(), s.issueDate(), s.counterparty(), s.supplierInvoiceNumber(), s.issuer(), s.original(),
                s.returnReason(), s.lines(), createdAt, s.createdBy(), s.updatedAt(), s.updatedBy(), s.emittedAt(),
                s.emittedBy(), s.annulledAt(), s.annulledBy(), s.annulReason(), null));
    }

    private BillingDocumentQueryService queryService() {
        return new BillingDocumentQueryService(adapter, new BillingDocumentLoader(adapter),
                new BillingReferenceResolver((enterprise, thirdId, role) -> Optional.empty(),
                        (enterprise, productId, kind) -> Optional.empty(), enterprise -> Optional.empty()));
    }

    @Test
    void RET_8c_a_return_persists_type_original_reference_reason_and_stable_source_lines() {
        BillingDocument original = twoLineSale(uuid(1), 7L);
        BillingDocument ret = emittedReturnOf(original, uuid(2), 1L,
                returnLineOf(original, 2, "2"), returnLineOf(original, 1, "3"));

        BillingDocument saved = adapter.save(ret);
        flushAndClear();

        assertThat(jdbc.queryForObject("select document_type from billing_documents", String.class))
                .isEqualTo("SALE_RETURN");
        assertThat(jdbc.queryForObject("select number_display from billing_documents", String.class))
                .isEqualTo("NC-000001");
        assertThat(jdbc.queryForObject("select cast(original_document_id as varchar) from billing_documents",
                String.class)).isEqualTo(uuid(1).toString());
        assertThat(jdbc.queryForObject("select original_document_type from billing_documents", String.class))
                .isEqualTo("SALE_INVOICE");
        assertThat(jdbc.queryForObject("select original_number_display from billing_documents", String.class))
                .isEqualTo("FV-000007");
        assertThat(jdbc.queryForObject("select cast(original_issue_date as varchar) from billing_documents",
                String.class)).isEqualTo(TODAY.toString());
        assertThat(jdbc.queryForObject("select return_reason from billing_documents", String.class))
                .isEqualTo("producto defectuoso");
        assertThat(jdbc.queryForList("select source_line_no from billing_document_lines order by line_no",
                Integer.class)).containsExactly(2, 1);

        BillingDocument found = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        assertThat(found.state()).isEqualTo(saved.state());
        assertThat(found.lines()).extracting(DocumentLine::sourceLineNo).containsExactly(2, 1);
        assertThat(found.returnReason()).isEqualTo("producto defectuoso");
    }

    @Test
    void RET_8c_editing_a_return_draft_updates_lines_in_place_and_keeps_source_lines_consistent() {
        BillingDocument original = twoLineSale(uuid(1), 7L);
        BillingDocument saved = adapter.save(returnDraftOf(original, uuid(2), "motivo",
                returnLineOf(original, 1, "3"), returnLineOf(original, 2, "2")));
        flushAndClear();
        List<Long> rowIds = jdbc.queryForList("select id from billing_document_lines order by line_no", Long.class);

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.replaceReturnDraft(null, "otro motivo", List.of(returnLineOf(original, 2, "4")),
                NOW.plusSeconds(30), USER);
        BillingDocument afterEdit = adapter.save(loaded);
        flushAndClear();

        assertThat(afterEdit.version()).isEqualTo(1L);
        assertThat(count("billing_document_lines")).isEqualTo(1L);
        Long keptRowId = jdbc.queryForObject("select id from billing_document_lines", Long.class);
        assertThat(keptRowId).isEqualTo(rowIds.get(0));
        assertThat(jdbc.queryForObject("select source_line_no from billing_document_lines", Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("select quantity from billing_document_lines", BigDecimal.class))
                .isEqualByComparingTo("4");
        BillingDocument reloaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        assertThat(reloaded.returnReason()).isEqualTo("otro motivo");
        assertThat(reloaded.lines()).hasSize(1);
        assertThat(reloaded.lines().get(0).sourceLineNo()).isEqualTo(2);
        assertThat(reloaded.totals().total().value()).isEqualByComparingTo("200.00");
    }

    @Test
    void EMT_3_return_numbers_are_unique_per_type_and_independent_from_invoice_numbers() {
        adapter.save(emittedSale());
        adapter.save(returnOf(uuid(2), uuid(1), DocumentStatus.EMITIDA, 7L));
        flushAndClear();

        assertThat(count("billing_documents")).isEqualTo(2L);
        assertThatThrownBy(() -> adapter.save(returnOf(uuid(3), uuid(1), DocumentStatus.EMITIDA, 7L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void RET_11_find_returns_of_orders_by_creation_ascending() {
        adapter.save(returnWithCreatedAt(uuid(3), uuid(1), NOW.plusSeconds(120)));
        adapter.save(returnWithCreatedAt(uuid(2), uuid(1), NOW));
        adapter.save(returnWithCreatedAt(uuid(4), uuid(1), NOW.plusSeconds(60)));
        flushAndClear();

        assertThat(adapter.findReturnsOf(ENTERPRISE_ID, uuid(1))).extracting(BillingDocument::id)
                .containsExactly(uuid(2), uuid(4), uuid(3));
    }

    @Test
    void RET_11_aggregate_adds_each_return_header_total_once_even_with_several_lines() {
        BillingDocument original = twoLineSale(uuid(1), 7L);
        adapter.save(emittedReturnOf(original, uuid(2), 1L,
                returnLineOf(original, 1, "3"), returnLineOf(original, 2, "2")));
        adapter.save(emittedReturnOf(original, uuid(3), 2L, returnLineOf(original, 1, "1")));
        flushAndClear();

        List<InvoiceReturnAggregate> result = adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN);

        assertThat(result).hasSize(1);
        InvoiceReturnAggregate aggregate = result.get(0);
        assertThat(aggregate.originalId()).isEqualTo(uuid(1));
        assertThat(aggregate.count()).isEqualTo(2L);
        // 3 x 100 + 19% = 357.00 ; 2 x 50 + 0% = 100.00 ; 1 x 100 + 19% = 119.00
        assertThat(aggregate.total()).isEqualTo(money("576.00"));
        assertThat(aggregate.quantity()).isEqualByComparingTo("6");
    }

    @Test
    void RET_7_an_annulled_return_leaves_the_aggregates_and_the_emitted_check() {
        BillingDocument original = twoLineSale(uuid(1), 7L);
        BillingDocument saved = adapter.save(emittedReturnOf(original, uuid(2), 1L, returnLineOf(original, 1, "3")));
        flushAndClear();
        assertThat(adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN)).hasSize(1);
        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isTrue();

        BillingDocument loaded = adapter.findById(ENTERPRISE_ID, saved.id()).orElseThrow();
        loaded.annul("cliente desistio", false, LATER.plusSeconds(120), USER);
        adapter.save(loaded);
        flushAndClear();

        assertThat(adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN)).isEmpty();
        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.EMITIDA)).isFalse();
        assertThat(adapter.existsReturnInStatus(ENTERPRISE_ID, uuid(1), DocumentStatus.ANULADA)).isTrue();
        assertThat(adapter.findReturnsOf(ENTERPRISE_ID, uuid(1))).hasSize(1);
    }

    @Test
    void PUR_aggregate_of_purchase_returns_is_found_by_purchase_return_type_only() {
        BillingDocument purchase = emittedInvoice(uuid(1), DocumentType.PURCHASE_INVOICE, 7L,
                List.of(lineOf(1, product(1L), "10")));
        adapter.save(emittedReturnOf(purchase, uuid(2), 1L, returnLineOf(purchase, 1, "4")));
        flushAndClear();

        List<InvoiceReturnAggregate> result =
                adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).originalId()).isEqualTo(uuid(1));
        assertThat(result.get(0).count()).isEqualTo(1L);
        assertThat(result.get(0).total()).isEqualTo(money("476.00"));
        assertThat(result.get(0).quantity()).isEqualByComparingTo("4");
        assertThat(adapter.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN)).isEmpty();
        assertThat(jdbc.queryForObject("select number_display from billing_documents where document_type = "
                + "'PURCHASE_RETURN'", String.class)).isEqualTo("DC-000001");
    }

    @Test
    void RET_11_sold_quantities_keep_fractional_quantities_with_scale_three() {
        adapter.save(emittedSaleWithLines(uuid(31), 7L, lineOf(1, product(1L), "2.5"), lineOf(2, product(2L), "1.25")));
        flushAndClear();

        Map<UUID, BigDecimal> sold = adapter.soldQuantities(ENTERPRISE_ID, List.of(uuid(31)));

        assertThat(sold.get(uuid(31))).isEqualByComparingTo("3.75");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void TEN_1_sold_quantities_and_summaries_by_ids_do_not_cross_tenants() {
        asTenant(TENANT_A, () -> adapter.save(relocated(draftWithLines(3), uuid(21), ENTERPRISE_ID)));

        assertThat(asTenant(TENANT_B, () -> adapter.soldQuantities(ENTERPRISE_ID, List.of(uuid(21))))).isEmpty();
        assertThat(asTenant(TENANT_B, () -> adapter.findSummariesByIds(ENTERPRISE_ID, List.of(uuid(21)))))
                .isEmpty();
        assertThat(asTenant(TENANT_A, () -> adapter.soldQuantities(ENTERPRISE_ID, List.of(uuid(21)))))
                .containsOnlyKeys(uuid(21));
        assertThat(asTenant(TENANT_A, () -> adapter.findSummariesByIds(ENTERPRISE_ID, List.of(uuid(21)))))
                .hasSize(1);
    }

    @Test
    void RET_11_invoices_with_returns_list_shows_partial_and_total_and_hides_annulled_or_unreturned_invoices() {
        BillingDocument partial = twoLineSale(uuid(1), 7L);
        BillingDocument total = emittedSaleWithLines(uuid(40), 8L, lineOf(1, product(1L), "4"));
        BillingDocument allAnnulled = emittedSaleWithLines(uuid(50), 9L, lineOf(1, product(1L), "5"));
        BillingDocument untouched = emittedSaleWithLines(uuid(60), 10L, lineOf(1, product(1L), "5"));
        adapter.save(partial);
        adapter.save(total);
        adapter.save(allAnnulled);
        adapter.save(untouched);
        adapter.save(emittedReturnOf(partial, uuid(2), 1L,
                returnLineOf(partial, 1, "3"), returnLineOf(partial, 2, "2")));
        adapter.save(emittedReturnOf(total, uuid(41), 2L, returnLineOf(total, 1, "4")));
        adapter.save(annulledReturnOf(allAnnulled, uuid(51), 3L, returnLineOf(allAnnulled, 1, "5")));
        flushAndClear();

        List<InvoiceWithReturnsSummary> result = queryService().list(ENTERPRISE_ID, DocumentType.SALE_INVOICE);

        assertThat(result).extracting(item -> item.summary().id()).containsExactlyInAnyOrder(uuid(1), uuid(40));
        InvoiceWithReturnsSummary partialRow = result.stream().filter(i -> i.summary().id().equals(uuid(1)))
                .findFirst().orElseThrow();
        assertThat(partialRow.state()).isEqualTo(ReturnStatus.PARCIAL);
        assertThat(partialRow.returnsCount()).isEqualTo(1);
        assertThat(partialRow.returnedTotal()).isEqualTo(money("457.00"));
        InvoiceWithReturnsSummary totalRow = result.stream().filter(i -> i.summary().id().equals(uuid(40)))
                .findFirst().orElseThrow();
        assertThat(totalRow.state()).isEqualTo(ReturnStatus.TOTAL);
        assertThat(totalRow.returnedTotal()).isEqualTo(money("476.00"));
        assertThat(queryService().list(OTHER_ENTERPRISE_ID, DocumentType.SALE_INVOICE)).isEmpty();
        assertThat(queryService().list(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE)).isEmpty();
    }

    @Test
    void RET_3_availability_over_the_real_adapter_counts_only_emitted_returns() {
        BillingDocument original = twoLineSale(uuid(1), 7L);
        adapter.save(original);
        adapter.save(emittedReturnOf(original, uuid(2), 1L,
                returnLineOf(original, 1, "3"), returnLineOf(original, 2, "2")));
        adapter.save(returnDraftOf(original, uuid(70), "borrador", returnLineOf(original, 1, "9")));
        adapter.save(annulledReturnOf(original, uuid(71), 4L, returnLineOf(original, 1, "1")));
        flushAndClear();

        ReturnAvailability availability = queryService().availability(ENTERPRISE_ID, DocumentType.SALE_INVOICE, uuid(1));

        assertThat(availability.status()).isEqualTo(ReturnStatus.PARCIAL);
        assertThat(availability.returnable()).isTrue();
        assertThat(availability.lines()).hasSize(2);
        ReturnLineAvailability first = availability.lines().get(0);
        assertThat(first.sold()).isEqualByComparingTo("10");
        assertThat(first.returned()).isEqualByComparingTo("3");
        assertThat(first.available()).isEqualByComparingTo("7");
        ReturnLineAvailability second = availability.lines().get(1);
        assertThat(second.sold()).isEqualByComparingTo("4");
        assertThat(second.returned()).isEqualByComparingTo("2");
        assertThat(second.available()).isEqualByComparingTo("2");
    }

    @Test
    void RET_10_returns_of_over_the_real_adapter_numbers_by_ascending_number_keeps_annulled_and_puts_drafts_last() {
        BillingDocument original = twoLineSale(uuid(1), 7L);
        adapter.save(original);
        adapter.save(returnDraftOf(original, uuid(70), "borrador", returnLineOf(original, 1, "1")));
        adapter.save(annulledReturnOf(original, uuid(71), 4L, returnLineOf(original, 1, "1")));
        adapter.save(emittedReturnOf(original, uuid(2), 1L, returnLineOf(original, 1, "1")));
        flushAndClear();

        List<ReturnSummary> result = queryService().returnsOf(ENTERPRISE_ID, DocumentType.SALE_INVOICE, uuid(1));

        assertThat(result).extracting(item -> item.summary().id()).containsExactly(uuid(2), uuid(71), uuid(70));
        assertThat(result).extracting(ReturnSummary::ordinal).containsExactly(1, 2, null);
    }
}
