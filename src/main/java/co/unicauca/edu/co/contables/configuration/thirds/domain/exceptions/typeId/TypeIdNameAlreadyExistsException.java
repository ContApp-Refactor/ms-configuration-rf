package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando ya existe un tipo de identificación con el mismo nombre
 *
 * Se utiliza en validaciones de unicidad para nombres de tipos de identificación.
 */
public class TypeIdNameAlreadyExistsException extends BaseBusinessException {

    /**
     * @brief Constructor con nombre del tipo de identificación
     *
     * Crea una excepción específica cuando se detecta que ya existe otro tipo de identificación
     * con el mismo nombre, generando automáticamente un mensaje descriptivo.
     * @param typeIdName nombre del tipo de identificación que ya existe
     */
    public TypeIdNameAlreadyExistsException(String typeIdName) {
        super(TypeIdErrorCode.TYPE_ID_NAME_ALREADY_EXISTS,
              "Ya existe un tipo de identificación con el nombre '" + typeIdName + "'");
    }

    /**
     * @brief Constructor con nombre del tipo de identificación y causa
     * @param typeIdName nombre del tipo de identificación que ya existe
     * @param cause causa original del error que provocó esta excepción
     */
    public TypeIdNameAlreadyExistsException(String typeIdName, Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_NAME_ALREADY_EXISTS,
              "Ya existe un tipo de identificación con el nombre '" + typeIdName + "'", cause);
    }
}
