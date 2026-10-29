package co.unicauca.edu.co.contables.billing.domain.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSaleWithLines;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnLineAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RET-3, RET-9, RET-10 (RN-19..RN-21): disponibilidad, estado derivado y ordinales. */
class ReturnAvailabilityCalculatorTest {

    private final ReturnAvailabilityCalculator calculator = new ReturnAvailabilityCalculator();

    /** Factura FV-000007: linea 1 = producto 1 x10 ; linea 2 = producto 2 x5 (100.00 + 19%). */
    private BillingDocument invoice;

    @BeforeEach
    void setUp() {
        invoice = emittedSaleWithLines(uuid(1), 7L, lineOf(1, product(1L), "10"), lineOf(2, product(2L), "5"));
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static DocumentLine requested(int sourceLineNo, String quantity) {
        return DocumentLine.restore(1, sourceLineNo, 1L, "P1", "Producto 1", 1L, "Unidad", qty(quantity),
                money("100.00"), rate("19"), null, null);
    }

    private static ReturnLineAvailability line(ReturnAvailability availability, int lineNo) {
        return availability.lines().stream().filter(l -> l.lineNo() == lineNo).findFirst().orElseThrow();
    }

    // ---------- availability ----------

    @Test
    void RET_3_without_returns_everything_sold_is_available() {
        ReturnAvailability availability = calculator.availability(invoice, List.of());

        assertThat(availability.invoiceId()).isEqualTo(uuid(1));
        assertThat(availability.lines()).hasSize(2);
        assertThat(line(availability, 1).sold()).isEqualByComparingTo(bd("10"));
        assertThat(line(availability, 1).returned()).isEqualByComparingTo(bd("0"));
        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("10"));
        assertThat(availability.status()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
        assertThat(availability.returnable()).isTrue();
    }

    @Test
    void RET_3_available_is_sold_minus_emitted_returns() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "4"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(emittedReturn));

