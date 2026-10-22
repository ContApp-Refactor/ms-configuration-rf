package co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para unidad de medida no encontrada
 *
 * Se lanza cuando se intenta acceder a una unidad de medida que no existe
 * o no está activa en el sistema.
 */
public class UnitOfMeasureNotFoundException extends BaseBusinessException {
    
    public UnitOfMeasureNotFoundException() {
        super(ErrorCode.UNITOFMEASURE_NOT_FOUND);
    }
    
    public UnitOfMeasureNotFoundException(Long id) {
        super(ErrorCode.UNITOFMEASURE_NOT_FOUND, 
              String.format("No se encontró la unidad de medida con ID %d", id));
    }
    
    public UnitOfMeasureNotFoundException(String nameOrMessage, boolean isCustomMessage) {
        super(ErrorCode.UNITOFMEASURE_NOT_FOUND, 
              isCustomMessage ? nameOrMessage : 
              String.format("No se encontró la unidad de medida con nombre %s", nameOrMessage));
    }
}
