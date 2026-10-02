package co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para abreviación de unidad de medida duplicada
 *
 * Se lanza cuando se intenta crear o actualizar una unidad de medida con una abreviación
 * que ya está siendo usada por otra unidad de medida en la misma empresa.
 */
public class UnitOfMeasureAbbreviationAlreadyExistsException extends BaseBusinessException {
    
    public UnitOfMeasureAbbreviationAlreadyExistsException() {
        super(ErrorCode.UNITOFMEASURE_ABBREVIATION_ALREADY_EXISTS);
    }
    
    public UnitOfMeasureAbbreviationAlreadyExistsException(String abbreviation) {
        super(ErrorCode.UNITOFMEASURE_ABBREVIATION_ALREADY_EXISTS, 
              String.format("Ya existe una unidad de medida con la abreviación '%s'", abbreviation));
    }
}
