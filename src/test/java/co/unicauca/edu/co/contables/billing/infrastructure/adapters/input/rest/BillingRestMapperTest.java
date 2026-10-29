package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readyPurchaseDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingDocumentSummaryResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingInvoiceLineResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingInvoiceResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.InvoiceDraftRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.InvoiceLineRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.mapper.BillingCents;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.mapper.BillingRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** DOC-4 / DOC-6: conversion exacta centavos <-> pesos y mapeo sin confiar en valores del cliente. */
class BillingRestMapperTest {

    private final BillingRestMapper mapper = new BillingRestMapper();

    // ---------- BillingCents (DOC-6) ----------

    @Test
    void DOC_6_cents_in_is_exact_two_decimals() {
        assertThat(BillingCents.pesos(1234L)).isEqualByComparingTo("12.34");
        assertThat(BillingCents.pesos(1234L).scale()).isEqualTo(2);
        assertThat(BillingCents.pesos(5L)).isEqualByComparingTo("0.05");
        assertThat(BillingCents.pesos(0L)).isEqualByComparingTo("0.00");
    }

    @Test
    void DOC_4_null_cents_means_no_override() {
        assertThat(BillingCents.pesos(null)).isNull();
    }

    @Test
    void DOC_6_cents_out_is_exact_long() {
        assertThat(BillingCents.cents(money("12.34"))).isEqualTo(1234L);
        assertThat(BillingCents.cents(money("0.00"))).isZero();
        assertThat(BillingCents.cents(Money.ofCents(9_007_199_254_740_993L))).isEqualTo(9_007_199_254_740_993L);
    }

    @Test
    void DOC_6_round_trip_cents_pesos_cents() {
        for (long cents : new long[] {0L, 1L, 99L, 100L, 123_456_789L}) {
            assertThat(BillingCents.cents(Money.of(BillingCents.pesos(cents)))).isEqualTo(cents);
        }
    }

    // ---------- request -> command ----------

