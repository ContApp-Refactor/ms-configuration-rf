package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import co.unicauca.edu.co.contables.billing.domain.service.ReturnAvailabilityCalculator;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingReturnResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.InvoiceWithReturnsResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnAvailabilityResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnDraftRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnLineRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnSummaryResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.mapper.BillingRestMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * H8d (8d.1): mapeo REST de devoluciones. Peticion -> comando (solo campos editables, ids
 * validados), documento -> respuesta con centavos exactos, ordinal, referencia a la original y
 * disponibilidad / lista "facturas con devolucion".
 */
class BillingReturnRestMapperTest {

    private final BillingRestMapper mapper = new BillingRestMapper();

    private static ReturnDraftRequest request(String invoiceId, ReturnLineRequest... lines) {
        return new ReturnDraftRequest(invoiceId, LocalDate.of(2026, 1, 20), "producto defectuoso",
                List.of(lines), 3L);
    }

    // ---------- request -> command ----------

    @Test
    void RET_9_command_carries_original_id_date_reason_lines_and_version() {
        ReturnDraftCommand command = mapper.toCommand(request(uuid(1).toString(),
                new ReturnLineRequest(1, new BigDecimal("1.5")), new ReturnLineRequest(2, BigDecimal.TEN)));

        assertThat(command.originalDocumentId()).isEqualTo(uuid(1));
        assertThat(command.issueDate()).isEqualTo(LocalDate.of(2026, 1, 20));
        assertThat(command.reason()).isEqualTo("producto defectuoso");
        assertThat(command.expectedVersion()).isEqualTo(3L);
        assertThat(command.lines()).hasSize(2);
        assertThat(command.lines().get(0).sourceLineNo()).isEqualTo(1);
        assertThat(command.lines().get(0).quantity()).isEqualByComparingTo("1.5");
        assertThat(command.lines().get(1).sourceLineNo()).isEqualTo(2);
    }

    @Test
    void RET_9_command_trims_the_invoice_id() {
        ReturnDraftCommand command = mapper.toCommand(request("  " + uuid(1) + " "));

        assertThat(command.originalDocumentId()).isEqualTo(uuid(1));
    }

    @Test
    void RET_9_command_with_null_lines_is_an_empty_list() {
        ReturnDraftCommand command = mapper.toCommand(
                new ReturnDraftRequest(uuid(1).toString(), null, null, null, null));

        assertThat(command.lines()).isEmpty();
        assertThat(command.issueDate()).isNull();
        assertThat(command.reason()).isNull();
        assertThat(command.expectedVersion()).isNull();
    }

