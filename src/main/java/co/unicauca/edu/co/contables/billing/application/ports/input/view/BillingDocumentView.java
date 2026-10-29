package co.unicauca.edu.co.contables.billing.application.ports.input.view;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;

/**
 * Documento listo para presentar. {@code returnStatus} (facturas emitidas) y {@code returnOrdinal}
 * (devoluciones emitidas/anuladas) son derivados y pueden ser nulos.
 */
public record BillingDocumentView(BillingDocument doc, ReturnStatus returnStatus, Integer returnOrdinal) {

    public static BillingDocumentView of(BillingDocument doc) {
        return new BillingDocumentView(doc, null, null);
    }
}
