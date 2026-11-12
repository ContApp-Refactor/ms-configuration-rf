package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

/** Metodo de pago activo para el selector (id + nombre). */
public record BillingPaymentMethodResponse(String id, String name) {
}
