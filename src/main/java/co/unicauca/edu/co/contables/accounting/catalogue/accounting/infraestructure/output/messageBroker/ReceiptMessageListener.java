package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker;

import org.apache.commons.math3.analysis.function.Abs;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IReceiptProcessInputPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ValidationException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptEventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IReceiptEventInboundMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.RabbitAccountingConfig;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ReceiptMessageListener extends AbstractMessageListener<EventDTO<ReceiptEventDTO>> {
    private final IReceiptProcessInputPort receiptProcessInputPort;
    private final IReceiptEventInboundMapper receiptEventMapper;

    @Qualifier("messageErrorHandlingAdapter")
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;

    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
    }

    @RabbitListener(queues = RabbitAccountingConfig.RECEIPT_ACCOUNTING_QUEUE)
    public void processReceiptEvent(EventDTO<ReceiptEventDTO> event, Message message, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Evento recibido: '{}' para el recibo: {}",
                event.getType(),
                event.getData() != null ? event.getData().getReceiptCode() : "N/A");
        handleMessage(event, channel, deliveryTag);
    }

    @Override
    protected void validateEvent(EventDTO<ReceiptEventDTO> event) throws ValidationException {
        if (event == null)
            throw new ValidationException("Validation failed: Event is null");
        if (event.getData() == null)
            throw new ValidationException("Validation failed: Event data is null");
        if (event.getType() == null)
            throw new ValidationException("Validation failed: Event type is null");

        ReceiptEventDTO data = event.getData();
        if (data.getReceiptCode() == null || data.getReceiptCode().isBlank())
            throw new ValidationException("Validation failed: ReceiptCode is null or blank");
        if (data.getThirdPartyId() == null)
            throw new ValidationException("Validation failed: ThirdPartyId is null");
        if (data.getEnterpriseId() == null)
            throw new ValidationException("Validation failed: EnterpriseId is null");
        if (data.getStatus() == null)
            throw new ValidationException("Validation failed: Status is null");
        if (data.getReceiptTypeId() == null)
            throw new ValidationException("Validation failed: ReceiptTypeId is null");

        if (data.getReceiptTypeId() != null) {
            if (data.getReceiptTypeId() == 1) {
                if (data.getDetails() == null || data.getDetails().isEmpty())
                    throw new ValidationException("Validation failed: Details list is null or empty");
                for (var detail : data.getDetails()) {
                    if (detail.getAccountingAccount() == null)
                        throw new ValidationException("Validation failed: Detail AccountCode is null");
                    if (detail.getAmountPaid() == null)
                        throw new ValidationException("Validation failed: Detail AmountPaid is null");
                    if (detail.getInvoiceId() == null)
                        throw new ValidationException("Validation failed: Detail InvoiceId is null");
                    if (detail.getInvoiceCode() == null || detail.getInvoiceCode().isBlank())
                        throw new ValidationException("Validation failed: Detail InvoiceCode is null or blank");
                }
            } else if (data.getReceiptTypeId() == 2) {
                if (data.getLedgerAccountId() == null)
                    throw new ValidationException("Validation failed: LedgerAccountId is null for ReceiptTypeId 2");

            } else {
                throw new ValidationException("Validation failed: Unsupported ReceiptTypeId");
            }
        }

        if (data.getTotalAmount() == null)
            throw new ValidationException("Validation failed: TotalAmount is null");
        if (data.getPaymentMethodId() == null)
            throw new ValidationException("Validation failed: PaymentMethodId is null");
        if (data.getPaymentMethodAccount() == null)
            throw new ValidationException("Validation failed: PaymentMethodAccount is null");
        if (data.getIssueDate() == null)
            throw new ValidationException("Validation failed: IssueDate is null");
        if (data.getTotalAmount().compareTo(new java.math.BigDecimal("0")) < 0)
            throw new ValidationException("Validation failed: TotalAmount is negative");

    }

    @Override
    protected void processEvent(EventDTO<ReceiptEventDTO> event) {
        ReceiptEventDTO receiptDTO = event.getData();
        try {
            Receipt receipt = receiptEventMapper.toDomain(receiptDTO);
            switch (event.getType()) {
                case "RECEIPT_CREATED":
                    receiptProcessInputPort.processReceiptCreation(receipt);
                    break;
                case "RECEIPT_VOIDED":
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

    @Override
    protected String getEntityType() {
        return "Receipt";
    }

    @Override
    protected String extractEventType(EventDTO<ReceiptEventDTO> event) {
        return event != null ? event.getType() : "unknown";
    }

    @Override
    protected String convertEventToJson(EventDTO<ReceiptEventDTO> event) {
        if (event == null || event.getData() == null) {
            return "{\"error\": \"Event or event data is null\"}";
        }
        return JsonUtils.receiptDtoToJsonWithNullHandling(event.getData());
    }
}
