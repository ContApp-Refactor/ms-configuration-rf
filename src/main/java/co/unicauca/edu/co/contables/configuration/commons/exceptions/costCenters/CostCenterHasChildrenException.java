package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para centro de costo con hijos
 *
 * Se lanza cuando se intenta eliminar un centro de costo que tiene
 * centros de costo hijos asociados, impidiendo la eliminación.
 */
public class CostCenterHasChildrenException extends BaseBusinessException {

    public CostCenterHasChildrenException() {
        super(CostCentersErrorCode.COST_CENTER_HAS_CHILDREN);
    }

    public CostCenterHasChildrenException(String code, String name) {
        super(
            CostCentersErrorCode.COST_CENTER_HAS_CHILDREN,
            String.format("No se puede eliminar el centro de costo '%s' (%s) porque tiene centros de costo hijos asociados", name, code)
        );
    }

    public CostCenterHasChildrenException( String code) {
        super(
            CostCentersErrorCode.COST_CENTER_HAS_CHILDREN,
            String.format("No se puede eliminar el centro de costo '%s' porque tiene centros de costo hijos asociados", code)
        );
    }
}
