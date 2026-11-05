package co.unicauca.edu.co.contables.inventory.products.domain.exception.productType;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para tipo de producto con asociaciones activas
 *
 * Se lanza cuando se intenta eliminar un tipo de producto que tiene productos
 * asociados, impidiendo la eliminación por integridad de datos.
 */
public class ProductTypeAssociatedException extends BaseBusinessException {
    
    public ProductTypeAssociatedException() {
        super(ErrorCode.PRODUCT_TYPE_ASSOCIATED);
    }
    
    public ProductTypeAssociatedException(Long id) {
        super(ErrorCode.PRODUCT_TYPE_ASSOCIATED, 
              String.format("El tipo de producto con ID %d no se puede eliminar porque está asociado con productos", id));
    }
    
    public ProductTypeAssociatedException(String name) {
        super(ErrorCode.PRODUCT_TYPE_ASSOCIATED, 
              String.format("El tipo de producto '%s' no se puede eliminar porque está asociado con productos", name));
    }
}
