package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.counterpartyFor;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Metodo de pago de la factura de venta: obligatorio al emitir, cuenta bancaria, snapshots, inmutabilidad. */
class BillingDocumentPaymentTest {

    private static BillingDocument saleWith(PaymentTerms payment) {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(counterpartyFor(DocumentType.SALE_INVOICE), TODAY, null,
                List.of(lineOf(1, product(1L), "2")), payment, NOW, USER);
        return doc;
    }

    private static void emit(BillingDocument doc) {
        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 5L), issuer(), null, LATER, OTHER_USER);
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    // ---------- borrador ----------

    @Test
    void a_draft_may_have_no_payment_method_yet() {
        assertThat(saleWith(null).payment()).isNull();
    }

    @Test
    void the_draft_keeps_only_the_method_id() {
        PaymentTerms payment = saleWith(PaymentTerms.draft(7L)).payment();

        assertThat(payment).isEqualTo(new PaymentTerms(7L, null));
    }

    @Test
    void no_method_means_no_payment() {
        assertThat(PaymentTerms.draft(null)).isNull();
    }

    @Test
    void changing_the_issue_date_keeps_the_payment_untouched() {
        BillingDocument doc = saleWith(methodTerms(7L));
        PaymentTerms before = doc.payment();

        doc.replaceInvoiceDraft(doc.counterparty(), LocalDate.of(2026, 3, 1), null, doc.lines(), doc.payment(), NOW,
                USER);

        assertThat(doc.payment()).isEqualTo(before);
    }

    // ---------- emision ----------

    @Test
    void emit_without_payment_method_is_rejected() {
        BillingDocument doc = saleWith(null);

        assertCode(catchThrowable(() -> emit(doc)), BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(doc.number()).isNull();
    }

    @Test
    void emit_with_a_method_succeeds_and_keeps_the_frozen_name() {
        BillingDocument doc = saleWith(methodTerms(7L));
        doc.freezePaymentMethodName("Efectivo");
        emit(doc);

        assertThat(doc.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(doc.payment().paymentMethodName()).isEqualTo("Efectivo");
    }

    @Test
    void freezing_a_payment_sets_the_method_name_and_keeps_the_id() {
        BillingDocument doc = saleWith(PaymentTerms.draft(7L));

        doc.freezePayment("Transferencia bancaria");

        assertThat(doc.payment()).isEqualTo(new PaymentTerms(7L, "Transferencia bancaria"));
    }

    @Test
    void freezing_with_no_payment_does_nothing() {
        BillingDocument doc = saleWith(null);

        doc.freezePayment("X");

        assertThat(doc.payment()).isNull();
    }

    @Test
    void withoutSnapshot_clears_the_name_but_keeps_the_id() {
        PaymentTerms frozen = PaymentTerms.draft(7L).withMethodName("M");

        assertThat(frozen.withoutSnapshot().paymentMethodName()).isNull();
        assertThat(frozen.withoutSnapshot().paymentMethodId()).isEqualTo(7L);
    }

    // ---------- inmutabilidad ----------

    @Test
    void an_emitted_invoice_does_not_admit_payment_changes() {
        BillingDocument doc = saleWith(methodTerms(7L));
        emit(doc);

        assertCode(catchThrowable(() -> doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(),
                methodTerms(8L), LATER, USER)), BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(catchThrowable(() -> doc.freezePaymentMethodName("X")), BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(catchThrowable(() -> doc.freezePayment("X")), BillingErrorCode.BILLING_INVALID_STATE);
        assertThat(doc.payment().paymentMethodId()).isEqualTo(7L);
    }

    @Test
    void annulling_keeps_the_payment() {
        BillingDocument doc = saleWith(methodTerms(7L));
        emit(doc);

        doc.annul("error", false, LATER.plusSeconds(1), OTHER_USER);

        assertThat(doc.payment().paymentMethodId()).isEqualTo(7L);
    }

    // ---------- solo ventas ----------

    @Test
    void a_purchase_invoice_keeps_payment_and_requires_it_to_emit() {
        BillingDocument purchase = readyPurchaseDraft();

        purchase.replaceInvoiceDraft(purchase.counterparty(), TODAY, "F-77", purchase.lines(), methodTerms(2L),
                NOW, USER);
        assertThat(purchase.payment().paymentMethodId()).isEqualTo(2L);
        purchase.emit(DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 5L), issuer(), null, LATER, OTHER_USER);
        assertThat(purchase.status()).isEqualTo(DocumentStatus.EMITIDA);

        BillingDocument noPayment = readyPurchaseDraft();
        noPayment.replaceInvoiceDraft(noPayment.counterparty(), TODAY, "F-77", noPayment.lines(), null, NOW, USER);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> noPayment.emit(
                DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 6L), issuer(), null, LATER, OTHER_USER))
                .isInstanceOf(co.unicauca.edu.co.contables.billing.domain.exception.BillingException.class);
    }

    @Test
    void returns_never_carry_payment_and_only_invoices_support_it() {
        assertThat(DocumentType.SALE_INVOICE.supportsPayment()).isTrue();
        assertThat(DocumentType.PURCHASE_INVOICE.supportsPayment()).isTrue();
        assertThat(DocumentType.SALE_RETURN.supportsPayment()).isFalse();
        assertThat(DocumentType.PURCHASE_RETURN.supportsPayment()).isFalse();
    }

    @Test
    void restoring_a_return_drops_any_payment_in_the_state() {
        BillingDocument sale = readyPurchaseDraft();
        BillingDocumentState withPayment = new BillingDocumentState(sale.id(), sale.enterpriseId(),
                DocumentType.PURCHASE_RETURN, sale.status(), null, TODAY, sale.counterparty(), "F-77", null, null, null,
                sale.lines(), NOW, USER, NOW, USER, null, null, null, null, null, 1L, methodTerms(1L));

        assertThat(BillingDocument.restore(withPayment).payment()).isNull();
    }

    // ---------- duplicado ----------

    @Test
    void duplicating_keeps_the_method_id_and_drops_the_frozen_name() {
        BillingDocument source = saleWith(PaymentTerms.draft(7L));
        source.freezePayment("Transferencia");

        BillingDocument copy = source.duplicateAsDraft(UUID.randomUUID(), TODAY, NOW, USER);

        assertThat(copy.payment()).isEqualTo(new PaymentTerms(7L, null));
    }
}
