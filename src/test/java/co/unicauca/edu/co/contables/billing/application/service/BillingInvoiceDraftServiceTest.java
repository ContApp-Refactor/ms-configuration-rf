package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.CUSTOMER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.inactiveProduct;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** DRF-1..4, DRF-6, DRF-3, DOC-3/4 (RN-04, RN-10, RN-11): servicio de borradores de factura. */
@ExtendWith(MockitoExtension.class)
class BillingInvoiceDraftServiceTest {

    private static final UUID NEW_ID = uuid(5);
    private static final UUID EXISTING_ID = uuid(1);

    @Mock
    private BillingDocumentRepositoryPort repository;
    @Mock
    private ThirdPartyPort thirdParties;
    @Mock
    private ProductPort products;
    @Mock
    private EnterprisePort enterprises;
    @Mock
    private BillingExecutionContextPort context;
    @Mock
    private BillingClockPort clock;
    @Mock
    private BillingIdGeneratorPort ids;

    private BillingInvoiceDraftService service;

    @BeforeEach
    void setUp() {
        lenient().when(context.username()).thenReturn(USER);
        lenient().when(clock.now()).thenReturn(NOW);
        lenient().when(clock.today()).thenReturn(TODAY);
        lenient().when(ids.newId()).thenReturn(NEW_ID);
        lenient().when(repository.save(any(BillingDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new BillingInvoiceDraftService(repository, new BillingDocumentLoader(repository),
                new BillingReferenceResolver(thirdParties, products, enterprises), context, clock, ids);
    }

    // ---------- helpers ----------

    private static InvoiceLineCommand line(long productId, String quantity) {
        return new InvoiceLineCommand(productId, new BigDecimal(quantity), null, null);
    }

    private static InvoiceDraftCommand command(Long counterpartyId, String supplierNumber, Long version,
            InvoiceLineCommand... lines) {
        return new InvoiceDraftCommand(counterpartyId, null, supplierNumber, List.of(lines), version);
    }

    private void productExists(long id, DocumentKind kind) {
        when(products.findProduct(ENTERPRISE_ID, id, kind)).thenReturn(Optional.of(product(id)));
    }

    private void customerExists() {
        when(thirdParties.findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER))
                .thenReturn(Optional.of(customer()));
    }

    private void stored(BillingDocument doc) {
        when(repository.findById(ENTERPRISE_ID, doc.id())).thenReturn(Optional.of(doc));
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    // ---------- create ----------

    @Test
    void DRF_1_create_resolves_counterparty_then_products_then_replaces_and_saves_in_order() {
        customerExists();
        productExists(1L, DocumentKind.SALE);
        productExists(2L, DocumentKind.SALE);

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, null, null, line(1L, "2"), line(2L, "1")));

        InOrder order = inOrder(thirdParties, products, repository);
        order.verify(thirdParties).findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER);
        order.verify(products).findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE);
        order.verify(products).findProduct(ENTERPRISE_ID, 2L, DocumentKind.SALE);
        order.verify(repository).save(created);

