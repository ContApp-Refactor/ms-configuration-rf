package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.base;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ValidationException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMessageListener<U> {
     protected IMessageErrorHandlingPort messageErrorHandlingPort;

     protected IEventRecoveryActionPort<U> eventRecoveryActionPort;

    protected void handleMessage(U event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());
            
            validateEvent(event); 
            
            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());

        } catch (ValidationException ve) {
            log.warn("Invalid {} event received: {}. Saving error to database.", getEntityType(), ve.getMessage());
            handleValidationError(event, ve);
            acknowledgeMessage(channel, deliveryTag);

        } catch (Exception e) {
            handleProcessingError(e, event, channel, deliveryTag);
        }
    }

    protected abstract void processEvent(U event);
    protected abstract void validateEvent(U event) throws ValidationException; 
    protected abstract String getEntityType();
    protected abstract String extractEventType(U event);
    protected abstract String convertEventToJson(U event);

    private void handleProcessingError(Exception e, U event, Channel channel, long deliveryTag) {
        try {
            log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);
            
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = String.format("Processing error: %s", e.getMessage());
                
                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }
            
            acknowledgeMessage(channel, deliveryTag);
        } catch (Exception ackException) {
            log.error("Error acknowledging message: {}", ackException.getMessage());
        }
    }

    private void handleValidationError(U event, ValidationException e) {
        try {
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = e.getMessage(); 
                
                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }
        } catch (Exception ex) {
            log.error("Error saving validation error to database: {}", ex.getMessage());
        }
    }
    
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", e.getMessage());
        }
    }
    
}
