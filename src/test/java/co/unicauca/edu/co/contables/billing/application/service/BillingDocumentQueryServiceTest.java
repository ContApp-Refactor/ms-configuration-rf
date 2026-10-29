package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSaleWithLines;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.InvoiceReturnAggregate;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnLineAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** QRY-1, QRY-2, DRF-7, DRF-8 (DOC-3): consultas de documentos. */
@ExtendWith(MockitoExtension.class)
class BillingDocumentQueryServiceTest {

    private static final UUID ID = uuid(1);

    @Mock
    private BillingDocumentRepositoryPort repository;
    @Mock
    private ThirdPartyPort thirdParties;
    @Mock
    private ProductPort products;
    @Mock
    private EnterprisePort enterprises;

    private BillingDocumentQueryService service;

    @BeforeEach
    void setUp() {
        service = new BillingDocumentQueryService(repository, new BillingDocumentLoader(repository),
                new BillingReferenceResolver(thirdParties, products, enterprises));
    }

    private void stored(BillingDocument doc) {
        when(repository.findById(ENTERPRISE_ID, doc.id())).thenReturn(Optional.of(doc));
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    // ---------- get ----------

    @Test
    void DRF_8_get_of_a_draft_reresolves_the_catalog_in_memory_and_never_saves() {
        BillingDocument doc = withVersion(readySaleDraft(), 1L);
        stored(doc);
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE))
                .thenReturn(Optional.of(product(1L, "200.00", "19")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        DocumentLine line = view.doc().lines().get(0);
        assertThat(line.catalogUnitPrice()).isEqualTo(money("200.00"));
        assertThat(line.unitPrice()).isEqualTo(money("200.00"));
        verify(repository, never()).save(any());
    }

    @Test
    void DRF_8_totals_of_a_draft_are_consistent_with_the_presented_lines() {
        stored(withVersion(readySaleDraft(), 1L));
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE))
                .thenReturn(Optional.of(product(1L, "200.00", "19")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        assertThat(view.doc().totals().subtotal()).isEqualTo(money("400.00"));
        assertThat(view.doc().totals().tax()).isEqualTo(money("76.00"));
        assertThat(view.doc().totals().total()).isEqualTo(money("476.00"));
    }

    @Test
    void RN_10_get_of_a_draft_keeps_manual_overrides_when_the_catalog_changes() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(
                DocumentLine.forInvoice(1, product(1L), qty("2"), money("150.00"), null)), NOW, USER);
        stored(withVersion(doc, 1L));
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE))
                .thenReturn(Optional.of(product(1L, "200.00", "10")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        DocumentLine line = view.doc().lines().get(0);
        assertThat(line.unitPrice()).isEqualTo(money("150.00"));
        assertThat(line.catalogUnitPrice()).isEqualTo(money("200.00"));
        assertThat(line.taxRate()).isEqualTo(rate("10"));
    }

    @Test
    void DRF_8_get_of_a_draft_keeps_the_stored_snapshot_of_a_product_that_no_longer_resolves() {
        BillingDocument doc = withVersion(readySaleDraft(), 1L);
        stored(doc);
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE)).thenReturn(Optional.empty());

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        assertThat(view.doc().lines().get(0).catalogUnitPrice()).isEqualTo(money("100.00"));
        assertThat(view.doc().totals().total()).isEqualTo(money("238.00"));
    }

    @Test
    void DRF_8_get_of_a_purchase_draft_resolves_products_with_the_purchase_kind() {
        stored(withVersion(readyPurchaseDraft(), 1L));
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.PURCHASE))
                .thenReturn(Optional.of(product(1L, "80.00", "19")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, ID);

        assertThat(view.doc().lines().get(0).unitPrice()).isEqualTo(money("80.00"));
    }

    @Test
    void DRF_8_get_of_a_draft_without_lines_does_not_call_the_product_port() {
        stored(withVersion(draftSale(), 0L));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        verifyNoInteractions(products);
        assertThat(view.doc().lines()).isEmpty();
    }

    @Test
    void EMT_2_get_of_an_emitted_invoice_is_frozen_and_does_not_consult_the_catalog() {
        stored(withVersion(emittedSale(), 1L));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        verifyNoInteractions(products);
        assertThat(view.doc().status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(view.doc().lines().get(0).unitPrice()).isEqualTo(money("100.00"));
        assertThat(view.doc().number()).isNotNull();
    }

    @Test
    void EMT_2_get_of_an_annulled_invoice_does_not_consult_the_catalog() {
        stored(withVersion(annulledSale(), 2L));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        verifyNoInteractions(products);
        assertThat(view.doc().status()).isEqualTo(DocumentStatus.ANULADA);
    }

    @Test
    void RET_9_get_of_an_emitted_invoice_without_returns_derives_SIN_DEVOLUCION() {
        stored(withVersion(emittedSale(), 1L));

        BillingDocumentView view = service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID);

        assertThat(view.returnStatus()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
        assertThat(view.returnOrdinal()).isNull();
    }

    @Test
    void QRY_1_get_of_an_unknown_document_fails_404() {
        when(repository.findById(ENTERPRISE_ID, ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    @Test
    void QRY_2_get_in_another_enterprise_behaves_as_not_found() {
        when(repository.findById(OTHER_ENTERPRISE_ID, ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.get(OTHER_ENTERPRISE_ID, DocumentType.SALE_INVOICE, ID));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verify(repository).findById(OTHER_ENTERPRISE_ID, ID);
    }

    @Test
    void PUR_3_get_through_the_wrong_type_behaves_as_not_found() {
        stored(withVersion(readySaleDraft(), 1L));

        Throwable thrown = catchThrowable(() -> service.get(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, ID));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verifyNoInteractions(products);
    }

    // ---------- list ----------

    @Test
    void DRF_7_list_by_status_delegates_to_the_repository_scoped_by_enterprise_and_type() {
        BillingDocumentSummary first = BillingDocumentSummary.from(withVersion(readySaleDraft(), 1L));
        BillingDocumentSummary second = BillingDocumentSummary.from(withVersion(draftSale(), 0L));
        when(repository.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, DocumentStatus.BORRADOR, null))
                .thenReturn(List.of(first, second));

        List<BillingDocumentSummary> result = service.list(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                DocumentStatus.BORRADOR, null);

        assertThat(result).containsExactly(first, second);
        verifyNoInteractions(products, thirdParties);
    }

    @Test
    void QRY_1_list_without_status_passes_null_through() {
        when(repository.findSummaries(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, null, null))
                .thenReturn(List.of());

        List<BillingDocumentSummary> result = service.list(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, null, null);

        assertThat(result).isEmpty();
        verify(repository).findSummaries(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, null, null);
    }

    @Test
    void QRY_2_list_is_scoped_to_the_requested_enterprise() {
        BillingDocumentSummary mine = BillingDocumentSummary.from(withVersion(readySaleDraft(), 1L));
        when(repository.findSummaries(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null))
                .thenReturn(List.of(mine));
        when(repository.findSummaries(OTHER_ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null))
                .thenReturn(List.of());

        assertThat(service.list(ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null)).containsExactly(mine);
        assertThat(service.list(OTHER_ENTERPRISE_ID, DocumentType.SALE_INVOICE, null, null)).isEmpty();
    }

    @Test
    void QRY_1_list_forwards_the_original_document_filter() {
        UUID original = uuid(9);
        when(repository.findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, null, original))
                .thenReturn(List.of());

        service.list(ENTERPRISE_ID, DocumentType.SALE_RETURN, null, original);

        verify(repository).findSummaries(ENTERPRISE_ID, DocumentType.SALE_RETURN, null, original);
    }

    @Test
    void DRF_7_summary_of_a_draft_exposes_counterparty_totals_and_no_number() {
        BillingDocumentSummary summary = BillingDocumentSummary.from(withVersion(readySaleDraft(), 3L));

        assertThat(summary.id()).isEqualTo(ID);
        assertThat(summary.type()).isEqualTo(DocumentType.SALE_INVOICE);
        assertThat(summary.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(summary.number()).isNull();
        assertThat(summary.counterpartyId()).isEqualTo(customer().thirdId());
        assertThat(summary.counterpartyName()).isEqualTo(customer().name());
        assertThat(summary.totals().total()).isEqualTo(money("238.00"));
        assertThat(summary.originalDocumentId()).isNull();
        assertThat(summary.version()).isEqualTo(3L);
    }

    @Test
    void DRF_7_summary_of_a_partial_draft_has_null_counterparty_fields() {
        BillingDocumentSummary summary = BillingDocumentSummary.from(draftSale());

        assertThat(summary.counterpartyId()).isNull();
        assertThat(summary.counterpartyName()).isNull();
        assertThat(summary.totals().total()).isEqualTo(money("0.00"));
    }

    // ---------- devoluciones: datos derivados en GET (RET-9, RET-10) ----------

    private static final UUID RETURN_ID = uuid(2);
    private static final DocumentType SALE = DocumentType.SALE_INVOICE;
    private static final DocumentType SALE_RETURN = DocumentType.SALE_RETURN;

    /** Venta emitida FV-000007 (uuid(1)): linea 1 con 10 unidades y linea 2 con 5. */
    private static BillingDocument saleInvoice() {
        return emittedSaleWithLines(ID, 7L, lineOf(1, product(1L), "10"), lineOf(2, product(2L), "5"));
    }

    private void returnsOf(BillingDocument... returns) {
        when(repository.findReturnsOf(ENTERPRISE_ID, ID)).thenReturn(List.of(returns));
    }

    private static void assertCodeOf(Throwable thrown, BillingErrorCode code) {
        assertCode(thrown, code);
    }

    @Test
    void RET_9_an_emitted_invoice_with_a_partial_emitted_return_is_PARCIAL() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(emittedReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "4")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE, ID);

        assertThat(view.returnStatus()).isEqualTo(ReturnStatus.PARCIAL);
        assertThat(view.returnOrdinal()).isNull();
    }

    @Test
    void RET_9_an_emitted_invoice_whose_every_line_was_returned_is_TOTAL() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(emittedReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "10"),
                returnLineOf(invoice, 2, "5")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE, ID);

        assertThat(view.returnStatus()).isEqualTo(ReturnStatus.TOTAL);
    }

    @Test
    void RET_9_draft_and_annulled_returns_do_not_change_the_derived_status() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(returnDraftOf(invoice, uuid(3), "borrador", returnLineOf(invoice, 1, "10")),
                annulledReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "10")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE, ID);

        assertThat(view.returnStatus()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    @Test
    void RET_9_the_derived_status_is_read_from_the_returns_of_this_invoice_only() {
        stored(withVersion(saleInvoice(), 1L));

        service.get(ENTERPRISE_ID, SALE, ID);

        verify(repository).findReturnsOf(ENTERPRISE_ID, ID);
    }

    @Test
    void RET_9_a_draft_invoice_has_no_derived_return_status_and_returns_are_not_read() {
        stored(withVersion(readySaleDraft(), 1L));
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE)).thenReturn(Optional.of(product(1L)));

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE, ID);

        assertThat(view.returnStatus()).isNull();
        verify(repository, never()).findReturnsOf(any(), any());
    }

    @Test
    void RET_9_an_annulled_invoice_has_no_derived_return_status_and_returns_are_not_read() {
        stored(withVersion(annulledSale(), 2L));

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE, ID);

        assertThat(view.returnStatus()).isNull();
        verify(repository, never()).findReturnsOf(any(), any());
    }

    @Test
    void RET_10_an_emitted_return_exposes_its_ordinal_among_the_returns_of_its_original() {
        BillingDocument invoice = saleInvoice();
        BillingDocument previous = annulledReturnOf(invoice, uuid(3), 1L, returnLineOf(invoice, 1, "2"));
        BillingDocument target = emittedReturnOf(invoice, RETURN_ID, 2L, returnLineOf(invoice, 1, "3"));
        stored(target);
        returnsOf(previous, target);

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE_RETURN, RETURN_ID);

        assertThat(view.doc()).isSameAs(target);
        assertThat(view.returnOrdinal()).isEqualTo(2);
        assertThat(view.returnStatus()).isNull();
    }

    @Test
    void RET_10_an_annulled_return_keeps_its_ordinal() {
        BillingDocument invoice = saleInvoice();
        BillingDocument target = annulledReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "3"));
        stored(target);
        returnsOf(target);

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE_RETURN, RETURN_ID);

        assertThat(view.returnOrdinal()).isEqualTo(1);
    }

    @Test
    void RET_10_the_ordinal_is_computed_even_if_the_list_of_returns_lacks_the_requested_one() {
        BillingDocument invoice = saleInvoice();
        BillingDocument previous = emittedReturnOf(invoice, uuid(3), 1L, returnLineOf(invoice, 1, "2"));
        BillingDocument target = emittedReturnOf(invoice, RETURN_ID, 2L, returnLineOf(invoice, 1, "3"));
        stored(target);
        returnsOf(previous);

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE_RETURN, RETURN_ID);

        assertThat(view.returnOrdinal()).isEqualTo(2);
    }

    @Test
    void RET_10_a_draft_return_has_no_ordinal_and_no_catalog_refresh() {
        BillingDocument invoice = saleInvoice();
        stored(returnDraftOf(invoice, RETURN_ID, "motivo", returnLineOf(invoice, 1, "3")));

        BillingDocumentView view = service.get(ENTERPRISE_ID, SALE_RETURN, RETURN_ID);

        assertThat(view.returnOrdinal()).isNull();
        verify(repository, never()).findReturnsOf(any(), any());
        verifyNoInteractions(products, thirdParties);
    }

    @Test
    void PUR_3_get_of_an_invoice_through_the_return_type_is_not_found() {
        stored(withVersion(saleInvoice(), 1L));

        Throwable thrown = catchThrowable(() -> service.get(ENTERPRISE_ID, SALE_RETURN, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    @Test
    void PUR_3_get_of_a_return_through_the_invoice_type_is_not_found() {
        BillingDocument invoice = saleInvoice();
        stored(emittedReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "3")));

        Throwable thrown = catchThrowable(() -> service.get(ENTERPRISE_ID, SALE, RETURN_ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    // ---------- devoluciones: disponibilidad (RET-9) ----------

    @Test
    void RET_9_availability_reports_sold_returned_and_available_per_line() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(emittedReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "4")));

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, SALE, ID);

        assertThat(availability.invoiceId()).isEqualTo(ID);
        assertThat(availability.status()).isEqualTo(ReturnStatus.PARCIAL);
        assertThat(availability.returnable()).isTrue();
        ReturnLineAvailability first = availability.lines().get(0);
        assertThat(first.lineNo()).isEqualTo(1);
        assertThat(first.productId()).isEqualTo(1L);
        assertThat(first.sold()).isEqualByComparingTo("10");
        assertThat(first.returned()).isEqualByComparingTo("4");
        assertThat(first.available()).isEqualByComparingTo("6");
        ReturnLineAvailability second = availability.lines().get(1);
        assertThat(second.returned()).isEqualByComparingTo("0");
        assertThat(second.available()).isEqualByComparingTo("5");
    }

    @Test
    void RET_9_availability_of_an_invoice_without_returns_is_SIN_DEVOLUCION_and_returnable() {
        stored(saleInvoice());
        returnsOf();

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, SALE, ID);

        assertThat(availability.status()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
        assertThat(availability.returnable()).isTrue();
    }

    @Test
    void RET_9_availability_of_a_fully_returned_invoice_is_TOTAL_and_not_returnable() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(emittedReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "10"),
                returnLineOf(invoice, 2, "5")));

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, SALE, ID);

        assertThat(availability.status()).isEqualTo(ReturnStatus.TOTAL);
        assertThat(availability.returnable()).isFalse();
    }

    @Test
    void RET_7_availability_grows_back_when_a_return_is_annulled() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(annulledReturnOf(invoice, RETURN_ID, 1L, returnLineOf(invoice, 1, "4")));

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, SALE, ID);

        assertThat(availability.lines().get(0).available()).isEqualByComparingTo("10");
        assertThat(availability.status()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    @Test
    void RET_1_availability_of_an_annulled_invoice_is_not_returnable() {
        stored(annulledSale());
        returnsOf();

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, SALE, ID);

        assertThat(availability.returnable()).isFalse();
    }

    @Test
    void RET_1_availability_of_a_draft_invoice_is_not_returnable() {
        stored(readySaleDraft());
        returnsOf();

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, SALE, ID);

        assertThat(availability.returnable()).isFalse();
    }

    @Test
    void PUR_1_availability_of_a_purchase_invoice_uses_the_purchase_type() {
        stored(emittedPurchase());
        returnsOf();

        ReturnAvailability availability = service.availability(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, ID);

        assertThat(availability.returnable()).isTrue();
        assertThat(availability.lines()).hasSize(1);
    }

    @Test
    void PUR_3_availability_through_the_wrong_kind_is_not_found() {
        stored(emittedPurchase());

        Throwable thrown = catchThrowable(() -> service.availability(ENTERPRISE_ID, SALE, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verify(repository, never()).findReturnsOf(any(), any());
    }

    @Test
    void QRY_1_availability_of_an_unknown_invoice_is_not_found() {
        when(repository.findById(ENTERPRISE_ID, ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.availability(ENTERPRISE_ID, SALE, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    @Test
    void STA_3_availability_requires_an_invoice_type() {
        Throwable thrown = catchThrowable(() -> service.availability(ENTERPRISE_ID, SALE_RETURN, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_VALIDATION_ERROR);
        verifyNoInteractions(repository);
    }

    // ---------- devoluciones de una factura (RET-10, RET-11) ----------

    @Test
    void RET_10_returns_of_an_invoice_come_with_ordinal_numbered_first_and_drafts_last() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        BillingDocument second = emittedReturnOf(invoice, uuid(4), 2L, returnLineOf(invoice, 1, "1"));
        BillingDocument first = annulledReturnOf(invoice, uuid(3), 1L, returnLineOf(invoice, 1, "1"));
        BillingDocument draft = returnDraftOf(invoice, uuid(5), "borrador", returnLineOf(invoice, 1, "1"));
        returnsOf(draft, second, first);

        List<ReturnSummary> result = service.returnsOf(ENTERPRISE_ID, SALE, ID);

        assertThat(result).extracting(r -> r.summary().id()).containsExactly(uuid(3), uuid(4), uuid(5));
        assertThat(result).extracting(ReturnSummary::ordinal).containsExactly(1, 2, null);
    }

    @Test
    void RET_11_the_return_summary_exposes_status_number_original_and_totals() {
        BillingDocument invoice = saleInvoice();
        stored(invoice);
        returnsOf(emittedReturnOf(invoice, uuid(3), 1L, returnLineOf(invoice, 1, "2")));

        ReturnSummary result = service.returnsOf(ENTERPRISE_ID, SALE, ID).get(0);

        assertThat(result.summary().status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(result.summary().number().display()).isEqualTo("NC-000001");
        assertThat(result.summary().originalDocumentId()).isEqualTo(ID);
        assertThat(result.summary().totals().total()).isEqualTo(money("238.00"));
    }

    @Test
    void RET_11_an_invoice_without_returns_lists_nothing() {
        stored(saleInvoice());
        returnsOf();

        assertThat(service.returnsOf(ENTERPRISE_ID, SALE, ID)).isEmpty();
    }

    @Test
    void QRY_1_returns_of_an_unknown_invoice_is_not_found() {
        when(repository.findById(ENTERPRISE_ID, ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.returnsOf(ENTERPRISE_ID, SALE, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    @Test
    void PUR_3_returns_of_a_purchase_invoice_through_the_sale_resource_is_not_found() {
        stored(emittedPurchase());

        Throwable thrown = catchThrowable(() -> service.returnsOf(ENTERPRISE_ID, SALE, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }

    @Test
    void STA_3_returns_of_requires_an_invoice_type() {
        Throwable thrown = catchThrowable(() -> service.returnsOf(ENTERPRISE_ID, SALE_RETURN, ID));

        assertCodeOf(thrown, BillingErrorCode.BILLING_VALIDATION_ERROR);
        verifyNoInteractions(repository);
    }

    // ---------- facturas con devoluciones (RET-11, HU-24) ----------

    private static InvoiceReturnAggregate aggregate(UUID originalId, long count, String total, String quantity) {
        return new InvoiceReturnAggregate(originalId, count, money(total), new BigDecimal(quantity));
    }

    @Test
    void RET_11_invoices_with_returns_derive_count_total_and_state_from_the_aggregates() {
        BillingDocument partial = emittedSaleWithLines(uuid(1), 7L, lineOf(1, product(1L), "15"));
        BillingDocument total = emittedSaleWithLines(uuid(2), 8L, lineOf(1, product(1L), "15"));
        when(repository.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN)).thenReturn(List.of(
                aggregate(uuid(1), 1, "476.00", "4.000"),
                aggregate(uuid(2), 2, "1785.00", "15.000")));
        when(repository.findSummariesByIds(eq(ENTERPRISE_ID), anyCollection())).thenReturn(List.of(
                BillingDocumentSummary.from(partial), BillingDocumentSummary.from(total)));
        when(repository.soldQuantities(eq(ENTERPRISE_ID), anyCollection())).thenReturn(Map.of(
                uuid(1), new BigDecimal("15.000"), uuid(2), new BigDecimal("15.000")));

        List<InvoiceWithReturnsSummary> result = service.list(ENTERPRISE_ID, SALE);

        assertThat(result).hasSize(2);
        InvoiceWithReturnsSummary first = result.get(0);
        assertThat(first.summary().id()).isEqualTo(uuid(1));
        assertThat(first.returnsCount()).isEqualTo(1);
        assertThat(first.returnedTotal()).isEqualTo(money("476.00"));
        assertThat(first.state()).isEqualTo(ReturnStatus.PARCIAL);
        InvoiceWithReturnsSummary second = result.get(1);
        assertThat(second.returnsCount()).isEqualTo(2);
        assertThat(second.returnedTotal()).isEqualTo(money("1785.00"));
        assertThat(second.state()).isEqualTo(ReturnStatus.TOTAL);
    }

    @Test
    void RET_11_the_aggregates_are_asked_for_the_return_type_of_the_requested_invoice_type() {
        when(repository.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN)).thenReturn(List.of());

        List<InvoiceWithReturnsSummary> result =
                service.list(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE);

        assertThat(result).isEmpty();
        verify(repository).aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN);
    }

    @Test
    void RET_11_without_emitted_returns_the_summaries_and_quantities_are_not_queried() {
        when(repository.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN)).thenReturn(List.of());

        assertThat(service.list(ENTERPRISE_ID, SALE)).isEmpty();

        verify(repository, never()).findSummariesByIds(any(), anyCollection());
        verify(repository, never()).soldQuantities(any(), anyCollection());
    }

    @Test
    void RET_11_the_order_of_the_summaries_is_preserved_and_unmatched_rows_are_skipped() {
        BillingDocument newer = emittedSaleWithLines(uuid(2), 8L, lineOf(1, product(1L), "10"));
        BillingDocument older = emittedSaleWithLines(uuid(1), 7L, lineOf(1, product(1L), "10"));
        when(repository.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN)).thenReturn(List.of(
                aggregate(uuid(1), 1, "119.00", "1.000"),
                aggregate(uuid(2), 1, "119.00", "1.000"),
                aggregate(uuid(9), 1, "119.00", "1.000")));
        when(repository.findSummariesByIds(eq(ENTERPRISE_ID), anyCollection())).thenReturn(List.of(
                BillingDocumentSummary.from(newer), BillingDocumentSummary.from(older)));
        when(repository.soldQuantities(eq(ENTERPRISE_ID), anyCollection())).thenReturn(Map.of(
                uuid(1), new BigDecimal("10.000"), uuid(2), new BigDecimal("10.000")));

        List<InvoiceWithReturnsSummary> result = service.list(ENTERPRISE_ID, SALE);

        assertThat(result).extracting(r -> r.summary().id()).containsExactly(uuid(2), uuid(1));
    }

    @Test
    void RET_11_an_invoice_without_sold_quantity_data_is_skipped_instead_of_misclassified() {
        BillingDocument invoice = emittedSaleWithLines(uuid(1), 7L, lineOf(1, product(1L), "10"));
        when(repository.aggregateEmittedReturns(ENTERPRISE_ID, DocumentType.SALE_RETURN))
                .thenReturn(List.of(aggregate(uuid(1), 1, "119.00", "1.000")));
        when(repository.findSummariesByIds(eq(ENTERPRISE_ID), anyCollection()))
                .thenReturn(List.of(BillingDocumentSummary.from(invoice)));
        when(repository.soldQuantities(eq(ENTERPRISE_ID), anyCollection())).thenReturn(Map.of());

        assertThat(service.list(ENTERPRISE_ID, SALE)).isEmpty();
    }

    @Test
    void STA_3_invoices_with_returns_requires_an_invoice_type() {
        Throwable thrown = catchThrowable(() -> service.list(ENTERPRISE_ID, SALE_RETURN));

        assertCodeOf(thrown, BillingErrorCode.BILLING_VALIDATION_ERROR);
        verifyNoInteractions(repository);
    }
}