    @Test
    void DOC_4_request_to_command_converts_ids_and_cents_to_pesos() {
        InvoiceDraftRequest request = new InvoiceDraftRequest("10", LocalDate.of(2026, 2, 1), null,
                List.of(new InvoiceLineRequest("7", new BigDecimal("2.5"), 15000L, new BigDecimal("5"))), 3L);

        InvoiceDraftCommand command = mapper.toCommand(request);

        assertThat(command.counterpartyId()).isEqualTo(10L);
        assertThat(command.issueDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(command.expectedVersion()).isEqualTo(3L);
        assertThat(command.lines()).hasSize(1);
        InvoiceLineCommand line = command.lines().get(0);
        assertThat(line.productId()).isEqualTo(7L);
        assertThat(line.quantity()).isEqualByComparingTo("2.5");
        assertThat(line.unitPriceOverride()).isEqualByComparingTo("150.00");
        assertThat(line.taxRateOverride()).isEqualByComparingTo("5");
    }

    @Test
    void RN_11_null_price_and_tax_mean_follow_catalog() {
        InvoiceDraftRequest request = new InvoiceDraftRequest("10", null, null,
                List.of(new InvoiceLineRequest("7", BigDecimal.ONE, null, null)), null);

        InvoiceLineCommand line = mapper.toCommand(request).lines().get(0);

        assertThat(line.unitPriceOverride()).isNull();
        assertThat(line.taxRateOverride()).isNull();
    }

    @Test
    void DRF_1_blank_or_missing_client_means_no_counterparty() {
        assertThat(mapper.toCommand(new InvoiceDraftRequest(null, null, null, List.of(), null)).counterpartyId())
                .isNull();
        assertThat(mapper.toCommand(new InvoiceDraftRequest("  ", null, null, List.of(), null)).counterpartyId())
                .isNull();
    }

    @Test
    void DRF_1_null_lines_become_empty_list() {
        assertThat(mapper.toCommand(new InvoiceDraftRequest("10", null, null, null, null)).lines()).isEmpty();
    }

    @Test
    void PUR_2_supplier_invoice_number_is_passed_through() {
        InvoiceDraftCommand command = mapper.toCommand(
                new InvoiceDraftRequest("20", null, SUPPLIER_INVOICE_NUMBER, List.of(), null));

        assertThat(command.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
    }

    @Test
    void VALIDATION_non_numeric_client_id_is_validation_error() {
        assertThatThrownBy(() -> mapper.toCommand(new InvoiceDraftRequest("abc", null, null, List.of(), null)))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
    }

    @Test
    void VALIDATION_non_numeric_product_id_is_validation_error() {
        InvoiceDraftRequest request = new InvoiceDraftRequest("10", null, null,
                List.of(new InvoiceLineRequest("xyz", BigDecimal.ONE, null, null)), null);

        assertThatThrownBy(() -> mapper.toCommand(request))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_VALIDATION_ERROR);
    }

    @Test
    void DOC_4_client_totals_and_catalog_values_are_ignored_when_deserializing() throws Exception {
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        String body = """
                {"clientId":"10","date":"2026-01-15","totalCents":1,"subtotalCents":1,
                 "totals":{"totalCents":1},
                 "lines":[{"productId":"1","cantidad":2,"productName":"hack","precioCatalogoCents":1,
                           "ivaCatalogoPct":0,"subtotalCents":1,"ivaCents":1,"totalCents":1}]}
                """;

        InvoiceDraftRequest request = json.readValue(body, InvoiceDraftRequest.class);
        InvoiceDraftCommand command = mapper.toCommand(request);

        assertThat(command.lines()).hasSize(1);
        assertThat(command.lines().get(0).unitPriceOverride()).isNull();
        assertThat(command.lines().get(0).taxRateOverride()).isNull();
        assertThat(command.lines().get(0).quantity()).isEqualByComparingTo("2");
    }

    // ---------- document -> response ----------

    @Test
    void DOC_6_draft_response_exposes_cents_and_catalog_references() {
        BillingDocument doc = withVersion(readySaleDraft(), 4L);

        BillingInvoiceResponse response = mapper.toResponse(BillingDocumentView.of(doc));

        assertThat(response.id()).isEqualTo(doc.id().toString());
        assertThat(response.number()).isNull();
        assertThat(response.status()).isEqualTo("BORRADOR");
        assertThat(response.kind()).isEqualTo("SALE");
        assertThat(response.documentType()).isEqualTo("SALE_INVOICE");
        assertThat(response.clientId()).isEqualTo("10");
        assertThat(response.clientName()).isEqualTo(customer().name());
        assertThat(response.clientDocument()).isEqualTo(customer().idNumber());
        assertThat(response.supplierInvoiceNumber()).isNull();
        assertThat(response.date()).isEqualTo(TODAY);
        assertThat(response.version()).isEqualTo(4L);
        assertThat(response.returnStatus()).isNull();
        assertThat(response.totals().subtotalCents()).isEqualTo(20000L);
        assertThat(response.totals().ivaCents()).isEqualTo(3800L);
        assertThat(response.totals().totalCents()).isEqualTo(23800L);

        assertThat(response.lines()).hasSize(1);
        BillingInvoiceLineResponse line = response.lines().get(0);
        assertThat(line.lineNo()).isEqualTo(1);
        assertThat(line.productId()).isEqualTo("1");
        assertThat(line.productCode()).isEqualTo("P1");
        assertThat(line.unitOfMeasure()).isEqualTo("Unidad");
        assertThat(line.cantidad()).isEqualByComparingTo("2");
        assertThat(line.precioCatalogoCents()).isEqualTo(10000L);
        assertThat(line.ivaCatalogoPct()).isEqualByComparingTo("19");
        assertThat(line.precioUnitarioCents()).isEqualTo(10000L);
        assertThat(line.ivaPct()).isEqualByComparingTo("19");
        assertThat(line.precioEditado()).isFalse();
        assertThat(line.ivaEditado()).isFalse();
        assertThat(line.subtotalCents()).isEqualTo(20000L);
        assertThat(line.ivaCents()).isEqualTo(3800L);
        assertThat(line.totalCents()).isEqualTo(23800L);
    }

    @Test
    void DOC_5_rounding_example_3x333_is_999_190_1189() {
        BillingDocument doc = BillingDocument.newInvoiceDraft(co.unicauca.edu.co.contables.billing.support
                .BillingTestData.uuid(9), co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID,
                DocumentType.SALE_INVOICE, TODAY, NOW, USER);
        doc.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(lineOf(1, product(1L, "3.33", "19"), "3")), NOW, USER);

        BillingInvoiceResponse response = mapper.toResponse(BillingDocumentView.of(doc));

        assertThat(response.lines().get(0).subtotalCents()).isEqualTo(999L);
        assertThat(response.lines().get(0).ivaCents()).isEqualTo(190L);
        assertThat(response.lines().get(0).totalCents()).isEqualTo(1189L);
        assertThat(response.totals().totalCents()).isEqualTo(1189L);
    }

