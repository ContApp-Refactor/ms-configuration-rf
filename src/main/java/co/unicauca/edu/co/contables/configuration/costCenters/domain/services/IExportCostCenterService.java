package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import org.springframework.core.io.Resource;

/**
 * Servicio para exportar centros de costo en formato Excel.
 */
public interface IExportCostCenterService {

    /**
     * Exporta centros de costo de una empresa en formato Excel.
     * El archivo incluye código, nombre y estado del centro de costo.
     *
     * @param enterpriseId ID de la empresa
     * @param status Estado de los centros de costo
     * @return Resource que contiene el archivo Excel
     */
    Resource exportCostCenters(String enterpriseId, Boolean status);
}
