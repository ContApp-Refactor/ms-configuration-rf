package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para prefijo de código inválido en centros de costo
 *
 * Se lanza cuando se intenta actualizar el código de un centro de costo hijo
 * sin mantener el prefijo del código del padre, asegurando la jerarquía.
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
