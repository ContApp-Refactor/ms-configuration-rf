package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta eliminar un tipo de identificación que está siendo utilizado por terceros existentes
 *
 * Se utiliza en operaciones de eliminación de tipos de identificación para prevenir
 * la ruptura de integridad referencial cuando existen terceros que dependen de ese tipo.
 */
public class TypeIdInUseException extends BaseBusinessException {

    /**
     * @brief Constructor con ID específico del tipo de identificación
     *
     * Crea una excepción específica cuando se intenta eliminar un tipo de identificación
     * que tiene terceros asociados, identificándolo por su ID.
     * @param typeIdId el ID del tipo de identificación que está en uso
     */
    public TypeIdInUseException(Long typeIdId) {
        super(TypeIdErrorCode.TYPE_ID_IN_USE,
              "El tipo de identificación con ID " + typeIdId + " está siendo utilizado por terceros existentes y no puede ser eliminado");
    }

    /**
     * @brief Constructor con nombre específico del tipo de identificación para eliminación
     *
     * Crea una excepción específica cuando se intenta eliminar un tipo de identificación
     * que tiene terceros asociados, identificándolo por su nombre.
     * @param typeIdName el nombre del tipo de identificación que está en uso
     */
    public TypeIdInUseException(String typeIdName) {
        super(TypeIdErrorCode.TYPE_ID_IN_USE,
              "El tipo de identificación " + typeIdName + " está siendo utilizado por terceros existentes y no puede ser eliminado");
    }

    /**
     * @brief Constructor con nombre específico del tipo de identificación para edición
     *
     * Crea una excepción específica cuando se intenta editar un tipo de identificación
     * que tiene terceros con movimientos contables asociados.
     * @param typeIdName el nombre del tipo de identificación que está en uso
     * @param isEditOperation indica si es una operación de edición (true) o eliminación (false)
     */
    public TypeIdInUseException(String typeIdName, boolean isEditOperation) {
        super(TypeIdErrorCode.TYPE_ID_IN_USE,
              isEditOperation ?
              "No se puede editar el tipo de identificación " + typeIdName + " porque tiene terceros con movimientos contables" :
              "El tipo de identificación " + typeIdName + " está siendo utilizado por terceros existentes y no puede ser eliminado");
    }

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de identificación en uso.
     */
    public TypeIdInUseException() {
        super(TypeIdErrorCode.TYPE_ID_IN_USE);
    }

    /**
     * @brief Constructor con causa
     * @param cause la causa original del error que provocó esta excepción
     */
    public TypeIdInUseException(Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_IN_USE, TypeIdErrorCode.TYPE_ID_IN_USE.getMessage(), cause);
    }
}
