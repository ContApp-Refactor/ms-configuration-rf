package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Factura de compra sobre el mismo agregado que la venta (PUR-1..3, RN-24): proveedor como
 * contraparte, numero de factura del proveedor obligatorio al emitir, prefijo FC y costo como
 * referencia de catalogo.
 */
class BillingDocumentPurchaseTest {

    private static void assertCode(Executable action, BillingErrorCode expected) {
        assertThatThrownBy(action::execute)
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(expected);
    }

    // ---------- tipo y rol ----------

    @Test
    void PUR_1_purchase_invoice_is_a_purchase_kind_invoice_with_fc_prefix_and_supplier_role() {
        assertThat(DocumentType.PURCHASE_INVOICE.kind()).isEqualTo(DocumentKind.PURCHASE);
        assertThat(DocumentType.PURCHASE_INVOICE.isInvoice()).isTrue();
        assertThat(DocumentType.PURCHASE_INVOICE.prefix()).isEqualTo("FC");
        assertThat(DocumentType.PURCHASE_INVOICE.kind().counterpartyRole()).isEqualTo(CounterpartyRole.SUPPLIER);
    }

    @Test
    void PUR_1_new_purchase_draft_is_partial_unnumbered_and_of_purchase_type() {
        BillingDocument doc = draftPurchase();

        assertThat(doc.type()).isEqualTo(DocumentType.PURCHASE_INVOICE);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(doc.number()).isNull();
        assertThat(doc.counterparty()).isNull();
        assertThat(doc.supplierInvoiceNumber()).isNull();
    }

    @Test
    void PUR_1_purchase_draft_accepts_a_supplier_and_rejects_a_customer() {
        BillingDocument doc = draftPurchase();

        doc.replaceInvoiceDraft(supplier(), TODAY, SUPPLIER_INVOICE_NUMBER, List.of(), NOW, USER);
        assertThat(doc.counterparty().role()).isEqualTo(CounterpartyRole.SUPPLIER);

        assertCode(() -> doc.replaceInvoiceDraft(customer(), TODAY, SUPPLIER_INVOICE_NUMBER, List.of(), NOW, USER),
                BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND);
        assertThat(doc.counterparty().role()).isEqualTo(CounterpartyRole.SUPPLIER);
    }

    // ---------- costo como referencia de catalogo ----------

    @Test
    void PUR_1_purchase_line_follows_the_catalog_cost_and_overrides_it() {
        BillingDocument doc = draftPurchase();
        doc.replaceInvoiceDraft(supplier(), TODAY, SUPPLIER_INVOICE_NUMBER, List.of(
                lineOf(1, product(1L, "80.00", "19"), "2"),
                DocumentLine.forInvoice(2, product(2L, "50.00", "19"), Quantity.of(new java.math.BigDecimal("1")),
                        money("45.00"), null)), NOW, USER);

        assertThat(doc.lines().get(0).unitPrice()).isEqualTo(money("80.00"));
        assertThat(doc.lines().get(0).priceOverridden()).isFalse();
        assertThat(doc.lines().get(1).unitPrice()).isEqualTo(money("45.00"));
        assertThat(doc.lines().get(1).catalogUnitPrice()).isEqualTo(money("50.00"));
        assertThat(doc.lines().get(1).priceOverridden()).isTrue();
        // 160.00 + 45.00 = 205.00 ; IVA 30.40 + 8.55 = 38.95
        assertThat(doc.totals().subtotal()).isEqualTo(money("205.00"));
        assertThat(doc.totals().tax()).isEqualTo(money("38.95"));
        assertThat(doc.totals().total()).isEqualTo(money("243.95"));
    }

