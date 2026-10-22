package co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports;

import java.util.List;

public interface ResourceUsageProvider {
    /**
     * Return a list of resource usage notifications.
     * @return A list of objects implementing ResourceUsageNotification.
     */
    List<ResourceUsageNotification> getUsageNotifications();
}
