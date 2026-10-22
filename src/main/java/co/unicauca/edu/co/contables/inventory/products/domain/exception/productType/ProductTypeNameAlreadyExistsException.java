package co.unicauca.edu.co.contables.inventory.products.domain.exception.productType;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para nombre de tipo de producto duplicado
 *
 * Se lanza cuando se intenta crear o actualizar un tipo de producto con un nombre
 * que ya está siendo usado por otro tipo de producto en la misma empresa.
 */
public class ProductTypeNameAlreadyExistsException extends BaseBusinessException {
    
    public ProductTypeNameAlreadyExistsException() {
        super(ErrorCode.PRODUCT_TYPE_NAME_ALREADY_EXISTS);
    }
    
    public ProductTypeNameAlreadyExistsException(String name) {
        super(ErrorCode.PRODUCT_TYPE_NAME_ALREADY_EXISTS, 
              String.format("Ya existe un tipo de producto con el nombre '%s'", name));
    }
}
