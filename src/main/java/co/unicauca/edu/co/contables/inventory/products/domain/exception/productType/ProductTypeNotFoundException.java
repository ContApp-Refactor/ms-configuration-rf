package co.unicauca.edu.co.contables.inventory.products.domain.exception.productType;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para tipo de producto no encontrado
 *
 * Se lanza cuando se intenta acceder a un tipo de producto que no existe
 * o no está activo en el sistema.
 */
public class ProductTypeNotFoundException extends BaseBusinessException {
    
    public ProductTypeNotFoundException() {
        super(ErrorCode.PRODUCT_TYPE_NOT_FOUND);
    }
    
    public ProductTypeNotFoundException(Long id) {
        super(ErrorCode.PRODUCT_TYPE_NOT_FOUND, 
              String.format("No se encontró el tipo de producto con ID %d", id));
    }
    
    public ProductTypeNotFoundException(String nameOrMessage, boolean isCustomMessage) {
        super(ErrorCode.PRODUCT_TYPE_NOT_FOUND, 
              isCustomMessage ? nameOrMessage : 
              String.format("No se encontró el tipo de producto con nombre %s", nameOrMessage));
    }
}
