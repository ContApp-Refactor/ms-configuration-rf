package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.MessageProcessingError;

import java.util.Optional;

public interface IMessageProcessingErrorPersistencePort {
    Optional<MessageProcessingError> findById(Long id);
    Optional<MessageProcessingError> findLastRecord();
    void deleteAll();
}
