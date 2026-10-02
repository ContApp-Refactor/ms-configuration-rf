package co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para unidad de medida con asociaciones activas
 *
 * Se lanza cuando se intenta eliminar una unidad de medida que tiene productos
 * asociados, impidiendo la eliminación por integridad de datos.
 */
public class UnitOfMeasureAssociatedException extends BaseBusinessException {
    
    public UnitOfMeasureAssociatedException() {
        super(ErrorCode.UNITOFMEASURE_ASSOCIATED);
    }
    
    public UnitOfMeasureAssociatedException(Long id) {
        super(ErrorCode.UNITOFMEASURE_ASSOCIATED, 
              String.format("La unidad de medida con ID %d no se puede eliminar porque está asociada con productos", id));
    }
    
    public UnitOfMeasureAssociatedException(String name) {
        super(ErrorCode.UNITOFMEASURE_ASSOCIATED, 
              String.format("La unidad de medida '%s' no se puede eliminar porque está asociada con productos", name));
    }
}