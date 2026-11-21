package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.listener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSyncDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("debtPaymentsSpringEventsInvoiceEventListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsInvoiceEventListener {

    private static final String ENTITY_TYPE = "Invoice";

    private final InvoicePersistenceAdapter invoicePersistenceAdapter;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;
    private final IEventRecoveryActionPort<EventDto<InvoiceSyncDto>> invoiceRecoveryActionPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInvoiceSale(InvoiceSaleEvent event) {
        handleInvoiceEvent(toLocalEvent(event));
    }

    private EventDto<InvoiceSyncDto> toLocalEvent(InvoiceSaleEvent event) {
        if (event == null) {
            return null;
        }

        String type = event.getType() != null ? event.getType().name() : null;
        ReceiptSalesDtoRequest source = event.getData();
        if (source == null) {
            return new EventDto<>(type, null);
        }

        InvoiceSyncDto data = new InvoiceSyncDto();
        data.setFactCode(source.getFactCode());
        data.setEntId(source.getEntId());
        data.setThirdId(source.getThirdId());
        data.setTotalValue(source.getTotalValue());
        data.setTotalPay(source.getTotalPay());
        data.setPendingValue(source.getPendingValue());
        data.setCreationDate(source.getCreationDate());
        data.setExpirationDate(source.getExpirationDate());
        data.setActive(source.isActive());
        data.setAccountingAccount(source.getAccountingAccount());

        return new EventDto<>(type, data);
    }

    private void handleInvoiceEvent(EventDto<InvoiceSyncDto> event) {
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

    private String validateEvent(EventDto<InvoiceSyncDto> event) {
        if (event == null) {
            return "Event is null";
        }
        if (event.getData() == null) {
            return "Event data is null";
        }
        if (event.getType() == null) {
            return "Event type is null";
        }

        InvoiceSyncDto data = event.getData();
        if (data.getFactCode() == null) {
            return "FactCode is null";
        }
        if (data.getAccountingAccount() == null) {
            return "AccountingAccount is null";
        }
        if (data.getEntId() == null) {
            return "EntId is null";
        }
        if (data.getExpirationDate() == null) {
            return "ExpirationDate is null";
        }
        if (data.getPendingValue() == null) {
            return "PendingValue is null";
        }
        if (data.getThirdId() == null) {
            return "ThirdId is null";
        }
        if (data.getTotalPay() == null) {
            return "TotalPay is null";
        }
        if (data.getTotalValue() == null) {
            return "TotalValue is null";
        }

        return null;
    }

    private void processEvent(EventDto<InvoiceSyncDto> event) {
        InvoiceSyncDto dto = event.getData();
        String factCode = dto.getFactCode().toString() != null ? dto.getFactCode().toString() : "N/A";

        try {
            switch (event.getType()) {
                case "SALE":
                    log.info("Processing SALE event for invoice factCode: {}", dto.getFactCode());
                    invoicePersistenceAdapter.saveOrUpdate(dto);
                    log.info("Successfully processed sale for invoice factCode: {}", dto.getFactCode());
                    break;

                case "DELETED":
                    log.info("Processing DELETE event for invoice factCode: {}", dto.getFactCode());
                    invoicePersistenceAdapter.delete(dto.getFactCode());
                    log.info("Successfully processed delete for invoice factCode: {}", dto.getFactCode());
                    break;

                default:
                    log.warn("Unknown event type '{}' for invoice. Message will be acknowledged and ignored.",
                            event.getType());
            }
        } catch (Exception e) {
            log.error("Database operation failed for invoice factCode: {}. Error: {}", factCode, e.getMessage());
            throw e;
        }
    }

    private void handleValidationError(EventDto<InvoiceSyncDto> event, String validationError) {
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

    private void handleProcessingError(Exception e, EventDto<InvoiceSyncDto> event) {
        try {
            log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage(), e);

            boolean recoveryExecuted = attemptRecovery(event);

            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        String.format("Processing error: %s%s",
                                e.getMessage(),
                                recoveryExecuted ? " (Recovery action executed)" : ""),
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception handlerException) {
            log.error("Error handling processing error: {}", handlerException.getMessage());
        }
    }

    private boolean attemptRecovery(EventDto<InvoiceSyncDto> event) {
        if (invoiceRecoveryActionPort == null) {
            log.debug("No recovery action port configured for {}", ENTITY_TYPE);
            return false;
        }

        try {
            if (invoiceRecoveryActionPort.canHandle(event)) {
                log.info("Attempting recovery action for failed {} event", ENTITY_TYPE);
                boolean success = invoiceRecoveryActionPort.executeRecoveryAction(event);

                if (success) {
                    log.info("Recovery action executed successfully for {} event", ENTITY_TYPE);
                } else {
                    log.warn("Recovery action failed for {} event", ENTITY_TYPE);
                }

                return success;
            } else {
                log.debug("Recovery action cannot handle this {} event", ENTITY_TYPE);
                return false;
            }
        } catch (Exception recoveryException) {
            log.error("Error executing recovery action for {} event: {}",
                    ENTITY_TYPE, recoveryException.getMessage(), recoveryException);
            return false;
        }
    }

    private String extractEventType(EventDto<InvoiceSyncDto> event) {
        if (event == null) {
            return null;
        }

        return event.getType() != null ? event.getType() : null;
    }

    private String convertEventToJson(EventDto<InvoiceSyncDto> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }

        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \"" +
                    (event.getType() != null ? event.getType() : "null") + "\"}";
        }

        return JsonUtils.toJsonWithNullHandling(event.getData());
    }
}
