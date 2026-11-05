package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.adapter;

import java.time.Instant;

import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.MessageErrorEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository.IMessageErrorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// El nombre del bean por defecto (messageErrorHandlingAdapter) ya lo usa
// debt_payments y Spring aborta con ConflictingBeanDefinitionException. El
// nombre explícito deja convivir ambos adaptadores: cada uno implementa su
// propio puerto en su propio paquete.
@Repository("auditMessageErrorHandlingAdapter")
@RequiredArgsConstructor
@Slf4j
public class MessageErrorHandlingAdapter implements IMessageErrorHandlingPort {

    private final IMessageErrorRepository messageRepository;

    @Override
    public void saveProcessingError(String eventType, String errorDescription, String messageData, String entityType,
            String errorStage) {
        try {
            MessageErrorEntity error = MessageErrorEntity.builder()
                    .eventType(eventType)
                    .entityType(entityType)
                    .errorDescription(errorDescription)
                    .messageData(messageData)
                    .errorAt(Instant.now())
                    .errorStage(errorStage)
                    .build();

            messageRepository.save(error);
            log.info("Error saved for entity type: {}, event type: {}", entityType, eventType);

        } catch (Exception e) {
            log.error("Failed to save error to database: {}", e.getMessage());
        }
    }

}