        assertThat(line(availability, 1).sold()).isEqualByComparingTo(bd("10"));
        assertThat(line(availability, 1).returned()).isEqualByComparingTo(bd("4"));
        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("6"));
        assertThat(line(availability, 2).returned()).isEqualByComparingTo(bd("0"));
        assertThat(line(availability, 2).available()).isEqualByComparingTo(bd("5"));
    }

    @Test
    void RET_3_draft_returns_do_not_count() {
        BillingDocument draft = returnDraftOf(invoice, uuid(2), "motivo", returnLineOf(invoice, 1, "5"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(draft));

        assertThat(line(availability, 1).returned()).isEqualByComparingTo(bd("0"));
        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("10"));
        assertThat(availability.status()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    @Test
    void RET_3_annulled_returns_do_not_count() {
        BillingDocument annulled = annulledReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "3"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(annulled));

        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("10"));
        assertThat(availability.status()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    @Test
    void RET_3_sold_10_emitted_4_with_a_draft_of_5_and_an_annulled_of_3_still_has_6() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "4"));
        BillingDocument draft = returnDraftOf(invoice, uuid(3), "motivo", returnLineOf(invoice, 1, "5"));
        BillingDocument annulled = annulledReturnOf(invoice, uuid(4), 2L, returnLineOf(invoice, 1, "3"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(emittedReturn, draft, annulled));

        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("6"));
    }

    @Test
    void RET_3_several_emitted_returns_accumulate() {
        BillingDocument first = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "4"));
        BillingDocument second = emittedReturnOf(invoice, uuid(3), 2L, returnLineOf(invoice, 1, "3"),
                returnLineOf(invoice, 2, "1"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(first, second));

        assertThat(line(availability, 1).returned()).isEqualByComparingTo(bd("7"));
        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("3"));
        assertThat(line(availability, 2).returned()).isEqualByComparingTo(bd("1"));
        assertThat(line(availability, 2).available()).isEqualByComparingTo(bd("4"));
    }

    @Test
    void RET_3_returns_of_another_invoice_are_ignored() {
        BillingDocument other = emittedSaleWithLines(uuid(50), 8L, lineOf(1, product(1L), "10"));
        BillingDocument foreignReturn = emittedReturnOf(other, uuid(51), 1L, returnLineOf(other, 1, "9"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(foreignReturn));

        assertThat(line(availability, 1).returned()).isEqualByComparingTo(bd("0"));
    }

    @Test
    void RET_3_fractional_quantities_are_exact() {
        BillingDocument fractional = emittedSaleWithLines(uuid(60), 9L, lineOf(1, product(1L), "2.500"));
        BillingDocument emittedReturn = emittedReturnOf(fractional, uuid(61), 1L, returnLineOf(fractional, 1, "0.125"));

        ReturnAvailability availability = calculator.availability(fractional, List.of(emittedReturn));

        assertThat(line(availability, 1).returned()).isEqualByComparingTo(bd("0.125"));
        assertThat(line(availability, 1).available()).isEqualByComparingTo(bd("2.375"));
    }

    @Test
    void RET_3_availability_lines_expose_product_and_frozen_effective_price_and_tax() {
        BillingDocument priced = emittedSaleWithLines(uuid(62), 10L,
                DocumentLine.forInvoice(1, product(3L, "100.00", "19"), qty("2"), money("150.00"), rate("5")));

        ReturnLineAvailability only = calculator.availability(priced, List.of()).lines().get(0);

        assertThat(only.lineNo()).isEqualTo(1);
        assertThat(only.productId()).isEqualTo(3L);
        assertThat(only.productName()).isEqualTo("Producto 3");
        assertThat(only.unitPrice()).isEqualTo(money("150.00"));
        assertThat(only.taxRate()).isEqualTo(rate("5"));
    }

    @Test
    void RET_3_availability_rejects_a_return_document_as_invoice() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "1"));

        assertThatThrownBy(() -> calculator.availability(emittedReturn, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- estado derivado y returnable ----------

    @Test
    void RET_9_some_returned_and_some_available_is_PARCIAL() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "10"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(emittedReturn));

        assertThat(availability.status()).isEqualTo(ReturnStatus.PARCIAL);
        assertThat(availability.returnable()).isTrue();
    }

    @Test
    void RET_9_everything_returned_is_TOTAL_and_not_returnable() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "10"),
                returnLineOf(invoice, 2, "5"));

        ReturnAvailability availability = calculator.availability(invoice, List.of(emittedReturn));

        assertThat(availability.status()).isEqualTo(ReturnStatus.TOTAL);
        assertThat(availability.returnable()).isFalse();
    }

    @Test
    void RET_9_total_split_across_two_returns_is_TOTAL() {
        BillingDocument first = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "10"));
        BillingDocument second = emittedReturnOf(invoice, uuid(3), 2L, returnLineOf(invoice, 2, "5"));

        assertThat(calculator.availability(invoice, List.of(first, second)).status()).isEqualTo(ReturnStatus.TOTAL);
    }

    @Test
    void RET_9_annulling_a_return_moves_TOTAL_back_to_PARCIAL_and_then_SIN_DEVOLUCION() {
        BillingDocument first = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "10"));
        BillingDocument second = emittedReturnOf(invoice, uuid(3), 2L, returnLineOf(invoice, 2, "5"));
        assertThat(calculator.availability(invoice, List.of(first, second)).status()).isEqualTo(ReturnStatus.TOTAL);

        BillingDocument secondAnnulled = annulledReturnOf(invoice, uuid(3), 2L, returnLineOf(invoice, 2, "5"));
        assertThat(calculator.availability(invoice, List.of(first, secondAnnulled)).status())
                .isEqualTo(ReturnStatus.PARCIAL);

        BillingDocument firstAnnulled = annulledReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "10"));
        assertThat(calculator.availability(invoice, List.of(firstAnnulled, secondAnnulled)).status())
                .isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    @Test
    void RET_9_annulled_invoice_is_not_returnable() {
        BillingDocument annulled = annulledSale();

        ReturnAvailability availability = calculator.availability(annulled, List.of());

        assertThat(availability.returnable()).isFalse();
        assertThat(availability.status()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    // ---------- assertWithinAvailability ----------

    @Test
    void RET_3_accepts_exactly_the_available_quantity() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "4"));

        assertThatCode(() -> calculator.assertWithinAvailability(invoice, List.of(emittedReturn),
                List.of(requested(1, "6")))).doesNotThrowAnyException();
    }

    @Test
    void RET_3_sold_10_returned_4_requesting_7_exceeds() {
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "4"));

        assertThatThrownBy(() -> calculator.assertWithinAvailability(invoice, List.of(emittedReturn),
                List.of(requested(1, "7"))))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED);
    }

    @Test
    void RET_3_draft_and_annulled_returns_do_not_reduce_what_can_be_requested() {
        BillingDocument draft = returnDraftOf(invoice, uuid(3), "motivo", returnLineOf(invoice, 1, "5"));
        BillingDocument annulled = annulledReturnOf(invoice, uuid(4), 2L, returnLineOf(invoice, 1, "3"));

        assertThatCode(() -> calculator.assertWithinAvailability(invoice, List.of(draft, annulled),
                List.of(requested(1, "10")))).doesNotThrowAnyException();
    }

    @Test
    void RET_3_exceeding_on_the_second_line_only_is_detected() {
        assertThatThrownBy(() -> calculator.assertWithinAvailability(invoice, List.of(),
                List.of(requested(1, "10"), requested(2, "5.001"))))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED);
    }

    @Test
    void RET_3_unknown_source_line_is_RETURN_LINE_NOT_FOUND() {
        assertThatThrownBy(() -> calculator.assertWithinAvailability(invoice, List.of(), List.of(requested(3, "1"))))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_RETURN_LINE_NOT_FOUND);
    }

    @Test
    void RET_3_duplicate_source_line_is_INVALID_LINE() {
        assertThatThrownBy(() -> calculator.assertWithinAvailability(invoice, List.of(),
                List.of(requested(1, "1"), requested(1, "2"))))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void RET_3_line_without_source_line_is_INVALID_LINE() {
        DocumentLine orphan = DocumentLine.restore(1, null, 1L, "P1", "Producto 1", 1L, "Unidad", qty("1"),
                money("100.00"), rate("19"), null, null);

        assertThatThrownBy(() -> calculator.assertWithinAvailability(invoice, List.of(), List.of(orphan)))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void RET_4_an_empty_request_is_accepted_for_partial_drafts() {
        assertThatCode(() -> calculator.assertWithinAvailability(invoice, List.of(), List.of()))
                .doesNotThrowAnyException();
    }

    // ---------- ordinales ----------

    @Test
    void RET_10_ordinals_follow_ascending_number_and_keep_annulled() {
        Map<UUID, Long> numbers = new LinkedHashMap<>();
        numbers.put(uuid(13), 30L);
        numbers.put(uuid(11), 5L);
        numbers.put(uuid(12), 12L);

        Map<UUID, Integer> ordinals = ReturnAvailabilityCalculator.ordinals(numbers);

        assertThat(ordinals).containsExactly(entry(uuid(11), 1), entry(uuid(12), 2), entry(uuid(13), 3));
    }

    @Test
    void RET_10_no_returns_means_no_ordinals() {
        assertThat(ReturnAvailabilityCalculator.ordinals(Map.of())).isEmpty();
    }

    @Test
    void RET_10_ordinalsOf_numbers_emitted_and_annulled_returns_and_skips_drafts() {
        BillingDocument annulled = annulledReturnOf(invoice, uuid(2), 1L, returnLineOf(invoice, 1, "1"));
        BillingDocument emittedReturn = emittedReturnOf(invoice, uuid(3), 2L, returnLineOf(invoice, 1, "1"));
        BillingDocument draft = returnDraftOf(invoice, uuid(4), "motivo", returnLineOf(invoice, 1, "1"));

        Map<UUID, Integer> ordinals = ReturnAvailabilityCalculator.ordinalsOf(List.of(draft, emittedReturn, annulled));

        assertThat(ordinals).containsExactly(entry(uuid(2), 1), entry(uuid(3), 2));
        assertThat(ordinals).doesNotContainKey(uuid(4));
    }
}
