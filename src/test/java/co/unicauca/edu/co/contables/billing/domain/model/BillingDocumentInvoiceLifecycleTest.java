package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.inactiveCustomer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.inactiveIssuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Ciclo de vida de facturas (venta y compra) del agregado BillingDocument: DOC-1..7, STA-1..3,
 * DRF-1/2/8, EMT-1/2/4, ANL-1/2 y RN-05 (inmutabilidad).
 */
class BillingDocumentInvoiceLifecycleTest {

    // ---------- creacion / borrador parcial ----------

    @Test
    void DRF_1_new_draft_is_partial_and_unnumbered() {
        BillingDocument doc = draftSale();

        assertThat(doc.id()).isEqualTo(uuid(1));
        assertThat(doc.enterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(doc.type()).isEqualTo(DocumentType.SALE_INVOICE);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(doc.number()).isNull();
        assertThat(doc.counterparty()).isNull();
        assertThat(doc.supplierInvoiceNumber()).isNull();
        assertThat(doc.issuer()).isNull();
        assertThat(doc.original()).isNull();
        assertThat(doc.lines()).isEmpty();
        assertThat(doc.totals()).isEqualTo(DocumentTotals.zero());
        assertThat(doc.issueDate()).isEqualTo(TODAY);
        assertThat(doc.createdAt()).isEqualTo(NOW);
        assertThat(doc.createdBy()).isEqualTo(USER);
        assertThat(doc.updatedAt()).isEqualTo(NOW);
        assertThat(doc.version()).isNull();
        assertThat(doc.currency()).isEqualTo("COP");
    }

    @Test
    void DOC_1_new_invoice_draft_rejects_return_types() {
        assertThatThrownBy(() -> BillingDocument.newInvoiceDraft(uuid(1), ENTERPRISE_ID,
                DocumentType.SALE_RETURN, TODAY, NOW, USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- replaceInvoiceDraft ----------

    @Test
    void DRF_2_replace_sets_header_lines_and_recomputes_totals() {
        BillingDocument doc = draftSale();
        LocalDate newDate = LocalDate.of(2026, 2, 1);

        doc.replaceInvoiceDraft(customer(), newDate, null,
                List.of(lineOf(1, product(1L, "3.33", "19"), "3")), LATER, OTHER_USER);

        assertThat(doc.counterparty()).isEqualTo(customer());
        assertThat(doc.issueDate()).isEqualTo(newDate);
        assertThat(doc.lines()).hasSize(1);
        assertThat(doc.totals()).isEqualTo(new DocumentTotals(Money.ofCents(999L), Money.ofCents(190L),
                Money.ofCents(1189L)));
        assertThat(doc.updatedAt()).isEqualTo(LATER);
        assertThat(doc.updatedBy()).isEqualTo(OTHER_USER);
        assertThat(doc.createdAt()).isEqualTo(NOW);
        assertThat(doc.createdBy()).isEqualTo(USER);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void DOC_5_totals_are_the_sum_of_rounded_lines_not_the_rounding_of_the_sum() {
        BillingDocument doc = draftSale();

        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(
                lineOf(1, product(1L, "3.33", "19"), "3"),
                lineOf(2, product(2L, "0.05", "10"), "1")), NOW, USER);

        // linea 1: 9.99 / 1.90 / 11.89 ; linea 2: 0.05 / 0.01 (0.005 HALF_UP) / 0.06
        assertThat(doc.totals().subtotal()).isEqualTo(money("10.04"));
        assertThat(doc.totals().tax()).isEqualTo(money("1.91"));
        assertThat(doc.totals().total()).isEqualTo(money("11.95"));
    }

    @Test
    void D9_replace_renumbers_lines_one_to_n_in_request_order() {
        BillingDocument doc = draftSale();

        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(
                lineOf(9, product(7L), "1"),
                lineOf(4, product(3L), "1"),
                lineOf(2, product(5L), "1")), NOW, USER);

        assertThat(doc.lines()).extracting(DocumentLine::lineNo).containsExactly(1, 2, 3);
        assertThat(doc.lines()).extracting(DocumentLine::productId).containsExactly(7L, 3L, 5L);
    }

    @Test
    void DRF_2_replace_is_a_full_replace_that_shrinks_and_is_idempotent() {
        BillingDocument doc = readySaleDraft();
        List<DocumentLine> single = List.of(lineOf(1, product(2L), "5"));

        doc.replaceInvoiceDraft(customer(), TODAY, null, single, LATER, USER);
        BillingDocumentState afterFirst = doc.state();
        doc.replaceInvoiceDraft(customer(), TODAY, null, single, LATER, USER);

        assertThat(doc.lines()).hasSize(1);
        assertThat(doc.lines().get(0).productId()).isEqualTo(2L);
        assertThat(doc.state()).isEqualTo(afterFirst);
    }

    @Test
    void DRF_1_replace_with_no_counterparty_and_null_lines_keeps_a_valid_partial_draft() {
        BillingDocument doc = readySaleDraft();

        doc.replaceInvoiceDraft(null, TODAY, null, null, LATER, USER);

        assertThat(doc.counterparty()).isNull();
        assertThat(doc.lines()).isEmpty();
        assertThat(doc.totals()).isEqualTo(DocumentTotals.zero());
    }

    @Test
    void DRF_2_replace_with_null_date_keeps_the_current_date() {
        BillingDocument doc = readySaleDraft();

        doc.replaceInvoiceDraft(customer(), null, null, List.of(), LATER, USER);

        assertThat(doc.issueDate()).isEqualTo(TODAY);
    }

    @Test
    void DRF_4_replace_rejects_a_counterparty_with_the_wrong_role() {
        BillingDocument sale = draftSale();
        BillingDocument purchase = draftPurchase();

        assertThatThrownBy(() -> sale.replaceInvoiceDraft(supplier(), TODAY, null, List.of(), NOW, USER))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND);
        assertThatThrownBy(() -> purchase.replaceInvoiceDraft(customer(), TODAY, "X", List.of(), NOW, USER))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND);
        assertThat(sale.counterparty()).isNull();
    }

    @Test
    void PUR_2_sale_ignores_supplier_invoice_number() {
        BillingDocument doc = draftSale();

        doc.replaceInvoiceDraft(customer(), TODAY, "F-77", List.of(), NOW, USER);

        assertThat(doc.supplierInvoiceNumber()).isNull();
    }

    @Test
    void PUR_2_purchase_keeps_trimmed_supplier_invoice_number_and_blank_becomes_null() {
        BillingDocument doc = draftPurchase();

        doc.replaceInvoiceDraft(supplier(), TODAY, "  F-77  ", List.of(), NOW, USER);
        assertThat(doc.supplierInvoiceNumber()).isEqualTo("F-77");

        doc.replaceInvoiceDraft(supplier(), TODAY, "   ", List.of(), NOW, USER);
        assertThat(doc.supplierInvoiceNumber()).isNull();
    }

    @Test
    void PUR_2_supplier_invoice_number_longer_than_50_is_a_validation_error() {
        BillingDocument doc = draftPurchase();
        String tooLong = "X".repeat(51);

        assertThatThrownBy(() -> doc.replaceInvoiceDraft(supplier(), TODAY, tooLong, List.of(), NOW, USER))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
        assertThat(doc.supplierInvoiceNumber()).isNull();

        doc.replaceInvoiceDraft(supplier(), TODAY, "X".repeat(50), List.of(), NOW, USER);
        assertThat(doc.supplierInvoiceNumber()).hasSize(50);
    }

    // ---------- refreshCatalog (DRF-8, RN-10) ----------

    @Test
    void DRF_8_refreshCatalog_moves_non_overridden_lines_and_recomputes_totals_without_touching_audit() {
        BillingDocument doc = readySaleDraft();

        doc.refreshCatalog(Map.of(1L, product(1L, "200.00", "19")));

        assertThat(doc.lines().get(0).unitPrice()).isEqualTo(money("200.00"));
        assertThat(doc.totals().total()).isEqualTo(money("476.00"));
        assertThat(doc.updatedAt()).isEqualTo(NOW);
        assertThat(doc.version()).isNull();
    }

    @Test
    void RN_10_refreshCatalog_keeps_price_override_and_ignores_missing_products() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(
                DocumentLine.forInvoice(1, product(1L), qty("1"), money("150.00"), null),
                lineOf(2, product(2L), "1")), NOW, USER);

