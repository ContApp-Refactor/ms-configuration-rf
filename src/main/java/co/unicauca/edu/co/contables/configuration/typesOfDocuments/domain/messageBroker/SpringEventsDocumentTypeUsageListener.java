package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.DocumentTypeUsageDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeUsage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsDocumentTypeUsageListener {

    private final IDocumentTypeUsage documentTypeUsage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDocumentTypeUsed(DocumentTypeUsageDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid document type usage event received");
                return;
            }

            log.info("Registrando uso de tipo de documento ID: {}, empresa: {}, cantidad: {}",
                    event.getDocumentTypeId(), event.getEnterpriseId(), event.getQuantityUsed());
            documentTypeUsage.incrementUsageCount(event.getDocumentTypeId(), event.getEnterpriseId());
            log.info("Uso registrado correctamente para tipo de documento ID: {}", event.getDocumentTypeId());
        } catch (Exception e) {
            log.error("Error procesando evento de tipo de documento: {}", e.getMessage());
        }
    }

    private boolean isValidEvent(DocumentTypeUsageDto data) {
        if (data == null) {
            log.warn("Evento es null");
            return false;
        }

        if (data.getDocumentTypeId() == null) {
            log.warn("DocumentTypeId es null - campo obligatorio");
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
