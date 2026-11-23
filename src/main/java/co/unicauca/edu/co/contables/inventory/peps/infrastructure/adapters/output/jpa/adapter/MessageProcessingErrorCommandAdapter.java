package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IMessageProcessingErrorCommandOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief JPA adapter for MessageProcessingError write operations
 * 
 * Implements the output port for persisting message processing error data
 * modifications using JPA repository.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingErrorCommandAdapter implements IMessageProcessingErrorCommandOutputPort {
    private final IMessageProcessingErrorRepository messageProcessingErrorRepository;

    /**
     * @brief Deletes all message processing error records
     */
    @Override
    public void deleteAll() {
        log.debug("Deleting all message processing errors from database");
        messageProcessingErrorRepository.deleteAll();
    }

    /**
     * @brief Deletes a specific message processing error by ID
     * @param id Error record identifier
     */
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting message processing error by id: {}", id);
        messageProcessingErrorRepository.deleteById(id);
    }
    
}
