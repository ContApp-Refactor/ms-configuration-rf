package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.draftSaleReturnState;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedPurchase;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentAnnulled;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentEmitted;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.BillingEventCatalog.Action;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.BillingEventCatalog.Entry;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Catalogo de eventos (EVT-2, design §8): tipo de documento x accion -> eventType y routingKey. Son
 * 8 pares fijos; el exchange es el topic nuevo con prefijo {@code billing.} (EVT-5).
 */
class BillingEventCatalogTest {

    private static void assertEntry(DocumentType type, Action action, String eventType, String routingKey) {
        Entry entry = BillingEventCatalog.resolve(type, action);
        assertThat(entry.eventType()).as("eventType de %s/%s", type, action).isEqualTo(eventType);
        assertThat(entry.routingKey()).as("routingKey de %s/%s", type, action).isEqualTo(routingKey);
    }

    @Test
    void EVT_2_sale_invoice_pairs() {
        assertEntry(DocumentType.SALE_INVOICE, Action.EMITTED, "FacturaVentaEmitida", "sale-invoice.emitted.v1");
        assertEntry(DocumentType.SALE_INVOICE, Action.ANNULLED, "FacturaVentaAnulada", "sale-invoice.annulled.v1");
    }

    @Test
    void EVT_2_sale_return_pairs_emit_is_FacturaVentaDevuelta_and_annul_is_DevolucionVentaAnulada() {
        assertEntry(DocumentType.SALE_RETURN, Action.EMITTED, "FacturaVentaDevuelta", "sale-invoice.returned.v1");
        assertEntry(DocumentType.SALE_RETURN, Action.ANNULLED, "DevolucionVentaAnulada", "sale-return.annulled.v1");
    }

    @Test
    void EVT_2_purchase_invoice_pairs() {
        assertEntry(DocumentType.PURCHASE_INVOICE, Action.EMITTED, "FacturaCompraEmitida",
                "purchase-invoice.emitted.v1");
        assertEntry(DocumentType.PURCHASE_INVOICE, Action.ANNULLED, "FacturaCompraAnulada",
                "purchase-invoice.annulled.v1");
    }

    @Test
    void EVT_2_purchase_return_pairs_emit_is_FacturaCompraDevuelta_and_annul_is_DevolucionCompraAnulada() {
        assertEntry(DocumentType.PURCHASE_RETURN, Action.EMITTED, "FacturaCompraDevuelta",
                "purchase-invoice.returned.v1");
        assertEntry(DocumentType.PURCHASE_RETURN, Action.ANNULLED, "DevolucionCompraAnulada",
                "purchase-return.annulled.v1");
    }

    @Test
    void EVT_2_all_eight_pairs_are_distinct_and_versioned() {
        Set<String> eventTypes = new HashSet<>();
        Set<String> routingKeys = new HashSet<>();
        for (DocumentType type : DocumentType.values()) {
            for (Action action : Action.values()) {
                Entry entry = BillingEventCatalog.resolve(type, action);
                eventTypes.add(entry.eventType());
                routingKeys.add(entry.routingKey());
                assertThat(entry.routingKey()).endsWith(".v1");
            }
        }
        assertThat(eventTypes).hasSize(8);
        assertThat(routingKeys).hasSize(8);
    }

    @Test
    void EVT_2_resolve_from_domain_event_uses_the_document_type_and_the_event_kind() {
        BillingDocument sale = emittedSale();

        assertThat(BillingEventCatalog.resolve(new BillingDocumentEmitted(uuid(1), NOW, sale, null)).eventType())
                .isEqualTo("FacturaVentaEmitida");
        assertThat(BillingEventCatalog.resolve(new BillingDocumentAnnulled(uuid(2), NOW, annulledSale(), null))
                .routingKey()).isEqualTo("sale-invoice.annulled.v1");
        assertThat(BillingEventCatalog.resolve(new BillingDocumentEmitted(uuid(3), NOW, emittedPurchase(), null))
                .routingKey()).isEqualTo("purchase-invoice.emitted.v1");

        BillingDocument saleReturn = BillingDocument.restore(draftSaleReturnState("defecto"));
        assertThat(BillingEventCatalog.resolve(new BillingDocumentEmitted(uuid(4), NOW, saleReturn, null))
                .eventType()).isEqualTo("FacturaVentaDevuelta");
        assertThat(BillingEventCatalog.resolve(new BillingDocumentAnnulled(uuid(5), NOW, saleReturn, null))
                .eventType()).isEqualTo("DevolucionVentaAnulada");
    }

    @Test
    void EVT_5_topology_names_are_new_and_prefixed_with_billing() {
        assertThat(BillingMessagingTopology.DOCUMENTS_EXCHANGE).isEqualTo("billing.documents.exchange");
        assertThat(BillingMessagingTopology.UNROUTED_EXCHANGE).isEqualTo("billing.documents.unrouted.exchange");
        assertThat(BillingMessagingTopology.UNROUTED_QUEUE).isEqualTo("billing.documents.unrouted.queue");
        assertThat(BillingMessagingTopology.DEAD_LETTER_EXCHANGE).isEqualTo("billing.documents.dlx");
        assertThat(BillingMessagingTopology.DEAD_LETTER_QUEUE).isEqualTo("billing.documents.dlq");
        assertThat(BillingMessagingTopology.DOCUMENTS_EXCHANGE).doesNotContain("invoice.exchange");
    }

    @Test
    void null_arguments_are_rejected() {
        assertThatThrownBy(() -> BillingEventCatalog.resolve(null, Action.EMITTED))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> BillingEventCatalog.resolve(DocumentType.SALE_INVOICE, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> BillingEventCatalog.resolve(null)).isInstanceOf(NullPointerException.class);
    }
}
