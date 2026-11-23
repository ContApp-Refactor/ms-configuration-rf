package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums;

import lombok.Getter;

/**
 * @brief Estados del ciclo de vida de procesos de importación Excel
 *
 * Define los diferentes estados posibles durante el proceso de importación
 * masiva de cuentas contables, desde pendiente hasta completado con o sin errores.
 */
@Getter
public enum ImportStatus {
    
    PENDING("Pendiente"),
    
    PROCESSING("Procesando"),
    
    COMPLETED("Completado"),
    
    COMPLETED_WITH_ERRORS("Completado con Errores"),
    
    FAILED("Fallido");

    private final String description;

    ImportStatus(String description) {
        this.description = description;
    }

    /**
     * @brief Determina si el proceso de importación ha finalizado
     * @return true si el estado es terminal (COMPLETED, COMPLETED_WITH_ERRORS, FAILED)
     */
    public boolean isFinished() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS || this == FAILED;
    }

    /**
     * @brief Verifica si el proceso tuvo al menos algunos registros exitosos
     * @return true si el estado indica procesamiento exitoso (COMPLETED o COMPLETED_WITH_ERRORS)
     */
    public boolean hasSuccessfulRecords() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS;
    }

    /**
     * @brief Determina si el proceso de importación experimentó errores
     * @return true si el estado indica presencia de errores (COMPLETED_WITH_ERRORS o FAILED)
     */
    public boolean hasErrors() {
        return this == COMPLETED_WITH_ERRORS || this == FAILED;
    }
}

