package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageNotification;

import java.util.List;

public interface IResourceUsageNotifierPort {
    /**
     * Notify all provided resource usage notifications.
     * @param notifications A list of ResourceUsageNotification instances to be notified.
     */
    void notifyAll(List<ResourceUsageNotification> notifications);
}
