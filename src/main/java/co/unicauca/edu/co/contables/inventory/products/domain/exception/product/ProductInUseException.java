package co.unicauca.edu.co.contables.inventory.products.domain.exception.product;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción lanzada cuando se intenta modificar o eliminar un producto en uso
 *
 * Esta excepción se utiliza cuando un producto tiene un contador de uso mayor a cero,
 * indicando que otros servicios están utilizándolo activamente.
 */
public class ProductInUseException extends BaseBusinessException {
    
    public ProductInUseException() {
        super(ErrorCode.PRODUCT_IN_USE);
    }
    
    public ProductInUseException(String customMessage) {
        super(ErrorCode.PRODUCT_IN_USE, customMessage);
    }
}

