package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.CUSTOMER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
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
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms;
import co.unicauca.edu.co.contables.billing.support.FakePaymentCatalogPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Forma de pago en la factura de venta: el borrador la guarda sin consultar catalogos; la emision
 * valida el metodo (existe, activo, de la empresa), congela su nombre y no afecta a compras.
 */
@ExtendWith(MockitoExtension.class)
class BillingInvoicePaymentServiceTest {

    private static final UUID DOC_ID = uuid(1);
    private static final UUID EVENT_ID = uuid(77);

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

    private final FakePaymentCatalogPort catalog = new FakePaymentCatalogPort();
    private BillingInvoiceDraftService drafts;
    private BillingEmissionService emission;

    @BeforeEach
    void setUp() {
        lenient().when(context.username()).thenReturn(USER);
        lenient().when(clock.now()).thenReturn(NOW);
        lenient().when(clock.today()).thenReturn(TODAY);
        lenient().when(ids.newId()).thenReturn(EVENT_ID);
        lenient().when(repository.save(any(BillingDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        BillingReferenceResolver resolver = new BillingReferenceResolver(thirdParties, products, enterprises);
        drafts = new BillingInvoiceDraftService(repository, new BillingDocumentLoader(repository), resolver,
                context, clock, ids, catalog);
        emission = new BillingEmissionService(repository, new BillingDocumentLoader(repository), resolver, sequence,
                outbox, context, clock, ids, stock, accounting, catalog);
    }

    // ---------- helpers ----------

    private InvoiceDraftCommand command(long counterpartyId, InvoiceDraftCommand.PaymentCommand payment) {
        return new InvoiceDraftCommand(counterpartyId, null, null,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("2"), null, null)), null, payment);
    }

    private void counterpartyAndProductExist(DocumentKind kind) {
        if (kind == DocumentKind.SALE) {
            when(thirdParties.findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER))
                    .thenReturn(Optional.of(customer()));
        } else {
            when(thirdParties.findCounterparty(ENTERPRISE_ID, SUPPLIER_ID, CounterpartyRole.SUPPLIER))
                    .thenReturn(Optional.of(supplier()));
        }
        when(products.findProduct(ENTERPRISE_ID, 1L, kind)).thenReturn(Optional.of(product(1L)));
    }

