package co.unicauca.edu.co.contables.notifications.application.input;

import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto;

/**
 * @brief Use case interface for processing notifications.
 * Defines the contract for handling invoice due reminder events.
 */
public interface IProcessNotificationUseCase {
    void processInvoiceDueReminder(InvoiceDueReminderEventDto eventData);
}
