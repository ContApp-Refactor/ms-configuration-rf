package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

/**
 * Metodo de pago de la factura de venta. {@code methodName} es el nombre congelado al emitir (nulo
 * en borrador).
 */
public record BillingPaymentResponse(
        String methodId,
        String methodName) {
}
