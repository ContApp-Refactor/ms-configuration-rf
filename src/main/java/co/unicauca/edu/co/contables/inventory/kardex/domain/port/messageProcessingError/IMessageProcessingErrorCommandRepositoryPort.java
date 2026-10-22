package co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError;

/**
 * @brief Output port for MessageProcessingError command operations
 * 
 * Defines the contract for persisting message processing error data
 * modifications in the persistence layer.
 */
public interface IMessageProcessingErrorCommandRepositoryPort {
    
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