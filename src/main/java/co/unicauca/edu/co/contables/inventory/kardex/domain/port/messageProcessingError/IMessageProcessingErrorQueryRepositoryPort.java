package co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;

/**
 * @brief Output port for MessageProcessingError query operations
 * 
 * Defines the contract for accessing message processing error data
 * from the persistence layer.
 */
public interface IMessageProcessingErrorQueryRepositoryPort {
    
    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    Optional<MessageProcessingError> findById(Long id);
    
    /**
     * @brief Finds the most recent message processing error
     * @return Optional containing the latest error record if found
     */
    Optional<MessageProcessingError> findLastRecord();
    
    /**
     * @brief Retrieves all message processing errors with pagination
     * @param pageable Pagination parameters
     * @return Paginated error records
     */
    Page<MessageProcessingError> findAll(Pageable pageable);
}