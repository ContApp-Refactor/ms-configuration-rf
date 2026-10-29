package co.unicauca.edu.co.contables.billing.application.ports.input.view;

import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;

/**
 * Factura que tiene devoluciones EMITIDA (RET-11, HU-24): cuantas, por cuanto y el estado
 * derivado. El estado sale de comparar la suma devuelta con la suma vendida (valido porque lo
 * devuelto nunca supera lo vendido por linea).
 */
public record InvoiceWithReturnsSummary(BillingDocumentSummary summary, int returnsCount, Money returnedTotal,
        ReturnStatus state) {
}
