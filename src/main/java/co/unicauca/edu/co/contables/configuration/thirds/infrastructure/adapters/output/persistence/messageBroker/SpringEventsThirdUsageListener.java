package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.ThirdUsedEvenDto;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.IThirdUsagePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsThirdUsageListener {

    private final IThirdUsagePort thirdUsagePort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onThirdUsed(ThirdUsedEvenDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid third usage event received");
                return;
            }

            log.info("Registrando uso de tercero ID: {}, cantidad: {}",
                    event.getThirdId(), event.getQuantityUsed());
            thirdUsagePort.incrementUsageCount(event.getThirdId());
            log.info("Uso registrado correctamente para tercero ID: {}", event.getThirdId());
        } catch (Exception e) {
            log.error("Error procesando evento de tercero: {}", e.getMessage());
        }
    }

    private boolean isValidEvent(ThirdUsedEvenDto data) {
        if (data == null) {
            log.warn("Event is null");
            return false;
        }

        if (data.getThirdId() == null) {
            log.warn("ThirdId is null - required field");;
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed is null or invalid - required field");
            return false;
        }

        return true;
    }
}
