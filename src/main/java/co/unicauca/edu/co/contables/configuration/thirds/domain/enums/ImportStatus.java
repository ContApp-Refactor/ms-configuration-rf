package co.unicauca.edu.co.contables.configuration.thirds.domain.enums;

import lombok.Getter;

/**
 * @brief Estados posibles del proceso de importación de terceros
 *
 * Define los diferentes estados que puede tener una importación
 * durante su ciclo de vida completo desde la recepción hasta el procesamiento final.
 */
@Getter
public enum ImportStatus {

    PENDING("Pendiente"),

    PROCESSING("Procesando"),

    COMPLETED("Completado"),

    COMPLETED_WITH_ERRORS("Completado con Errores"),

    FAILED("Fallido");

    private final String description;

    /**
     * @brief Constructor del enum
     * @param description Descripción legible del estado de importación
     */
    ImportStatus(String description) {
        this.description = description;
    }

    /**
     * @brief Verifica si el estado indica que la importación ha terminado
     *
     * Un proceso terminado puede haber sido exitoso, parcialmente exitoso o fallido,
     * pero ya no está en ejecución.
     * @return true si la importación ha terminado (exitosa o fallida)
     */
    public boolean isFinished() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS || this == FAILED;
    }

    /**
     * @brief Verifica si el estado indica éxito total o parcial
     *
     * Indica que al menos algunos registros fueron procesados exitosamente,
     * independientemente de si hubo errores en otros registros.
     * @return true si hay al menos algunos registros procesados exitosamente
     */
    public boolean hasSuccessfulRecords() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS;
    }

    /**
     * @brief Verifica si el estado indica que hubo errores durante el procesamiento
     *
     * Puede indicar errores parciales (COMPLETED_WITH_ERRORS) o totales (FAILED).
     * @return true si hubo errores en el procesamiento
     */
    public boolean hasErrors() {
        return this == COMPLETED_WITH_ERRORS || this == FAILED;
    }
}