        doc.refreshCatalog(Map.of(1L, product(1L, "200.00", "19")));

        assertThat(doc.lines().get(0).unitPrice()).isEqualTo(money("150.00"));
        assertThat(doc.lines().get(0).catalogUnitPrice()).isEqualTo(money("200.00"));
        // producto 2 no vino en el mapa: conserva su snapshot
        assertThat(doc.lines().get(1).unitPrice()).isEqualTo(money("100.00"));
    }

    // ---------- assertDeletable / assertEmittable ----------

    @Test
    void DRF_5_draft_is_deletable() {
        BillingDocument doc = readySaleDraft();

        doc.assertDeletable();

        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void EMT_1_ready_sale_and_purchase_drafts_are_emittable() {
        readySaleDraft().assertEmittable();
        readyPurchaseDraft().assertEmittable();
    }

    @Test
    void EMT_1_draft_without_counterparty_is_incomplete() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(null, TODAY, null, List.of(lineOf(1, product(1L), "1")), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void EMT_1_draft_without_lines_is_incomplete() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void EMT_1_invoice_with_zero_total_is_incomplete() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(
                DocumentLine.forInvoice(1, product(1L), qty("3"), Money.zero(), null)), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void PUR_2_purchase_without_supplier_invoice_number_is_incomplete() {
        BillingDocument doc = draftPurchase();
        doc.replaceInvoiceDraft(supplier(), TODAY, null, List.of(lineOf(1, product(1L), "1")), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void EMT_1_inactive_counterparty_is_an_inactive_reference() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(inactiveCustomer(), TODAY, null, List.of(lineOf(1, product(1L), "1")), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_INACTIVE_REFERENCE);
    }

    @Test
    void STA_3_assertEmittable_on_a_non_draft_is_invalid_state() {
        assertCode(emittedSale()::assertEmittable, BillingErrorCode.BILLING_INVALID_STATE);
    }

    @Test
    void RET_4_return_draft_without_reason_is_incomplete_and_with_reason_is_emittable() {
        BillingDocument withoutReason = BillingDocument.restore(draftSaleReturnState("  "));
        BillingDocument withReason = BillingDocument.restore(draftSaleReturnState("producto defectuoso"));

        assertCode(withoutReason::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
        withReason.assertEmittable();
    }

    // ---------- emit ----------

    @Test
    void EMT_2_emit_freezes_number_issuer_counterparty_and_audit() {
        BillingDocument doc = readySaleDraft();
        CounterpartySnapshot fresh = new CounterpartySnapshot(customer().thirdId(), CounterpartyRole.CUSTOMER,
                "Cliente renombrado", "NIT", "900999", "1", "Otra direccion", "nuevo@example.com", "3222222222", true);
        DocumentNumber number = DocumentNumber.of(DocumentType.SALE_INVOICE, 8L);

        doc.emit(number, issuer(), fresh, LATER, OTHER_USER);

        assertThat(doc.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(doc.number()).isEqualTo(number);
        assertThat(doc.number().display()).isEqualTo("FV-000008");
        assertThat(doc.issuer()).isEqualTo(issuer());
        assertThat(doc.counterparty()).isEqualTo(fresh);
        assertThat(doc.emittedAt()).isEqualTo(LATER);
        assertThat(doc.emittedBy()).isEqualTo(OTHER_USER);
        assertThat(doc.updatedAt()).isEqualTo(LATER);
        assertThat(doc.lines()).hasSize(1);
        assertThat(doc.totals().total()).isEqualTo(money("238.00"));
    }

    @Test
    void EMT_2_emit_without_fresh_counterparty_keeps_the_draft_one() {
        BillingDocument doc = readySaleDraft();

        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 1L), issuer(), null, LATER, USER);

        assertThat(doc.counterparty()).isEqualTo(customer());
    }

    @Test
    void EMT_3_purchase_emit_uses_fc_prefix_and_keeps_supplier_invoice_number() {
        BillingDocument doc = emittedPurchase();

        assertThat(doc.number().display()).isEqualTo("FC-000007");
        assertThat(doc.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
        assertThat(doc.counterparty().role()).isEqualTo(CounterpartyRole.SUPPLIER);
    }

    @Test
    void EMT_1_failed_emit_leaves_the_draft_untouched_for_every_failure_cause() {
        BillingDocument incomplete = draftSale();
        BillingDocumentState incompleteBefore = incomplete.state();
        BillingDocument inactiveIssuerDoc = readySaleDraft();
        BillingDocumentState inactiveIssuerBefore = inactiveIssuerDoc.state();
        BillingDocument nullIssuerDoc = readySaleDraft();
        BillingDocumentState nullIssuerBefore = nullIssuerDoc.state();
        BillingDocument inactiveFreshDoc = readySaleDraft();
        BillingDocumentState inactiveFreshBefore = inactiveFreshDoc.state();
        DocumentNumber number = DocumentNumber.of(DocumentType.SALE_INVOICE, 1L);

        assertCode(() -> incomplete.emit(number, issuer(), null, LATER, USER),
                BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
        assertCode(() -> inactiveIssuerDoc.emit(number, inactiveIssuer(), null, LATER, USER),
                BillingErrorCode.BILLING_INACTIVE_REFERENCE);
        assertCode(() -> nullIssuerDoc.emit(number, null, null, LATER, USER),
                BillingErrorCode.BILLING_ENTERPRISE_NOT_FOUND);
        assertCode(() -> inactiveFreshDoc.emit(number, issuer(), inactiveCustomer(), LATER, USER),
                BillingErrorCode.BILLING_INACTIVE_REFERENCE);

        assertThat(incomplete.state()).isEqualTo(incompleteBefore);
        assertThat(inactiveIssuerDoc.state()).isEqualTo(inactiveIssuerBefore);
        assertThat(nullIssuerDoc.state()).isEqualTo(nullIssuerBefore);
        assertThat(inactiveFreshDoc.state()).isEqualTo(inactiveFreshBefore);
        assertThat(inactiveFreshDoc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void EMT_3_emit_rejects_a_number_with_the_wrong_prefix() {
        BillingDocument doc = readySaleDraft();

        assertThatThrownBy(() -> doc.emit(DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 1L), issuer(), null,
                LATER, USER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void EMT_4_emit_on_a_non_draft_is_invalid_state() {
        BillingDocument emitted = emittedSale();

        assertCode(() -> emitted.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 9L), issuer(), null, LATER, USER),
                BillingErrorCode.BILLING_INVALID_STATE);
        assertThat(emitted.number().display()).isEqualTo("FV-000007");
    }

    // ---------- annul ----------

    @Test
    void ANL_1_annul_keeps_the_number_and_records_trimmed_reason_and_audit() {
        BillingDocument doc = emittedSale();

        doc.annul("  error de digitacion  ", false, LATER.plusSeconds(120), OTHER_USER);

        assertThat(doc.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(doc.number().display()).isEqualTo("FV-000007");
        assertThat(doc.annulReason()).isEqualTo("error de digitacion");
        assertThat(doc.annulledAt()).isEqualTo(LATER.plusSeconds(120));
        assertThat(doc.annulledBy()).isEqualTo(OTHER_USER);
        assertThat(doc.updatedAt()).isEqualTo(LATER.plusSeconds(120));
    }

    @Test
    void ANL_1_reason_is_required_between_1_and_500_chars() {
        BillingDocument doc = emittedSale();
        BillingDocumentState before = doc.state();

        assertCode(() -> doc.annul(null, false, LATER, USER), BillingErrorCode.BILLING_ANNUL_REASON_REQUIRED);
        assertCode(() -> doc.annul("   ", false, LATER, USER), BillingErrorCode.BILLING_ANNUL_REASON_REQUIRED);
        assertCode(() -> doc.annul("x".repeat(501), false, LATER, USER),
                BillingErrorCode.BILLING_ANNUL_REASON_REQUIRED);
        assertThat(doc.state()).isEqualTo(before);

        doc.annul("x".repeat(500), false, LATER, USER);
        assertThat(doc.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(doc.annulReason()).hasSize(500);
    }

    @Test
    void ANL_1_annul_of_draft_or_annulled_is_invalid_state() {
        BillingDocument draft = readySaleDraft();
        BillingDocument annulled = annulledSale();

        assertCode(() -> draft.annul("motivo", false, LATER, USER), BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(() -> annulled.annul("motivo", false, LATER, USER), BillingErrorCode.BILLING_INVALID_STATE);
        assertThat(draft.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(annulled.annulReason()).isEqualTo("error de digitacion");
    }

    @Test
    void ANL_2_invoice_with_emitted_returns_cannot_be_annulled() {
        BillingDocument doc = emittedSale();
        BillingDocumentState before = doc.state();

        assertCode(() -> doc.annul("motivo", true, LATER, USER), BillingErrorCode.BILLING_HAS_EMITTED_RETURNS);

        assertThat(doc.state()).isEqualTo(before);
        assertThat(doc.status()).isEqualTo(DocumentStatus.EMITIDA);
    }

    // ---------- RN-05 inmutabilidad ----------

    @Test
    void RN_05_emitted_document_rejects_every_mutator_and_state_does_not_change() {
        BillingDocument doc = emittedSale();

        assertEveryMutatorRejected(doc);
    }

    @Test
    void RN_05_annulled_document_rejects_every_mutator_including_annul() {
        BillingDocument doc = annulledSale();

        assertEveryMutatorRejected(doc);
        assertCode(() -> doc.annul("otro", false, LATER, USER), BillingErrorCode.BILLING_INVALID_STATE);
    }

    private static void assertEveryMutatorRejected(BillingDocument doc) {
        BillingDocumentState before = doc.state();
        Map<String, Executable> mutators = new LinkedHashMap<>();
        mutators.put("replaceInvoiceDraft", () -> doc.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(lineOf(1, product(9L), "1")), LATER, USER));
        mutators.put("refreshCatalog", () -> doc.refreshCatalog(Map.of(1L, product(1L, "999.00", "19"))));
        mutators.put("assertDeletable", doc::assertDeletable);
        mutators.put("emit", () -> doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 99L), issuer(), null,
                LATER, USER));
        mutators.put("duplicateAsDraft", () -> doc.duplicateAsDraft(uuid(50), TODAY, LATER, USER));

        for (Map.Entry<String, Executable> mutator : mutators.entrySet()) {
            assertThatThrownBy(mutator.getValue()::execute)
                    .as("mutator %s", mutator.getKey())
                    .isInstanceOf(BillingException.class)
                    .extracting(e -> ((BillingException) e).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_INVALID_STATE);
        }
        assertThat(mutators).hasSize(5);
        assertThat(doc.state()).isEqualTo(before);
    }

    // ---------- guards, getters, state ----------

    @Test
    void STA_3_ensureType_mismatch_behaves_as_not_found() {
        BillingDocument doc = draftSale();

        doc.ensureType(DocumentType.SALE_INVOICE);
        assertCode(() -> doc.ensureType(DocumentType.PURCHASE_INVOICE), BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    @Test
    void DRF_3_ensureVersion_null_skips_equal_passes_and_mismatch_conflicts() {
        BillingDocument persisted = withVersion(readySaleDraft(), 3L);

        persisted.ensureVersion(null);
        persisted.ensureVersion(3L);
        assertCode(() -> persisted.ensureVersion(2L), BillingErrorCode.BILLING_CONCURRENT_MODIFICATION);
        // un documento nunca persistido no puede coincidir con una version esperada
        assertCode(() -> readySaleDraft().ensureVersion(0L), BillingErrorCode.BILLING_CONCURRENT_MODIFICATION);
    }

    @Test
    void DOC_1_lines_getter_is_unmodifiable() {
        BillingDocument doc = readySaleDraft();

        assertThatThrownBy(() -> doc.lines().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> doc.lines().add(lineOf(2, product(2L), "1")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(doc.lines()).hasSize(1);
    }

    @Test
    void DOC_1_input_list_is_copied_so_later_changes_do_not_leak_into_the_document() {
        BillingDocument doc = draftSale();
        List<DocumentLine> input = new ArrayList<>(List.of(lineOf(1, product(1L), "1")));

        doc.replaceInvoiceDraft(customer(), TODAY, null, input, NOW, USER);
        input.add(lineOf(2, product(2L), "1"));

        assertThat(doc.lines()).hasSize(1);
    }

    @Test
    void D1_state_round_trip_through_restore_preserves_everything() {
        BillingDocument original = withVersion(emittedPurchase(), 4L);

        BillingDocument restored = BillingDocument.restore(original.state());

        assertThat(restored.state()).isEqualTo(original.state());
        assertThat(restored.version()).isEqualTo(4L);
        assertThat(restored.totals()).isEqualTo(original.totals());
        assertThat(restored.number()).isEqualTo(original.number());
        assertThat(restored.status()).isEqualTo(DocumentStatus.EMITIDA);
    }

    private static void assertCode(Executable executable, BillingErrorCode expected) {
        assertThatThrownBy(executable::execute)
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(expected);
    }
}
