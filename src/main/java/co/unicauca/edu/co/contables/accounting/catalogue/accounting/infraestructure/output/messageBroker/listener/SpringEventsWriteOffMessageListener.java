package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IWriteOffProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.PortfolioWriteOffResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IPortfolioWriteOffInboundMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ValidationException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.InvoiceSummaryResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.WriteOffDetailResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSummaryEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffDetailEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("catalogueSpringEventsWriteOffMessageListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsWriteOffMessageListener {

    private static final String ENTITY_TYPE = "PortfolioWriteOff";

    private final IWriteOffProcessInputPort writeOffProcessInputPort;
    private final IPortfolioWriteOffInboundMapper writeOffEventMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWriteOffEvent(WriteOffAccountingEvent event) {
        log.info("Evento de Castigo recibido: '{}' para el código: {}",
                event.getType(),
                event.getData() != null ? event.getData().getCode() : "N/A");
        handleWriteOffEvent(toLocalEvent(event));
    }

    private EventDTO<PortfolioWriteOffResponse> toLocalEvent(WriteOffAccountingEvent event) {
        if (event == null) {
            return null;
        }

        PortfolioWriteOffEventDto source = event.getData();
        if (source == null) {
            return new EventDTO<>(event.getType(), null);
        }

        PortfolioWriteOffResponse data = PortfolioWriteOffResponse.builder()
                .id(source.getId())
                .code(source.getCode())
                .justification(source.getJustification())
                .totalAmount(source.getTotalAmount())
                .writeOffDate(source.getWriteOffDate())
                .debitAuxiliaryAccount(source.getDebitAuxiliaryAccount())
                .debitAuxiliaryAccountId(source.getDebitAuxiliaryAccountId())
                .thirdId(source.getThirdId())
                .status(source.getStatus())
                .enterpriseId(source.getEnterpriseId())
                .centerCostId(source.getCenterCostId())
                .details(toLocalDetails(source.getDetails()))
                .build();

        return new EventDTO<>(event.getType(), data);
    }

    private List<WriteOffDetailResponse> toLocalDetails(List<WriteOffDetailEventDto> details) {
        if (details == null) {
            return null;
        }

        List<WriteOffDetailResponse> localDetails = new ArrayList<>(details.size());
        for (WriteOffDetailEventDto detail : details) {
            if (detail == null) {
                localDetails.add(null);
                continue;
            }

            localDetails.add(WriteOffDetailResponse.builder()
                    .amountWrittenOff(detail.getAmountWrittenOff())
                    .invoice(toLocalInvoice(detail.getInvoice()))
                    .build());
        }

        return localDetails;
    }

    private InvoiceSummaryResponse toLocalInvoice(InvoiceSummaryEventDto source) {
        if (source == null) {
            return null;
        }

        return InvoiceSummaryResponse.builder()
                .id(source.getId())
                .factCode(source.getFactCode())
                .totalValue(source.getTotalValue())
                .pendingValue(source.getPendingValue())
                .expirationDate(source.getExpirationDate())
                .accountingAccount(source.getAccountingAccount())
                .build();
    }

    private void handleWriteOffEvent(EventDTO<PortfolioWriteOffResponse> event) {
        try {
            log.info("Received {} message from in-process bus", ENTITY_TYPE);

            validateEvent(event);

            processEvent(event);
            log.info("{} message processed successfully", ENTITY_TYPE);

        } catch (ValidationException ve) {
            log.warn("Invalid {} event received: {}. Saving error to database.", ENTITY_TYPE, ve.getMessage());
            handleValidationError(event, ve);

        } catch (Exception e) {
            handleProcessingError(e, event);
        }
    }

    private void validateEvent(EventDTO<PortfolioWriteOffResponse> event) throws ValidationException {
        if (event == null)
            throw new ValidationException("Validation failed: Event is null");
        if (event.getData() == null)
            throw new ValidationException("Validation failed: Event data is null");
        if (event.getType() == null)
            throw new ValidationException("Validation failed: Event type is null");

        PortfolioWriteOffResponse data = event.getData();
        if (data.getId() == null)
            throw new ValidationException("Validation failed: Id is null");
        if (data.getTotalAmount() == null)
            throw new ValidationException("Validation failed: TotalAmount is null");
        if (data.getWriteOffDate() == null)
            throw new ValidationException("Validation failed: WriteOffDate is null");
        if (data.getDebitAuxiliaryAccount() == null)
            throw new ValidationException("Validation failed: DebitAuxiliaryAccount is null or blank");
        if (data.getDebitAuxiliaryAccountId() == null)
            throw new ValidationException("Validation failed: DebitAuxiliaryAccountId is null");

        if (data.getDetails() != null) {
            for (var detail : data.getDetails()) {
                if (detail.getAmountWrittenOff() == null)
                    throw new ValidationException("Validation failed: Detail AmountWrittenOff is null");
                if (detail.getInvoice() == null) {
                    throw new ValidationException("Validation failed: Detail Invoice is null");
                } else {
                    if (detail.getInvoice().getId() == null)
                        throw new ValidationException("Validation failed: Detail Invoice Id is null");
                    if (detail.getInvoice().getFactCode() == null || detail.getInvoice().getFactCode().isBlank())
                        throw new ValidationException("Validation failed: Detail Invoice Code is null or blank");
                    if (detail.getInvoice().getTotalValue() == null)
                        throw new ValidationException("Validation failed: Detail Invoice TotalValue is null");
                    if (detail.getInvoice().getPendingValue() == null)
                        throw new ValidationException("Validation failed: Detail Invoice PendingValue is null");
                    if (detail.getInvoice().getExpirationDate() == null)
                        throw new ValidationException("Validation failed: Detail Invoice ExpirationDate is null");
                    if (detail.getInvoice().getAccountingAccount() == null)
                        throw new ValidationException("Validation failed: Detail Invoice AccountingAccount is null");
                }
            }
        } else {
            throw new ValidationException("Validation failed: Details is null");
        }

        if (data.getCode() == null || data.getCode().isBlank())
            throw new ValidationException("Validation failed: Code is null or blank");
        if (data.getThirdId() == null)
            throw new ValidationException("Validation failed: ThirdId is null");
        if (data.getEnterpriseId() == null)
            throw new ValidationException("Validation failed: EnterpriseId is null");
        if (data.getStatus() == null)
            throw new ValidationException("Validation failed: Status is null");
    }

    private void processEvent(EventDTO<PortfolioWriteOffResponse> event) {
        PortfolioWriteOffResponse writeOffDTO = event.getData();
        try {
            PortfolioWriteOff writeOff = writeOffEventMapper.toDomain(writeOffDTO);

            switch (event.getType()) {
                case WriteOffAccountingEvent.WRITEOFF_CONFIRMED:
                    writeOffProcessInputPort.processWriteOffConfirmation(writeOff);
                    break;
                case WriteOffAccountingEvent.WRITEOFF_VOIDED:
                    writeOffProcessInputPort.processWriteOffAnnulment(writeOff);
                    break;
                default:
                    log.warn("Tipo de evento de castigo no soportado: '{}'. Mensaje ignorado.", event.getType());
                    break;
            }
            log.info("Evento de castigo {} procesado exitosamente.", writeOffDTO.getCode());
        } catch (Exception e) {
            log.error("Error al procesar evento para castigo {}.", writeOffDTO.getCode(), e);
            throw e;
        }
    }

    private void handleValidationError(EventDTO<PortfolioWriteOffResponse> event, ValidationException ve) {
        try {
            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        ve.getMessage(),
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    private void handleProcessingError(Exception e, EventDTO<PortfolioWriteOffResponse> event) {
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

    private String extractEventType(EventDTO<PortfolioWriteOffResponse> event) {
        return event != null ? event.getType() : "unknown";
    }

    private String convertEventToJson(EventDTO<PortfolioWriteOffResponse> event) {
        if (event == null || event.getData() == null) {
            return "{\"error\": \"Event or event data is null\"}";
        }
        return JsonUtils.writeOffDtoToJsonWithNullHandling(event.getData());
    }
}
