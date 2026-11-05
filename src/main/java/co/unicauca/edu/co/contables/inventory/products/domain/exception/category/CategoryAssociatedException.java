package co.unicauca.edu.co.contables.inventory.products.domain.exception.category;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para categoría con asociaciones activas
 *
 * Se lanza cuando se intenta eliminar una categoría que tiene productos
 * asociados, impidiendo la eliminación por integridad de datos.
 */
public class CategoryAssociatedException extends BaseBusinessException {
    
    public CategoryAssociatedException() {
        super(ErrorCode.CATEGORY_ASSOCIATED);
    }
    
    public CategoryAssociatedException(Long id) {
        super(ErrorCode.CATEGORY_ASSOCIATED, 
              String.format("La categoría con ID %d no se puede eliminar porque está asociada con productos", id));
    }
    
    public CategoryAssociatedException(String name) {
        super(ErrorCode.CATEGORY_ASSOCIATED, 
              String.format("La categoría '%s' no se puede eliminar porque está asociada con productos", name));
    }
}
