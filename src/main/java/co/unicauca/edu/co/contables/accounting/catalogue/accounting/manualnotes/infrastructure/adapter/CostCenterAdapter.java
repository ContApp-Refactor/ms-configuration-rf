package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.adapter;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.CostCenterPort;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import org.springframework.stereotype.Component;

/** Adapter local de solo lectura a centros de costo activos de último nivel. */
@Component
public class CostCenterAdapter implements CostCenterPort {
    private final ICostCenterService costCenters;
    public CostCenterAdapter(ICostCenterService costCenters) { this.costCenters = costCenters; }
    @Override
    public boolean existsAndIsActive(String code, String enterpriseId) {
        return costCenters.findActiveLastLevelCostCenters(enterpriseId).stream()
                .anyMatch(center -> code.equals(center.getCode()) && Boolean.TRUE.equals(center.getStatus()));
    }
}
