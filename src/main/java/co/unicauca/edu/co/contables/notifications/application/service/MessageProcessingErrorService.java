package co.unicauca.edu.co.contables.notifications.application.service;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.notifications.application.input.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.notifications.application.input.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.notifications.application.output.IMessageProcessingErrorPersistencePort;
import co.unicauca.edu.co.contables.notifications.domain.exception.MessageProcessingErrorNotFoundException;
import co.unicauca.edu.co.contables.notifications.domain.model.MessageProcessingError;

import lombok.RequiredArgsConstructor;

@Service("notificationsMessageProcessingErrorService")
@RequiredArgsConstructor
public class MessageProcessingErrorService implements IMessageProcessingErrorCommandPort, IMessageProcessingErrorQueryPort{
    
    private final IMessageProcessingErrorPersistencePort persistencePort;

    @Override
    public MessageProcessingError findLastRecord() {
        return persistencePort.findLastRecord().orElseThrow(() -> 
            new MessageProcessingErrorNotFoundException("No message processing errors found"));
    }

    @Override
    public void deleteAll() {
        persistencePort.deleteAll();
    }

    @Override
    public MessageProcessingError findById(Long id) {
        return persistencePort.findById(id).orElseThrow(() -> 
            new MessageProcessingErrorNotFoundException("MessageProcessingError with ID " + id + " not found"));
    }
    
}
