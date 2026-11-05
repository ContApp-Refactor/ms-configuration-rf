package co.unicauca.edu.co.contables.billing.domain.model;

/** Copia congelable de los datos de la empresa emisora. */
public record IssuerSnapshot(String enterpriseId, String name, String nit, String dv, boolean active) {
}
