package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.counterpartyFor;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Retenciones de la factura: se restan del total, se calculan sobre el SUBTOTAL COMPLETO con el
 * redondeo del IVA, un maximo por codigo, el total no puede quedar negativo y la factura emitida
 * las congela.
 */
class BillingDocumentWithholdingTest {

    // Producto 10.000 x 2 = subtotal 20.000, IVA combinado 15% (a 5% + b 10%) = 3.000
    private static final ProductSnapshot PRODUCT = product(1L, "10000.00", "15");

    private static Withholding w(WithholdingCode code, long taxId, String name, String pct) {
        return new Withholding(code, taxId, name, rate(pct));
    }

    private static BillingDocument draftWith(DocumentType type, String quantity, ProductSnapshot product,
            List<Withholding> withholdings) {
        BillingDocument doc = draft(type);
        doc.replaceInvoiceDraft(counterpartyFor(type), TODAY, type == DocumentType.PURCHASE_INVOICE ? "F-1" : null,
                List.of(lineOf(1, product, quantity)), methodTerms(1L), withholdings, NOW, USER);
        return doc;
    }

    private static BillingDocument sale(List<Withholding> withholdings) {
        return draftWith(DocumentType.SALE_INVOICE, "2", PRODUCT, withholdings);
    }

    private static void emit(BillingDocument doc) {
        doc.emit(DocumentNumber.of(doc.type(), 5L), issuer(), null, LATER, OTHER_USER);
    }

