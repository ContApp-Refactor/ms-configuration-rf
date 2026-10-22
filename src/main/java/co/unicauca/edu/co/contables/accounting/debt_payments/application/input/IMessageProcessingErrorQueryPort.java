package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.MessageProcessingError;

/**
 * @brief Input port for MessageProcessingError query operations
 * 
 * Provides query capabilities for retrieving message processing error records
 * with pagination support, individual record access and latest record access.
 */
public interface IMessageProcessingErrorQueryPort {
    
    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    MessageProcessingError findById(Long id);
    
    /**
     * @brief Finds the most recent message processing error
     * @return Optional containing the latest error record if found
     */
    MessageProcessingError findLastRecord();
}
