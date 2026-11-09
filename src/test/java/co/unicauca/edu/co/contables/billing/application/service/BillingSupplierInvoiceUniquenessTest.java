package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.AccountingEntryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingOutboxPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.DocumentNumberSequencePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.StockMovementPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unicidad del N.º de factura del proveedor dentro de un mismo proveedor (clave: tenant + empresa +
 * proveedor + numero) al crear/actualizar el borrador y al emitir. El filtrado por estado
 * (ANULADA no cuenta), por tipo (devoluciones no cuentan) y la comparacion sin mayusculas se prueban
 * contra la base en {@code BillingDocumentPersistenceAdapterTest}.
 */
@ExtendWith(MockitoExtension.class)
class BillingSupplierInvoiceUniquenessTest {

    private static final UUID NEW_ID = uuid(5);
    private static final UUID EXISTING_ID = uuid(1);
    private static final long OTHER_SUPPLIER_ID = 21L;

    @Mock
    private BillingDocumentRepositoryPort repository;
    @Mock
    private ThirdPartyPort thirdParties;
    @Mock(answer = org.mockito.Answers.CALLS_REAL_METHODS)
    private ProductPort products;
    @Mock
    private EnterprisePort enterprises;
    @Mock
    private DocumentNumberSequencePort sequence;
    @Mock
    private BillingOutboxPort outbox;
    @Mock
    private BillingExecutionContextPort context;
    @Mock
    private BillingClockPort clock;
    @Mock
    private BillingIdGeneratorPort ids;
    @Mock
    private StockMovementPort stock;
    @Mock
    private AccountingEntryPort accounting;

    private BillingInvoiceDraftService drafts;
    private BillingEmissionService emission;

