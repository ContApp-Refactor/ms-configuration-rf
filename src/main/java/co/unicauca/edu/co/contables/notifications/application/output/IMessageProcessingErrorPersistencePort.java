package co.unicauca.edu.co.contables.notifications.application.output;

import java.util.Optional;
import co.unicauca.edu.co.contables.notifications.domain.model.MessageProcessingError;

public interface IMessageProcessingErrorPersistencePort {
    Optional<MessageProcessingError> findById(Long id);
    Optional<MessageProcessingError> findLastRecord();
    void deleteAll();
}

