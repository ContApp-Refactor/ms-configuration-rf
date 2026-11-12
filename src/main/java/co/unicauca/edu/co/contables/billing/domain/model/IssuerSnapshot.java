package co.unicauca.edu.co.contables.billing.domain.model;

/**
 * Copia congelable de los datos de la empresa emisora. Los datos de contacto y presentacion
 * ({@code address} .. {@code logo}) son opcionales: nulos en documentos anteriores a este dato o si
 * la empresa no los tiene. {@code economicActivity} es el codigo de la actividad principal (no hay
 * catalogo de nombres); {@code logo} es una referencia liviana (URL/ruta), nunca el contenido.
 */
public record IssuerSnapshot(
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

    /** Sin datos de contacto (usos anteriores y documentos historicos). */
    public IssuerSnapshot(String enterpriseId, String name, String nit, String dv, boolean active) {
        this(enterpriseId, name, nit, dv, active, null, null, null, null, null, null, null);
    }
}
