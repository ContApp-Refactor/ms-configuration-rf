package co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para nombre de unidad de medida duplicado
 *
 * Se lanza cuando se intenta crear o actualizar una unidad de medida con un nombre
 * que ya está siendo usado por otra unidad de medida en la misma empresa.
 */
public class UnitOfMeasureNameAlreadyExistsException extends BaseBusinessException {
    
    public UnitOfMeasureNameAlreadyExistsException() {
        super(ErrorCode.UNITOFMEASURE_NAME_ALREADY_EXISTS);
    }
    
    public UnitOfMeasureNameAlreadyExistsException(String name) {
        super(ErrorCode.UNITOFMEASURE_NAME_ALREADY_EXISTS, 
              String.format("Ya existe una unidad de medida con el nombre '%s'", name));
    }
}
