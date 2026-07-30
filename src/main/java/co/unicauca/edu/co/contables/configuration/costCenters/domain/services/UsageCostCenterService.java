package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Servicio para manejo del contador de uso de centros de costo
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageCostCenterService implements IUsageCostCenter {

    private final ICostCenterService costCenterService;

    @Override
    public void incrementUsageCount(Long costCenterId) {
        log.debug("Incrementando contador de uso para centro de costo con ID: {}", costCenterId);

        try {
            // Buscar el centro de costo por ID
            CostCenter costCenter = costCenterService.findById(costCenterId);
            if (costCenter != null) {
                // Calcular el nuevo valor del contador de uso
                Integer newUsageCount = costCenter.getUsageCount() != null ?
                    costCenter.getUsageCount() + 1 : 1;

                // Actualizar el contador de uso usando el servicio
                costCenterService.updateUsageCount(costCenterId, newUsageCount);
                log.debug("Contador de uso incrementado para centro de costo con ID: {} (nuevo valor: {})",
                         costCenterId, newUsageCount);
            } else {
                log.warn("Centro de costo con ID {} no encontrado, no se puede incrementar el contador de uso", costCenterId);
            }
        } catch (Exception e) {
            log.error("Error al incrementar contador de uso para centro de costo ID {}: {}", costCenterId, e.getMessage());
        }
    }
}
