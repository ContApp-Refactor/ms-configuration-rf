package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.LATER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.OTHER_USER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TODAY;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.methodTerms;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.readySaleDraft;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.withVersion;
import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentEmitted;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** El bloque {@code payment} del evento de emision: tipado en ventas, null en los demas tipos. */
class BillingEventSerializerPaymentTest {

    private final BillingEventSerializer serializer = new BillingEventSerializer();
    private final ObjectMapper reader = new ObjectMapper();

    private JsonNode payloadOf(BillingDocument doc) throws Exception {
        String json = serializer.serialize(new BillingDocumentEmitted(uuid(77), NOW, withVersion(doc, 1L), null),
                TENANT_A);
        return reader.readTree(json).get("payload");
    }

    private static BillingDocument emittedSaleWith(PaymentTerms payment, String methodName) {
        BillingDocument doc = readySaleDraft();
        doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(), payment, NOW, "tester");
        if (methodName != null) {
            doc.freezePaymentMethodName(methodName);
        }
        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 7L), issuer(), null, LATER, OTHER_USER);
        return doc;
    }

    private static List<String> names(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    @Test
    void a_sale_publishes_exactly_the_two_payment_fields() throws Exception {
        JsonNode payment = payloadOf(emittedSaleWith(PaymentTerms.draft(7L), "Efectivo")).get("payment");

        assertThat(names(payment)).containsExactly("paymentMethodId", "paymentMethodName");
        assertThat(payment.get("paymentMethodId").asLong()).isEqualTo(7L);
        assertThat(payment.get("paymentMethodName").asText()).isEqualTo("Efectivo");
    }

    @Test
    void a_sale_payment_has_no_bank_fields_and_stays_event_version_1() throws Exception {
        BillingDocument doc = readySaleDraft();
        doc.replaceInvoiceDraft(doc.counterparty(), TODAY, null, doc.lines(),
                PaymentTerms.draft(7L),
                NOW, "tester");
        doc.freezePayment("Transferencia bancaria");
        doc.emit(DocumentNumber.of(DocumentType.SALE_INVOICE, 7L), issuer(), null, LATER, OTHER_USER);

        JsonNode root = reader.readTree(serializer.serialize(
                new BillingDocumentEmitted(uuid(77), NOW, withVersion(doc, 1L), null), TENANT_A));
        JsonNode payment = root.get("payload").get("payment");

        assertThat(payment.has("bankAccountId")).isFalse();
        assertThat(payment.has("bankAccountName")).isFalse();
        assertThat(payment.get("paymentMethodName").asText()).isEqualTo("Transferencia bancaria");
        assertThat(root.get("eventVersion").asInt()).isEqualTo(1);
    }

    @Test
    void a_purchase_invoice_publishes_the_two_payment_fields() throws Exception {
        BillingDocument doc = emittedPurchase();
        JsonNode payment = payloadOf(doc).get("payment");

        assertThat(names(payment)).containsExactly("paymentMethodId", "paymentMethodName");
        assertThat(payment.get("paymentMethodId").asLong()).isEqualTo(1L);
    }

    @Test
    void a_return_keeps_payment_null() throws Exception {
        BillingDocument original = emittedPurchase();
        JsonNode payload = payloadOf(co.unicauca.edu.co.contables.billing.support.BillingTestData
                .emittedReturnOf(original, uuid(5), 1L,
                        co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf(original, 1, "1")));

        assertThat(payload.has("payment")).isTrue();
        assertThat(payload.get("payment").isNull()).isTrue();
    }

    @Test
    void a_sale_without_payment_data_keeps_payment_null() throws Exception {
        assertThat(payloadOf(draftSale()).get("payment").isNull()).isTrue();
    }
}
