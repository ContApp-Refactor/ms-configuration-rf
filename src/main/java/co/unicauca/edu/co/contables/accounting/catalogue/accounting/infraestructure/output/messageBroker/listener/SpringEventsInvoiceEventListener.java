package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IInvoiceProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IInvoiceEventMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("catalogueSpringEventsInvoiceEventListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsInvoiceEventListener {

    private static final String ENTITY_TYPE = "Invoice";

    private final InvoicePersistenceAdapter invoicePersistenceAdapter;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;
    private final IInvoiceProcessInputPort invoiceProcessInputPort;
    private final IInvoiceEventMapper invoiceEventMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInvoiceSale(InvoiceSaleEvent event) {
        handleInvoiceEvent(toLocalEvent(event));
    }

    private EventDTO<InvoiceSyncDto> toLocalEvent(InvoiceSaleEvent event) {
        if (event == null) {
            return null;
        }

        String type = event.getType() != null ? event.getType().name() : null;
        ReceiptSalesDtoRequest source = event.getData();
        if (source == null) {
            return new EventDTO<>(type, null);
        }

        InvoiceSyncDto data = new InvoiceSyncDto();
        data.setFactCode(source.getFactCode());
        data.setEntId(source.getEntId());
        data.setThirdId(source.getThirdId());
        data.setTotalValue(toBigDecimal(source.getTotalValue()));
        data.setTotalPay(toBigDecimal(source.getTotalPay()));
        data.setPendingValue(toBigDecimal(source.getPendingValue()));
        data.setExpirationDate(source.getExpirationDate());
        data.setCreationDate(source.getCreationDate());
        data.setActive(source.isActive());
        data.setAccountingAccount(source.getAccountingAccount());

        return new EventDTO<>(type, data);
    }

    private BigDecimal toBigDecimal(Long value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private void handleInvoiceEvent(EventDTO<InvoiceSyncDto> event) {
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

    private String validateEvent(EventDTO<InvoiceSyncDto> event) {
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
        if (data.getCreationDate() == null) {
            return "CreationDate is null";
        }

        return null;
    }

    private void processEvent(EventDTO<InvoiceSyncDto> event) {
        InvoiceSyncDto dto = event.getData();
        String factCode = dto.getFactCode().toString() != null ? dto.getFactCode().toString() : "N/A";

        if (dto.getCreationDate() == null) {
            log.warn("CreationDate is null for invoice factCode: {}. Setting to current date.", factCode);
            dto.setCreationDate(LocalDate.now());
        }

        try {
            switch (event.getType()) {
                case "SALE":
                    InvoiceReplica invoice = invoiceEventMapper.toDomain(dto);
                    invoiceProcessInputPort.processInvoiceCreation(invoice);
                    log.info("Successfully processed sale for invoice factCode: {}", factCode);
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

    private void handleValidationError(EventDTO<InvoiceSyncDto> event, String validationError) {
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

    private void handleProcessingError(Exception e, EventDTO<InvoiceSyncDto> event) {
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

    private String extractEventType(EventDTO<InvoiceSyncDto> event) {
        if (event == null) {
            return null;
        }

        return event.getType() != null ? event.getType() : null;
    }

    private String convertEventToJson(EventDTO<InvoiceSyncDto> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }

        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \"" +
                    (event.getType() != null ? event.getType() : "null") + "\"}";
        }

        return JsonUtils.invoiceDtoToJsonWithNullHandling(event.getData());
    }
}