        assertThat(created.id()).isEqualTo(NEW_ID);
        assertThat(created.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(created.type()).isEqualTo(DocumentType.SALE_INVOICE);
        assertThat(created.enterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(created.number()).isNull();
        assertThat(created.counterparty()).isEqualTo(customer());
        assertThat(created.lines()).hasSize(2);
        assertThat(created.lines().get(0).lineNo()).isEqualTo(1);
        assertThat(created.lines().get(1).lineNo()).isEqualTo(2);
        assertThat(created.createdBy()).isEqualTo(USER);
        assertThat(created.createdAt()).isEqualTo(NOW);
        assertThat(created.totals().total()).isEqualTo(money("357.00"));
    }

    @Test
    void DRF_1_create_accepts_a_partial_draft_without_counterparty_or_lines_and_defaults_the_date_to_today() {
        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                new InvoiceDraftCommand(null, null, null, null, null));

        verifyNoInteractions(thirdParties, products);
        verify(repository).save(created);
        assertThat(created.counterparty()).isNull();
        assertThat(created.lines()).isEmpty();
        assertThat(created.issueDate()).isEqualTo(TODAY);
        assertThat(created.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void DRF_1_create_keeps_the_issue_date_sent_by_the_client() {
        LocalDate sent = LocalDate.of(2026, 2, 1);

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                new InvoiceDraftCommand(null, sent, null, List.of(), null));

        assertThat(created.issueDate()).isEqualTo(sent);
    }

    @Test
    void DRF_4_create_with_unknown_counterparty_fails_422_and_does_not_look_up_products_or_save() {
        when(thirdParties.findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER))
                .thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, null, null, line(1L, "1"))));

        assertCode(thrown, BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND);
        verifyNoInteractions(products);
        verify(repository, never()).save(any());
    }

    @Test
    void DRF_4_create_with_unknown_product_fails_422_and_does_not_save() {
        customerExists();
        productExists(1L, DocumentKind.SALE);
        when(products.findProduct(ENTERPRISE_ID, 99L, DocumentKind.SALE)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, null, null, line(1L, "1"), line(99L, "1"))));

        assertCode(thrown, BillingErrorCode.BILLING_PRODUCT_NOT_FOUND);
        verify(repository, never()).save(any());
    }

    @Test
    void DRF_4_inactive_product_is_allowed_in_a_draft() {
        customerExists();
        when(products.findProduct(ENTERPRISE_ID, 3L, DocumentKind.SALE)).thenReturn(Optional.of(inactiveProduct(3L)));

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, null, null, line(3L, "1")));

        assertThat(created.lines()).hasSize(1);
        verify(repository).save(created);
    }

    @Test
    void DRF_4_inactive_counterparty_is_allowed_in_a_draft_and_blocked_only_at_emit() {
        when(thirdParties.findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER))
                .thenReturn(Optional.of(co.unicauca.edu.co.contables.billing.support.BillingTestData.inactiveCustomer()));

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, null, null));

        assertThat(created.counterparty().active()).isFalse();
        verify(repository).save(created);
    }

    @Test
    void DRF_4_a_repeated_product_is_resolved_only_once() {
        productExists(1L, DocumentKind.SALE);

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(null, null, null, line(1L, "1"), line(1L, "2")));

        verify(products, times(1)).findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE);
        assertThat(created.lines()).hasSize(2);
        assertThat(created.lines().get(1).quantity()).isEqualTo(qty("2"));
    }

    @Test
    void DOC_4_overrides_are_applied_and_null_overrides_follow_the_catalog() {
        productExists(1L, DocumentKind.SALE);
        productExists(2L, DocumentKind.SALE);
        InvoiceLineCommand overridden = new InvoiceLineCommand(1L, new BigDecimal("2"), new BigDecimal("150.00"),
                new BigDecimal("5"));

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(null, null, null, overridden, line(2L, "1")));

        DocumentLine first = created.lines().get(0);
        DocumentLine second = created.lines().get(1);
        assertThat(first.priceOverridden()).isTrue();
        assertThat(first.taxOverridden()).isTrue();
        assertThat(first.unitPrice()).isEqualTo(money("150.00"));
        assertThat(first.taxRate()).isEqualTo(rate("5"));
        assertThat(first.catalogUnitPrice()).isEqualTo(money("100.00"));
        assertThat(second.priceOverridden()).isFalse();
        assertThat(second.taxOverridden()).isFalse();
        assertThat(second.unitPrice()).isEqualTo(money("100.00"));
    }

    @Test
    void DOC_7_invalid_quantity_is_rejected_with_invalid_line_and_nothing_is_saved() {
        productExists(1L, DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(null, null, null, line(1L, "0"))));

        assertCode(thrown, BillingErrorCode.BILLING_INVALID_LINE);
        verify(repository, never()).save(any());
    }

    @Test
    void create_with_a_return_type_is_rejected_as_a_validation_error() {
        Throwable thrown = catchThrowable(() -> service.create(ENTERPRISE_ID, DocumentType.SALE_RETURN,
                command(null, null, null)));

        assertCode(thrown, BillingErrorCode.BILLING_VALIDATION_ERROR);
        verifyNoInteractions(thirdParties, products, repository);
    }

    @Test
    void PUR_2_purchase_create_resolves_a_supplier_with_the_purchase_kind_and_keeps_the_supplier_invoice_number() {
        when(thirdParties.findCounterparty(ENTERPRISE_ID, SUPPLIER_ID, CounterpartyRole.SUPPLIER))
                .thenReturn(Optional.of(supplier()));
        productExists(1L, DocumentKind.PURCHASE);

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                command(SUPPLIER_ID, SUPPLIER_INVOICE_NUMBER, null, line(1L, "1")));

        assertThat(created.type()).isEqualTo(DocumentType.PURCHASE_INVOICE);
        assertThat(created.counterparty()).isEqualTo(supplier());
        assertThat(created.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
    }

    @Test
    void PUR_2_sale_create_ignores_the_supplier_invoice_number() {
        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(null, "IGNORED-1", null));

        assertThat(created.supplierInvoiceNumber()).isNull();
    }

    // ---------- update ----------

    @Test
    void DRF_2_update_loads_then_resolves_then_replaces_and_saves_in_order() {
        BillingDocument stored = withVersion(draftSale(), 3L);
        stored(stored);
        customerExists();
        productExists(1L, DocumentKind.SALE);

        BillingDocument updated = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID,
                command(CUSTOMER_ID, null, 3L, line(1L, "2")));

        InOrder order = inOrder(repository, thirdParties, products);
        order.verify(repository).findById(ENTERPRISE_ID, EXISTING_ID);
        order.verify(thirdParties).findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER);
        order.verify(products).findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE);
        order.verify(repository).save(updated);

        assertThat(updated.counterparty()).isEqualTo(customer());
        assertThat(updated.lines()).hasSize(1);
        assertThat(updated.updatedAt()).isEqualTo(NOW);
        assertThat(updated.updatedBy()).isEqualTo(USER);
    }

    @Test
    void DRF_2_update_is_a_full_replace_and_idempotent() {
        BillingDocument stored = withVersion(readySaleDraft(), 1L);
        stored(stored);
        productExists(1L, DocumentKind.SALE);
        InvoiceDraftCommand sameCommand = command(null, null, null, line(1L, "2"));

        BillingDocument first = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID, sameCommand);
        var stateAfterFirst = first.state();
        BillingDocument second = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID, sameCommand);

        assertThat(second.state()).isEqualTo(stateAfterFirst);
        assertThat(second.counterparty()).isNull();
        assertThat(second.lines()).hasSize(1);
    }

    @Test
    void DRF_2_update_with_a_null_issue_date_keeps_the_current_date() {
        BillingDocument stored = withVersion(readySaleDraft(), 1L);
        stored(stored);

        BillingDocument updated = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID,
                command(null, null, null));

        assertThat(updated.issueDate()).isEqualTo(TODAY);
    }

    @Test
    void RN_11_update_with_null_overrides_clears_them_and_the_line_follows_the_catalog() {
        BillingDocument stored = draftSale();
        stored.replaceInvoiceDraft(null, TODAY, null, List.of(
                DocumentLine.forInvoice(1, product(1L), qty("2"), money("150.00"), rate("5"))), NOW, USER);
        stored(withVersion(stored, 2L));
        productExists(1L, DocumentKind.SALE);

        BillingDocument updated = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID,
                command(null, null, 2L, line(1L, "2")));

        DocumentLine updatedLine = updated.lines().get(0);
        assertThat(updatedLine.priceOverridden()).isFalse();
        assertThat(updatedLine.taxOverridden()).isFalse();
        assertThat(updatedLine.unitPrice()).isEqualTo(money("100.00"));
    }

    @Test
    void DOC_3_update_refreshes_the_catalog_reference_from_the_product_port() {
        BillingDocument stored = withVersion(readySaleDraft(), 1L);
        stored(stored);
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.SALE))
                .thenReturn(Optional.of(product(1L, "200.00", "19")));

        BillingDocument updated = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID,
                command(null, null, null, line(1L, "2")));

        assertThat(updated.lines().get(0).catalogUnitPrice()).isEqualTo(money("200.00"));
        assertThat(updated.lines().get(0).unitPrice()).isEqualTo(money("200.00"));
    }

    @Test
    void DRF_3_update_with_a_stale_version_fails_409_without_calling_other_modules_or_saving() {
        stored(withVersion(draftSale(), 4L));

        Throwable thrown = catchThrowable(() -> service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                EXISTING_ID, command(CUSTOMER_ID, null, 3L, line(1L, "1"))));

        assertCode(thrown, BillingErrorCode.BILLING_CONCURRENT_MODIFICATION);
        verifyNoInteractions(thirdParties, products);
        verify(repository, never()).save(any());
    }

    @Test
    void DRF_3_update_without_a_version_skips_the_optimistic_check() {
        stored(withVersion(draftSale(), 4L));

        BillingDocument updated = service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID,
                command(null, null, null));

        verify(repository).save(updated);
    }

    @Test
    void DOC_NOT_FOUND_update_of_an_unknown_document_fails_404() {
        when(repository.findById(ENTERPRISE_ID, EXISTING_ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                EXISTING_ID, command(null, null, null)));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verify(repository, never()).save(any());
    }

    @Test
    void PUR_3_update_through_the_wrong_type_behaves_as_not_found() {
        stored(withVersion(draftSale(), 1L));

        Throwable thrown = catchThrowable(() -> service.update(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                EXISTING_ID, command(null, null, null)));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verifyNoInteractions(thirdParties, products);
        verify(repository, never()).save(any());
    }

    @Test
    void RN_05_update_of_an_emitted_invoice_fails_409_and_saves_nothing() {
        stored(withVersion(emittedSale(), 1L));

        Throwable thrown = catchThrowable(() -> service.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                EXISTING_ID, command(null, null, null)));

        assertCode(thrown, BillingErrorCode.BILLING_INVALID_STATE);
        verify(repository, never()).save(any());
    }

    // ---------- duplicate ----------

    @Test
    void DRF_6_duplicate_saves_a_new_draft_with_a_new_id_todays_date_and_the_same_lines() {
        BillingDocument source = withVersion(readySaleDraft(), 2L);
        stored(source);

        BillingDocument copy = service.duplicate(ENTERPRISE_ID, DocumentType.SALE_INVOICE, EXISTING_ID);

        verify(repository).save(copy);
        assertThat(copy.id()).isEqualTo(NEW_ID);
        assertThat(copy.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(copy.number()).isNull();
        assertThat(copy.issueDate()).isEqualTo(TODAY);
        assertThat(copy.createdBy()).isEqualTo(USER);
        assertThat(copy.counterparty()).isEqualTo(source.counterparty());
        assertThat(copy.lines()).isEqualTo(source.lines());
    }

    @Test
    void DRF_6_duplicate_of_a_purchase_does_not_copy_the_supplier_invoice_number() {
        BillingDocument source = readyPurchaseDraft();
        stored(source);

        BillingDocument copy = service.duplicate(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, EXISTING_ID);

        assertThat(copy.supplierInvoiceNumber()).isNull();
    }

    @Test
    void DRF_6_duplicate_of_a_non_draft_fails_409_and_saves_nothing() {
        stored(emittedSale());

        Throwable thrown = catchThrowable(() -> service.duplicate(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                EXISTING_ID));

        assertCode(thrown, BillingErrorCode.BILLING_INVALID_STATE);
        verify(repository, never()).save(any());
    }

    @Test
    void DRF_6_duplicate_through_the_wrong_type_behaves_as_not_found() {
        stored(readySaleDraft());

        Throwable thrown = catchThrowable(() -> service.duplicate(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                EXISTING_ID));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        verify(repository, never()).save(any());
    }

    @Test
    void DRF_6_duplicate_of_an_unknown_document_fails_404() {
        when(repository.findById(ENTERPRISE_ID, EXISTING_ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> service.duplicate(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                EXISTING_ID));

        assertCode(thrown, BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
    }
}
