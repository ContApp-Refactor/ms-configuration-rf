package co.unicauca.edu.co.contables.configuration.thirds.domain.enums;

/**
 * @brief Campos opcionales exportables para terceros
 *
 * Representa los atributos geográficos y personales que pueden incluirse
 * opcionalmente en la exportación de datos de terceros.
 */
public enum ExportableField {

    GENDER("Género", "gender"),
    COUNTRY("País", "country"),
    STATE("Departamento", "state"),
    CITY("Ciudad", "city");

    private final String displayName;
    private final String fieldName;

    /**
     * @brief Constructor del enum
     * @param displayName Nombre para mostrar en interfaces de usuario
     * @param fieldName Nombre técnico del campo en el sistema
     */
    ExportableField(String displayName, String fieldName) {
        this.displayName = displayName;
        this.fieldName = fieldName;
    }

    /**
     * @brief Obtiene el nombre para mostrar del campo
     * @return Nombre legible para interfaces de usuario
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @brief Obtiene el nombre técnico del campo
     * @return Nombre del campo utilizado internamente en el sistema
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @brief Obtiene el ExportableField a partir del nombre del campo
     *
     * Permite buscar un campo exportable por su nombre técnico,
     * facilitando la configuración dinámica de exportaciones.
     * @param fieldName nombre técnico del campo a buscar
     * @return ExportableField correspondiente o null si no existe
     */
    public static ExportableField fromFieldName(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        for (ExportableField field : values()) {
            if (field.fieldName.equalsIgnoreCase(fieldName)) {
                return field;
            }
        }
        return null;
    }
}
