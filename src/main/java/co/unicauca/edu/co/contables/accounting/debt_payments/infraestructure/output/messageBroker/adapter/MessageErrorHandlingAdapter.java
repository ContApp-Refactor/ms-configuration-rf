package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IMessageProcessingErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @brief Adapter for handling message processing errors
 * 
 * Persists error information when message processing fails,
 * providing audit trail and debugging capabilities.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageErrorHandlingAdapter implements IMessageErrorHandlingPort {

    private final IMessageProcessingErrorRepository errorRepository;

    /**
     * @brief Saves processing error information to database
     * @param eventType Type of event that failed
     * @param errorDescription Description of the error
     * @param messageData Original message data
     * @param entityType Type of entity being processed
     */
    @Override
    public void saveProcessingError(String eventType, String errorDescription, String messageData, String entityType) {
        try {
            MessageProcessingErrorEntity errorEntity = new MessageProcessingErrorEntity();
            errorEntity.setEventType(eventType != null ? eventType : "null_event_type");
            errorEntity.setErrorDescription(errorDescription);
            errorEntity.setMessageData(messageData);
            errorEntity.setEntityType(entityType);
            
            errorRepository.save(errorEntity);
            log.info("Processing error saved for entity type: {}, event type: {}", entityType, eventType);
            
        } catch (Exception e) {
            log.error("Failed to save processing error to database: {}", e.getMessage(), e);
        }
    }
}
