package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para centro de costo ya existente
 *
 * Se lanza cuando un centro de costo ya existe (por código o nombre)
 * dentro de una empresa, evitando duplicados.
 */
public class CostCentersAlreadyExistsException extends BaseBusinessException {

    public CostCentersAlreadyExistsException() {
        super(CostCentersErrorCode.COST_CENTER_ALREADY_EXISTS);
    }

    public CostCentersAlreadyExistsException(String code, String idEnterprise) {
        super(
            CostCentersErrorCode.COST_CENTER_ALREADY_EXISTS,
            String.format("Ya existe un centro de costo con código %s en la empresa %s", code, idEnterprise)
        );
    }

    public CostCentersAlreadyExistsException(String name, String idEnterprise, boolean byName) {
        super(
            CostCentersErrorCode.COST_CENTER_ALREADY_EXISTS,
            String.format("Ya existe un centro de costo con nombre '%s' en la empresa %s", name, idEnterprise)
        );
    }

    public CostCentersAlreadyExistsException(String code, String name, String idEnterprise) {
        super(
            CostCentersErrorCode.COST_CENTER_ALREADY_EXISTS,
            String.format(
                "Ya existe un centro de costo con código %s o nombre '%s' en la empresa %s",
                code,
                name,
                idEnterprise
            )
        );
    }
}


