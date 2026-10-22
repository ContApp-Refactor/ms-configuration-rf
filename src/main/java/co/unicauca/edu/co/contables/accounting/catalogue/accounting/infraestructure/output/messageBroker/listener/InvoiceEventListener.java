package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IInvoiceProcessInputPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ValidationException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IInvoiceEventMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.config.RabbitConfig;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;
import com.rabbitmq.client.Channel;

import java.time.LocalDate;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("invoiceAccountingEventListener")
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventListener extends AbstractMessageListener<EventDTO<InvoiceSyncDto>> {
    private final InvoicePersistenceAdapter invoicePersistenceAdapter;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;
    private final IEventRecoveryActionPort<EventDTO<InvoiceSyncDto>> productRecoveryActionPort;
    private final IInvoiceProcessInputPort invoiceProcessInputPort;
    private final IInvoiceEventMapper invoiceEventMapper;

    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
        this.eventRecoveryActionPort = productRecoveryActionPort;
    }

    @RabbitListener(queues = RabbitConfig.INVOICE_ACCOUNTING_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleInvoiceEvent(
            Message message,
            EventDTO<InvoiceSyncDto> event, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received event type '{}' for invoice with ID: {}",
                event.getType(),
                event.getData() != null ? event.getData().getFactCode() : "NA");

        handleMessage(event, channel, tag);
    }

    @Override
    protected void processEvent(EventDTO<InvoiceSyncDto> event) {
        InvoiceSyncDto dto = event.getData();
        String factCode = dto.getFactCode().toString() != null ? dto.getFactCode().toString() : "N/A";

        // --- INICIO DE LA MODIFICACIÓN ---
        // Si la fecha de creación es nula en el DTO recibido,
        // la establecemos a la fecha del día actual.
        if (dto.getCreationDate() == null) {
            log.warn("CreationDate is null for invoice factCode: {}. Setting to current date.", factCode);
            dto.setCreationDate(LocalDate.now());
        }
        // --- FIN DE LA MODIFICACIÓN ---

        try {

            switch (event.getType()) {
                case "SALE":
                    // 1. MAPEAR: Convertir el DTO de infraestructura a un modelo de dominio.
                    InvoiceReplica invoice = invoiceEventMapper.toDomain(dto);

                    // 2. INVOCAR: Realizar una única llamada al caso de uso de la aplicación.
                    // Toda la orquestación (guardar y actualizar saldos) está ahora dentro del
                    // servicio.
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

    /**
     * @brief Validates invoice event data integrity
     * @param event Invoice event to validate
     */
    @Override
    protected void validateEvent(EventDTO<InvoiceSyncDto> event) throws ValidationException {
        if (event == null) throw new ValidationException("Validation failed: Event is null");
        if (event.getData() == null) throw new ValidationException("Validation failed: Event data is null");
        if (event.getType() == null) throw new ValidationException("Validation failed: Event type is null");
        
        InvoiceSyncDto data = event.getData();
        if (data.getFactCode() == null) throw new ValidationException("Validation failed: FactCode is null");
        if (data.getAccountingAccount() == null) throw new ValidationException("Validation failed: AccountingAccount is null");
        if (data.getEntId() == null) throw new ValidationException("Validation failed: EntId is null");
        if (data.getExpirationDate() == null) throw new ValidationException("Validation failed: ExpirationDate is null");
        if (data.getPendingValue() == null) throw new ValidationException("Validation failed: PendingValue is null");
        if (data.getThirdId() == null) throw new ValidationException("Validation failed: ThirdId is null");
        if (data.getTotalPay() == null) throw new ValidationException("Validation failed: TotalPay is null");
        if (data.getTotalValue() == null) throw new ValidationException("Validation failed: TotalValue is null");
        if (data.getCreationDate() == null) throw new ValidationException("Validation failed: CreationDate is null");
    }

    @Override
    protected String getEntityType() {
        return "Invoice";
    }

    @Override
    protected String extractEventType(EventDTO<InvoiceSyncDto> event) {
        if (event == null) {
            return null;
        }

        return event.getType() != null ? event.getType() : null;
    }

    @Override
    protected String convertEventToJson(EventDTO<InvoiceSyncDto> event) {
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
