package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentAnnulled;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDomainEvent;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.util.Objects;

/**
 * Catalogo fijo de eventos (EVT-2, design §8): tipo de documento x accion -> {@code eventType} y
 * {@code routingKey}. Las devoluciones emitidas se publican como "Factura...Devuelta" y su anulacion
 * como "Devolucion...Anulada" (supuesto A1).
 */
public final class BillingEventCatalog {

    /** Hecho de negocio que origina el evento. */
    public enum Action {
        EMITTED,
        ANNULLED
    }

    /** Nombre del evento y clave de enrutamiento. */
    public record Entry(String eventType, String routingKey) {
    }

    private BillingEventCatalog() {
    }

    public static Entry resolve(DocumentType type, Action action) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(action, "action");
        boolean emitted = action == Action.EMITTED;
        return switch (type) {
            case SALE_INVOICE -> emitted
                    ? new Entry("FacturaVentaEmitida", "sale-invoice.emitted.v1")
                    : new Entry("FacturaVentaAnulada", "sale-invoice.annulled.v1");
            case SALE_RETURN -> emitted
                    ? new Entry("FacturaVentaDevuelta", "sale-invoice.returned.v1")
                    : new Entry("DevolucionVentaAnulada", "sale-return.annulled.v1");
            case PURCHASE_INVOICE -> emitted
                    ? new Entry("FacturaCompraEmitida", "purchase-invoice.emitted.v1")
                    : new Entry("FacturaCompraAnulada", "purchase-invoice.annulled.v1");
            case PURCHASE_RETURN -> emitted
                    ? new Entry("FacturaCompraDevuelta", "purchase-invoice.returned.v1")
                    : new Entry("DevolucionCompraAnulada", "purchase-return.annulled.v1");
        };
    }

    public static Entry resolve(BillingDomainEvent event) {
        Objects.requireNonNull(event, "event");
        Action action = event instanceof BillingDocumentAnnulled ? Action.ANNULLED : Action.EMITTED;
        return resolve(event.document().type(), action);
    }
}