    @BeforeEach
    void setUp() {
        lenient().when(context.username()).thenReturn(USER);
        lenient().when(clock.now()).thenReturn(NOW);
        lenient().when(clock.today()).thenReturn(TODAY);
        lenient().when(ids.newId()).thenReturn(NEW_ID);
        lenient().when(repository.save(any(BillingDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        BillingReferenceResolver resolver = new BillingReferenceResolver(thirdParties, products, enterprises);
        drafts = new BillingInvoiceDraftService(repository, new BillingDocumentLoader(repository), resolver, context,
                clock, ids);
        emission = new BillingEmissionService(repository, new BillingDocumentLoader(repository), resolver, sequence,
                outbox, context, clock, ids, stock, accounting,
                new co.unicauca.edu.co.contables.billing.support.FakePaymentCatalogPort());
    }

    // ---------- helpers ----------

    private static CounterpartySnapshot otherSupplier() {
        return new CounterpartySnapshot(OTHER_SUPPLIER_ID, CounterpartyRole.SUPPLIER, "Proveedor B", "NIT",
                "9002", "1", "Calle 1", "b@example.com", "3000000000", true);
    }

    private static InvoiceDraftCommand command(Long counterpartyId, String supplierNumber, Long version) {
        return new InvoiceDraftCommand(counterpartyId, null, supplierNumber,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("1"), null, null)), version);
    }

    private void supplierExists(CounterpartySnapshot snapshot) {
        when(thirdParties.findCounterparty(ENTERPRISE_ID, snapshot.thirdId(), CounterpartyRole.SUPPLIER))
                .thenReturn(Optional.of(snapshot));
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.PURCHASE)).thenReturn(Optional.of(product(1L)));
    }

    private void duplicateExists(long supplierId, String number, BillingDocument existing) {
        lenient().when(repository.findPurchaseInvoiceBySupplierNumber(eq(ENTERPRISE_ID), eq(supplierId), eq(number), any()))
                .thenReturn(Optional.of(BillingDocumentSummary.from(existing)));
    }

    private static void assertDuplicate(Throwable thrown) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_DUPLICATE_SUPPLIER_INVOICE);
    }

    // ---------- crear ----------

    @Test
    void create_with_a_number_already_used_by_the_same_supplier_is_rejected_and_saves_nothing() {
        supplierExists(supplier());
        duplicateExists(SUPPLIER_ID, "1234", draftPurchase());

        Throwable thrown = catchThrowable(
                () -> drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, command(SUPPLIER_ID, "1234", null)));

        assertDuplicate(thrown);
        assertThat(thrown.getMessage()).contains("N.º 1234").contains("Usa un número diferente");
        verify(repository, never()).save(any(BillingDocument.class));
    }

    @Test
    void create_trims_the_number_before_checking_so_spaces_do_not_evade_the_rule() {
        supplierExists(supplier());
        duplicateExists(SUPPLIER_ID, "FE-1", draftPurchase());

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                command(SUPPLIER_ID, "  FE-1  ", null)));

        assertDuplicate(thrown);
        verify(repository).findPurchaseInvoiceBySupplierNumber(ENTERPRISE_ID, SUPPLIER_ID, "FE-1", null);
    }

    @Test
    void the_message_includes_the_consecutive_number_when_the_existing_invoice_is_emitted() {
        supplierExists(supplier());
        duplicateExists(SUPPLIER_ID, "1234", emittedPurchase());

        Throwable thrown = catchThrowable(
                () -> drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, command(SUPPLIER_ID, "1234", null)));

        assertDuplicate(thrown);
        assertThat(thrown.getMessage()).contains("FC-000007");
    }

    @Test
    void the_same_number_for_another_supplier_is_accepted() {
        supplierExists(otherSupplier());
        // el proveedor A tiene 1234; la consulta del proveedor B no encuentra nada
        duplicateExists(SUPPLIER_ID, "1234", draftPurchase());

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                command(OTHER_SUPPLIER_ID, "1234", null));

        assertThat(created.supplierInvoiceNumber()).isEqualTo("1234");
        verify(repository).findPurchaseInvoiceBySupplierNumber(ENTERPRISE_ID, OTHER_SUPPLIER_ID, "1234", null);
    }

    @Test
    void a_draft_without_supplier_or_without_number_has_nothing_to_check() {
        when(products.findProduct(ENTERPRISE_ID, 1L, DocumentKind.PURCHASE)).thenReturn(Optional.of(product(1L)));
        drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, command(null, "1234", null));
        supplierExists(supplier());
        drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, command(SUPPLIER_ID, "   ", null));

        verify(repository, never()).findPurchaseInvoiceBySupplierNumber(anyString(), anyLong(), anyString(), any());
    }

    @Test
    void a_sale_invoice_never_checks_the_supplier_number() {
        drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                new InvoiceDraftCommand(null, null, "IGNORED", List.of(), null));

        verify(repository, never()).findPurchaseInvoiceBySupplierNumber(anyString(), anyLong(), anyString(), any());
    }

    @Test
    void purchase_returns_are_never_checked_because_they_copy_the_number_of_the_original() {
        SupplierInvoiceNumberGuard guard = new SupplierInvoiceNumberGuard(repository);

        guard.assertUnique(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN, SUPPLIER_ID, "F-77", EXISTING_ID);

        verifyNoInteractions(repository);
    }

    // ---------- actualizar ----------

    @Test
    void update_excludes_the_document_itself_so_re_saving_the_same_number_is_not_a_false_positive() {
        BillingDocument stored = withVersion(readyPurchaseDraft(), 1L);
        when(repository.findById(ENTERPRISE_ID, EXISTING_ID)).thenReturn(Optional.of(stored));
        supplierExists(supplier());

        BillingDocument updated = drafts.update(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, EXISTING_ID,
                command(SUPPLIER_ID, "F-77", 1L));

        assertThat(updated.supplierInvoiceNumber()).isEqualTo("F-77");
        verify(repository).findPurchaseInvoiceBySupplierNumber(ENTERPRISE_ID, SUPPLIER_ID, "F-77", EXISTING_ID);
    }

    @Test
    void update_to_a_number_used_by_another_document_of_the_supplier_is_rejected() {
        BillingDocument stored = withVersion(readyPurchaseDraft(), 1L);
        when(repository.findById(ENTERPRISE_ID, EXISTING_ID)).thenReturn(Optional.of(stored));
        supplierExists(supplier());
        duplicateExists(SUPPLIER_ID, "1234", emittedPurchase());

        Throwable thrown = catchThrowable(() -> drafts.update(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                EXISTING_ID, command(SUPPLIER_ID, "1234", 1L)));

        assertDuplicate(thrown);
        verify(repository, never()).save(any(BillingDocument.class));
    }

    // ---------- emitir ----------

    @Test
    void emit_revalidates_and_rejects_without_consuming_a_consecutive() {
        when(repository.findByIdForUpdate(ENTERPRISE_ID, EXISTING_ID)).thenReturn(Optional.of(readyPurchaseDraft()));
        when(thirdParties.findCounterparty(ENTERPRISE_ID, SUPPLIER_ID, CounterpartyRole.SUPPLIER))
                .thenReturn(Optional.of(supplier()));
        duplicateExists(SUPPLIER_ID, "F-77", emittedPurchase());

        Throwable thrown = catchThrowable(
                () -> emission.emit(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, EXISTING_ID, null));

        assertDuplicate(thrown);
        verify(repository).findPurchaseInvoiceBySupplierNumber(eq(ENTERPRISE_ID), eq(SUPPLIER_ID), eq("F-77"),
                eq(EXISTING_ID));
        verifyNoInteractions(sequence, outbox);
        verify(repository, never()).save(any(BillingDocument.class));
    }
}
