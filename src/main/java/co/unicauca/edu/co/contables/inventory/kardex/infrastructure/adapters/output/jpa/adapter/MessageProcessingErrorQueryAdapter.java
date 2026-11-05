package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError.IMessageProcessingErrorQueryRepositoryPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IMessageProcessingErrorEntityMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief JPA adapter for MessageProcessingError read operations
 * 
 * Implements the output port for reading message processing error records
 * using JPA repository and entity mapping.
 */
@Repository("kardexMessageProcessingErrorQueryAdapter")@RequiredArgsConstructor
@Slf4j
public class MessageProcessingErrorQueryAdapter implements IMessageProcessingErrorQueryRepositoryPort {

    private final IMessageProcessingErrorRepository messageProcessingErrorRepository;
    private final IMessageProcessingErrorEntityMapper messageProcessingErrorMapper;

    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    @Override
    public Optional<MessageProcessingError> findById(Long id) {
        log.debug("Finding message processing error by id: {}", id);
        Optional<MessageProcessingErrorEntity> entity = messageProcessingErrorRepository.findById(id);
        return entity.map(messageProcessingErrorMapper::toDomain);
    }

    /**
     * @brief Finds the most recent message processing error
     * @return Optional containing the latest error record if found
     */
    @Override
    public Optional<MessageProcessingError> findLastRecord() {
        log.debug("Finding the most recent message processing error");
        Optional<MessageProcessingErrorEntity> entity = messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc();
        return entity.map(messageProcessingErrorMapper::toDomain);
    }

    /**
     * @brief Retrieves all message processing errors with pagination
     * @param pageable Pagination parameters
     * @return Paginated error records
     */
    @Override
    public Page<MessageProcessingError> findAll(Pageable pageable) {
        log.debug("Finding all message processing errors with pagination - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<MessageProcessingErrorEntity> entities = messageProcessingErrorRepository.findAll(pageable);
        return entities.map(messageProcessingErrorMapper::toDomain);
    }
}