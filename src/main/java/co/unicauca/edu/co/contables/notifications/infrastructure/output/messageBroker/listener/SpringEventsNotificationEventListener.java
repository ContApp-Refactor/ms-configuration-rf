package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEvent;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.notifications.application.input.IProcessNotificationUseCase;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("notificationsSpringEventsNotificationListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsNotificationEventListener {

    private static final String ENTITY_TYPE = "Notification.InvoiceDueReminder";

    private final IProcessNotificationUseCase processNotificationUseCase;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInvoiceDueReminder(InvoiceDueReminderEvent event) {
        handleNotificationEvent(toLocalEvent(event));
    }

    private EventDto<InvoiceDueReminderEventDto> toLocalEvent(InvoiceDueReminderEvent event) {
        if (event == null) {
            return null;
        }

        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto source =
                event.getData();
        if (source == null) {
            return new EventDto<>(event.getType(), null);
        }

        List<InvoiceDetailEventDto> details = source.getInvoiceDetails() == null
                ? null
                : source.getInvoiceDetails().stream()
                        .map(this::toLocalDetail)
                        .collect(Collectors.toList());

        InvoiceDueReminderEventDto data = new InvoiceDueReminderEventDto();
        data.setThirdPartyId(source.getThirdPartyId());
        data.setInvoiceDetails(details);

        return new EventDto<>(event.getType(), data);
    }

    private InvoiceDetailEventDto toLocalDetail(
            co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto source) {
        if (source == null) {
            return null;
        }

        InvoiceDetailEventDto detail = new InvoiceDetailEventDto();
        detail.setInvoiceId(source.getInvoiceId());
        detail.setInvoiceCode(source.getInvoiceCode());
        detail.setExpirationDate(source.getExpirationDate());
        detail.setTotalAmount(source.getTotalAmount());
        detail.setPendingValue(source.getPendingValue());
        return detail;
    }

    private void handleNotificationEvent(EventDto<InvoiceDueReminderEventDto> event) {
        try {
            log.info("Received {} message from in-process bus", ENTITY_TYPE);

            String validationError = validateEvent(event);
            if (validationError != null) {
                log.warn("Invalid {} event received: {}. Saving error to database.", ENTITY_TYPE, validationError);
                handleValidationError(event, validationError);
                return;
            }

            processEvent(event);
            log.info("{} message processed successfully", ENTITY_TYPE);
        } catch (Exception e) {
            handleProcessingError(e, event);
        }
    }

    private String validateEvent(EventDto<InvoiceDueReminderEventDto> event) {
        if (event == null) {
            return "Event is null";
        }
        if (event.getData() == null) {
            return "Event data is null";
        }
        if (event.getType() == null) {
            return "Event type is null";
        }

        InvoiceDueReminderEventDto data = event.getData();
        if (data.getThirdPartyId() == null) {
            return "thirdPartyId is null";
        }
        if (data.getInvoiceDetails() == null || data.getInvoiceDetails().isEmpty()) {
            return "invoiceDetails is null or empty";
        }
        if (data.getInvoiceDetails().stream().anyMatch(Objects::isNull)) {
            return "invoiceDetails list contains null items";
        }

        for (InvoiceDetailEventDto detail : data.getInvoiceDetails()) {
            if (detail.getInvoiceId() == null) {
                return "invoiceId is null in invoiceDetails";
            }
            if (detail.getInvoiceCode() == null) {
                return "invoiceCode is null in invoiceDetails";
            }
            if (detail.getExpirationDate() == null) {
                return "expirationDate is null in invoiceDetails";
            }
            if (detail.getTotalAmount() == null) {
                return "totalAmount is null in invoiceDetails";
            }
            if (detail.getPendingValue() == null) {
                return "pendingValue is null in invoiceDetails";
            }
        }

        return null;
    }

    private void processEvent(EventDto<InvoiceDueReminderEventDto> event) {
        String eventType = event.getType();
        InvoiceDueReminderEventDto data = event.getData();
        Long thirdPartyId = data != null ? data.getThirdPartyId() : null;

        try {
            String tenantIdFromContext = TenantContext.getTenantId();
            log.info("[LISTENER-LOG] Valor de TenantContext ANTES de la operación de BD: '{}'", tenantIdFromContext);

            switch (eventType) {
                case "INVOICE_DUE_REMINDER":
                    processNotificationUseCase.processInvoiceDueReminder(data);
                    break;
                default:
                    log.warn("Evento de notificación recibido con tipo desconocido: {}. Descartando.", eventType);
                    break;
            }
            log.info("Evento de notificación procesado exitosamente. Tipo: {}", eventType);
        } catch (Exception e) {
            log.error("Error de negocio al procesar notificación para el tercero: {}. Error: {}", thirdPartyId,
                    e.getMessage());
            throw e;
        }
    }

    private void handleValidationError(EventDto<InvoiceDueReminderEventDto> event, String validationError) {
        try {
            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        "Validation failed: " + validationError,
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    private void handleProcessingError(Exception e, EventDto<InvoiceDueReminderEventDto> event) {
        try {
            log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage(), e);

            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        String.format("Processing error: %s", e.getMessage()),
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception handlerException) {
            log.error("Error handling processing error: {}", handlerException.getMessage());
        }
    }

    private String extractEventType(EventDto<InvoiceDueReminderEventDto> event) {
        return event != null ? event.getType() : "unknown";
    }

    private String convertEventToJson(EventDto<InvoiceDueReminderEventDto> event) {
        if (event == null || event.getData() == null) {
            return "{\"error\": \"Event or event data is null\"}";
        }
        return JsonUtils.reminderDtoToJsonWithNullHandling(event.getData());
    }
}
