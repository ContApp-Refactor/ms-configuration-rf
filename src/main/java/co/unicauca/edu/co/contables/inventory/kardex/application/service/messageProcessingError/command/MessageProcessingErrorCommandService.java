package co.unicauca.edu.co.contables.inventory.kardex.application.service.messageProcessingError.command;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.messageProcessingError.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError.IMessageProcessingErrorCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError.IMessageProcessingErrorQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for MessageProcessingError command operations
 * 
 * Handles message processing error deletion operations with validation
 * and transaction management.
 */
@Service("kardexMessageProcessingErrorCommandService")@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageProcessingErrorCommandService implements IMessageProcessingErrorCommandPort {
    
    private final IMessageProcessingErrorCommandRepositoryPort messageProcessingErrorCommandRepositoryPort;
    private final IMessageProcessingErrorQueryRepositoryPort messageProcessingErrorQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Deletes all message processing error records
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all message processing errors");
        messageProcessingErrorCommandRepositoryPort.deleteAll();
        log.info("All message processing errors deleted successfully");
    }

    /**
     * @brief Deletes a specific message processing error by ID
     * @param id Error record identifier
     */
    @Override
    public void deleteById(Long id) {
        log.info("Deleting message processing error by id: {}", id);

        // Validate existence and log a concise summary if found
        var maybeError = messageProcessingErrorQueryRepositoryPort.findById(id);
        if (!maybeError.isPresent()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404,
                    messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, "Message processing error with id: " + id));
            return; // safeguard after formatter call
        }

        // Optionally enforce invariants from the domain
        try {
            maybeError.get().requireValid();
        } catch (IllegalArgumentException ex) {
            log.warn("Deleting MPE {} with invalid state: {}", id, ex.getMessage());
        }

        log.debug("About to delete MPE: {}", maybeError.get().summary(180));

        messageProcessingErrorCommandRepositoryPort.deleteById(id);
        log.info("Message processing error with id {} deleted successfully", id);
    }
}