package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando los datos geográficos son inválidos
 *
 * Se utiliza cuando se detectan violaciones en la jerarquía geográfica,
 * como asignar una ciudad a un estado que no pertenece al mismo país,
 * o datos geográficos que no cumplen con las reglas de negocio.
 */
public class GeographyInvalidDataException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para violaciones de jerarquía geográfica.
     */
    public GeographyInvalidDataException() {
        super(GeographyErrorCode.GEOGRAPHY_HIERARCHY_VIOLATION);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe la violación específica
     */
    public GeographyInvalidDataException(String customMessage) {
        super(GeographyErrorCode.GEOGRAPHY_HIERARCHY_VIOLATION, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe la violación específica
     * @param cause causa original del error que provocó esta excepción
     */
    public GeographyInvalidDataException(String customMessage, Throwable cause) {
        super(GeographyErrorCode.GEOGRAPHY_HIERARCHY_VIOLATION, customMessage, cause);
    }
}
