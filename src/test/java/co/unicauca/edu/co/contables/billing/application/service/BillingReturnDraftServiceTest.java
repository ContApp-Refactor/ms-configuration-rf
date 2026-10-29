package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedInvoice;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSaleWithLines;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
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

/**
 * RET-1..4, RET-6 (RN-17..19, RN-23b): guardado de borradores de devolucion (crear y autosave). El
 * servicio carga la factura original, valida las lineas pedidas contra la original (referencia
 * conocida y sin repetir ANTES de heredar) y contra la disponibilidad (solo devoluciones EMITIDA
 * cuentan), y persiste. Mockito sobre los puertos.
 */
@ExtendWith(MockitoExtension.class)
class BillingReturnDraftServiceTest {

    private static final UUID RETURN_ID = uuid(2);
    private static final UUID ORIGINAL_ID = uuid(10);
    private static final DocumentType SALE_RETURN = DocumentType.SALE_RETURN;

    @Mock
    private BillingDocumentRepositoryPort repository;
    @Mock
    private BillingExecutionContextPort context;
    @Mock
    private BillingClockPort clock;
    @Mock
    private BillingIdGeneratorPort ids;

    private BillingReturnDraftService service;

    @BeforeEach
    void setUp() {
        lenient().when(context.username()).thenReturn(USER);
        lenient().when(clock.now()).thenReturn(NOW);
        lenient().when(clock.today()).thenReturn(TODAY);
        lenient().when(ids.newId()).thenReturn(RETURN_ID);
        lenient().when(repository.save(any(BillingDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new BillingReturnDraftService(repository, new BillingDocumentLoader(repository), context, clock,
                ids);
    }

    // ---------- helpers ----------

    /** Venta emitida FV-000007 con linea 1 (10 unidades) y linea 2 (5 unidades), 100.00 + 19%. */
    private static BillingDocument saleOriginal() {
        return emittedSaleWithLines(ORIGINAL_ID, 7L, lineOf(1, product(1L), "10"), lineOf(2, product(2L), "5"));
    }

    private void stored(BillingDocument doc) {
        when(repository.findById(ENTERPRISE_ID, doc.id())).thenReturn(Optional.of(doc));
    }

    private void returnsOfOriginal(BillingDocument... returns) {
        when(repository.findReturnsOf(ENTERPRISE_ID, ORIGINAL_ID)).thenReturn(List.of(returns));
    }

    private static ReturnLineCommand line(int sourceLineNo, String quantity) {
        return new ReturnLineCommand(sourceLineNo, new BigDecimal(quantity));
    }

    private static ReturnDraftCommand command(LocalDate date, String reason, Long version,
            ReturnLineCommand... lines) {
        return new ReturnDraftCommand(ORIGINAL_ID, date, reason, List.of(lines), version);
    }

    private BillingDocument create(ReturnDraftCommand command) {
        return service.create(ENTERPRISE_ID, SALE_RETURN, command);
    }

    private BillingDocument update(ReturnDraftCommand command) {
        return service.update(ENTERPRISE_ID, SALE_RETURN, RETURN_ID, command);
    }

    private static BillingErrorCode codeOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(BillingException.class);
        return ((BillingException) thrown).errorCode();
    }

    private void assertNothingSaved() {
        verify(repository, never()).save(any(BillingDocument.class));
    }

    // ---------- crear ----------

    @Test
    void RET_1_create_loads_the_original_reads_its_returns_and_saves_in_that_order() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();

        BillingDocument created = create(command(null, "producto defectuoso", null, line(1, "4")));

        InOrder order = inOrder(repository);
        order.verify(repository).findById(ENTERPRISE_ID, ORIGINAL_ID);
        order.verify(repository).findReturnsOf(ENTERPRISE_ID, ORIGINAL_ID);
        order.verify(repository).save(created);
    }

    @Test
    void RET_1_create_builds_a_borrador_of_the_return_type_that_references_the_original() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();

        BillingDocument created = create(command(null, "producto defectuoso", null, line(1, "4")));

        assertThat(created.id()).isEqualTo(RETURN_ID);
        assertThat(created.type()).isEqualTo(SALE_RETURN);
        assertThat(created.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(created.number()).isNull();
        assertThat(created.original().id()).isEqualTo(ORIGINAL_ID);
        assertThat(created.original().number().display()).isEqualTo("FV-000007");
        assertThat(created.enterpriseId()).isEqualTo(ENTERPRISE_ID);
        assertThat(created.counterparty()).isEqualTo(original.counterparty());
        assertThat(created.createdBy()).isEqualTo(USER);
        assertThat(created.createdAt()).isEqualTo(NOW);
    }

    @Test
    void RET_2_create_inherits_the_frozen_price_and_tax_of_the_original_line() {
        BillingDocument original = emittedInvoice(ORIGINAL_ID, DocumentType.SALE_INVOICE, 7L, List.of(
                DocumentLine.forInvoice(1, product(1L), qty("10"), money("150.00"), rate("5"))));
        stored(original);
        returnsOfOriginal();

        BillingDocument created = create(command(null, "motivo", null, line(1, "4")));

        DocumentLine inherited = created.lines().get(0);
        assertThat(inherited.sourceLineNo()).isEqualTo(1);
        assertThat(inherited.unitPrice()).isEqualTo(money("150.00"));
        assertThat(inherited.taxRate()).isEqualTo(rate("5"));
        assertThat(inherited.priceOverridden()).isFalse();
        assertThat(created.totals().subtotal()).isEqualTo(money("600.00"));
        assertThat(created.totals().tax()).isEqualTo(money("30.00"));
        assertThat(created.totals().total()).isEqualTo(money("630.00"));
    }

    @Test
    void RET_2_create_numbers_the_lines_in_request_order_keeping_the_source_line() {
        stored(saleOriginal());
        returnsOfOriginal();

        BillingDocument created = create(command(null, "motivo", null, line(2, "1"), line(1, "3")));

        assertThat(created.lines()).extracting(DocumentLine::lineNo).containsExactly(1, 2);
        assertThat(created.lines()).extracting(DocumentLine::sourceLineNo).containsExactly(2, 1);
        assertThat(created.lines().get(0).productId()).isEqualTo(2L);
    }

    @Test
    void RET_1_create_without_date_defaults_to_the_clock_today() {
        stored(saleOriginal());
        returnsOfOriginal();

        BillingDocument created = create(command(null, "motivo", null, line(1, "1")));

        assertThat(created.issueDate()).isEqualTo(TODAY);
    }

    @Test
    void RET_1_create_with_date_keeps_the_requested_date() {
        stored(saleOriginal());
        returnsOfOriginal();
        LocalDate requested = LocalDate.of(2026, 2, 1);

        BillingDocument created = create(command(requested, "motivo", null, line(1, "1")));

        assertThat(created.issueDate()).isEqualTo(requested);
    }

    @Test
    void RET_4_create_accepts_a_partial_draft_without_reason_or_lines() {
        stored(saleOriginal());

        BillingDocument created = create(new ReturnDraftCommand(ORIGINAL_ID, null, null, null, null));

        assertThat(created.lines()).isEmpty();
        assertThat(created.returnReason()).isNull();
        verify(repository).save(created);
    }

    @Test
    void RET_4_create_trims_the_reason() {
        stored(saleOriginal());
        returnsOfOriginal();

        BillingDocument created = create(command(null, "  producto defectuoso  ", null, line(1, "1")));

        assertThat(created.returnReason()).isEqualTo("producto defectuoso");
    }

    @Test
    void RET_1_the_persisted_instance_is_the_one_returned() {
        stored(saleOriginal());
        returnsOfOriginal();
        BillingDocument persisted = withVersion(readySaleDraft(), 0L);
        when(repository.save(any(BillingDocument.class))).thenReturn(persisted);

        BillingDocument result = create(command(null, "motivo", null, line(1, "1")));

        assertThat(result).isSameAs(persisted);
    }

    @Test
    void RET_8_create_never_saves_the_original() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();

        create(command(null, "motivo", null, line(1, "1")));

        verify(repository, never()).save(original);
        verify(repository, never()).findByIdForUpdate(any(), any());
    }

