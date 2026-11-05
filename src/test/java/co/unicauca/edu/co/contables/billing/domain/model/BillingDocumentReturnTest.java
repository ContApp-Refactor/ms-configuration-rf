package co.unicauca.edu.co.contables.billing.domain.model;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedInvoice;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSaleWithLines;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.inactiveIssuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RET-1, RET-2, RET-4, RET-6, RET-7, RET-8 (RN-17..RN-19, RN-22, RN-23, RN-23b): devoluciones como
 * documento. Dominio puro, sin Spring.
 */
class BillingDocumentReturnTest {

    private static final LocalDate RETURN_DAY = LocalDate.of(2026, 2, 1);

    /** FV-000007: linea 1 = producto 1 x10 ; linea 2 = producto 2 x5 (100.00 + 19%). */
    private BillingDocument invoice;

    @BeforeEach
    void setUp() {
        invoice = emittedSaleWithLines(uuid(1), 7L, lineOf(1, product(1L), "10"), lineOf(2, product(2L), "5"));
    }

    private static void assertCode(Throwable thrown, BillingErrorCode expected) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(expected);
    }

    // ---------- newReturnDraft (RET-1) ----------

    @Test
    void RET_1_new_return_draft_of_a_sale_invoice_is_a_partial_NC_draft() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, LATER, OTHER_USER);

        assertThat(draft.id()).isEqualTo(uuid(2));
        assertThat(draft.type()).isEqualTo(DocumentType.SALE_RETURN);
        assertThat(draft.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(draft.enterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(draft.number()).isNull();
        assertThat(draft.version()).isNull();
        assertThat(draft.lines()).isEmpty();
        assertThat(draft.returnReason()).isNull();
        assertThat(draft.issueDate()).isEqualTo(RETURN_DAY);
        assertThat(draft.createdAt()).isEqualTo(LATER);
        assertThat(draft.createdBy()).isEqualTo(OTHER_USER);
        assertThat(draft.updatedAt()).isEqualTo(LATER);
        assertThat(draft.totals().total()).isEqualTo(Money.zero());
        assertThat(draft.issuer()).isNull();
    }

    @Test
    void RET_1_the_draft_references_the_original_and_copies_its_counterparty() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, LATER, OTHER_USER);

        assertThat(draft.original()).isEqualTo(new OriginalDocumentRef(uuid(1), DocumentType.SALE_INVOICE,
                DocumentNumber.of(DocumentType.SALE_INVOICE, 7L), TODAY));
        assertThat(draft.counterparty()).isEqualTo(invoice.counterparty());
        assertThat(draft.counterparty()).isEqualTo(customer());
        assertThat(draft.supplierInvoiceNumber()).isNull();
    }

    @Test
    void RET_1_new_return_draft_of_a_purchase_invoice_is_a_DC_draft_with_supplier_data() {
        BillingDocument purchase = emittedPurchase();

        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.PURCHASE_RETURN, purchase,
                RETURN_DAY, LATER, USER);

        assertThat(draft.type()).isEqualTo(DocumentType.PURCHASE_RETURN);
        assertThat(draft.counterparty()).isEqualTo(supplier());
        assertThat(draft.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
        assertThat(draft.original().type()).isEqualTo(DocumentType.PURCHASE_INVOICE);
        assertThat(draft.original().number().display()).isEqualTo("FC-000007");
    }

    @Test
    void RET_1_original_in_BORRADOR_is_ORIGINAL_NOT_EMITTED() {
        BillingDocument draftInvoice = readySaleDraft();

        assertThatThrownBy(() -> BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, draftInvoice,
                RETURN_DAY, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED));
    }

    @Test
    void RET_1_original_ANULADA_is_ORIGINAL_NOT_EMITTED() {
        BillingDocument annulled = annulledSale();

        assertThatThrownBy(() -> BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, annulled,
                RETURN_DAY, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED));
    }

    @Test
    void RET_1_sale_return_against_a_purchase_invoice_behaves_as_not_found() {
        BillingDocument purchase = emittedPurchase();

        assertThatThrownBy(() -> BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, purchase,
                RETURN_DAY, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND));
    }

    @Test
    void RET_1_purchase_return_against_a_sale_invoice_behaves_as_not_found() {
        assertThatThrownBy(() -> BillingDocument.newReturnDraft(uuid(2), DocumentType.PURCHASE_RETURN, invoice,
                RETURN_DAY, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND));
    }

    @Test
    void RET_1_a_return_cannot_be_the_original_of_another_return() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "1"));

        assertThatThrownBy(() -> BillingDocument.newReturnDraft(uuid(3), DocumentType.SALE_RETURN, emittedReturn,
                RETURN_DAY, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND));
    }

    @Test
    void RET_1_an_invoice_type_is_not_a_valid_return_type() {
        assertThatThrownBy(() -> BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_INVOICE, invoice,
                RETURN_DAY, LATER, USER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void RET_8_creating_a_return_never_modifies_the_original() {
        BillingDocumentState before = invoice.state();

        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, LATER, USER);
        draft.replaceReturnDraft(RETURN_DAY, "defectuoso", List.of(returnLineOf(invoice, 1, "3")), LATER, USER);
        draft.emit(DocumentNumber.of(DocumentType.SALE_RETURN, 1L), issuer(), null, LATER, USER);

        assertThat(invoice.state()).isEqualTo(before);
        assertThat(invoice.status()).isEqualTo(DocumentStatus.EMITIDA);
    }

    // ---------- DocumentLine.forReturn (RET-2) ----------

    @Test
    void RET_2_return_line_inherits_the_effective_price_and_tax_of_the_original_line() {
        DocumentLine source = DocumentLine.forInvoice(4, product(9L, "100.00", "19"), qty("10"), money("150.00"),
                rate("5"));

        DocumentLine returned = DocumentLine.forReturn(1, source, qty("4"));

        assertThat(returned.lineNo()).isEqualTo(1);
        assertThat(returned.sourceLineNo()).isEqualTo(4);
        assertThat(returned.productId()).isEqualTo(9L);
        assertThat(returned.productCode()).isEqualTo(source.productCode());
        assertThat(returned.productName()).isEqualTo(source.productName());
        assertThat(returned.unitOfMeasureId()).isEqualTo(source.unitOfMeasureId());
        assertThat(returned.unitOfMeasureName()).isEqualTo(source.unitOfMeasureName());
        assertThat(returned.quantity()).isEqualTo(qty("4"));
        assertThat(returned.unitPrice()).isEqualTo(money("150.00"));
        assertThat(returned.taxRate()).isEqualTo(rate("5"));
        assertThat(returned.catalogUnitPrice()).isEqualTo(money("150.00"));
        assertThat(returned.catalogTaxRate()).isEqualTo(rate("5"));
        assertThat(returned.priceOverridden()).isFalse();
        assertThat(returned.taxOverridden()).isFalse();
    }

    @Test
    void RET_2_return_line_inherits_catalog_values_when_the_original_had_no_override() {
        DocumentLine source = lineOf(2, product(5L, "80.00", "10"), "3");

        DocumentLine returned = DocumentLine.forReturn(1, source, qty("1"));

        assertThat(returned.unitPrice()).isEqualTo(money("80.00"));
        assertThat(returned.taxRate()).isEqualTo(rate("10"));
        assertThat(returned.sourceLineNo()).isEqualTo(2);
    }

    @Test
    void RET_2_return_line_amounts_use_the_inherited_values() {
        DocumentLine returned = DocumentLine.forReturn(1, lineOf(1, product(1L), "10"), qty("4"));

        assertThat(returned.subtotal()).isEqualTo(money("400.00"));
        assertThat(returned.taxAmount()).isEqualTo(money("76.00"));
        assertThat(returned.total()).isEqualTo(money("476.00"));
    }

    @Test
    void RET_2_return_line_quantity_equal_to_the_sold_one_is_accepted() {
        DocumentLine returned = DocumentLine.forReturn(1, lineOf(1, product(1L), "10"), qty("10"));

        assertThat(returned.quantity()).isEqualTo(qty("10"));
    }

    @Test
    void RET_2_return_line_quantity_above_the_sold_one_is_rejected() {
        DocumentLine source = lineOf(1, product(1L), "10");

        assertThatThrownBy(() -> DocumentLine.forReturn(1, source, qty("10.001")))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED));
    }

    // ---------- replaceReturnDraft (RET-2, RET-4, RET-6) ----------

    @Test
    void RET_2_replace_stores_lines_renumbered_keeping_source_line_and_computes_totals() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);

        draft.replaceReturnDraft(RETURN_DAY, "producto defectuoso",
                List.of(returnLineOf(invoice, 2, "1"), returnLineOf(invoice, 1, "4")), LATER, OTHER_USER);

        assertThat(draft.lines()).extracting(DocumentLine::lineNo).containsExactly(1, 2);
        assertThat(draft.lines()).extracting(DocumentLine::sourceLineNo).containsExactly(2, 1);
        assertThat(draft.totals().subtotal()).isEqualTo(money("500.00"));
        assertThat(draft.totals().tax()).isEqualTo(money("95.00"));
        assertThat(draft.totals().total()).isEqualTo(money("595.00"));
        assertThat(draft.returnReason()).isEqualTo("producto defectuoso");
        assertThat(draft.updatedAt()).isEqualTo(LATER);
        assertThat(draft.updatedBy()).isEqualTo(OTHER_USER);
        assertThat(draft.createdAt()).isEqualTo(NOW);
    }

    @Test
    void RET_6_replace_is_a_full_idempotent_replacement() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);
        draft.replaceReturnDraft(RETURN_DAY, "uno", List.of(returnLineOf(invoice, 1, "9")), NOW, USER);

        draft.replaceReturnDraft(RETURN_DAY, "dos", List.of(returnLineOf(invoice, 2, "2")), LATER, USER);
        BillingDocumentState once = draft.state();
        draft.replaceReturnDraft(RETURN_DAY, "dos", List.of(returnLineOf(invoice, 2, "2")), LATER, USER);

        assertThat(draft.state()).isEqualTo(once);
        assertThat(draft.lines()).hasSize(1);
        assertThat(draft.lines().get(0).sourceLineNo()).isEqualTo(2);
        assertThat(draft.returnReason()).isEqualTo("dos");
    }

    @Test
    void RET_4_reason_is_trimmed_and_a_blank_one_is_stored_as_null_in_a_partial_draft() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);

        draft.replaceReturnDraft(RETURN_DAY, "  danado  ", List.of(), NOW, USER);
        assertThat(draft.returnReason()).isEqualTo("danado");

        draft.replaceReturnDraft(RETURN_DAY, "   ", List.of(), NOW, USER);
        assertThat(draft.returnReason()).isNull();

        draft.replaceReturnDraft(RETURN_DAY, null, null, NOW, USER);
        assertThat(draft.returnReason()).isNull();
        assertThat(draft.lines()).isEmpty();
    }

    @Test
    void RET_4_reason_longer_than_500_is_rejected_and_the_draft_is_untouched() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = draft.state();
        String tooLong = "x".repeat(501);

        assertThatThrownBy(() -> draft.replaceReturnDraft(RETURN_DAY, tooLong, List.of(), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_VALIDATION_ERROR));
        assertThat(draft.state()).isEqualTo(before);
    }

    @Test
    void RET_4_reason_of_exactly_500_is_accepted() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);

        draft.replaceReturnDraft(RETURN_DAY, "x".repeat(500), List.of(), NOW, USER);

        assertThat(draft.returnReason()).hasSize(500);
    }

    @Test
    void RET_6_a_null_date_keeps_the_current_one() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);

        draft.replaceReturnDraft(null, "motivo", List.of(), LATER, USER);

        assertThat(draft.issueDate()).isEqualTo(RETURN_DAY);
    }

    @Test
    void RET_2_replace_rejects_two_lines_of_the_same_source_line() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);
        BillingDocumentState before = draft.state();

        assertThatThrownBy(() -> draft.replaceReturnDraft(RETURN_DAY, "motivo",
                List.of(returnLineOf(invoice, 1, "1"), returnLineOf(invoice, 1, "2")), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_LINE));
        assertThat(draft.state()).isEqualTo(before);
    }

    @Test
    void RET_2_replace_rejects_a_line_without_source_line() {
        BillingDocument draft = BillingDocument.newReturnDraft(uuid(2), DocumentType.SALE_RETURN, invoice,
                RETURN_DAY, NOW, USER);
        DocumentLine invoiceLine = lineOf(1, product(1L), "1");

        assertThatThrownBy(() -> draft.replaceReturnDraft(RETURN_DAY, "motivo", List.of(invoiceLine), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_LINE));
    }

    // ---------- emision (RET-4) ----------

    @Test
    void RET_4_draft_without_reason_cannot_be_emitted() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), null, returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = draft.state();

        assertThatThrownBy(draft::assertEmittable)
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DRAFT_INCOMPLETE));
        assertThatThrownBy(() -> draft.emit(DocumentNumber.of(DocumentType.SALE_RETURN, 1L), issuer(), null, LATER,
                USER)).satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DRAFT_INCOMPLETE));
        assertThat(draft.state()).isEqualTo(before);
    }

    @Test
    void RET_4_draft_with_blank_reason_cannot_be_emitted() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "   ", returnLineOf(invoice, 1, "1"));

        assertThatThrownBy(draft::assertEmittable)
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DRAFT_INCOMPLETE));
    }

    @Test
    void RET_4_draft_without_lines_cannot_be_emitted() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo");

        assertThatThrownBy(draft::assertEmittable)
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_DRAFT_INCOMPLETE));
    }

    @Test
    void RET_4_complete_draft_is_emittable_and_emit_assigns_NC_number() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "4"));

        draft.assertEmittable();
        draft.emit(DocumentNumber.of(DocumentType.SALE_RETURN, 3L), issuer(), null, LATER, OTHER_USER);

        assertThat(draft.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(draft.number().display()).isEqualTo("NC-000003");
        assertThat(draft.issuer()).isEqualTo(issuer());
        assertThat(draft.emittedAt()).isEqualTo(LATER);
        assertThat(draft.emittedBy()).isEqualTo(OTHER_USER);
        assertThat(draft.original().id()).isEqualTo(uuid(1));
    }

    @Test
    void RET_4_emit_of_a_purchase_return_assigns_DC_number() {
        BillingDocument purchase = emittedInvoice(uuid(10), DocumentType.PURCHASE_INVOICE, 5L,
                List.of(lineOf(1, product(1L), "6")));
        BillingDocument draft = returnDraftOf(purchase, uuid(11), "no conforme", returnLineOf(purchase, 1, "2"));

        draft.emit(DocumentNumber.of(DocumentType.PURCHASE_RETURN, 1L), issuer(), null, LATER, USER);

        assertThat(draft.number().display()).isEqualTo("DC-000001");
        assertThat(draft.type()).isEqualTo(DocumentType.PURCHASE_RETURN);
    }

    @Test
    void RET_4_emit_with_the_prefix_of_an_invoice_is_rejected() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));

        assertThatThrownBy(() -> draft.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 3L), issuer(), null, LATER,
                USER)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void RET_4_emit_keeps_the_counterparty_of_the_original_even_if_a_fresh_one_is_given() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));

        draft.emit(DocumentNumber.of(DocumentType.SALE_RETURN, 1L), issuer(), customer(99L), LATER, USER);

        assertThat(draft.counterparty()).isEqualTo(customer());
    }

    @Test
    void RET_4_emit_with_an_inactive_issuer_is_INACTIVE_REFERENCE_and_leaves_the_draft_untouched() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = draft.state();

        assertThatThrownBy(() -> draft.emit(DocumentNumber.of(DocumentType.SALE_RETURN, 1L),
                inactiveIssuer(), null, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INACTIVE_REFERENCE));
        assertThat(draft.state()).isEqualTo(before);
    }

    // ---------- inmutabilidad y estado (RN-23b) ----------

    @Test
    void RN_23b_draft_return_can_be_deleted() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));

        draft.assertDeletable();
    }

    @Test
    void RN_05_emitted_return_rejects_every_mutator_with_INVALID_STATE() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = emittedReturn.state();

        assertThatThrownBy(() -> emittedReturn.replaceReturnDraft(RETURN_DAY, "otro", List.of(), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThatThrownBy(emittedReturn::assertDeletable)
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThatThrownBy(emittedReturn::assertEmittable)
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThatThrownBy(() -> emittedReturn.emit(DocumentNumber.of(DocumentType.SALE_RETURN, 9L), issuer(), null,
                LATER, USER)).satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThat(emittedReturn.state()).isEqualTo(before);
    }

    @Test
    void RN_05_annulled_return_rejects_replace_and_delete() {
        BillingDocument annulledReturn = annulledReturnOf(invoice, uuid(3), 2L, returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = annulledReturn.state();

        assertThatThrownBy(() -> annulledReturn.replaceReturnDraft(RETURN_DAY, "otro", List.of(), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThatThrownBy(annulledReturn::assertDeletable)
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThat(annulledReturn.state()).isEqualTo(before);
    }

    @Test
    void RET_6_invoice_operations_are_rejected_on_a_return_draft() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = draft.state();

        assertThatThrownBy(() -> draft.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(lineOf(1, product(1L), "1")), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThatThrownBy(() -> draft.refreshCatalog(java.util.Map.of(1L, product(1L, "200.00", "19"))))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThatThrownBy(() -> draft.duplicateAsDraft(uuid(9), TODAY, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThat(draft.state()).isEqualTo(before);
    }

    @Test
    void RET_6_return_operations_are_rejected_on_an_invoice() {
        BillingDocument draftInvoice = readySaleDraft();
        BillingDocumentState before = draftInvoice.state();

        assertThatThrownBy(() -> draftInvoice.replaceReturnDraft(RETURN_DAY, "motivo", List.of(), LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
        assertThat(draftInvoice.state()).isEqualTo(before);
    }

    // ---------- anulacion (RET-7, RN-22) ----------

    @Test
    void RET_7_annulling_an_emitted_return_requires_reason_and_keeps_the_number() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 4L, returnLineOf(invoice, 1, "1"));

        emittedReturn.annul("  error al registrar  ", false, LATER.plusSeconds(120), OTHER_USER);

        assertThat(emittedReturn.status()).isEqualTo(DocumentStatus.ANULADA);
        assertThat(emittedReturn.number().display()).isEqualTo("NC-000004");
        assertThat(emittedReturn.annulReason()).isEqualTo("error al registrar");
        assertThat(emittedReturn.annulledBy()).isEqualTo(OTHER_USER);
        assertThat(emittedReturn.annulledAt()).isEqualTo(LATER.plusSeconds(120));
    }

    @Test
    void RET_7_annulling_a_return_without_reason_is_ANNUL_REASON_REQUIRED() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 4L, returnLineOf(invoice, 1, "1"));
        BillingDocumentState before = emittedReturn.state();

        assertThatThrownBy(() -> emittedReturn.annul("  ", false, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_ANNUL_REASON_REQUIRED));
        assertThatThrownBy(() -> emittedReturn.annul(null, false, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_ANNUL_REASON_REQUIRED));
        assertThat(emittedReturn.state()).isEqualTo(before);
    }

    @Test
    void RET_7_annulling_a_draft_return_is_INVALID_STATE() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "1"));

        assertThatThrownBy(() -> draft.annul("motivo", false, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_INVALID_STATE));
    }

    @Test
    void RET_7_the_has_emitted_returns_flag_only_blocks_invoices_not_returns() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 4L, returnLineOf(invoice, 1, "1"));

        emittedReturn.annul("motivo", true, LATER, USER);

        assertThat(emittedReturn.status()).isEqualTo(DocumentStatus.ANULADA);
    }

    @Test
    void ANL_2_an_invoice_with_emitted_returns_still_cannot_be_annulled() {
        BillingDocument sale = emittedSale();

        assertThatThrownBy(() -> sale.annul("motivo", true, LATER, USER))
                .satisfies(e -> assertCode(e, BillingErrorCode.BILLING_HAS_EMITTED_RETURNS));
    }

    // ---------- restore ----------

    @Test
    void RET_1_the_interim_return_state_builder_can_still_be_restored() {
        BillingDocument restored = BillingDocument.restore(draftSaleReturnState("motivo"));

        assertThat(restored.type()).isEqualTo(DocumentType.SALE_RETURN);
        assertThat(restored.original().id()).isEqualTo(uuid(1));
        assertThat(restored.totals().total()).isEqualTo(money("119.00"));
    }

}
