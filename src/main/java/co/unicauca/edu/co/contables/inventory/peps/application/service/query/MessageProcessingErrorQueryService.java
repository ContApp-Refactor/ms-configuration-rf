package co.unicauca.edu.co.contables.inventory.peps.application.service.query;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IMessageProcessingErrorQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for MessageProcessingError query operations
 * 
 * Provides read access to message processing error records with
 * pagination support, individual record access and latest record access.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingErrorQueryService implements  IMessageProcessingErrorQueryPort{
   
    private final IMessageProcessingErrorQueryOutputPort messageProcessingErrorQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    @Override
    public Optional<MessageProcessingError> findById(Long id) {
        log.info("Finding message processing error by id: {}", id);
        Optional<MessageProcessingError> messageProcessingError = messageProcessingErrorQueryRepositoryPort.findById(id);
        if (!messageProcessingError.isPresent()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NOT_FOUND,"Message processing error with id:"+id));
        }else{
            try {
                messageProcessingError.get().requireValid();
            } catch (IllegalArgumentException ex) {
                log.warn("MPE {} has invalid state: {}", id, ex.getMessage());
            
            }
            log.debug("MPE found: {}", messageProcessingError.get().summary(180));
            
        }
        return messageProcessingError;
    }

    /**
     * @brief Finds the most recent message processing error
     * @return Optional containing the latest error record if found
     */
   @Override
    public Optional<MessageProcessingError> findLastRecord() {
        log.info("Finding the most recent message processing error");
        Optional<MessageProcessingError> messageProcessingError = messageProcessingErrorQueryRepositoryPort.findLastRecord();
        if (!messageProcessingError.isPresent()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, "No message processing errors found"));
        } else {
            try {
                messageProcessingError.get().requireValid();
            } catch (IllegalArgumentException ex) {
                log.warn("Last MPE has invalid state: {}", ex.getMessage());
            }
            log.debug("Last MPE: {}", messageProcessingError.get().summary(180));
        }
        return messageProcessingError;
    }

    /**
     * @brief Retrieves all message processing errors with pagination
     * @param pageable Pagination parameters
     * @return Paginated error records
     */
    @Override
    public Page<MessageProcessingError> findAll(Pageable pageable) {
        log.info("Finding all message processing errors with pagination - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return messageProcessingErrorQueryRepositoryPort.findAll(pageable);
    }
    
}
