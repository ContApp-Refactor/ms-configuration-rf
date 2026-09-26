package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IMessageProcessingErrorCommandPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IMessageProcessingErrorQueryPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IMessageProcessingErrorPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.MessageProcessingErrorNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.MessageProcessingError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProcessingErrorService implements IMessageProcessingErrorCommandPort, IMessageProcessingErrorQueryPort{
    
    private final IMessageProcessingErrorPersistencePort persistencePort;
    
    @Override
    public MessageProcessingError findById(Long id) {
        return persistencePort.findById(id).orElseThrow(() -> 
            new MessageProcessingErrorNotFoundException("MessageProcessingError with ID " + id + " not found"));
    }

    @Override
    public MessageProcessingError findLastRecord() {
        return persistencePort.findLastRecord().orElseThrow(() -> 
            new MessageProcessingErrorNotFoundException("No message processing errors found"));
    }

    @Override
    public void deleteAll() {
        persistencePort.deleteAll();
    }
    
}
