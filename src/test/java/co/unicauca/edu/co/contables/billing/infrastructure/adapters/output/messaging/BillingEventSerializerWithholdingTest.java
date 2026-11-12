package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.rate;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentEmitted;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.Withholding;
import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Retenciones en el evento de emision: aditivas (eventVersion sigue en 1), snapshot congelado, total
 * neto y lista vacia cuando no hay.
 */
class BillingEventSerializerWithholdingTest {

    private final BillingEventSerializer serializer = new BillingEventSerializer();
    private final ObjectMapper reader = new ObjectMapper();

    private JsonNode rootOf(BillingDocument doc) throws Exception {
        return reader.readTree(serializer.serialize(
                new BillingDocumentEmitted(uuid(77), NOW, withVersion(doc, 1L), null), TENANT_A));
    }

    private static BillingDocument emittedExample() {
        BillingDocument doc = draftSale();
        doc.replaceInvoiceDraft(readySaleDraft().counterparty(), TODAY, null,
                List.of(lineOf(1, product(1L, "10000.00", "15"), "2")), methodTerms(1L), List.of(
                        new Withholding(WithholdingCode.RETE_ICA, 3L, "c", rate("20")),
                        new Withholding(WithholdingCode.RETE_FUENTE, 2L, "b", rate("10"))), NOW, "tester");
        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 7L), issuer(), null, LATER, OTHER_USER);
        return doc;
    }

    private static List<String> names(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    @Test
    void the_event_publishes_each_withholding_with_its_frozen_snapshot() throws Exception {
        JsonNode root = rootOf(emittedExample());
        JsonNode withholdings = root.get("payload").get("withholdings");

        assertThat(withholdings).hasSize(2);
        assertThat(names(withholdings.get(0))).containsExactly("code", "taxId", "taxName", "ratePct", "amount");
        assertThat(withholdings.get(0).get("code").asText()).isEqualTo("RETE_ICA");
        assertThat(withholdings.get(0).get("taxId").asLong()).isEqualTo(3L);
        assertThat(withholdings.get(0).get("taxName").asText()).isEqualTo("c");
        assertThat(withholdings.get(0).get("ratePct").decimalValue()).isEqualByComparingTo("20.00");
        assertThat(withholdings.get(0).get("amount").decimalValue()).isEqualByComparingTo("4000.00");
        assertThat(withholdings.get(1).get("code").asText()).isEqualTo("RETE_FUENTE");
        assertThat(withholdings.get(1).get("amount").decimalValue()).isEqualByComparingTo("2000.00");
    }

    @Test
    void the_totals_carry_the_withholdings_and_the_net_total() throws Exception {
        JsonNode totals = rootOf(emittedExample()).get("payload").get("totals");

        assertThat(totals.get("subtotal").decimalValue()).isEqualByComparingTo("20000.00");
        assertThat(totals.get("tax").decimalValue()).isEqualByComparingTo("3000.00");
        assertThat(totals.get("withholdings").decimalValue()).isEqualByComparingTo("6000.00");
        assertThat(totals.get("total").decimalValue()).isEqualByComparingTo("17000.00");
    }

    @Test
    void the_change_is_additive_and_stays_event_version_1() throws Exception {
        JsonNode root = rootOf(emittedExample());

        assertThat(root.get("eventVersion").asInt()).isEqualTo(1);
        assertThat(root.get("payload").has("payment")).isTrue();
        assertThat(root.get("payload").has("lines")).isTrue();
    }

    @Test
    void an_invoice_without_withholdings_publishes_an_empty_list_and_zero() throws Exception {
        JsonNode payload = rootOf(emittedPurchase()).get("payload");

        assertThat(payload.get("withholdings").isArray()).isTrue();
        assertThat(payload.get("withholdings")).isEmpty();
        assertThat(payload.get("totals").get("withholdings").decimalValue()).isEqualByComparingTo("0");
    }
}