    @Test
    void RN_10_overridden_line_flags_edited_and_keeps_catalog_reference() {
        BillingDocument doc = readySaleDraft();
        DocumentLine overridden = DocumentLine.forInvoice(1, product(1L, "100.00", "19"), qty("1"),
                money("150.00"), rate("5"));
        doc.replaceInvoiceDraft(customer(), TODAY, null, List.of(overridden), NOW, USER);

        BillingInvoiceLineResponse line = mapper.toResponse(BillingDocumentView.of(doc)).lines().get(0);

        assertThat(line.precioEditado()).isTrue();
        assertThat(line.ivaEditado()).isTrue();
        assertThat(line.precioUnitarioCents()).isEqualTo(15000L);
        assertThat(line.precioCatalogoCents()).isEqualTo(10000L);
        assertThat(line.ivaPct()).isEqualByComparingTo("5");
        assertThat(line.ivaCatalogoPct()).isEqualByComparingTo("19");
    }

    @Test
    void EMT_3_emitted_response_exposes_display_number_and_audit() {
        BillingDocument doc = emittedSale();

        BillingInvoiceResponse response = mapper.toResponse(BillingDocumentView.of(doc));

        assertThat(response.number()).isEqualTo("FV-000007");
        assertThat(response.status()).isEqualTo("EMITIDA");
        assertThat(response.emittedAt()).isEqualTo(doc.emittedAt());
        assertThat(response.annulledAt()).isNull();
        assertThat(response.annulReason()).isNull();
    }

    @Test
    void PUR_2_purchase_response_echoes_supplier_invoice_number() {
        BillingInvoiceResponse response = mapper.toResponse(BillingDocumentView.of(readyPurchaseDraft()));

        assertThat(response.kind()).isEqualTo("PURCHASE");
        assertThat(response.documentType()).isEqualTo("PURCHASE_INVOICE");
        assertThat(response.supplierInvoiceNumber()).isEqualTo(SUPPLIER_INVOICE_NUMBER);
    }

    @Test
    void DRF_1_empty_draft_response_has_no_client_and_zero_totals() {
        BillingDocument doc = co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale();

        BillingInvoiceResponse response = mapper.toResponse(BillingDocumentView.of(doc));

        assertThat(response.clientId()).isNull();
        assertThat(response.clientName()).isNull();
        assertThat(response.clientDocument()).isNull();
        assertThat(response.lines()).isEmpty();
        assertThat(response.totals().totalCents()).isZero();
        assertThat(response.version()).isNull();
    }

    @Test
    void RET_9_return_status_is_exposed_by_name_when_present() {
        BillingDocumentView view = new BillingDocumentView(emittedSale(), ReturnStatus.PARCIAL, null);

        assertThat(mapper.toResponse(view).returnStatus()).isEqualTo("PARCIAL");
    }

    @Test
    void QRY_1_summary_response_maps_header_and_totals_without_lines() {
        BillingDocument doc = withVersion(emittedSale(), 2L);

        BillingDocumentSummaryResponse response = mapper.toSummaryResponse(BillingDocumentSummary.from(doc));

        assertThat(response.id()).isEqualTo(doc.id().toString());
        assertThat(response.number()).isEqualTo("FV-000007");
        assertThat(response.status()).isEqualTo("EMITIDA");
        assertThat(response.kind()).isEqualTo("SALE");
        assertThat(response.documentType()).isEqualTo("SALE_INVOICE");
        assertThat(response.clientId()).isEqualTo("10");
        assertThat(response.clientName()).isEqualTo(customer().name());
        assertThat(response.date()).isEqualTo(TODAY);
        assertThat(response.totals().totalCents()).isEqualTo(23800L);
        assertThat(response.version()).isEqualTo(2L);
        assertThat(response.emittedAt()).isEqualTo(doc.emittedAt());
    }

    @Test
    void QRY_1_summary_response_of_empty_draft_has_null_client() {
        BillingDocumentSummaryResponse response = mapper.toSummaryResponse(
                BillingDocumentSummary.from(co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale()));

        assertThat(response.number()).isNull();
        assertThat(response.clientId()).isNull();
        assertThat(response.totals().totalCents()).isZero();
    }
}
