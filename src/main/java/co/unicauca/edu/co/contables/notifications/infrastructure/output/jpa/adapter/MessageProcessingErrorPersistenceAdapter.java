package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.notifications.application.output.IMessageProcessingErrorPersistencePort;
import co.unicauca.edu.co.contables.notifications.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.mapper.IMessageProcessingErrorPersistenceMapper;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IMessageProcessingErrorRepository;

import lombok.RequiredArgsConstructor;

@Component("notificationsMessageProcessingErrorPersistenceAdapter")
@RequiredArgsConstructor
public class MessageProcessingErrorPersistenceAdapter implements IMessageProcessingErrorPersistencePort {

    private final IMessageProcessingErrorRepository repository;
    private final IMessageProcessingErrorPersistenceMapper mapper;

    @Override
    public Optional<MessageProcessingError> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<MessageProcessingError> findLastRecord() {
        return repository.findFirstByOrderByIdDesc().map(mapper::toDomain);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
    
}
