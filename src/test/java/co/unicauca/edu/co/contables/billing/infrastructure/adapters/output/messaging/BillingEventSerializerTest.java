package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.money;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.qty;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentAnnulled;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentEmitted;
import co.unicauca.edu.co.contables.billing.domain.event.ReturnEventContext;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocumentState;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Serializacion del sobre y la carga de eventos (EVT-3, design §8): esquema fijo, dinero en pesos
 * con escala 2, cantidades con escala 3, fechas ISO y bloques opcionales presentes como null. El
 * serializador es Java + Jackson, sin Spring (el dominio no se contamina).
 */
class BillingEventSerializerTest {

    private final BillingEventSerializer serializer = new BillingEventSerializer();
    private final ObjectMapper reader = new ObjectMapper();

    private JsonNode parse(String json) throws Exception {
        return reader.readTree(json);
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private BillingDocumentEmitted emittedEvent(BillingDocument doc) {
        return new BillingDocumentEmitted(uuid(77), NOW, doc, null);
    }

    /** Devolucion de venta EMITIDA (NC-000003) sobre la factura FV-000007, para el bloque return/original. */
    private static BillingDocument emittedSaleReturn() {
        BillingDocumentState draft = draftSaleReturnState("producto defectuoso");
        return BillingDocument.restore(new BillingDocumentState(draft.id(), draft.enterpriseId(), draft.type(),
                DocumentStatus.EMITIDA, DocumentNumber.of(DocumentType.SALE_RETURN, 3L), draft.issueDate(),
                draft.counterparty(), null, issuer(), draft.original(), draft.returnReason(), draft.lines(),
                draft.createdAt(), draft.createdBy(), LATER, OTHER_USER, LATER, OTHER_USER, null, null, null, 2L));
    }

    // ---------- sobre ----------

    @Test
    void EVT_3_envelope_has_exactly_the_agreed_fields() throws Exception {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        JsonNode root = parse(serializer.serialize(emittedEvent(doc), TENANT_A));

        assertThat(fieldNames(root)).containsExactly("eventId", "eventType", "eventVersion", "occurredAt",
                "tenantId", "enterpriseId", "correlationId", "sourceDocument", "payload");
        assertThat(root.get("eventId").asText()).isEqualTo(uuid(77).toString());
        assertThat(root.get("eventType").asText()).isEqualTo("FacturaVentaEmitida");
        assertThat(root.get("eventVersion").asInt()).isEqualTo(1);
        assertThat(root.get("occurredAt").asText()).isEqualTo("2026-01-15T15:00:00Z");
        assertThat(root.get("tenantId").asText()).isEqualTo(TENANT_A);
        assertThat(root.get("enterpriseId").asText()).isEqualTo(ENTERPRISE_ID);
        assertThat(root.get("correlationId").asText()).isEqualTo(doc.id().toString());
    }

    @Test
    void EVT_3_source_document_carries_type_id_and_display_number() throws Exception {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        JsonNode source = parse(serializer.serialize(emittedEvent(doc), TENANT_A)).get("sourceDocument");

        assertThat(fieldNames(source)).containsExactly("type", "id", "number");
        assertThat(source.get("type").asText()).isEqualTo("SALE_INVOICE");
        assertThat(source.get("id").asText()).isEqualTo(doc.id().toString());
        assertThat(source.get("number").asText()).isEqualTo("FV-000007");
    }

    @Test
    void EVT_3_payload_has_exactly_the_agreed_fields() throws Exception {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        JsonNode payload = parse(serializer.serialize(emittedEvent(doc), TENANT_A)).get("payload");

        assertThat(fieldNames(payload)).containsExactly("documentId", "documentVersion", "documentType", "kind",
                "number", "issueDate", "emittedAt", "emittedBy", "currency", "issuer", "counterparty",
                "supplierInvoiceNumber", "lines", "totals", "payment", "original", "return", "annulment");
    }

    // ---------- carga ----------

    @Test
    void EVT_3_payload_header_issuer_and_counterparty_snapshots() throws Exception {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        JsonNode payload = parse(serializer.serialize(emittedEvent(doc), TENANT_A)).get("payload");

        assertThat(payload.get("documentId").asText()).isEqualTo(doc.id().toString());
        assertThat(payload.get("documentVersion").asLong()).isEqualTo(1L);
        assertThat(payload.get("documentType").asText()).isEqualTo("SALE_INVOICE");
        assertThat(payload.get("kind").asText()).isEqualTo("SALE");
        assertThat(payload.get("number").asText()).isEqualTo("FV-000007");
        assertThat(payload.get("issueDate").asText()).isEqualTo("2026-01-15");
        assertThat(payload.get("emittedAt").asText()).isEqualTo("2026-01-15T16:30:00Z");
        assertThat(payload.get("emittedBy").asText()).isEqualTo(OTHER_USER);
        assertThat(payload.get("currency").asText()).isEqualTo("COP");

        JsonNode issuer = payload.get("issuer");
        assertThat(fieldNames(issuer)).containsExactly("enterpriseId", "name", "nit", "dv");
        assertThat(issuer.get("enterpriseId").asText()).isEqualTo(ENTERPRISE_ID);
        assertThat(issuer.get("name").asText()).isEqualTo("Empresa Emisora SAS");
        assertThat(issuer.get("nit").asText()).isEqualTo("900123456");
        assertThat(issuer.get("dv").asText()).isEqualTo("1");

        JsonNode counterparty = payload.get("counterparty");
        assertThat(fieldNames(counterparty)).containsExactly("role", "thirdId", "name", "idType", "idNumber",
                "verificationNumber", "address", "email", "phone");
        assertThat(counterparty.get("role").asText()).isEqualTo("CUSTOMER");
        assertThat(counterparty.get("thirdId").asLong()).isEqualTo(10L);
        assertThat(counterparty.get("name").asText()).isEqualTo("Cliente 10");
        assertThat(counterparty.get("idType").asText()).isEqualTo("NIT");
        assertThat(counterparty.get("idNumber").asText()).isEqualTo("900110");
        assertThat(counterparty.get("verificationNumber").asText()).isEqualTo("7");
        assertThat(counterparty.get("email").asText()).isEqualTo("cliente@example.com");
    }

    @Test
    void EVT_3_line_fields_and_totals_for_a_catalog_priced_line() throws Exception {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        JsonNode payload = parse(serializer.serialize(emittedEvent(doc), TENANT_A)).get("payload");

        JsonNode line = payload.get("lines").get(0);
        assertThat(fieldNames(line)).containsExactly("lineNo", "sourceLineNo", "productId", "productCode",
                "productName", "unitOfMeasureId", "unitOfMeasureName", "quantity", "catalogUnitPrice",
                "unitPrice", "taxRate", "subtotal", "taxAmount", "total");
        assertThat(line.get("lineNo").asInt()).isEqualTo(1);
        assertThat(line.get("sourceLineNo").isNull()).isTrue();
        assertThat(line.get("productId").asLong()).isEqualTo(1L);
        assertThat(line.get("productCode").asText()).isEqualTo("P1");
        assertThat(line.get("productName").asText()).isEqualTo("Producto 1");
        assertThat(line.get("unitOfMeasureId").asLong()).isEqualTo(1L);
        assertThat(line.get("unitOfMeasureName").asText()).isEqualTo("Unidad");

        JsonNode totals = payload.get("totals");
        assertThat(fieldNames(totals)).containsExactly("subtotal", "tax", "total");
        assertThat(totals.get("subtotal").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(totals.get("tax").decimalValue()).isEqualByComparingTo("38.00");
        assertThat(totals.get("total").decimalValue()).isEqualByComparingTo("238.00");
    }

    @Test
    void EVT_3_amounts_are_json_decimals_in_pesos_with_scale_2_and_quantity_with_scale_3() {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        String json = serializer.serialize(emittedEvent(doc), TENANT_A);

        assertThat(json)
                .contains("\"quantity\":2.000")
                .contains("\"catalogUnitPrice\":100.00")
                .contains("\"unitPrice\":100.00")
                .contains("\"taxRate\":19.00")
                .contains("\"subtotal\":200.00")
                .contains("\"taxAmount\":38.00")
                .contains("\"tax\":38.00")
                .contains("\"total\":238.00");
        assertThat(json).doesNotContain("Cents").doesNotContain("20000");
    }

    @Test
    void EVT_3_overridden_price_and_rate_and_fractional_quantity_keep_their_scale_and_rounding() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(customer(), TODAY, null,
                List.of(DocumentLine.forInvoice(1, product(1L), qty("1.5"), money("10.10"), rate("5"))), NOW, USER);
        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 9L), issuer(), null, LATER, OTHER_USER);

