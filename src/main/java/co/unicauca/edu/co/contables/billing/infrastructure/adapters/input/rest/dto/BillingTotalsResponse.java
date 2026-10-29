package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

/** Totales del documento en centavos enteros (DOC-6). */
public record BillingTotalsResponse(long subtotalCents, long ivaCents, long totalCents) {
}
