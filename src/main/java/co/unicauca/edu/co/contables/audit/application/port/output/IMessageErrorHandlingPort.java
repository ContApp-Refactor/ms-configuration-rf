package co.unicauca.edu.co.contables.audit.application.port.output;

public interface IMessageErrorHandlingPort {

    /**
     * @brief Saves error information when message processing fails
     * @param eventType        Type of event that failed (may be null)
     * @param errorDescription Description of the error that occurred
     * @param messageData      Message data in JSON format
     * @param entityType       Type of entity being processed
     */
    void saveProcessingError(
            String eventType,
            String errorDescription,
            String messageData,
            String entityType,
            String errorStage);
}
