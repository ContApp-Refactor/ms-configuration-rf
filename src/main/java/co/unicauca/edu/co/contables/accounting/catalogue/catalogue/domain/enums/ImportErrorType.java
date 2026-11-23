package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums;

import lombok.Getter;

/**
 * @brief Clasificación de tipos de errores durante importación Excel
 *
 * Define categorías de errores que pueden ocurrir durante el proceso de importación
 * masiva de cuentas contables, facilitando el manejo, priorización y presentación al usuario.
 */
@Getter
public enum ImportErrorType {
    
    
    VALIDATION_ERROR("Error de Validación"),
    
    FORMAT_ERROR("Error de Formato"),
    
    REFERENCE_ERROR("Error de Referencia"),
    
    BUSINESS_RULE_ERROR("Error de Regla de Negocio"),
    
    DUPLICATE_ERROR("Error de Duplicado"),
    
    HIERARCHY_ERROR("Error de Jerarquía"),
    
    SYSTEM_ERROR("Error del Sistema");

    private final String description;

    ImportErrorType(String description) {
        this.description = description;
    }

    /**
     * @brief Determina si el error puede ser corregido por el usuario
     * @return true si el error no es SYSTEM_ERROR (todos los demás son recuperables)
     */
    public boolean isRecoverable() {
        return this != SYSTEM_ERROR;
    }

    /**
     * @brief Determina si el error necesita intervención del usuario para resolverse
     * @return true si el error requiere corrección manual (todos excepto SYSTEM_ERROR)
     */
    public boolean requiresUserAction() {
        return this == VALIDATION_ERROR || 
               this == FORMAT_ERROR || 
               this == REFERENCE_ERROR || 
               this == BUSINESS_RULE_ERROR ||
               this == DUPLICATE_ERROR ||
               this == HIERARCHY_ERROR;
    }

    /**
     * @brief Obtiene nivel de prioridad para ordenamiento y presentación de errores
     * @return número de prioridad (1=máxima, 7=mínima) basado en criticidad del error
     */
    public int getPriority() {
        return switch (this) {
            case SYSTEM_ERROR -> 1;
            case HIERARCHY_ERROR -> 2;
            case BUSINESS_RULE_ERROR -> 3;
            case REFERENCE_ERROR -> 4;
            case VALIDATION_ERROR -> 5;
            case FORMAT_ERROR -> 6;
            case DUPLICATE_ERROR -> 7;
        };
    }
}

