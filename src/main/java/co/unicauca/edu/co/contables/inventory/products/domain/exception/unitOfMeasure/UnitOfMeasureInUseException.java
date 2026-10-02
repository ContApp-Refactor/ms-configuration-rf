package co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para unidad de medida con productos en uso
 *
 * Se lanza cuando se intenta editar una unidad de medida que contiene productos
 * que ya han sido utilizados, impidiendo la modificación por integridad de datos.
 */
public class UnitOfMeasureInUseException extends BaseBusinessException {

    public UnitOfMeasureInUseException() {
        super(ErrorCode.UNITOFMEASURE_IN_USE);
    }
}
