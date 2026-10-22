package co.unicauca.edu.co.contables.configuration.thirds.domain.enums;

import lombok.Getter;

/**
 * @brief Tipos de errores que pueden ocurrir durante la importación de terceros
 *
 * Clasifica los errores para facilitar su manejo, presentación al usuario
 * y determinación de estrategias de recuperación.
 */
@Getter
public enum ImportErrorType {

    SYSTEM_ERROR("Error del Sistema"),

    BUSINESS_RULE_ERROR("Error de Regla de Negocio"),

    REFERENCE_ERROR("Error de Referencia"),

    VALIDATION_ERROR("Error de Validación"),

    FORMAT_ERROR("Error de Formato"),

    DUPLICATE_ERROR("Error de Duplicado");

    private final String description;

    /**
     * @brief Constructor del enum
     * @param description Descripción legible del tipo de error
     */
    ImportErrorType(String description) {
        this.description = description;
    }

    /**
     * @brief Verifica si el error es de tipo recuperable
     *
     * Los errores recuperables pueden ser corregidos por el usuario
     * sin requerir intervención técnica.
     * @return true si el error es recuperable, false para errores del sistema
     */
    public boolean isRecoverable() {
        return this != SYSTEM_ERROR;
    }

    /**
     * @brief Verifica si el error requiere acción del usuario
     *
     * Determina si el error necesita corrección manual por parte
     * del usuario o puede resolverse automáticamente.
     * @return true si requiere corrección por parte del usuario
     */
    public boolean requiresUserAction() {
        return this == VALIDATION_ERROR ||
               this == FORMAT_ERROR ||
               this == REFERENCE_ERROR ||
               this == BUSINESS_RULE_ERROR ||
               this == DUPLICATE_ERROR;
    }

    /**
     * @brief Obtiene la prioridad del error para ordenamiento
     *
     * Define el orden de importancia de los errores para su presentación
     * y procesamiento. Menor número indica mayor prioridad.
     * @return nivel de prioridad del 1 al 6 (1 = más crítico)
     */
    public int getPriority() {
        return switch (this) {
            case SYSTEM_ERROR -> 1;
            case BUSINESS_RULE_ERROR -> 2;
            case REFERENCE_ERROR -> 3;
            case VALIDATION_ERROR -> 4;
            case FORMAT_ERROR -> 5;
            case DUPLICATE_ERROR -> 6;
        };
    }
}
