package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta crear un tercero con un tipo de tercero que no existe
 *
 * Se utiliza en validaciones de integridad referencial cuando se asigna un tipo de tercero
 * a un tercero, asegurando que el tipo de tercero referenciado realmente exista.
 */
public class ThirdTypeForeignKeyViolationException extends BaseBusinessException {

    /**
     * @brief Constructor con ID del tipo de tercero
     *
     * Crea una excepción específica cuando se detecta que el tipo de tercero
     * referenciado por un tercero no existe en el sistema.
     * @param thirdTypeId ID del tipo de tercero que no existe
     */
    public ThirdTypeForeignKeyViolationException(String thirdTypeId) {
        super(ThirdTypeErrorCode.THIRD_TYPE_FOREIGN_KEY_VIOLATION,
              "El tipo de tercero con ID '" + thirdTypeId + "' no existe");
    }

    /**
     * @brief Constructor con ID del tipo de tercero y causa
     * @param thirdTypeId ID del tipo de tercero que no existe
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdTypeForeignKeyViolationException(String thirdTypeId, Throwable cause) {
        super(ThirdTypeErrorCode.THIRD_TYPE_FOREIGN_KEY_VIOLATION,
              "El tipo de tercero con ID '" + thirdTypeId + "' no existe", cause);
    }
}