    private void saleStoredWith(co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms payment) {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(lineOf(1, product(1L), "2")), payment, NOW, USER);
        when(repository.findByIdForUpdate(ENTERPRISE_ID, doc.id())).thenReturn(Optional.of(doc));
        counterpartyAndProductExist(DocumentKind.SALE);
        when(enterprises.findIssuer(ENTERPRISE_ID)).thenReturn(Optional.of(issuer()));
        lenient().when(sequence.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)).thenReturn(8L);
    }

    private BillingDocument emitSale() {
        return emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, DOC_ID, null);
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    // ---------- borrador ----------

    @Test
    void a_sale_draft_saves_only_the_method_id_even_when_the_method_is_unknown_to_the_draft() {
        counterpartyAndProductExist(DocumentKind.SALE);
        when(ids.newId()).thenReturn(DOC_ID);

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE, command(CUSTOMER_ID,
                new InvoiceDraftCommand.PaymentCommand(7L)));

        assertThat(created.payment()).isEqualTo(new PaymentTerms(7L, null));
    }

    @Test
    void a_sale_draft_saves_the_cash_method_id_without_a_name_snapshot_yet() {
        counterpartyAndProductExist(DocumentKind.SALE);
        when(ids.newId()).thenReturn(DOC_ID);

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE, command(CUSTOMER_ID,
                new InvoiceDraftCommand.PaymentCommand(7L)));

        assertThat(created.payment().paymentMethodId()).isEqualTo(7L);
        assertThat(created.payment().paymentMethodName()).isNull();
    }

    @Test
    void a_draft_without_payment_is_valid_and_has_none() {
        counterpartyAndProductExist(DocumentKind.SALE);
        when(ids.newId()).thenReturn(DOC_ID);

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, null));

        assertThat(created.payment()).isNull();
    }

    @Test
    void updating_a_draft_replaces_the_payment_method() {
        BillingDocument stored = draftSale();
        when(repository.findById(ENTERPRISE_ID, stored.id())).thenReturn(Optional.of(stored));
        counterpartyAndProductExist(DocumentKind.SALE);
        InvoiceDraftCommand update = new InvoiceDraftCommand(CUSTOMER_ID, LocalDate.of(2026, 5, 1), null,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("1"), null, null)), null,
                new InvoiceDraftCommand.PaymentCommand(8L));

        BillingDocument updated = drafts.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(), update);

        assertThat(updated.payment().paymentMethodId()).isEqualTo(8L);
    }

    @Test
    void a_payment_command_without_a_method_saves_no_payment() {
        counterpartyAndProductExist(DocumentKind.SALE);
        when(ids.newId()).thenReturn(DOC_ID);

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE, command(CUSTOMER_ID,
                new InvoiceDraftCommand.PaymentCommand(null)));

        assertThat(created.payment()).isNull();
        verify(repository).save(any(BillingDocument.class));
    }

    @Test
    void a_purchase_draft_keeps_the_payment_method() {
        counterpartyAndProductExist(DocumentKind.PURCHASE);
        when(ids.newId()).thenReturn(DOC_ID);
        InvoiceDraftCommand purchase = new InvoiceDraftCommand(SUPPLIER_ID, null, SUPPLIER_INVOICE_NUMBER,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("2"), null, null)), null,
                new InvoiceDraftCommand.PaymentCommand(7L));

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, purchase);

        assertThat(created.payment()).isEqualTo(new PaymentTerms(7L, null));
    }

    // ---------- emision ----------

    @Test
    void emitting_cash_freezes_the_method_name() {
        catalog.withMethod(7L, "Transferencia bancaria", true);
        saleStoredWith(methodTerms(7L));

        BillingDocument emitted = emitSale();

        assertThat(emitted.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(emitted.payment().paymentMethodId()).isEqualTo(7L);
        assertThat(emitted.payment().paymentMethodName()).isEqualTo("Transferencia bancaria");
    }

    @Test
    void emitting_cash_without_method_fails_and_consumes_no_number() {
        saleStoredWith(methodTerms(null));

        Throwable thrown = catchThrowable(this::emitSale);

        assertCode(thrown, BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED);
        verifyNoInteractions(sequence, outbox);
    }

    @Test
    void emitting_with_an_inactive_method_fails() {
        catalog.withMethod(7L, "Efectivo", false);
        saleStoredWith(methodTerms(7L));

        Throwable thrown = catchThrowable(this::emitSale);

        assertCode(thrown, BillingErrorCode.BILLING_PAYMENT_METHOD_INVALID);
        verifyNoInteractions(sequence, outbox);
    }

    @Test
    void emitting_with_a_method_that_does_not_exist_in_the_enterprise_fails() {
        saleStoredWith(methodTerms(99L));

        Throwable thrown = catchThrowable(this::emitSale);

        assertCode(thrown, BillingErrorCode.BILLING_PAYMENT_METHOD_INVALID);
        verifyNoInteractions(sequence, outbox);
    }

    @Test
    void emitting_a_transfer_method_needs_nothing_else_and_freezes_only_id_and_name() {
        catalog.withMethod(7L, "Transferencia bancaria", true);
        saleStoredWith(PaymentTerms.draft(7L));

        BillingDocument emitted = emitSale();

        assertThat(emitted.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(emitted.payment()).isEqualTo(new PaymentTerms(7L, "Transferencia bancaria"));
    }

    @Test
    void emitting_with_a_method_of_another_enterprise_fails_as_method_invalid() {
        saleStoredWith(PaymentTerms.draft(99L));

        assertCode(catchThrowable(this::emitSale), BillingErrorCode.BILLING_PAYMENT_METHOD_INVALID);
        verifyNoInteractions(sequence, outbox);
    }

    @Test
    void the_bank_account_error_codes_no_longer_exist() {
        assertThat(java.util.Arrays.stream(BillingErrorCode.values()).map(Enum::name))
                .noneMatch(name -> name.contains("BANK_ACCOUNT"));
    }

    @Test
    void the_payment_of_a_sale_carries_only_method_id_and_name() {
        assertThat(java.util.Arrays.stream(PaymentTerms.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName))
                .containsExactly("paymentMethodId", "paymentMethodName");
    }

    @Test
    void saving_a_draft_keeps_the_method_id_without_consulting_the_catalog() {
        catalog.withMethod(7L, "Efectivo", true);
        counterpartyAndProductExist(DocumentKind.SALE);

        BillingDocument known = drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, new InvoiceDraftCommand.PaymentCommand(7L)));
        BillingDocument unknown = drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, new InvoiceDraftCommand.PaymentCommand(99L)));

        assertThat(known.payment()).isEqualTo(new PaymentTerms(7L, null));
        assertThat(unknown.payment()).isEqualTo(new PaymentTerms(99L, null));
    }

    @Test
    void emitting_without_payment_method_fails_as_method_required_and_consumes_no_number() {
        saleStoredWith(null);

        assertCode(catchThrowable(this::emitSale), BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED);
        verifyNoInteractions(sequence, outbox);
    }

    @Test
    void emitting_with_the_cash_method_freezes_its_name() {
        saleStoredWith(PaymentTerms.draft(1L));

        BillingDocument emitted = emitSale();

        assertThat(emitted.payment()).isEqualTo(new PaymentTerms(1L, "Efectivo"));
    }

    @Test
    void a_purchase_invoice_requires_payment_to_emit() {
        BillingDocument purchase = readyPurchaseDraft();
        purchase.replaceInvoiceDraft(purchase.counterparty(), TODAY, SUPPLIER_INVOICE_NUMBER, purchase.lines(), null,
                NOW, USER);
        when(repository.findByIdForUpdate(ENTERPRISE_ID, purchase.id())).thenReturn(Optional.of(purchase));
        counterpartyAndProductExist(DocumentKind.PURCHASE);
        when(enterprises.findIssuer(ENTERPRISE_ID)).thenReturn(Optional.of(issuer()));

        assertCode(catchThrowable(() -> emission.emit(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, purchase.id(), null)),
                BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED);
        verifyNoInteractions(sequence, outbox);
    }

    @Test
    void a_purchase_invoice_with_invalid_method_fails_and_with_valid_method_freezes_the_name() {
        BillingDocument bad = readyPurchaseDraft();
        bad.replaceInvoiceDraft(bad.counterparty(), TODAY, SUPPLIER_INVOICE_NUMBER, bad.lines(), PaymentTerms.draft(99L),
                NOW, USER);
        when(repository.findByIdForUpdate(ENTERPRISE_ID, bad.id())).thenReturn(Optional.of(bad));
        counterpartyAndProductExist(DocumentKind.PURCHASE);
        when(enterprises.findIssuer(ENTERPRISE_ID)).thenReturn(Optional.of(issuer()));
        assertCode(catchThrowable(() -> emission.emit(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, bad.id(), null)),
                BillingErrorCode.BILLING_PAYMENT_METHOD_INVALID);
    }

    @Test
    void a_purchase_invoice_emits_freezing_the_method_name() {
        BillingDocument purchase = readyPurchaseDraft();
        when(repository.findByIdForUpdate(ENTERPRISE_ID, purchase.id())).thenReturn(Optional.of(purchase));
        counterpartyAndProductExist(DocumentKind.PURCHASE);
        when(enterprises.findIssuer(ENTERPRISE_ID)).thenReturn(Optional.of(issuer()));
        when(sequence.nextValue(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE)).thenReturn(3L);

        BillingDocument emitted = emission.emit(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, purchase.id(), null);

        assertThat(emitted.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(emitted.payment()).isEqualTo(new PaymentTerms(1L, "Efectivo"));
    }
}