    @Test
    void RET_9_command_with_blank_or_malformed_invoice_id_is_a_validation_error() {
        assertThatThrownBy(() -> mapper.toCommand(request(" ")))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR));
        assertThatThrownBy(() -> mapper.toCommand(request(null)))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR));
        assertThatThrownBy(() -> mapper.toCommand(request("no-es-un-uuid")))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR));
    }

    @Test
    void RET_9_command_line_without_source_line_or_quantity_is_a_validation_error_not_an_NPE() {
        assertThatThrownBy(() -> mapper.toCommand(request(uuid(1).toString(),
                new ReturnLineRequest(null, BigDecimal.ONE))))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR));
        assertThatThrownBy(() -> mapper.toCommand(request(uuid(1).toString(),
                new ReturnLineRequest(1, null))))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR));
    }

    // ---------- document -> BillingReturnResponse ----------

    @Test
    void RET_9_return_draft_response_references_the_original_and_inherits_price_in_cents() {
        BillingDocument original = emittedSale();
        BillingDocument draft = withVersion(
                returnDraftOf(original, uuid(2), "producto defectuoso", returnLineOf(original, 1, "1")), 0L);

        BillingReturnResponse response = mapper.toReturnResponse(BillingDocumentView.of(draft));

        assertThat(response.id()).isEqualTo(uuid(2).toString());
        assertThat(response.number()).isNull();
        assertThat(response.status()).isEqualTo("BORRADOR");
        assertThat(response.kind()).isEqualTo("SALE");
        assertThat(response.documentType()).isEqualTo("SALE_RETURN");
        assertThat(response.invoiceId()).isEqualTo(uuid(1).toString());
        assertThat(response.originalNumber()).isEqualTo("FV-000007");
        assertThat(response.originalDate()).isEqualTo(TODAY);
        assertThat(response.reason()).isEqualTo("producto defectuoso");
        assertThat(response.ordinal()).isNull();
        assertThat(response.clientId()).isEqualTo("10");
        assertThat(response.clientName()).isEqualTo("Cliente 10");
        assertThat(response.clientDocument()).isEqualTo("900110");
        assertThat(response.date()).isEqualTo(TODAY);
        assertThat(response.version()).isZero();
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).lineNo()).isEqualTo(1);
        assertThat(response.lines().get(0).sourceLineNo()).isEqualTo(1);
        assertThat(response.lines().get(0).productId()).isEqualTo("1");
        assertThat(response.lines().get(0).cantidadDevuelta()).isEqualByComparingTo("1");
        assertThat(response.lines().get(0).precioUnitarioCents()).isEqualTo(10000L);
        assertThat(response.lines().get(0).ivaPct()).isEqualByComparingTo("19");
        assertThat(response.lines().get(0).subtotalCents()).isEqualTo(10000L);
        assertThat(response.lines().get(0).ivaCents()).isEqualTo(1900L);
        assertThat(response.lines().get(0).totalCents()).isEqualTo(11900L);
        assertThat(response.totals().subtotalCents()).isEqualTo(10000L);
        assertThat(response.totals().ivaCents()).isEqualTo(1900L);
        assertThat(response.totals().totalCents()).isEqualTo(11900L);
    }

    @Test
    void RET_10_emitted_return_response_has_number_ordinal_and_emission_data() {
        BillingDocument original = emittedSale();
        BillingDocument emitted = withVersion(
                emittedReturnOf(original, uuid(2), 3L, returnLineOf(original, 1, "1")), 1L);

        BillingReturnResponse response = mapper.toReturnResponse(new BillingDocumentView(emitted, null, 2));

        assertThat(response.number()).isEqualTo("NC-000003");
        assertThat(response.status()).isEqualTo("EMITIDA");
        assertThat(response.ordinal()).isEqualTo(2);
        assertThat(response.emittedAt()).isEqualTo(emitted.emittedAt());
        assertThat(response.annulledAt()).isNull();
        assertThat(response.annulReason()).isNull();
    }

    @Test
    void PUR_return_response_of_a_purchase_return_has_dc_number_and_purchase_kind() {
        BillingDocument original = emittedPurchase();
        BillingDocument emitted = emittedReturnOf(original, uuid(2), 4L, returnLineOf(original, 1, "2"));

        BillingReturnResponse response = mapper.toReturnResponse(BillingDocumentView.of(emitted));

        assertThat(response.number()).isEqualTo("DC-000004");
        assertThat(response.kind()).isEqualTo("PURCHASE");
        assertThat(response.documentType()).isEqualTo("PURCHASE_RETURN");
        assertThat(response.originalNumber()).isEqualTo("FC-000007");
        assertThat(response.clientId()).isEqualTo("20");
    }

    @Test
    void RET_7_annulled_return_response_keeps_number_and_reason() {
        BillingDocument original = emittedSale();
        BillingDocument doc = emittedReturnOf(original, uuid(2), 3L, returnLineOf(original, 1, "1"));
        doc.annul("cliente desistio", false, doc.emittedAt().plusSeconds(60), "approver");

        BillingReturnResponse response = mapper.toReturnResponse(new BillingDocumentView(doc, null, 1));

        assertThat(response.status()).isEqualTo("ANULADA");
        assertThat(response.number()).isEqualTo("NC-000003");
        assertThat(response.annulReason()).isEqualTo("cliente desistio");
        assertThat(response.annulledAt()).isNotNull();
        assertThat(response.ordinal()).isEqualTo(1);
    }

    // ---------- summaries ----------

    @Test
    void RET_10_return_summary_response_carries_ordinal_and_original_reference() {
        BillingDocument original = emittedSale();
        BillingDocument emitted = emittedReturnOf(original, uuid(2), 3L, returnLineOf(original, 1, "1"));

        ReturnSummaryResponse response = mapper.toReturnSummaryResponse(
                new ReturnSummary(BillingDocumentSummary.from(emitted), 2));

        assertThat(response.id()).isEqualTo(uuid(2).toString());
        assertThat(response.number()).isEqualTo("NC-000003");
        assertThat(response.status()).isEqualTo("EMITIDA");
        assertThat(response.kind()).isEqualTo("SALE");
        assertThat(response.documentType()).isEqualTo("SALE_RETURN");
        assertThat(response.invoiceId()).isEqualTo(uuid(1).toString());
        assertThat(response.originalNumber()).isEqualTo("FV-000007");
        assertThat(response.ordinal()).isEqualTo(2);
        assertThat(response.clientName()).isEqualTo("Cliente 10");
        assertThat(response.totals().totalCents()).isEqualTo(11900L);
        assertThat(response.emittedAt()).isNotNull();
    }

    @Test
    void RET_10_draft_summary_has_null_ordinal_and_null_number() {
        BillingDocument original = emittedSale();
        BillingDocument draft = returnDraftOf(original, uuid(2), null, returnLineOf(original, 1, "1"));

        ReturnSummaryResponse response = mapper.toReturnSummaryResponse(BillingDocumentSummary.from(draft), null);

        assertThat(response.number()).isNull();
        assertThat(response.ordinal()).isNull();
        assertThat(response.status()).isEqualTo("BORRADOR");
    }

    // ---------- availability ----------

    @Test
    void RET_9_availability_response_has_sold_returned_available_state_and_cents_price() {
        BillingDocument original = emittedSale(); // linea 1: 2 unidades, 100.00 + 19%
        BillingDocument emitted = emittedReturnOf(original, uuid(2), 3L, returnLineOf(original, 1, "1"));
        ReturnAvailability availability = new ReturnAvailabilityCalculator()
                .availability(original, List.of(emitted));

        ReturnAvailabilityResponse response = mapper.toAvailabilityResponse(availability);

        assertThat(response.invoiceId()).isEqualTo(uuid(1).toString());
        assertThat(response.state()).isEqualTo("PARCIAL");
        assertThat(response.returnable()).isTrue();
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).lineNo()).isEqualTo(1);
        assertThat(response.lines().get(0).productId()).isEqualTo("1");
        assertThat(response.lines().get(0).productName()).isEqualTo("Producto 1");
        assertThat(response.lines().get(0).sold()).isEqualByComparingTo("2");
        assertThat(response.lines().get(0).returned()).isEqualByComparingTo("1");
        assertThat(response.lines().get(0).available()).isEqualByComparingTo("1");
        assertThat(response.lines().get(0).precioUnitarioCents()).isEqualTo(10000L);
        assertThat(response.lines().get(0).ivaPct()).isEqualByComparingTo("19");
    }

    @Test
    void RET_9_availability_of_a_fully_returned_invoice_is_total_and_not_returnable() {
        BillingDocument original = emittedSale();
        BillingDocument emitted = emittedReturnOf(original, uuid(2), 3L, returnLineOf(original, 1, "2"));
        ReturnAvailability availability = new ReturnAvailabilityCalculator()
                .availability(original, List.of(emitted));

        ReturnAvailabilityResponse response = mapper.toAvailabilityResponse(availability);

        assertThat(response.state()).isEqualTo("TOTAL");
        assertThat(response.returnable()).isFalse();
        assertThat(response.lines().get(0).available()).isEqualByComparingTo("0");
    }

    // ---------- facturas con devolucion ----------

    @Test
    void RET_11_with_returns_response_maps_invoice_data_counts_and_derived_state() {
        BillingDocument invoice = emittedSale();

        InvoiceWithReturnsResponse response = mapper.toWithReturnsResponse(
                new InvoiceWithReturnsSummary(BillingDocumentSummary.from(invoice), 2, money("119.00"),
                        ReturnStatus.PARCIAL));

        assertThat(response.invoiceId()).isEqualTo(uuid(1).toString());
        assertThat(response.invoiceNumber()).isEqualTo("FV-000007");
        assertThat(response.kind()).isEqualTo("SALE");
        assertThat(response.clientName()).isEqualTo("Cliente 10");
        assertThat(response.supplierInvoiceNumber()).isNull();
        assertThat(response.invoiceDate()).isEqualTo(TODAY);
        assertThat(response.invoiceTotalCents()).isEqualTo(23800L);
        assertThat(response.returnsCount()).isEqualTo(2);
        assertThat(response.returnedTotalCents()).isEqualTo(11900L);
        assertThat(response.state()).isEqualTo("PARCIAL");
    }

    @Test
    void RET_11_with_returns_response_of_a_purchase_invoice_carries_the_supplier_invoice_number() {
        BillingDocument invoice = emittedPurchase();

        InvoiceWithReturnsResponse response = mapper.toWithReturnsResponse(
                new InvoiceWithReturnsSummary(BillingDocumentSummary.from(invoice), 1, money("238.00"),
                        ReturnStatus.TOTAL));

        assertThat(response.invoiceNumber()).isEqualTo("FC-000007");
        assertThat(response.kind()).isEqualTo("PURCHASE");
        assertThat(response.supplierInvoiceNumber()).isEqualTo("F-77");
        assertThat(response.state()).isEqualTo("TOTAL");
        assertThat(response.returnedTotalCents()).isEqualTo(23800L);
    }
}
