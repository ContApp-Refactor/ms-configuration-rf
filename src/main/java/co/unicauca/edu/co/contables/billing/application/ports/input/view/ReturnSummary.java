package co.unicauca.edu.co.contables.billing.application.ports.input.view;

/**
 * Resumen de una devolucion con su ordinal dentro de la factura original (1, 2, 3...). El ordinal
 * es nulo mientras la devolucion es un borrador (sin numero, RET-10).
 */
public record ReturnSummary(BillingDocumentSummary summary, Integer ordinal) {
}
