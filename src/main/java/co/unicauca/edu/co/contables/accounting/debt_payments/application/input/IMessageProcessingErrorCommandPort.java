package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

/**
 * @brief Input port for MessageProcessingError command operations
 * 
 * Provides command capabilities for managing message processing error records
 * including deletion operations.
 */
public interface IMessageProcessingErrorCommandPort {
    
    /**
     * @brief Deletes all message processing error records
     */
    void deleteAll();
}
