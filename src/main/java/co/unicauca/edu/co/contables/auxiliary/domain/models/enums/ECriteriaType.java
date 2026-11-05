package co.unicauca.edu.co.contables.auxiliary.domain.models.enums;

/**
 * @brief Enumeración de niveles de detalle aplicables a las cuentas.
 *
 * Cada valor representa un nivel del plan contable (clase, grupo, cuenta,
 * subcuenta, cuenta auxiliar) y expone su etiqueta amigable mediante
 * {@link #getDisplayName()} para el reporte.
 */
public enum ECriteriaType {
    NUMBER_CLASS("Clase"),
    GROUP("Grupo"),
    ACCOUNT("Cuenta"),
    SUB_ACCOUNT("Subcuenta"),
    AUXILIARY_ACCOUNT("Cuenta Auxiliar");

    private final String displayName;

    ECriteriaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
