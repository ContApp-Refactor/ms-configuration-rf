package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando los datos del tipo de tercero son inválidos
 *
 * Se utiliza cuando se detectan datos incorrectos o inconsistentes
 * en la información de un tipo de tercero durante validaciones de negocio.
 */
public class ThirdTypeInvalidDataException extends BaseBusinessException {

    /**
     * @brief Constructor con mensaje de error
     * @param message mensaje descriptivo que especifica qué datos son inválidos
     */
    public ThirdTypeInvalidDataException(String message) {
        super(ThirdTypeErrorCode.THIRD_TYPE_INVALID_DATA, message);
    }
}