        String json = serializer.serialize(emittedEvent(withVersion(doc, 1L)), TENANT_A);

        assertThat(json)
                .contains("\"quantity\":1.500")
                .contains("\"catalogUnitPrice\":100.00")
                .contains("\"unitPrice\":10.10")
                .contains("\"taxRate\":5.00")
                .contains("\"subtotal\":15.15")
                .contains("\"taxAmount\":0.76")
                .contains("\"total\":15.91");
    }

    @Test
    void EVT_3_optional_blocks_are_present_as_null_for_an_emitted_invoice() throws Exception {
        BillingDocument doc = withVersion(emittedSale(), 1L);

        JsonNode payload = parse(serializer.serialize(emittedEvent(doc), TENANT_A)).get("payload");

        assertThat(payload.has("payment")).isTrue();
        assertThat(payload.get("payment").isNull()).isTrue();
        assertThat(payload.get("original").isNull()).isTrue();
        assertThat(payload.get("return").isNull()).isTrue();
        assertThat(payload.get("annulment").isNull()).isTrue();
        assertThat(payload.get("supplierInvoiceNumber").isNull()).isTrue();
    }

    @Test
    void PUR_2_purchase_event_carries_supplier_invoice_number_and_supplier_role() throws Exception {
        BillingDocument doc = withVersion(emittedPurchase(), 1L);

        JsonNode root = parse(serializer.serialize(emittedEvent(doc), TENANT_A));

        assertThat(root.get("eventType").asText()).isEqualTo("FacturaCompraEmitida");
        assertThat(root.get("sourceDocument").get("type").asText()).isEqualTo("PURCHASE_INVOICE");
        assertThat(root.get("sourceDocument").get("number").asText()).isEqualTo("FC-000007");
        JsonNode payload = root.get("payload");
        assertThat(payload.get("kind").asText()).isEqualTo("PURCHASE");
        assertThat(payload.get("supplierInvoiceNumber").asText()).isEqualTo("F-77");
        assertThat(payload.get("counterparty").get("role").asText()).isEqualTo("SUPPLIER");
    }

    // ---------- anulacion ----------

    @Test
    void EVT_3_annulled_event_adds_the_annulment_block_and_keeps_the_number() throws Exception {
        BillingDocument doc = withVersion(annulledSale(), 2L);
        BillingDocumentAnnulled event = new BillingDocumentAnnulled(uuid(78), LATER.plusSeconds(60), doc, null);

        JsonNode root = parse(serializer.serialize(event, TENANT_A));

        assertThat(root.get("eventType").asText()).isEqualTo("FacturaVentaAnulada");
        assertThat(root.get("occurredAt").asText()).isEqualTo("2026-01-15T16:31:00Z");
        JsonNode payload = root.get("payload");
        assertThat(payload.get("number").asText()).isEqualTo("FV-000007");
        assertThat(payload.get("documentVersion").asLong()).isEqualTo(2L);
        JsonNode annulment = payload.get("annulment");
        assertThat(fieldNames(annulment)).containsExactly("annulledAt", "annulledBy", "reason");
        assertThat(annulment.get("annulledAt").asText()).isEqualTo("2026-01-15T16:31:00Z");
        assertThat(annulment.get("annulledBy").asText()).isEqualTo(OTHER_USER);
        assertThat(annulment.get("reason").asText()).isEqualTo("error de digitacion");
    }

    // ---------- devolucion ----------

    @Test
    void EVT_3_return_event_adds_original_return_block_and_source_line_numbers() throws Exception {
        BillingDocument doc = emittedSaleReturn();
        BillingDocumentEmitted event = new BillingDocumentEmitted(uuid(79), LATER, doc,
                new ReturnEventContext(2, ReturnStatus.PARCIAL));

        JsonNode root = parse(serializer.serialize(event, TENANT_A));

        assertThat(root.get("eventType").asText()).isEqualTo("FacturaVentaDevuelta");
        assertThat(root.get("sourceDocument").get("type").asText()).isEqualTo("SALE_RETURN");
        assertThat(root.get("sourceDocument").get("number").asText()).isEqualTo("NC-000003");
        JsonNode payload = root.get("payload");
        assertThat(payload.get("lines").get(0).get("sourceLineNo").asInt()).isEqualTo(1);

        JsonNode original = payload.get("original");
        assertThat(fieldNames(original)).containsExactly("documentId", "documentType", "number", "issueDate");
        assertThat(original.get("documentId").asText()).isEqualTo(uuid(1).toString());
        assertThat(original.get("documentType").asText()).isEqualTo("SALE_INVOICE");
        assertThat(original.get("number").asText()).isEqualTo("FV-000007");
        assertThat(original.get("issueDate").asText()).isEqualTo("2026-01-15");

        JsonNode returnBlock = payload.get("return");
        assertThat(fieldNames(returnBlock)).containsExactly("reason", "ordinal", "originalReturnStatus");
        assertThat(returnBlock.get("reason").asText()).isEqualTo("producto defectuoso");
        assertThat(returnBlock.get("ordinal").asInt()).isEqualTo(2);
        assertThat(returnBlock.get("originalReturnStatus").asText()).isEqualTo("PARCIAL");
    }

    @Test
    void EVT_3_return_event_without_context_keeps_reason_and_nulls_ordinal_and_status() throws Exception {
        BillingDocumentEmitted event = new BillingDocumentEmitted(uuid(79), LATER, emittedSaleReturn(), null);

        JsonNode returnBlock = parse(serializer.serialize(event, TENANT_A)).get("payload").get("return");

        assertThat(returnBlock.get("reason").asText()).isEqualTo("producto defectuoso");
        assertThat(returnBlock.get("ordinal").isNull()).isTrue();
        assertThat(returnBlock.get("originalReturnStatus").isNull()).isTrue();
    }

    // ---------- robustez ----------

    @Test
    void same_event_serializes_to_the_same_json() {
        BillingDocumentEmitted event = emittedEvent(withVersion(emittedSale(), 1L));

        assertThat(serializer.serialize(event, TENANT_A)).isEqualTo(serializer.serialize(event, TENANT_A));
    }

    @Test
    void blank_or_null_tenant_is_rejected() {
        BillingDocumentEmitted event = emittedEvent(withVersion(emittedSale(), 1L));

        assertThatThrownBy(() -> serializer.serialize(event, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> serializer.serialize(event, "  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_event_is_rejected() {
        assertThatThrownBy(() -> serializer.serialize(null, TENANT_A)).isInstanceOf(NullPointerException.class);
    }
}