    private static BillingDocument emittedSaleWithWithholding() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));
        emit(doc);
        return doc;
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    // ---------- calculo ----------

    @Test
    void the_business_example_nets_to_17000() {
        BillingDocument doc = sale(List.of(
                w(WithholdingCode.RETE_ICA, 3L, "c", "20"),
                w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        DocumentTotals totals = doc.totals();
        assertThat(totals.subtotal().toCents()).isEqualTo(2_000_000L);
        assertThat(totals.tax().toCents()).isEqualTo(300_000L);
        assertThat(totals.withholdings().toCents()).isEqualTo(600_000L);
        assertThat(totals.total().toCents()).isEqualTo(1_700_000L);
        assertThat(doc.withholdings().get(0).amountOn(totals.subtotal()).toCents()).isEqualTo(400_000L);
        assertThat(doc.withholdings().get(1).amountOn(totals.subtotal()).toCents()).isEqualTo(200_000L);
    }

    @Test
    void without_withholdings_the_total_is_unchanged() {
        BillingDocument doc = sale(List.of());

        assertThat(doc.totals().withholdings().toCents()).isZero();
        assertThat(doc.totals().total().toCents()).isEqualTo(2_300_000L);
        assertThat(doc.withholdings()).isEmpty();
    }

    @Test
    void null_withholdings_mean_none() {
        assertThat(sale(null).withholdings()).isEmpty();
    }

    @Test
    void the_rate_applies_to_the_whole_subtotal_not_to_the_tax_base() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_IVA, 1L, "a", "5")));

        assertThat(doc.totals().withholdings().toCents()).isEqualTo(100_000L);
    }

    @Test
    void each_withholding_rounds_half_up_like_the_vat() {
        // subtotal 100.05 x 2.5% = 2.50125 -> 2.50 ; x 0.5% = 0.50025 -> 0.50
        ProductSnapshot cents = product(1L, "100.05", "0");
        BillingDocument doc = draftWith(DocumentType.SALE_INVOICE, "1", cents, List.of(
                w(WithholdingCode.RETE_FUENTE, 2L, "b", "2.5"),
                w(WithholdingCode.RETE_ICA, 3L, "c", "0.5")));

        assertThat(doc.totals().withholdings().toCents()).isEqualTo(250L + 50L);
        assertThat(doc.totals().total().toCents()).isEqualTo(10_005L - 300L);
    }

    @Test
    void the_same_tax_may_back_two_different_withholdings() {
        BillingDocument doc = sale(List.of(
                w(WithholdingCode.RETE_ICA, 2L, "b", "10"),
                w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        assertThat(doc.withholdings()).hasSize(2);
        assertThat(doc.totals().withholdings().toCents()).isEqualTo(400_000L);
    }

    @Test
    void a_zero_rate_withholding_subtracts_nothing() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_IVA, 1L, "cero", "0")));

        assertThat(doc.totals().total().toCents()).isEqualTo(2_300_000L);
    }

    // ---------- reglas ----------

    @Test
    void only_one_withholding_per_code() {
        Throwable thrown = catchThrowable(() -> sale(List.of(
                w(WithholdingCode.RETE_ICA, 3L, "c", "20"),
                w(WithholdingCode.RETE_ICA, 2L, "b", "10"))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_INVALID);
    }

    @Test
    void auto_renta_is_valid_in_a_sale() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.AUTO_RENTA, 2L, "b", "10")));

        assertThat(doc.withholdings()).extracting(Withholding::code).containsExactly(WithholdingCode.AUTO_RENTA);
    }

    @Test
    void auto_renta_in_a_purchase_is_rejected() {
        Throwable thrown = catchThrowable(() -> draftWith(DocumentType.PURCHASE_INVOICE, "2", PRODUCT,
                List.of(w(WithholdingCode.AUTO_RENTA, 2L, "b", "10"))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_INVALID);
    }

    @Test
    void the_three_common_withholdings_are_valid_in_a_purchase() {
        BillingDocument doc = draftWith(DocumentType.PURCHASE_INVOICE, "2", PRODUCT, List.of(
                w(WithholdingCode.RETE_IVA, 1L, "a", "5"),
                w(WithholdingCode.RETE_ICA, 3L, "c", "20"),
                w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        assertThat(doc.withholdings()).hasSize(3);
        assertThat(doc.totals().total().toCents()).isEqualTo(2_300_000L - 100_000L - 400_000L - 200_000L);
    }

    @Test
    void code_rules_by_document_type() {
        assertThat(WithholdingCode.AUTO_RENTA.allowedFor(DocumentType.SALE_INVOICE)).isTrue();
        assertThat(WithholdingCode.AUTO_RENTA.allowedFor(DocumentType.PURCHASE_INVOICE)).isFalse();
        assertThat(WithholdingCode.RETE_IVA.allowedFor(DocumentType.PURCHASE_INVOICE)).isTrue();
        for (WithholdingCode code : WithholdingCode.values()) {
            assertThat(code.allowedFor(DocumentType.SALE_RETURN)).isFalse();
            assertThat(code.allowedFor(DocumentType.PURCHASE_RETURN)).isFalse();
        }
    }

    @Test
    void parse_accepts_known_codes_and_rejects_the_rest() {
        assertThat(WithholdingCode.parse(" rete_ica ")).isEqualTo(WithholdingCode.RETE_ICA);
        assertCode(catchThrowable(() -> WithholdingCode.parse("RETE_X")), BillingErrorCode.BILLING_WITHHOLDING_INVALID);
        assertCode(catchThrowable(() -> WithholdingCode.parse(null)), BillingErrorCode.BILLING_WITHHOLDING_INVALID);
        assertCode(catchThrowable(() -> WithholdingCode.parse(" ")), BillingErrorCode.BILLING_WITHHOLDING_INVALID);
    }

    @Test
    void returns_carry_no_withholdings() {
        BillingDocument original = emittedSaleWithWithholding();
        BillingDocument ret = BillingDocument.newReturnDraft(UUID.randomUUID(), DocumentType.SALE_RETURN, original,
                TODAY, NOW, USER);

        assertThat(ret.withholdings()).isEmpty();
        assertThat(ret.totals().withholdings().toCents()).isZero();
    }

    // ---------- total negativo ----------

    @Test
    void withholdings_above_subtotal_plus_tax_are_rejected_and_leave_the_draft_intact() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_IVA, 1L, "a", "5")));
        DocumentTotals before = doc.totals();

        // 100% + 100% + 100% del subtotal = 60.000 > 23.000
        Throwable thrown = catchThrowable(() -> doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(),
                doc.payment(), List.of(
                        w(WithholdingCode.RETE_IVA, 1L, "x", "100"),
                        w(WithholdingCode.RETE_ICA, 2L, "y", "100"),
                        w(WithholdingCode.RETE_FUENTE, 3L, "z", "100")), NOW, USER));

        assertCode(thrown, BillingErrorCode.BILLING_TOTAL_NEGATIVE);
        assertThat(doc.totals()).isEqualTo(before);
        assertThat(doc.withholdings()).hasSize(1);
    }

    @Test
    void a_total_of_exactly_zero_is_not_negative_but_is_not_emittable() {
        // 100% + 15% del subtotal = 23.000 = subtotal + IVA -> total 0
        BillingDocument doc = sale(List.of(
                w(WithholdingCode.RETE_IVA, 1L, "x", "100"),
                w(WithholdingCode.RETE_ICA, 2L, "y", "15")));

        assertThat(doc.totals().total().toCents()).isZero();
        assertCode(catchThrowable(() -> emit(doc)), BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void a_rate_increase_at_emission_that_turns_the_total_negative_is_rejected() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_IVA, 1L, "a", "5")));

        doc.refreshWithholdings(List.of(
                w(WithholdingCode.RETE_IVA, 1L, "a", "100"),
                w(WithholdingCode.RETE_ICA, 2L, "b", "100"),
                w(WithholdingCode.RETE_FUENTE, 3L, "c", "100")));

        assertCode(catchThrowable(doc::assertEmittable), BillingErrorCode.BILLING_TOTAL_NEGATIVE);
        assertCode(catchThrowable(() -> emit(doc)), BillingErrorCode.BILLING_TOTAL_NEGATIVE);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    // ---------- borrador / emitida ----------

    @Test
    void replacing_the_draft_without_stating_withholdings_keeps_the_current_ones() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(), doc.payment(), NOW, USER);

        assertThat(doc.withholdings()).hasSize(1);
        assertThat(doc.totals().total().toCents()).isEqualTo(2_100_000L);
    }

    @Test
    void replacing_with_an_empty_list_clears_them() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(), doc.payment(), List.of(), NOW, USER);

        assertThat(doc.withholdings()).isEmpty();
        assertThat(doc.totals().total().toCents()).isEqualTo(2_300_000L);
    }

    @Test
    void changing_the_lines_recalculates_the_withholdings() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, List.of(lineOf(1, PRODUCT, "4")), doc.payment(),
                doc.withholdings(), NOW, USER);

        assertThat(doc.totals().subtotal().toCents()).isEqualTo(4_000_000L);
        assertThat(doc.totals().withholdings().toCents()).isEqualTo(400_000L);
        assertThat(doc.totals().total().toCents()).isEqualTo(4_600_000L - 400_000L);
    }

    @Test
    void refreshing_updates_the_rate_and_name_with_the_current_catalog() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        doc.refreshWithholdings(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b nuevo", "11")));

        assertThat(doc.withholdings().get(0).taxName()).isEqualTo("b nuevo");
        assertThat(doc.totals().withholdings().toCents()).isEqualTo(220_000L);
    }

    @Test
    void emitting_freezes_the_withholdings_and_the_net_total() {
        BillingDocument doc = emittedSaleWithWithholding();

        assertThat(doc.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(doc.withholdings()).containsExactly(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10"));
        assertThat(doc.totals().total().toCents()).isEqualTo(2_100_000L);
    }

    @Test
    void an_emitted_invoice_is_read_only_for_withholdings() {
        BillingDocument doc = emittedSaleWithWithholding();

        assertCode(catchThrowable(() -> doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(),
                doc.payment(), List.of(), NOW, USER)), BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(catchThrowable(() -> doc.refreshWithholdings(List.of())), BillingErrorCode.BILLING_INVALID_STATE);
        assertThat(doc.withholdings()).hasSize(1);
    }

    @Test
    void an_annulled_invoice_keeps_its_withholdings_and_stays_read_only() {
        BillingDocument doc = emittedSaleWithWithholding();
        doc.annul("error", false, LATER.plusSeconds(60), OTHER_USER);

        assertThat(doc.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(doc.withholdings()).hasSize(1);
        assertCode(catchThrowable(() -> doc.refreshWithholdings(List.of())), BillingErrorCode.BILLING_INVALID_STATE);
    }

    @Test
    void duplicating_a_draft_keeps_the_withholdings() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        BillingDocument copy = doc.duplicateAsDraft(UUID.randomUUID(), TODAY, NOW, USER);

        assertThat(copy.withholdings()).isEqualTo(doc.withholdings());
        assertThat(copy.totals().total()).isEqualTo(doc.totals().total());
    }

    @Test
    void restoring_from_state_recomputes_the_net_totals() {
        BillingDocument doc = sale(List.of(w(WithholdingCode.RETE_ICA, 3L, "c", "20"),
                w(WithholdingCode.RETE_FUENTE, 2L, "b", "10")));

        BillingDocument restored = BillingDocument.restore(doc.state());

        assertThat(restored.withholdings()).isEqualTo(doc.withholdings());
        assertThat(restored.totals()).isEqualTo(doc.totals());
    }
}
