package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta crear un tercero con un tipo de identificación que no existe
 *
 * Se utiliza en validaciones de integridad referencial cuando se asigna un tipo de identificación
 * a un tercero.
 */
public class TypeIdForeignKeyViolationException extends BaseBusinessException {

    /**
     * @brief Constructor con ID del tipo de identificación
     *
     * Crea una excepción específica cuando se detecta que el tipo de identificación
     * referenciado por un tercero no existe en el sistema.
     * @param typeId ID del tipo de identificación que no existe
     */
    public TypeIdForeignKeyViolationException(String typeId) {
        super(TypeIdErrorCode.TYPE_ID_FOREIGN_KEY_VIOLATION,
              "El tipo de identificación con ID '" + typeId + "' no existe");
    }

    /**
     * @brief Constructor con ID del tipo de identificación y causa
     * @param typeId ID del tipo de identificación que no existe
     * @param cause causa original del error que provocó esta excepción
     */
    public TypeIdForeignKeyViolationException(String typeId, Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_FOREIGN_KEY_VIOLATION,
              "El tipo de identificación con ID '" + typeId + "' no existe", cause);
    }
}
