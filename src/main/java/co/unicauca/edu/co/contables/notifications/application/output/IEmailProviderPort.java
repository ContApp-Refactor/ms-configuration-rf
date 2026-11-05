package co.unicauca.edu.co.contables.notifications.application.output;

/**
 * @brief Port interface for sending emails.
 * Defines the contract for email notification services.
 */

public interface IEmailProviderPort {
    void sendInvoiceReminderEmail(String recipientName, String recipientEmail, String htmlContent);
}
