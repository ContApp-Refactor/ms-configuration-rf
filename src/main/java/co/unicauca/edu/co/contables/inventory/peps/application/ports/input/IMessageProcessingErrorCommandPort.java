package co.unicauca.edu.co.contables.inventory.peps.application.ports.input;

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
    
    /**
     * @brief Deletes a specific message processing error by ID
     * @param id Error record identifier
     */
    void deleteById(Long id);
}
