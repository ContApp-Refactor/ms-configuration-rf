package co.unicauca.edu.co.contables.inventory.products.domain.exception.category;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para categoría no encontrada
 *
 * Se lanza cuando se intenta acceder a una categoría que no existe
 * o no está activa en el sistema.
 */
public class CategoryNotFoundException extends BaseBusinessException {
    
    public CategoryNotFoundException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
    
    public CategoryNotFoundException(Long id) {
        super(ErrorCode.CATEGORY_NOT_FOUND, 
              String.format("No se encontró la categoría con ID %d", id));
    }
    
    public CategoryNotFoundException(String nameOrMessage, boolean isCustomMessage) {
        super(ErrorCode.CATEGORY_NOT_FOUND, 
              isCustomMessage ? nameOrMessage : 
              String.format("No se encontró la categoría con nombre %s", nameOrMessage));
    }
}
