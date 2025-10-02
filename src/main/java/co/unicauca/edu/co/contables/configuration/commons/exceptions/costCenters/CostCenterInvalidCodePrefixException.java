package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se intenta actualizar el código de un centro de costo hijo
 * sin mantener el prefijo del código del padre.
 * 
 * Esta validación permite que los hijos siempre mantengan el código del padre como prefijo.
 */
public class CostCenterInvalidCodePrefixException extends BaseBusinessException {

    public CostCenterInvalidCodePrefixException() {
        super(CostCentersErrorCode.COST_CENTER_INVALID_CODE_PREFIX);
    }

    public CostCenterInvalidCodePrefixException(String parentCode, String receivedCode) {
        super(
            CostCentersErrorCode.COST_CENTER_INVALID_CODE_PREFIX,
            String.format(
                "El código debe comenzar con el prefijo del padre '%s'. Código recibido: '%s'",
                parentCode,
                receivedCode
            )
        );
    }

    public CostCenterInvalidCodePrefixException(String message) {
        super(CostCentersErrorCode.COST_CENTER_INVALID_CODE_PREFIX, message);
    }
}
