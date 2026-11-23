package co.unicauca.edu.co.contables.inventory.products.domain.exception.product;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para referencia de producto duplicada
 *
 * Se lanza cuando se intenta crear o actualizar un producto con una referencia/SKU
 * que ya está siendo usado por otro producto en la misma empresa.
 */
public class ProductReferenceAlreadyExistsException extends BaseBusinessException {
    
    public ProductReferenceAlreadyExistsException() {
        super(ErrorCode.PRODUCT_REFERENCE_ALREADY_EXISTS);
    }
    
    public ProductReferenceAlreadyExistsException(String reference) {
        super(ErrorCode.PRODUCT_REFERENCE_ALREADY_EXISTS, 
              String.format("Ya existe un producto con la referencia '%s'", reference));
    }
}
