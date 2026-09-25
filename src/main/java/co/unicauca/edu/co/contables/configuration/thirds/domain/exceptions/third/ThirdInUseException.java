package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta modificar o eliminar un tercero en uso
 *
 * Esta excepción se utiliza cuando un tercero tiene un contador de uso mayor a cero,
 * indicando que tiene movimientos contables asociados y no puede ser modificado o eliminado.
 */
public class ThirdInUseException extends BaseBusinessException {   

    /**
     * Constructor que permite especificar si es una operación de edición o eliminación
     * @param idNumber número de identificación del tercero
     * @param isEditOperation true si es edición, false si es eliminación
     */
    public ThirdInUseException(String idNumber, boolean isEditOperation) {
        super(ThirdsErrorCode.THIRD_IN_USE,
              isEditOperation ?
              String.format("No se puede editar el tercero con identificación %s porque tiene movimientos contables", idNumber) :
              String.format("No se puede eliminar el tercero con identificación %s porque tiene movimientos contables", idNumber));
    }
}
