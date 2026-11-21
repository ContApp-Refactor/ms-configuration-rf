package co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUsageInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.dto.TaxUsageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsTaxUsageListener {

    private final ITaxUsageInputPort taxUsageInputPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTaxUsed(TaxUsageDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid tax usage event received");
                return;
            }

            log.info("Registrando uso de impuesto ID: {}, empresa: {}, cantidad: {}",
                    event.getTaxId(), event.getEnterpriseId(), event.getQuantityUsed());
            taxUsageInputPort.incrementUsageCount(event.getTaxId(), event.getEnterpriseId());
            log.info("Uso registrado correctamente para impuesto ID: {}", event.getTaxId());
        } catch (Exception e) {
            log.error("Error procesando evento de impuesto: {}", e.getMessage());
        }
    }

    private boolean isValidEvent(TaxUsageDto data) {
        if (data == null) {
            log.warn("Evento es null");
            return false;
        }

        if (data.getTaxId() == null) {
            log.warn("TaxId es null - campo obligatorio");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId es null o vacío - campo obligatorio");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed es null o inválido - campo obligatorio");
            return false;
        }

        return true;
    }
}
