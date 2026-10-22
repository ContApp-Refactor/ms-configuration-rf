package co.unicauca.edu.co.contables.inventory.products.domain.exception.category;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para categoría con productos en uso
 *
 * Se lanza cuando se intenta editar una categoría que contiene productos
 * que ya han sido utilizados, impidiendo la modificación por integridad de datos.
 */
public class CategoryInUseException extends BaseBusinessException {

    public CategoryInUseException() {
        super(ErrorCode.CATEGORY_IN_USE);
    }
}
