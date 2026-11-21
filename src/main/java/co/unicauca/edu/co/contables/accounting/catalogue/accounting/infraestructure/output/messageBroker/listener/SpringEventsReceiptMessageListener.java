package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IReceiptProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptDetailEventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptEventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IReceiptEventInboundMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("catalogueSpringEventsReceiptMessageListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsReceiptMessageListener {

    private static final String ENTITY_TYPE = "Receipt";

    private final IReceiptProcessInputPort receiptProcessInputPort;
    private final IReceiptEventInboundMapper receiptEventMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onReceiptEvent(ReceiptAccountingEvent event) {
        handleReceiptEvent(toLocalEvent(event));
    }

    private EventDTO<ReceiptEventDTO> toLocalEvent(ReceiptAccountingEvent event) {
        if (event == null) {
            return null;
        }

        ReceiptEventDto source = event.getData();
        if (source == null) {
            return new EventDTO<>(event.getType(), null);
        }

        ReceiptEventDTO data = new ReceiptEventDTO();
        data.setId(source.getId());
        data.setReceiptCode(source.getReceiptCode());
        data.setThirdPartyId(source.getThirdPartyId());
        data.setEnterpriseId(source.getEnterpriseId());
        data.setReceiptTypeId(source.getReceiptTypeId());
        data.setPaymentMethodId(source.getPaymentMethodId());
        data.setPaymentMethodAccount(source.getPaymentMethodAccount());
        data.setStatus(source.getStatus());
        data.setIssueDate(source.getIssueDate());
        data.setTotalAmount(source.getTotalAmount());
        data.setObservations(source.getObservations());
        data.setLedgerAccountId(source.getLedgerAccountId());
        data.setCenterCostId(source.getCenterCostId());
        data.setDetails(toLocalDetails(source.getDetails()));

        return new EventDTO<>(event.getType(), data);
    }

    private List<ReceiptDetailEventDTO> toLocalDetails(List<ReceiptDetailEventDto> details) {
        if (details == null) {
            return null;
        }

        List<ReceiptDetailEventDTO> localDetails = new ArrayList<>(details.size());
        for (ReceiptDetailEventDto detail : details) {
            if (detail == null) {
                localDetails.add(null);
                continue;
            }

            ReceiptDetailEventDTO localDetail = new ReceiptDetailEventDTO();
            localDetail.setInvoiceId(detail.getInvoiceId());
            localDetail.setAmountPaid(detail.getAmountPaid());
            localDetail.setInvoiceCode(detail.getInvoiceCode());
            localDetail.setAccountingAccount(detail.getAccountingAccount());
            localDetails.add(localDetail);
        }

        return localDetails;
    }

    private void handleReceiptEvent(EventDTO<ReceiptEventDTO> event) {
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

    private String validateEvent(EventDTO<ReceiptEventDTO> event) {
        if (event == null) {
            return "Event is null";
        }
        if (event.getData() == null) {
            return "Event data is null";
        }
        if (event.getType() == null) {
            return "Event type is null";
        }

        ReceiptEventDTO data = event.getData();
        if (data.getReceiptCode() == null || data.getReceiptCode().isBlank()) {
            return "ReceiptCode is null or blank";
        }
        if (data.getThirdPartyId() == null) {
            return "ThirdPartyId is null";
        }
        if (data.getEnterpriseId() == null) {
            return "EnterpriseId is null";
        }
        if (data.getStatus() == null) {
            return "Status is null";
        }
        if (data.getReceiptTypeId() == null) {
            return "ReceiptTypeId is null";
        }

        if (data.getReceiptTypeId() == 1L) {
            if (data.getDetails() == null || data.getDetails().isEmpty()) {
                return "Details list is null or empty";
            }
            for (ReceiptDetailEventDTO detail : data.getDetails()) {
                if (detail.getAccountingAccount() == null) {
                    return "Detail AccountCode is null";
                }
                if (detail.getAmountPaid() == null) {
                    return "Detail AmountPaid is null";
                }
                if (detail.getInvoiceId() == null) {
                    return "Detail InvoiceId is null";
                }
                if (detail.getInvoiceCode() == null || detail.getInvoiceCode().isBlank()) {
                    return "Detail InvoiceCode is null or blank";
                }
            }
        } else if (data.getReceiptTypeId() == 2L) {
            if (data.getLedgerAccountId() == null) {
                return "LedgerAccountId is null for ReceiptTypeId 2";
            }
        } else {
            return "Unsupported ReceiptTypeId";
        }

        if (data.getTotalAmount() == null) {
            return "TotalAmount is null";
        }
        if (data.getPaymentMethodId() == null) {
            return "PaymentMethodId is null";
        }
        if (data.getPaymentMethodAccount() == null) {
            return "PaymentMethodAccount is null";
        }
        if (data.getIssueDate() == null) {
            return "IssueDate is null";
        }
        if (data.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            return "TotalAmount is negative";
        }

        return null;
    }

    private void processEvent(EventDTO<ReceiptEventDTO> event) {
        ReceiptEventDTO receiptDTO = event.getData();
        try {
            Receipt receipt = receiptEventMapper.toDomain(receiptDTO);
            switch (event.getType()) {
                case ReceiptAccountingEvent.RECEIPT_CREATED:
                    receiptProcessInputPort.processReceiptCreation(receipt);
                    break;
                case ReceiptAccountingEvent.RECEIPT_VOIDED:
                    receiptProcessInputPort.processReceiptVoid(receipt);
                    break;
                default:
                    log.warn("Tipo de evento no soportado: '{}'. El mensaje será ignorado.", event.getType());
                    break;
            }
            log.info("Recibo {} procesado exitosamente.", receiptDTO.getReceiptCode());
        } catch (Exception e) {
            log.error("Error al procesar el evento para el recibo {}.", receiptDTO.getReceiptCode(), e);
            throw e;
        }
    }

    private void handleValidationError(EventDTO<ReceiptEventDTO> event, String validationError) {
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

    private void handleProcessingError(Exception e, EventDTO<ReceiptEventDTO> event) {
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

    private String extractEventType(EventDTO<ReceiptEventDTO> event) {
        return event != null ? event.getType() : "unknown";
    }

    private String convertEventToJson(EventDTO<ReceiptEventDTO> event) {
        if (event == null || event.getData() == null) {
            return "{\"error\": \"Event or event data is null\"}";
        }
        return JsonUtils.receiptDtoToJsonWithNullHandling(event.getData());
    }
}
