package co.unicauca.edu.co.contables.notifications.application.input;

import co.unicauca.edu.co.contables.notifications.domain.model.MessageProcessingError;

public interface IMessageProcessingErrorQueryPort {

    /**
     * @brief Finds a message processing error by ID
     * @param id Error record identifier
     * @return Optional containing the error record if found
     */
    MessageProcessingError findById(Long id);

    MessageProcessingError findLastRecord();
}
