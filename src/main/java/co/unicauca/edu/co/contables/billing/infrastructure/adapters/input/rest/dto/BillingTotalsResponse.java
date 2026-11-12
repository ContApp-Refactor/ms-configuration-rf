package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

/**
 * Totales del documento en centavos enteros (DOC-6). {@code withholdingsCents} es la suma de las
 * retenciones y {@code totalCents} ya es NETO (subtotal + IVA - retenciones).
 */
public record BillingTotalsResponse(long subtotalCents, long ivaCents, long totalCents, long withholdingsCents) {

    /** Sin retenciones. */
    public BillingTotalsResponse(long subtotalCents, long ivaCents, long totalCents) {
        this(subtotalCents, ivaCents, totalCents, 0L);
    }
}
