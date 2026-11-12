package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

/**
 * Emisor del documento. En EMITIDA/ANULADA es la copia congelada al emitir (RN-06) y nunca se
 * vuelve a consultar; en borrador de factura son los datos en vivo de la empresa (solo lectura).
 * {@code address} .. {@code logo} son opcionales (nulos en documentos emitidos antes de este dato o
 * si la empresa no los tiene). {@code economicActivity} es el codigo de la actividad principal y
 * {@code logo} una URL/ruta liviana (nunca el contenido; nulo si no hay una referencia liviana).
 */
public record BillingIssuerResponse(
        String enterpriseId,
        String name,
        String nit,
        String dv,
        boolean active,
        String address,
        String city,
        String department,
        String phone,
        String email,
        String economicActivity,
        String logo) {

    /** Solo los datos basicos del emisor. */
    public BillingIssuerResponse(String enterpriseId, String name, String nit, String dv, boolean active) {
        this(enterpriseId, name, nit, dv, active, null, null, null, null, null, null, null);
    }
}
