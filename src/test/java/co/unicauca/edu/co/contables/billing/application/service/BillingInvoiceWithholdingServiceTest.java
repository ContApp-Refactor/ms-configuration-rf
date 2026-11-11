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
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand.WithholdingCommand;
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
import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentEmitted;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.Withholding;
import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import co.unicauca.edu.co.contables.billing.support.FakePaymentCatalogPort;
import co.unicauca.edu.co.contables.billing.support.FakeTaxCatalogPort;
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
 * Retenciones en el borrador y en la emision: se validan contra los impuestos ACTIVOS de la empresa,
 * toman la tarifa vigente, se refrescan y congelan al emitir y no aplican a otros tipos.
 */
@ExtendWith(MockitoExtension.class)
class BillingInvoiceWithholdingServiceTest {

    private static final UUID DOC_ID = uuid(1);
    private static final UUID EVENT_ID = uuid(77);
    // 10.000 x 2 = subtotal 20.000 ; IVA combinado 15% = 3.000
    private static final ProductSnapshot PRODUCT = product(1L, "10000.00", "15");

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

    private final FakePaymentCatalogPort payments = new FakePaymentCatalogPort();
    private final FakeTaxCatalogPort taxes = FakeTaxCatalogPort.withExampleTaxes();
    private BillingInvoiceDraftService drafts;
    private BillingEmissionService emission;
    private BillingDocumentQueryService queries;

    @BeforeEach
    void setUp() {
        lenient().when(context.username()).thenReturn(USER);
        lenient().when(clock.now()).thenReturn(NOW);
        lenient().when(clock.today()).thenReturn(TODAY);
        lenient().when(ids.newId()).thenReturn(EVENT_ID);
        lenient().when(repository.save(any(BillingDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        BillingReferenceResolver resolver = new BillingReferenceResolver(thirdParties, products, enterprises);
        BillingDocumentLoader loader = new BillingDocumentLoader(repository);
        drafts = new BillingInvoiceDraftService(repository, loader, resolver, context, clock, ids, payments, taxes);
        emission = new BillingEmissionService(repository, loader, resolver, sequence, outbox, context, clock, ids,
                stock, accounting, payments, taxes);
        queries = new BillingDocumentQueryService(repository, loader, resolver, taxes);
    }

    // ---------- helpers ----------

    private static WithholdingCommand wc(WithholdingCode code, Long taxId) {
        return new WithholdingCommand(code, taxId);
    }

    private static InvoiceDraftCommand command(long counterpartyId, List<WithholdingCommand> withholdings) {
        return new InvoiceDraftCommand(counterpartyId, null, SUPPLIER_INVOICE_NUMBER,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("2"), null, null)), null,
                new InvoiceDraftCommand.PaymentCommand(1L), withholdings);
    }

    private void counterpartyAndProductExist(DocumentKind kind) {
        if (kind == DocumentKind.SALE) {
            lenient().when(thirdParties.findCounterparty(ENTERPRISE_ID, CUSTOMER_ID, CounterpartyRole.CUSTOMER))
                    .thenReturn(Optional.of(customer()));
        } else {
            lenient().when(thirdParties.findCounterparty(ENTERPRISE_ID, SUPPLIER_ID, CounterpartyRole.SUPPLIER))
                    .thenReturn(Optional.of(supplier()));
        }
        lenient().when(products.findProduct(ENTERPRISE_ID, 1L, kind)).thenReturn(Optional.of(PRODUCT));
    }

    private BillingDocument createSale(List<WithholdingCommand> withholdings) {
        counterpartyAndProductExist(DocumentKind.SALE);
        when(ids.newId()).thenReturn(DOC_ID);
        return drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE, command(CUSTOMER_ID, withholdings));
    }

