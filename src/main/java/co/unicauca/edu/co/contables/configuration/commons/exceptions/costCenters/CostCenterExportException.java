package co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para errores en exportación de centros de costo
 *
 * Se lanza cuando ocurre un error durante la exportación de centros de costo,
 * permitiendo mensajes personalizados y manejo de causas.
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
