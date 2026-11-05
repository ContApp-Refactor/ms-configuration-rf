package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta crear o actualizar un tipo de tercero con un nombre que ya existe
 *
 * Se utiliza en validaciones de unicidad para nombres de tipos de tercero,
 * asegurando que no se puedan crear duplicados basados en el nombre.
 */
public class ThirdTypeNameAlreadyExistsException extends BaseBusinessException {

    /**
     * @brief Constructor con nombre del tipo de tercero
     *
     * Crea una excepción específica cuando se detecta que ya existe otro tipo de tercero
     * con el mismo nombre, generando automáticamente un mensaje descriptivo.
     * @param thirdTypeName nombre del tipo de tercero que ya existe
     */
    public ThirdTypeNameAlreadyExistsException(String thirdTypeName) {
        super(ThirdTypeErrorCode.THIRD_TYPE_NAME_ALREADY_EXISTS,
              "Ya existe un tipo de tercero con el nombre '" + thirdTypeName + "'");
    }
}
