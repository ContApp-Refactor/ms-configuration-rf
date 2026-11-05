package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando no se encuentra un tipo de identificación
 *
 */
public class TypeIdNotFound extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de identificación no encontrados.
     */
    public TypeIdNotFound() {
        super(TypeIdErrorCode.TYPE_ID_NOT_FOUND);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public TypeIdNotFound(String customMessage) {
        super(TypeIdErrorCode.TYPE_ID_NOT_FOUND, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public TypeIdNotFound(String customMessage, Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_NOT_FOUND, customMessage, cause);
    }
}
