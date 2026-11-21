package co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.CostCenterUsedDto;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.IUsageCostCenter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsCostCenterUsageListener {

    private final IUsageCostCenter usageCostCenter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCostCenterUsed(CostCenterUsedDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid cost center usage event received");
                return;
            }

            log.info("Registrando uso de centro de costo ID: {}, cantidad: {}",
                    event.getCostCenterId(), event.getQuantityUsed());
            usageCostCenter.incrementUsageCount(event.getCostCenterId());
            log.info("Uso registrado correctamente para centro de costo ID: {}", event.getCostCenterId());
        } catch (Exception e) {
            log.error("Error procesando evento de centro de costo: {}", e.getMessage());
        }
    }

    private boolean isValidEvent(CostCenterUsedDto data) {
        if (data == null) {
            log.warn("Evento es null");
            return false;
        }

        if (data.getCostCenterId() == null) {
            log.warn("CostCenterId es null - campo obligatorio");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed es null o inválido - campo obligatorio");
            return false;
        }

        return true;
    }
}
