package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando los datos del tercero son inválidos
 *
 * Se utiliza cuando se detectan datos incorrectos o inconsistentes
 * en la información de un tercero durante validaciones de negocio.
 */
public class ThirdInvalidDataException extends BaseBusinessException {

    /**
     * @brief Constructor con mensaje de error
     * @param message mensaje descriptivo que especifica qué datos son inválidos
     */
    public ThirdInvalidDataException(String message) {
        super(ThirdsErrorCode.INVALID_THIRD_DATA, message);
    }
}
