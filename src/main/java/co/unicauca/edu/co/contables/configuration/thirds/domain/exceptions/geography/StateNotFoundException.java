package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando no se encuentra un estado/departamento o está inactivo
 *
 * Se utiliza en operaciones geográficas cuando se requiere validar la existencia
 * de un estado o departamento específico y este no existe o se encuentra inactivo.
 */
public class StateNotFoundException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para estados/departamentos no encontrados.
     */
    public StateNotFoundException() {
        super(GeographyErrorCode.STATE_NOT_FOUND);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public StateNotFoundException(String customMessage) {
        super(GeographyErrorCode.STATE_NOT_FOUND, customMessage);
    }

    /**
     * @brief Constructor con código de estado y país
     *
     * Crea una excepción específica para cuando un estado no existe en un país determinado,
     * generando automáticamente un mensaje descriptivo.
     * @param stateCode código del estado que no se encontró
     * @param countryCode código del país donde se buscó el estado
     */
    public StateNotFoundException(String stateCode, String countryCode) {
        super(GeographyErrorCode.STATE_NOT_FOUND,
              String.format("El estado/departamento con código '%s' no existe en el país '%s'", stateCode, countryCode));
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public StateNotFoundException(String customMessage, Throwable cause) {
        super(GeographyErrorCode.STATE_NOT_FOUND, customMessage, cause);
    }
}
