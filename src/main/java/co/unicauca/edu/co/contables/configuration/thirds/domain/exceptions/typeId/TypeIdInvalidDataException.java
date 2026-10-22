package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando los datos del tipo de identificación son inválidos
 *
 * Se utiliza cuando se detectan datos incorrectos o inconsistentes
 * en la información de un tipo de identificación durante validaciones de negocio.
 */
public class TypeIdInvalidDataException extends BaseBusinessException {

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje descriptivo que especifica qué datos son inválidos
     */
    public TypeIdInvalidDataException(String customMessage) {
        super(TypeIdErrorCode.TYPE_ID_INVALID_DATA, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje descriptivo que especifica qué datos son inválidos
     * @param cause causa original del error que provocó esta excepción
     */
    public TypeIdInvalidDataException(String customMessage, Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_INVALID_DATA, customMessage, cause);
    }
}
