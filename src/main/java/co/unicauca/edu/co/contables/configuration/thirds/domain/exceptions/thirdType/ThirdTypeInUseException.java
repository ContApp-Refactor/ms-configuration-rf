package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta eliminar un tipo de tercero que está siendo utilizado por terceros existentes
 *
 * Se utiliza en operaciones de eliminación de tipos de tercero para prevenir
 * la ruptura de integridad referencial cuando existen terceros que dependen de ese tipo.
 */
public class ThirdTypeInUseException extends BaseBusinessException {


     /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de tercero en uso.
     */
    public ThirdTypeInUseException() {
        super(ThirdTypeErrorCode.THIRD_TYPE_IN_USE);
    }
    
    /**
     * @brief Constructor con ID específico del tipo de tercero
     *
     * Crea una excepción específica cuando se intenta eliminar un tipo de tercero
     * que tiene terceros asociados, identificándolo por su ID.
     * @param thirdTypeId el ID del tipo de tercero que está en uso
     */
    public ThirdTypeInUseException(Long thirdTypeId) {
        super(ThirdTypeErrorCode.THIRD_TYPE_IN_USE,
              "El tipo de tercero con ID " + thirdTypeId + " está siendo utilizado por terceros existentes y no puede ser eliminado");
    }

    /**
     * @brief Constructor con nombre específico del tipo de tercero para eliminación
     *
     * Crea una excepción específica cuando se intenta eliminar un tipo de tercero
     * que tiene terceros asociados, identificándolo por su nombre.
     * @param thirdTypeName el nombre del tipo de tercero que está en uso
     */
    public ThirdTypeInUseException(String thirdTypeName) {
        super(ThirdTypeErrorCode.THIRD_TYPE_IN_USE,
              "El tipo de tercero " + thirdTypeName + " está siendo utilizado por terceros existentes y no puede ser eliminado");
    }

    /**
     * @brief Constructor con nombre específico del tipo de tercero para edición
     *
     * Crea una excepción específica cuando se intenta editar un tipo de tercero
     * que tiene terceros con movimientos contables asociados.
     * @param thirdTypeName el nombre del tipo de tercero que está en uso
     * @param isEditOperation indica si es una operación de edición (true) o eliminación (false)
     */
    public ThirdTypeInUseException(String thirdTypeName, boolean isEditOperation) {
        super(ThirdTypeErrorCode.THIRD_TYPE_IN_USE,
              isEditOperation ?
              "No se puede editar el tipo de tercero " + thirdTypeName + " porque tiene terceros con movimientos contables" :
              "El tipo de tercero " + thirdTypeName + " está siendo utilizado por terceros existentes y no puede ser eliminado");
    }


    /**
     * @brief Constructor con causa
     * @param cause la causa original del error que provocó esta excepción
     */
    public ThirdTypeInUseException(Throwable cause) {
        super(ThirdTypeErrorCode.THIRD_TYPE_IN_USE, ThirdTypeErrorCode.THIRD_TYPE_IN_USE.getMessage(), cause);
    }
}
