package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando no se encuentra un país o está inactivo
 *
 * Se utiliza en operaciones geográficas cuando se requiere validar la existencia
 * de un país específico y este no existe o se encuentra inactivo en el sistema.
 */
public class CountryNotFoundException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para países no encontrados.
     */
    public CountryNotFoundException() {
        super(GeographyErrorCode.COUNTRY_NOT_FOUND);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public CountryNotFoundException(String customMessage) {
        super(GeographyErrorCode.COUNTRY_NOT_FOUND, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public CountryNotFoundException(String customMessage, Throwable cause) {
        super(GeographyErrorCode.COUNTRY_NOT_FOUND, customMessage, cause);
    }
}
