package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

/**
 * @brief Puerto de entrada para operaciones de uso de centros de costo
 */
public interface IUsageCostCenter {

    /**
     * @brief Incrementa el contador de uso del centro de costo
     * @param costCenterId ID del centro de costo
     */
    void incrementUsageCount(Long costCenterId);
}
