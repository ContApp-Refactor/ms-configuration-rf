package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta modificar o eliminar un centro de costo que está siendo utilizado
 *
 * Se utiliza en operaciones de edición y eliminación de centros de costo para prevenir
 * la modificación o eliminación cuando el centro de costo tiene movimientos contables asociados.
 */
public class CostCenterInUseException extends BaseBusinessException {

  
    public CostCenterInUseException(String costCenterCode) {
        super(CostCentersErrorCode.COST_CENTER_IN_USE,
              "No se puede eliminar el centro de costo " + costCenterCode + " porque tiene movimientos contables");
    }

 
    public CostCenterInUseException(String costCenterCode, boolean isEditOperation) {
        super(CostCentersErrorCode.COST_CENTER_IN_USE,
              isEditOperation ?
              "No se puede editar el centro de costo " + costCenterCode + " porque tiene movimientos contables" :
              "No se puede eliminar el centro de costo " + costCenterCode + " porque tiene movimientos contables");
    }

    public CostCenterInUseException() {
        super(CostCentersErrorCode.COST_CENTER_IN_USE);
    }

    public CostCenterInUseException(Throwable cause) {
        super(CostCentersErrorCode.COST_CENTER_IN_USE, CostCentersErrorCode.COST_CENTER_IN_USE.getMessage(), cause);
    }
}
