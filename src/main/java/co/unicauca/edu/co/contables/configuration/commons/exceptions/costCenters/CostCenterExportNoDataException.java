package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando no hay centros de costo para exportar.
 */
public class CostCenterExportNoDataException extends BaseBusinessException {
    
    public CostCenterExportNoDataException() {
        super(CostCentersErrorCode.COST_CENTER_EXPORT_NO_DATA);
    }
    
    public CostCenterExportNoDataException(Boolean status) {
        super(CostCentersErrorCode.COST_CENTER_EXPORT_NO_DATA,
              status != null 
                ? String.format("No hay centros de costo %s para exportar", status ? "activos" : "inactivos")
                : "No hay centros de costo para exportar");
    }
}
