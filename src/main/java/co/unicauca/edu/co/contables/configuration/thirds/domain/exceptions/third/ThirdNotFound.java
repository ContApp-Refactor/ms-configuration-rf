package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando no se encuentra un tercero
 *
 * Se utiliza en operaciones que requieren la existencia de un tercero específico
 * (persona natural o jurídica) y este no existe en el sistema o se encuentra inactivo.
 */
public class ThirdNotFound extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para terceros no encontrados.
     */
    public ThirdNotFound() {
        super(ThirdsErrorCode.THIRD_NOT_FOUND);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public ThirdNotFound(String customMessage) {
        super(ThirdsErrorCode.THIRD_NOT_FOUND, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdNotFound(String customMessage, Throwable cause) {
        super(ThirdsErrorCode.THIRD_NOT_FOUND, customMessage, cause);
    }
}