    private BillingDocument storedSaleDraft(List<Withholding> withholdings) {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(lineOf(1, PRODUCT, "2")), methodTerms(1L),
                withholdings, NOW, USER);
        lenient().when(repository.findByIdForUpdate(ENTERPRISE_ID, doc.id())).thenReturn(Optional.of(doc));
        lenient().when(repository.findById(ENTERPRISE_ID, doc.id())).thenReturn(Optional.of(doc));
        counterpartyAndProductExist(DocumentKind.SALE);
        lenient().when(enterprises.findIssuer(ENTERPRISE_ID)).thenReturn(Optional.of(issuer()));
        lenient().when(sequence.nextValue(ENTERPRISE_ID, DocumentType.SALE_INVOICE)).thenReturn(8L);
        return doc;
    }

    private static Withholding stored(WithholdingCode code, long taxId, String name, String pct) {
        return new Withholding(code, taxId, name, co.unicauca.edu.co.contables.billing.support.BillingTestData
                .rate(pct));
    }

    private static void assertCode(Throwable thrown, BillingErrorCode code) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        assertThat(((BillingException) thrown).errorCode()).isEqualTo(code);
    }

    // ---------- borrador ----------

    @Test
    void the_business_example_saved_in_a_draft_nets_to_17000() {
        BillingDocument created = createSale(List.of(
                wc(WithholdingCode.RETE_ICA, 3L), wc(WithholdingCode.RETE_FUENTE, 2L)));

        assertThat(created.withholdings()).extracting(Withholding::code, Withholding::taxId, Withholding::taxName)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple(WithholdingCode.RETE_ICA, 3L, "c"),
                        org.assertj.core.api.Assertions.tuple(WithholdingCode.RETE_FUENTE, 2L, "b"));
        assertThat(created.totals().subtotal().toCents()).isEqualTo(2_000_000L);
        assertThat(created.totals().tax().toCents()).isEqualTo(300_000L);
        assertThat(created.totals().withholdings().toCents()).isEqualTo(600_000L);
        assertThat(created.totals().total().toCents()).isEqualTo(1_700_000L);
    }

    @Test
    void a_draft_without_withholdings_never_queries_the_tax_catalog() {
        BillingDocument created = createSale(null);

        assertThat(created.withholdings()).isEmpty();
        assertThat(created.totals().total().toCents()).isEqualTo(2_300_000L);
        assertThat(taxes.listCalls()).isZero();
    }

    @Test
    void the_tax_catalog_is_queried_once_per_save() {
        createSale(List.of(wc(WithholdingCode.RETE_ICA, 3L), wc(WithholdingCode.RETE_FUENTE, 2L),
                wc(WithholdingCode.RETE_IVA, 1L)));

        assertThat(taxes.listCalls()).isEqualTo(1);
    }

    @Test
    void a_missing_tax_id_is_rejected() {
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_ICA, null)))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID);
    }

    @Test
    void a_tax_that_does_not_exist_for_the_company_is_rejected() {
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_ICA, 999L)))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID);
    }

    @Test
    void an_inactive_tax_is_rejected() {
        taxes.deactivate(3L);
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_ICA, 3L)))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID);
    }

    @Test
    void a_tax_rate_outside_0_to_100_is_rejected() {
        taxes.withTax(9L, "raro", "150");
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_ICA, 9L)))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID);
    }

    @Test
    void a_missing_code_is_rejected() {
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(null, 3L)))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_INVALID);
    }

    @Test
    void a_repeated_code_is_rejected() {
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_ICA, 3L), wc(WithholdingCode.RETE_ICA, 2L)))));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_INVALID);
    }

    @Test
    void the_same_tax_may_be_used_by_different_withholdings() {
        BillingDocument created = createSale(List.of(wc(WithholdingCode.RETE_ICA, 2L),
                wc(WithholdingCode.RETE_FUENTE, 2L)));

        assertThat(created.withholdings()).hasSize(2);
    }

    @Test
    void a_purchase_draft_accepts_withholdings_but_not_auto_renta() {
        counterpartyAndProductExist(DocumentKind.PURCHASE);
        when(ids.newId()).thenReturn(DOC_ID);

        BillingDocument created = drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                command(SUPPLIER_ID, List.of(wc(WithholdingCode.RETE_FUENTE, 2L))));
        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                command(SUPPLIER_ID, List.of(wc(WithholdingCode.AUTO_RENTA, 2L)))));

        assertThat(created.totals().total().toCents()).isEqualTo(2_300_000L - 200_000L);
        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_INVALID);
    }

    @Test
    void auto_renta_is_accepted_in_a_sale() {
        BillingDocument created = createSale(List.of(wc(WithholdingCode.AUTO_RENTA, 1L)));

        assertThat(created.withholdings().get(0).code()).isEqualTo(WithholdingCode.AUTO_RENTA);
        assertThat(created.totals().withholdings().toCents()).isEqualTo(100_000L);
    }

    @Test
    void withholdings_above_the_total_are_rejected_with_their_own_code() {
        taxes.withTax(8L, "cien", "100");
        counterpartyAndProductExist(DocumentKind.SALE);

        Throwable thrown = catchThrowable(() -> drafts.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_IVA, 8L), wc(WithholdingCode.RETE_ICA, 8L),
                        wc(WithholdingCode.RETE_FUENTE, 8L)))));

        assertCode(thrown, BillingErrorCode.BILLING_TOTAL_NEGATIVE);
    }

    @Test
    void updating_a_draft_replaces_the_whole_set_and_an_empty_list_clears_it() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));

        BillingDocument replaced = drafts.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(),
                command(CUSTOMER_ID, List.of(wc(WithholdingCode.RETE_FUENTE, 2L))));
        assertThat(replaced.withholdings()).extracting(Withholding::code).containsExactly(WithholdingCode.RETE_FUENTE);

        BillingDocument cleared = drafts.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(),
                command(CUSTOMER_ID, List.of()));
        assertThat(cleared.withholdings()).isEmpty();
        assertThat(cleared.totals().total().toCents()).isEqualTo(2_300_000L);
    }

    @Test
    void an_emitted_invoice_does_not_accept_changes_to_withholdings() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(), null);

        Throwable thrown = catchThrowable(() -> drafts.update(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(),
                command(CUSTOMER_ID, List.of())));

        assertCode(thrown, BillingErrorCode.BILLING_INVALID_STATE);
        assertThat(stored.withholdings()).hasSize(1);
    }

    // ---------- emision ----------

    @Test
    void emitting_freezes_the_snapshot_with_the_rate_current_at_emission() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        taxes.deactivate(3L).withTax(3L, "c renombrado", "25");

        BillingDocument emitted = emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(), null);

        assertThat(emitted.status()).isEqualTo(DocumentStatus.EMITIDA);
        Withholding frozen = emitted.withholdings().get(0);
        assertThat(frozen.taxId()).isEqualTo(3L);
        assertThat(frozen.taxName()).isEqualTo("c renombrado");
        assertThat(frozen.rate().value()).isEqualByComparingTo("25");
        assertThat(emitted.totals().withholdings().toCents()).isEqualTo(500_000L);
        assertThat(emitted.totals().total().toCents()).isEqualTo(1_800_000L);
        verify(outbox).append(any(BillingDocumentEmitted.class));
    }

    @Test
    void emitting_with_a_tax_that_became_inactive_is_rejected_and_nothing_is_emitted() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        taxes.deactivate(3L);

        Throwable thrown = catchThrowable(() -> emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(),
                null));

        assertCode(thrown, BillingErrorCode.BILLING_WITHHOLDING_TAX_INVALID);
        assertThat(stored.status()).isEqualTo(DocumentStatus.BORRADOR);
        org.mockito.Mockito.verifyNoInteractions(outbox);
    }

    @Test
    void emitting_a_draft_without_withholdings_never_queries_the_tax_catalog() {
        BillingDocument stored = storedSaleDraft(List.of());

        BillingDocument emitted = emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(), null);

        assertThat(emitted.withholdings()).isEmpty();
        assertThat(taxes.listCalls()).isZero();
    }

    @Test
    void an_emission_where_the_new_rates_make_the_total_negative_is_rejected() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        taxes.withTax(3L, "c", "100").withTax(2L, "b", "100");
        stored.refreshWithholdings(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20"),
                stored(WithholdingCode.RETE_FUENTE, 2L, "b", "10"), stored(WithholdingCode.RETE_IVA, 1L, "a", "5")));
        taxes.withTax(1L, "a", "100");

        Throwable thrown = catchThrowable(() -> emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(),
                null));

        assertCode(thrown, BillingErrorCode.BILLING_TOTAL_NEGATIVE);
        assertThat(stored.status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    // ---------- lectura de borrador ----------

    @Test
    void reading_a_draft_shows_the_current_rate_without_persisting_it() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        taxes.withTax(3L, "c", "30");

        BillingDocument read = queries.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id()).doc();

        assertThat(read.withholdings().get(0).rate().value()).isEqualByComparingTo("30");
        assertThat(read.totals().withholdings().toCents()).isEqualTo(600_000L);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(any(BillingDocument.class));
    }

    @Test
    void reading_a_draft_keeps_the_saved_value_when_the_tax_is_gone_and_never_fails() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        taxes.deactivate(3L);

        BillingDocument read = queries.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id()).doc();

        assertThat(read.withholdings().get(0).rate().value()).isEqualByComparingTo("20");
        assertThat(read.totals().total().toCents()).isEqualTo(1_900_000L);
    }

    @Test
    void reading_an_emitted_invoice_returns_the_frozen_snapshot_even_if_the_tax_changes() {
        BillingDocument stored = storedSaleDraft(List.of(stored(WithholdingCode.RETE_ICA, 3L, "c", "20")));
        emission.emit(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id(), null);
        taxes.deactivate(3L).withTax(3L, "otro", "99");

        BillingDocument read = queries.get(ENTERPRISE_ID, DocumentType.SALE_INVOICE, stored.id()).doc();

        assertThat(read.withholdings().get(0).taxName()).isEqualTo("c");
        assertThat(read.withholdings().get(0).rate().value()).isEqualByComparingTo("20");
        assertThat(read.totals().total().toCents()).isEqualTo(1_900_000L);
    }
}
