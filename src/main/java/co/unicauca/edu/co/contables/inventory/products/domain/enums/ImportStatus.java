package co.unicauca.edu.co.contables.inventory.products.domain.enums;

import lombok.Getter;

/**
 * @brief Estados posibles del proceso de importación de productos
 *
 * Define los diferentes estados que puede tener una importación durante su ciclo de vida,
 * permitiendo seguimiento del progreso y resultado final del proceso.
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
     * @brief Verifica si el estado indica que la importación ha terminado.
     *
     * @return true si la importación ha terminado (exitosa o fallida)
     */
    public boolean isFinished() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS || this == FAILED;
    }

    /**
     * @brief Verifica si el estado indica éxito total o parcial.
     *
     * @return true si hay al menos algunos registros procesados exitosamente
     */
    public boolean hasSuccessfulRecords() {
        return this == COMPLETED || this == COMPLETED_WITH_ERRORS;
    }

    /**
     * @brief Verifica si el estado indica que hubo errores durante el procesamiento.
     *
     * @return true si hubo errores
     */
    public boolean hasErrors() {
        return this == COMPLETED_WITH_ERRORS || this == FAILED;
    }
}