package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.base;



import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;


import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.BaseException;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for RabbitMQ message listeners.
 * 
 * Provides common functionality for:
 * - Message processing with error handling
 * - Intelligent retry logic (domain vs infrastructure errors)
 * - Standardized logging
 * - Dead Letter Queue handling
 * 
 * @param <T> Event data type
 * @param <U> Event type enum
 */
@Slf4j
public abstract class AbstractMessageListener<T,U> {
    /**
     * @brief Port for handling processing errors
     * 
     * Must be injected by child classes to enable error persistence.
     */
    protected IMessageErrorHandlingPort messageErrorHandlingPort;

    /**
     * @brief Port for executing recovery actions
     * 
     * Optional - can be injected by child classes if automatic recovery is needed.
     */
    protected IEventRecoveryActionPort<T> eventRecoveryActionPort;

    /**
     * @brief Main method for handling incoming messages
     * 
     * Implements common logic for validation, processing, and acknowledgment.
     * 
     * @param event The event data
     * @param channel The RabbitMQ channel
     * @param deliveryTag The delivery tag for acknowledgment
     */
    protected void handleMessage(T event, Channel channel, long deliveryTag) {
        try {
            log.info("Received {} message from queue", getEntityType());
            
            if (!isValidEvent(event)) {
                log.warn("Invalid {} event received, saving error to database", getEntityType());
                handleValidationError(event);
                acknowledgeMessage(channel, deliveryTag);
                return;
            }
            
            processEvent(event);
            acknowledgeMessage(channel, deliveryTag);
            log.info("{} message processed successfully", getEntityType());
            
        } catch (Exception e) {
            handleProcessingError(e, event, channel, deliveryTag);
        }
    }

    /**
     * @brief Processes the specific event
     * 
     * Must be implemented by each listener to define business logic.
     * @param event The event to process
     */
    protected abstract void processEvent(T event);

    /**
     * @brief Validates if the event is valid for processing
     * @param event The event to validate
     * @return True if valid, false otherwise
     */
    protected abstract boolean isValidEvent(T event);

    /**
     * @brief Returns the entity type handled by this listener
     * 
     * Used for logging purposes.
     * @return String representing the entity type
     */
    protected abstract String getEntityType();

    /**
     * @brief Handles errors during message processing
     * 
     * Logs the error, attempts recovery, saves error details to DB,
     * and acknowledges the message to prevent infinite loops.
     */
    private void handleProcessingError(Exception e, T event, Channel channel, long deliveryTag) {
        try {
             // Log differently based on exception type
            if (e instanceof BaseException) {
                // For business exceptions, only show message without stack trace
                log.error("Error processing {} message: {}", getEntityType(), e.getMessage());
            } else {
                // For other exceptions, show full stack trace
                log.error("Error processing {} message: {}", getEntityType(), e.getMessage(), e);
            }
            
            // Attempt recovery action if available
            boolean recoveryExecuted = attemptRecovery(event);
            
            // Save error to database
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = String.format("Processing error: %s%s", 
                    e.getMessage(), 
                    recoveryExecuted ? " (Recovery action executed)" : "");
                
                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }
            
            acknowledgeMessage(channel, deliveryTag); // ACK to prevent redelivery
        } catch (Exception ackException) {
            log.error("Error acknowledging message: {}", ackException.getMessage());
        }
    }

    /**
     * @brief Handles event validation errors
     * 
     * Saves validation failure details to the database.
     */
    private void handleValidationError(T event) {
        try {
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String specificError=getValidationErrorMessage();
                 String errorDescription = specificError != null 
                    ? "Validation failed: " + specificError
                    : "Validation failed: Required fields are missing or invalid";
              
                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, getEntityType());
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    /**
     * @brief Attempts to execute a recovery action when event processing fails
     * 
     * @param event The event that failed processing
     * @return true if a recovery action was executed, false otherwise
     */
    private boolean attemptRecovery(T event) {
        if (eventRecoveryActionPort == null) {
            log.debug("No recovery action port configured for {}", getEntityType());
            return false;
        }
        
        try {
            if (eventRecoveryActionPort.canHandle(event)) {
                log.info("Attempting recovery action for failed {} event", getEntityType());
                boolean success = eventRecoveryActionPort.executeRecoveryAction(event);
                
                if (success) {
                    log.info("Recovery action executed successfully for {} event", getEntityType());
                } else {
                    log.warn("Recovery action failed for {} event", getEntityType());
                }
                
                return success;
            } else {
                log.debug("Recovery action cannot handle this {} event", getEntityType());
                return false;
            }
        } catch (Exception recoveryException) {
            log.error("Error executing recovery action for {} event: {}", 
                getEntityType(), recoveryException.getMessage(), recoveryException);
            return false;
        }
    }

    /**
     * @brief Sends message acknowledgment
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: {}", e.getMessage());
        }
    }

    /**
     * @brief Gets a safe entity identifier for logging
     * 
     * Optional method that can be overridden by specific listeners.
     * @param event The event object
     * @return String representation of the event or "unknown"
     */
    protected String getEntityIdentifierSafely(T event) {
        return event != null ? event.toString() : "unknown";
    }

    /**
     * @brief Utility method to extract message body as String
     */
    protected String getMessageBodyAsString(Message message) {
        try {
            return new String(message.getBody());
        } catch (Exception e) {
            log.warn("Error converting message body to string: {}", e.getMessage());
            return "unavailable";
        }
    }

    /**
     * @brief Extracts the event type from the message
     * 
     * Must be implemented by each listener.
     * @param event The event to extract type from
     * @return String representing event type, or null if undetermined
     */
    protected abstract String extractEventType(T event);

    /**
     * @brief Converts event to JSON for DB storage
     * 
     * Must be implemented by each listener.
     * @param event The event to convert
     * @return JSON formatted string of event data
     */
    protected abstract String convertEventToJson(T event);

     /**
     * @brief Gets specific validation error message if available
     * @return String with specific error message, or null if default
     */
    protected String getValidationErrorMessage() {
        return null; // Default implementation
    }
    
}