    @Test
    void PUR_1_refreshCatalog_moves_the_cost_reference_of_a_purchase_draft() {
        BillingDocument doc = readyPurchaseDraft();

        doc.refreshCatalog(Map.of(1L, product(1L, "150.00", "19")));

        assertThat(doc.lines().get(0).unitPrice()).isEqualTo(money("150.00"));
        assertThat(doc.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
    }

    // ---------- numero de factura del proveedor al emitir ----------

    @Test
    void PUR_2_purchase_draft_without_supplier_invoice_number_is_incomplete() {
        BillingDocument doc = draftPurchase();
        doc.replaceInvoiceDraft(supplier(), TODAY, null, List.of(lineOf(1, product(1L), "1")), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void PUR_2_blank_supplier_invoice_number_counts_as_missing() {
        BillingDocument doc = draftPurchase();
        doc.replaceInvoiceDraft(supplier(), TODAY, "   ", List.of(lineOf(1, product(1L), "1")), NOW, USER);

        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);
    }

    @Test
    void PUR_2_emit_without_supplier_invoice_number_fails_and_leaves_the_draft_untouched() {
        BillingDocument doc = draftPurchase();
        doc.replaceInvoiceDraft(supplier(), TODAY, null, List.of(lineOf(1, product(1L), "1")), NOW, USER);
        BillingDocumentState before = doc.state();

        assertCode(() -> doc.emit(DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 1L), issuer(), null, LATER,
                OTHER_USER), BillingErrorCode.BILLING_DRAFT_INCOMPLETE);

        assertThat(doc.state()).isEqualTo(before);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(doc.number()).isNull();
    }

    @Test
    void PUR_2_purchase_draft_with_supplier_invoice_number_is_emittable() {
        readyPurchaseDraft().assertEmittable();
    }

    // ---------- emision ----------

    @Test
    void PUR_1_emit_assigns_fc_number_freezes_supplier_data_and_keeps_the_supplier_invoice_number() {
        BillingDocument doc = readyPurchaseDraft();
        CounterpartySnapshot fresh = new CounterpartySnapshot(supplier().thirdId(), CounterpartyRole.SUPPLIER,
                "Proveedor renombrado", "NIT", "800999", "5", "Otra direccion", "nuevo@example.com", "3222222222",
                true);

        doc.emit(DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 12L), issuer(), fresh, LATER, OTHER_USER);

        assertThat(doc.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(doc.number().display()).isEqualTo("FC-000012");
        assertThat(doc.counterparty()).isEqualTo(fresh);
        assertThat(doc.counterparty().role()).isEqualTo(CounterpartyRole.SUPPLIER);
        assertThat(doc.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
        assertThat(doc.emittedAt()).isEqualTo(LATER);
        assertThat(doc.emittedBy()).isEqualTo(OTHER_USER);
        assertThat(doc.totals().total()).isEqualTo(money("238.00"));
    }

    @Test
    void PUR_1_emit_with_a_sale_prefix_is_rejected() {
        BillingDocument doc = readyPurchaseDraft();

        assertThatThrownBy(() -> doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 1L), issuer(), null, LATER,
                USER)).isInstanceOf(IllegalArgumentException.class);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void PUR_1_emit_with_a_customer_as_fresh_counterparty_is_rejected() {
        BillingDocument doc = readyPurchaseDraft();

        assertCode(() -> doc.emit(DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 1L), issuer(), customer(), LATER,
                USER), BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void PUR_1_emit_with_an_inactive_supplier_is_an_inactive_reference() {
        BillingDocument doc = readyPurchaseDraft();
        CounterpartySnapshot inactive = new CounterpartySnapshot(supplier().thirdId(), CounterpartyRole.SUPPLIER,
                "Proveedor", "NIT", "8001", "3", "Calle", "p@example.com", "3", false);

        assertCode(() -> doc.emit(DocumentNumber.of(DocumentType.PURCHASE_INVOICE, 1L), issuer(), inactive, LATER,
                USER), BillingErrorCode.BILLING_INACTIVE_REFERENCE);
        assertThat(doc.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    // ---------- duplicado (DRF-6) ----------

    @Test
    void PUR_2_duplicate_does_not_copy_the_supplier_invoice_number_but_keeps_supplier_and_lines() {
        BillingDocument source = readyPurchaseDraft();

        BillingDocument copy = source.duplicateAsDraft(uuid(9), TODAY, LATER, OTHER_USER);

        assertThat(copy.id()).isEqualTo(uuid(9));
        assertThat(copy.type()).isEqualTo(DocumentType.PURCHASE_INVOICE);
        assertThat(copy.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(copy.number()).isNull();
        assertThat(copy.supplierInvoiceNumber()).isNull();
        assertThat(copy.counterparty()).isEqualTo(supplier());
        assertThat(copy.lines()).isEqualTo(source.lines());
        assertThat(source.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
    }

    @Test
    void PUR_2_duplicate_of_a_purchase_must_get_a_new_supplier_invoice_number_before_emitting() {
        BillingDocument copy = readyPurchaseDraft().duplicateAsDraft(uuid(9), TODAY, LATER, OTHER_USER);

        assertCode(copy::assertEmittable, BillingErrorCode.BILLING_DRAFT_INCOMPLETE);

        copy.replaceInvoiceDraft(copy.counterparty(), TODAY, "F-78", copy.lines(), LATER, OTHER_USER);
        copy.assertEmittable();
    }

    @Test
    void PUR_3_duplicate_of_an_emitted_purchase_is_invalid_state() {
        assertCode(() -> emittedPurchase().duplicateAsDraft(uuid(9), TODAY, LATER, USER),
                BillingErrorCode.BILLING_INVALID_STATE);
    }

    // ---------- inmutabilidad y anulacion ----------

    @Test
    void RN_05_emitted_purchase_rejects_every_draft_mutator() {
        BillingDocument doc = emittedPurchase();

        assertCode(() -> doc.replaceInvoiceDraft(supplier(), TODAY, "OTRO", List.of(), LATER, USER),
                BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(() -> doc.refreshCatalog(Map.of(1L, product(1L, "500.00", "19"))),
                BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(doc::assertDeletable, BillingErrorCode.BILLING_INVALID_STATE);
        assertCode(doc::assertEmittable, BillingErrorCode.BILLING_INVALID_STATE);
        assertThat(doc.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
    }

    @Test
    void ANL_1_annulled_purchase_keeps_fc_number_and_supplier_invoice_number() {
        BillingDocument doc = emittedPurchase();

        doc.annul("factura duplicada", false, LATER.plusSeconds(60), OTHER_USER);

        assertThat(doc.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(doc.number().display()).isEqualTo("FC-000007");
        assertThat(doc.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
        assertThat(doc.annulReason()).isEqualTo("factura duplicada");
        assertThat(doc.annulledBy()).isEqualTo(OTHER_USER);
    }

    @Test
    void ANL_2_purchase_with_emitted_returns_cannot_be_annulled() {
        BillingDocument doc = emittedPurchase();

        assertCode(() -> doc.annul("motivo", true, LATER, USER), BillingErrorCode.BILLING_HAS_EMITTED_RETURNS);
        assertThat(doc.status()).isEqualTo(DocumentStatus.EMITIDA);
    }

    @Test
    void PUR_3_purchase_document_behaves_as_not_found_for_the_sale_resource_and_vice_versa() {
        BillingDocument doc = emittedPurchase();

        doc.ensureType(DocumentType.PURCHASE_INVOICE);
        assertCode(() -> doc.ensureType(DocumentType.SALE_INVOICE), BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertCode(() -> doc.ensureType(DocumentType.PURCHASE_RETURN), BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }
}
