package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

/**
 * Emisor congelado al emitir (RN-06): copia de los datos de la empresa en el momento de la
 * emision. Solo se informa en documentos EMITIDA o ANULADA; en borrador el objeto es nulo.
 */
public record BillingIssuerResponse(
        String enterpriseId,
        String name,
        String nit,
        String dv,
        boolean active) {
}
