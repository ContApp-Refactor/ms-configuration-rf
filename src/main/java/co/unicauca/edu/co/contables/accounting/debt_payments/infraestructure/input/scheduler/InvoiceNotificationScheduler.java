package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.scheduler;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IInvoiceNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * Scheduler class responsible for sending daily invoice notifications.
 * It triggers the notification process at a specified time each day.
 * Uses the IInvoiceNotificationUseCase to handle the business logic.
 */
public class InvoiceNotificationScheduler {
    private final IInvoiceNotificationUseCase invoiceNotificationUseCase;

    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9 AM
    public void sendDailyInvoiceNotifications() {
        invoiceNotificationUseCase.processAndPublishDueInvoices();
    }
}
