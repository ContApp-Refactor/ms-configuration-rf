package co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports;

/**
 * @brief Output port for message processing error handling
 * 
 * Provides mechanism to persist error information when
 * message processing operations fail, enabling audit and debugging.
 */
public interface IMessageErrorHandlingPort {
    
    /**
     * @brief Saves error information when message processing fails
     * @param eventType Type of event that failed (may be null)
     * @param errorDescription Description of the error that occurred
     * @param messageData Message data in JSON format
     * @param entityType Type of entity being processed
     */
    void saveProcessingError(String eventType, String errorDescription, String messageData, String entityType);
}