    @Test
    void PUR_1_a_purchase_return_copies_the_supplier_and_the_supplier_invoice_number() {
        BillingDocument original = emittedInvoice(ORIGINAL_ID, DocumentType.PURCHASE_INVOICE, 7L,
                List.of(lineOf(1, product(1L), "10")));
        stored(original);
        returnsOfOriginal();

        BillingDocument created = service.create(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN,
                command(null, "mercancia danada", null, line(1, "2")));

        assertThat(created.type()).isEqualTo(DocumentType.PURCHASE_RETURN);
        assertThat(created.counterparty().thirdId()).isEqualTo(original.counterparty().thirdId());
        assertThat(created.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
        assertThat(created.original().number().display()).isEqualTo("FC-000007");
    }

    // ---------- disponibilidad (RET-3) ----------

    @Test
    void RET_3_quantity_above_the_available_one_is_rejected_counting_only_emitted_returns() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal(emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "4")));

        Throwable thrown = catchThrowable(() -> create(command(null, "motivo", null, line(1, "7"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED);
        assertNothingSaved();
    }

    @Test
    void RET_3_quantity_equal_to_the_available_one_is_accepted() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal(emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "4")));

        BillingDocument created = create(command(null, "motivo", null, line(1, "6")));

        assertThat(created.lines()).hasSize(1);
    }

    @Test
    void RET_3_draft_and_annulled_returns_do_not_reduce_the_available_quantity() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal(
                returnDraftOf(original, uuid(21), "otro borrador", returnLineOf(original, 1, "5")),
                annulledReturnOf(original, uuid(22), 1L, returnLineOf(original, 1, "3")));

        BillingDocument created = create(command(null, "motivo", null, line(1, "10")));

        assertThat(created.lines().get(0).quantity()).isEqualTo(qty("10"));
    }

    @Test
    void RET_3_quantity_above_the_sold_one_is_rejected_by_the_line_itself() {
        stored(saleOriginal());

        Throwable thrown = catchThrowable(() -> create(command(null, "motivo", null, line(1, "11"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED);
        assertNothingSaved();
    }

    @Test
    void RET_3_the_second_line_is_also_checked() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal(emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 2, "4")));

        Throwable thrown = catchThrowable(
                () -> create(command(null, "motivo", null, line(1, "2"), line(2, "2"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED);
        assertNothingSaved();
    }

    // ---------- lineas invalidas (antes de heredar) ----------

    @Test
    void RET_2_an_unknown_source_line_is_rejected_before_reading_the_returns() {
        stored(saleOriginal());

        Throwable thrown = catchThrowable(() -> create(command(null, "motivo", null, line(99, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_RETURN_LINE_NOT_FOUND);
        verify(repository, never()).findReturnsOf(any(), any());
        assertNothingSaved();
    }

    @Test
    void RET_2_a_non_positive_source_line_is_unknown() {
        stored(saleOriginal());

        Throwable thrown = catchThrowable(() -> create(command(null, "motivo", null, line(0, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_RETURN_LINE_NOT_FOUND);
        assertNothingSaved();
    }

    @Test
    void RET_2_a_repeated_source_line_is_an_invalid_line() {
        stored(saleOriginal());

        Throwable thrown = catchThrowable(() -> create(command(null, "motivo", null, line(1, "1"), line(1, "2"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
        verify(repository, never()).findReturnsOf(any(), any());
        assertNothingSaved();
    }

    @Test
    void RET_2_a_zero_or_negative_quantity_is_an_invalid_line() {
        stored(saleOriginal());

        Throwable zero = catchThrowable(() -> create(command(null, "motivo", null, line(1, "0"))));
        Throwable negative = catchThrowable(() -> create(command(null, "motivo", null, line(1, "-1"))));

        assertThat(codeOf(zero)).isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
        assertThat(codeOf(negative)).isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
        assertNothingSaved();
    }

    @Test
    void RET_2_a_missing_quantity_is_an_invalid_line() {
        stored(saleOriginal());

        Throwable thrown = catchThrowable(() -> create(new ReturnDraftCommand(ORIGINAL_ID, null, "motivo",
                List.of(new ReturnLineCommand(1, null)), null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
        assertNothingSaved();
    }

    @Test
    void RET_4_a_reason_above_500_characters_is_a_validation_error() {
        stored(saleOriginal());
        returnsOfOriginal();

        Throwable thrown = catchThrowable(() -> create(command(null, "x".repeat(501), null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
        assertNothingSaved();
    }

    // ---------- original invalida ----------

    @Test
    void RET_1_a_draft_original_is_ORIGINAL_NOT_EMITTED() {
        BillingDocument draft = readySaleDraft();
        when(repository.findById(ENTERPRISE_ID, draft.id())).thenReturn(Optional.of(draft));

        Throwable thrown = catchThrowable(() -> create(new ReturnDraftCommand(draft.id(), null, "motivo", List.of(),
                null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED);
        verify(repository, never()).findReturnsOf(any(), any());
        assertNothingSaved();
    }

    @Test
    void RET_1_an_annulled_original_is_ORIGINAL_NOT_EMITTED() {
        BillingDocument annulled = annulledSale();
        when(repository.findById(ENTERPRISE_ID, annulled.id())).thenReturn(Optional.of(annulled));

        Throwable thrown = catchThrowable(
                () -> create(new ReturnDraftCommand(annulled.id(), null, "motivo", List.of(), null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED);
        assertNothingSaved();
    }

    @Test
    void RET_1_a_missing_original_is_not_found() {
        when(repository.findById(ENTERPRISE_ID, ORIGINAL_ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> create(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }

    @Test
    void PUR_3_a_sale_return_against_a_purchase_invoice_behaves_as_not_found() {
        BillingDocument purchase = emittedPurchase();
        when(repository.findById(ENTERPRISE_ID, purchase.id())).thenReturn(Optional.of(purchase));

        Throwable thrown = catchThrowable(() -> create(new ReturnDraftCommand(purchase.id(), null, "motivo",
                List.of(), null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }

    @Test
    void PUR_3_a_purchase_return_against_a_sale_invoice_behaves_as_not_found() {
        stored(saleOriginal());

        Throwable thrown = catchThrowable(() -> service.create(ENTERPRISE_ID, DocumentType.PURCHASE_RETURN,
                command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }

    @Test
    void RET_1_a_return_cannot_be_the_original_of_another_return() {
        BillingDocument original = saleOriginal();
        BillingDocument emittedReturn = emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "2"));
        when(repository.findById(ENTERPRISE_ID, emittedReturn.id())).thenReturn(Optional.of(emittedReturn));

        Throwable thrown = catchThrowable(() -> create(new ReturnDraftCommand(emittedReturn.id(), null, "motivo",
                List.of(), null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }

    // ---------- guardas de entrada ----------

    @Test
    void STA_3_an_invoice_type_is_not_accepted_by_the_return_flow() {
        Throwable create = catchThrowable(() -> service.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE,
                command(null, "motivo", null)));
        Throwable update = catchThrowable(() -> service.update(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE,
                RETURN_ID, command(null, "motivo", null)));

        assertThat(codeOf(create)).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
        assertThat(codeOf(update)).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
        verifyNoInteractions(repository);
    }

    @Test
    void RET_1_create_without_original_id_is_a_validation_error() {
        Throwable thrown = catchThrowable(
                () -> create(new ReturnDraftCommand(null, null, "motivo", List.of(), null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
        verifyNoInteractions(repository);
    }

    // ---------- autosave (RET-6) ----------

    private BillingDocument storedDraftReturn(BillingDocument original) {
        BillingDocument draft = returnDraftOf(original, RETURN_ID, "motivo viejo", returnLineOf(original, 1, "2"));
        stored(draft);
        return draft;
    }

    @Test
    void RET_6_update_loads_the_draft_then_the_original_then_the_returns_and_saves() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();
        BillingDocument draft = storedDraftReturn(original);

        BillingDocument updated = update(command(null, "motivo nuevo", null, line(1, "3"), line(2, "1")));

        InOrder order = inOrder(repository);
        order.verify(repository).findById(ENTERPRISE_ID, RETURN_ID);
        order.verify(repository).findById(ENTERPRISE_ID, ORIGINAL_ID);
        order.verify(repository).findReturnsOf(ENTERPRISE_ID, ORIGINAL_ID);
        order.verify(repository).save(updated);
        assertThat(updated).isSameAs(draft);
    }

    @Test
    void RET_6_update_replaces_the_reason_the_date_and_all_the_lines() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();
        storedDraftReturn(original);
        LocalDate newDate = LocalDate.of(2026, 3, 3);

        BillingDocument updated = update(command(newDate, "motivo nuevo", null, line(1, "3"), line(2, "1")));

        assertThat(updated.returnReason()).isEqualTo("motivo nuevo");
        assertThat(updated.issueDate()).isEqualTo(newDate);
        assertThat(updated.lines()).hasSize(2);
        assertThat(updated.lines()).extracting(l -> l.quantity().value().intValue()).containsExactly(3, 1);
        assertThat(updated.updatedAt()).isEqualTo(NOW);
        assertThat(updated.original().id()).isEqualTo(ORIGINAL_ID);
    }

    @Test
    void RET_6_update_without_date_keeps_the_current_date() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();
        storedDraftReturn(original);

        BillingDocument updated = update(command(null, "motivo", null, line(1, "1")));

        assertThat(updated.issueDate()).isEqualTo(TODAY);
    }

    @Test
    void RET_6_update_is_idempotent() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();
        BillingDocument draft = storedDraftReturn(original);
        ReturnDraftCommand same = command(null, "motivo", null, line(1, "3"));

        update(same);
        List<DocumentLine> firstLines = draft.lines();
        var firstTotals = draft.totals();
        update(same);

        assertThat(draft.lines()).isEqualTo(firstLines);
        assertThat(draft.totals()).isEqualTo(firstTotals);
    }

    @Test
    void RET_3_update_ignores_the_draft_itself_when_it_is_part_of_the_returns_list() {
        BillingDocument original = saleOriginal();
        stored(original);
        BillingDocument draft = storedDraftReturn(original);
        returnsOfOriginal(draft, emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "4")));

        BillingDocument updated = update(command(null, "motivo", null, line(1, "6")));

        assertThat(updated.lines().get(0).quantity()).isEqualTo(qty("6"));
    }

    @Test
    void RET_3_update_above_the_available_quantity_is_rejected_and_nothing_is_saved() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal(emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "4")));
        storedDraftReturn(original);

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "7"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED);
        assertNothingSaved();
    }

    @Test
    void RET_2_update_with_an_unknown_or_repeated_source_line_is_rejected() {
        BillingDocument original = saleOriginal();
        stored(original);
        storedDraftReturn(original);

        Throwable unknown = catchThrowable(() -> update(command(null, "motivo", null, line(5, "1"))));
        Throwable repeated = catchThrowable(() -> update(command(null, "motivo", null, line(2, "1"), line(2, "1"))));

        assertThat(codeOf(unknown)).isEqualTo(BillingErrorCode.BILLING_RETURN_LINE_NOT_FOUND);
        assertThat(codeOf(repeated)).isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
        assertNothingSaved();
    }

    @Test
    void RET_6_a_failed_update_leaves_the_draft_untouched() {
        BillingDocument original = saleOriginal();
        stored(original);
        BillingDocument draft = storedDraftReturn(original);
        var before = draft.state();

        catchThrowable(() -> update(command(null, "otro", null, line(1, "3"), line(1, "1"))));

        assertThat(draft.state()).isEqualTo(before);
    }

    @Test
    void RET_6_update_with_a_stale_expected_version_is_a_concurrent_modification() {
        BillingDocument original = saleOriginal();
        BillingDocument draft = withVersion(
                returnDraftOf(original, RETURN_ID, "motivo", returnLineOf(original, 1, "2")), 3L);
        stored(draft);

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", 2L, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_CONCURRENT_MODIFICATION);
        verify(repository, never()).findById(ENTERPRISE_ID, ORIGINAL_ID);
        assertNothingSaved();
    }

    @Test
    void RET_6_update_with_the_matching_expected_version_is_accepted() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();
        BillingDocument draft = withVersion(
                returnDraftOf(original, RETURN_ID, "motivo", returnLineOf(original, 1, "2")), 3L);
        stored(draft);

        BillingDocument updated = update(command(null, "motivo", 3L, line(1, "1")));

        assertThat(updated.lines()).hasSize(1);
    }

    @Test
    void RN_05_an_emitted_return_cannot_be_updated_and_the_original_is_not_read() {
        BillingDocument original = saleOriginal();
        BillingDocument emitted = emittedReturnOf(original, RETURN_ID, 1L, returnLineOf(original, 1, "2"));
        stored(emitted);

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_INVALID_STATE);
        verify(repository, never()).findById(ENTERPRISE_ID, ORIGINAL_ID);
        assertNothingSaved();
    }

    @Test
    void RN_05_an_annulled_return_cannot_be_updated() {
        BillingDocument original = saleOriginal();
        BillingDocument annulled = annulledReturnOf(original, RETURN_ID, 1L, returnLineOf(original, 1, "2"));
        stored(annulled);

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_INVALID_STATE);
        assertNothingSaved();
    }

    @Test
    void RET_5_update_after_the_original_was_annulled_is_ORIGINAL_NOT_EMITTED() {
        BillingDocument original = saleOriginal();
        BillingDocument draft = returnDraftOf(original, RETURN_ID, "motivo", returnLineOf(original, 1, "2"));
        original.annul("error", false, LATER, OTHER_USER);
        stored(original);
        stored(draft);

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED);
        assertNothingSaved();
    }

    @Test
    void RET_6_the_original_of_a_return_cannot_be_changed_by_an_update() {
        BillingDocument original = saleOriginal();
        storedDraftReturn(original);

        Throwable thrown = catchThrowable(() -> update(new ReturnDraftCommand(uuid(99), null, "motivo",
                List.of(line(1, "1")), null)));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
        assertNothingSaved();
    }

    @Test
    void RET_6_update_without_original_id_in_the_command_uses_the_stored_original() {
        BillingDocument original = saleOriginal();
        stored(original);
        returnsOfOriginal();
        storedDraftReturn(original);

        BillingDocument updated = update(new ReturnDraftCommand(null, null, "motivo", List.of(line(1, "1")), null));

        assertThat(updated.original().id()).isEqualTo(ORIGINAL_ID);
    }

    @Test
    void QRY_1_update_of_a_missing_return_is_not_found() {
        when(repository.findById(ENTERPRISE_ID, RETURN_ID)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }

    @Test
    void PUR_3_update_of_an_invoice_through_the_return_resource_is_not_found() {
        BillingDocument invoice = readySaleDraft();
        when(repository.findById(ENTERPRISE_ID, RETURN_ID)).thenReturn(Optional.of(invoice));

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }

    @Test
    void PUR_3_update_of_a_purchase_return_through_the_sale_return_resource_is_not_found() {
        BillingDocument purchaseOriginal = emittedInvoice(ORIGINAL_ID, DocumentType.PURCHASE_INVOICE, 7L,
                List.of(lineOf(1, product(1L), "10")));
        BillingDocument purchaseReturn = returnDraftOf(purchaseOriginal, RETURN_ID, "motivo",
                returnLineOf(purchaseOriginal, 1, "2"));
        stored(purchaseReturn);

        Throwable thrown = catchThrowable(() -> update(command(null, "motivo", null, line(1, "1"))));

        assertThat(codeOf(thrown)).isEqualTo(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND);
        assertNothingSaved();
    }
}
