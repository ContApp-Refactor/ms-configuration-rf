package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando no se encuentra un tipo de tercero
 *
 * Se utiliza en operaciones que requieren la existencia de un tipo de tercero específico
 * (cliente, proveedor, empleado, etc.) y este no existe en el sistema o se encuentra inactivo.
 */
public class ThirdTypeNotFound extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de tercero no encontrados.
     */
    public ThirdTypeNotFound() {
        super(ThirdTypeErrorCode.THIRD_TYPE_NOT_FOUND);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public ThirdTypeNotFound(String customMessage) {
        super(ThirdTypeErrorCode.THIRD_TYPE_NOT_FOUND, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdTypeNotFound(String customMessage, Throwable cause) {
        super(ThirdTypeErrorCode.THIRD_TYPE_NOT_FOUND, customMessage, cause);
    }
}
