package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceNotificationEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("invoiceNotificationEventPublisherAdapter")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsInvoiceNotificationEventPublisher implements IInvoiceNotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishInvoiceDueReminder(InvoiceDueReminderEventDto invoiceDueReminderEventDto) {
        log.info("Publishing invoice due reminder event for third party ID: {}",
                invoiceDueReminderEventDto.getThirdPartyId());

        eventPublisher.publishEvent(new InvoiceDueReminderEvent(invoiceDueReminderEventDto));
    }
}
