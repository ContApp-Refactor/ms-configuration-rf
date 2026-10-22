package co.unicauca.edu.co.contables.configuration.thirds.domain.enums;

import lombok.Getter;

/**
 * @brief Estados posibles del procesamiento de un registro individual durante la importación
 *
 * Define el resultado del procesamiento de cada registro de tercero,
 * permitiendo un seguimiento granular del éxito o fracaso de cada elemento procesado.
 */
@Getter
public enum ProcessingStatus {

    SUCCESS("Éxito"),

    DUPLICATE_SKIPPED("Duplicado Omitido"),

    FAILED("Fallido"),

    SKIPPED("Omitido");

    private final String description;

    /**
     * @brief Constructor del enum
     * @param description Descripción legible del estado de procesamiento
     */
    ProcessingStatus(String description) {
        this.description = description;
    }

    /**
     * @brief Verifica si el estado indica un procesamiento exitoso
     *
     * Indica que el registro fue procesado correctamente y guardado en el sistema.
     * @return true si el procesamiento fue exitoso
     */
    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    /**
     * @brief Verifica si el estado indica que el registro fue omitido
     *
     * Puede ser omitido por duplicados o por otras razones de validación.
     * @return true si el registro fue omitido por cualquier razón
     */
    public boolean wasSkipped() {
        return this == DUPLICATE_SKIPPED || this == SKIPPED;
    }

    /**
     * @brief Verifica si el estado indica un fallo en el procesamiento
     *
     * Indica que el registro no pudo ser procesado debido a errores
     * que impidieron su inserción o actualización.
     * @return true si el procesamiento falló
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * @brief Verifica si el estado debe contarse como un duplicado
     *
     * Específicamente identifica registros omitidos por ser duplicados
     * de otros ya existentes en el sistema.
     * @return true si fue omitido por ser duplicado
     */
    public boolean isDuplicate() {
        return this == DUPLICATE_SKIPPED;
    }
}
