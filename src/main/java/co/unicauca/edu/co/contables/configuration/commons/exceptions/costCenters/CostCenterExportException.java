package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando ocurre un error durante la exportación de centros de costo.
 */
public class CostCenterExportException extends BaseBusinessException {
    
    public CostCenterExportException() {
        super(CostCentersErrorCode.COST_CENTER_EXPORT_ERROR);
    }
    
    public CostCenterExportException(String customMessage) {
        super(CostCentersErrorCode.COST_CENTER_EXPORT_ERROR, customMessage);
    }
    
    public CostCenterExportException(String customMessage, Throwable cause) {
        super(CostCentersErrorCode.COST_CENTER_EXPORT_ERROR, customMessage, cause);
    }
}
