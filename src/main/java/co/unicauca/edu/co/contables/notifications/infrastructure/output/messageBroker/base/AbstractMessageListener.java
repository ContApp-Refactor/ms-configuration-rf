package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.base;

import com.rabbitmq.client.Channel;
import co.unicauca.edu.co.contables.notifications.domain.exception.ValidationException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;

import lombok.extern.slf4j.Slf4j;
// NOTA: La parte de Recovery es opcional, la dejamos comentada por ahora.
// import com.tuempresa.ms_notifications.domain.ports.IEventRecoveryActionPort;

@Slf4j
public abstract class AbstractMessageListener<T> {

    protected IMessageErrorHandlingPort messageErrorHandlingPort;
    // protected IEventRecoveryActionPort<T> eventRecoveryActionPort;

    protected void handleMessage(T event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());
            
            // 1. Llamamos al nuevo método de validación. Si falla, saltará al catch de ValidationException.
            validateEvent(event); 
            
            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());

        } catch (ValidationException ve) { // <-- 2. Catch específico para errores de validación
            log.warn("Invalid {} event received: {}. Saving error to database.", getEntityType(), ve.getMessage());
            handleValidationError(event, ve); // <-- 3. Pasamos la excepción para obtener el mensaje
            acknowledgeMessage(channel, deliveryTag);

        } catch (Exception e) {
            handleProcessingError(e, event, channel, deliveryTag);
        }
    }

    protected abstract void processEvent(T event);
    protected abstract void validateEvent(T event) throws ValidationException; 
    protected abstract String getEntityType();
    protected abstract String extractEventType(T event);
    protected abstract String convertEventToJson(T event);

    private void handleProcessingError(Exception e, T event, Channel channel, long deliveryTag) {
        try {
            log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);
            
            // boolean recoveryExecuted = attemptRecovery(event); // Opcional
            
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

    // 5. Modificamos este método para que acepte la excepción y use su mensaje.
    private void handleValidationError(T event, ValidationException e) {
        try {
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                // ¡AQUÍ ESTÁ LA MAGIA! Usamos el mensaje de la excepción.
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