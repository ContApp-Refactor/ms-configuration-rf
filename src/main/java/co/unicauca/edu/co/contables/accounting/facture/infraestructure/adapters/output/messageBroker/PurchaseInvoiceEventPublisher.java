package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceOutboxEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig.RabbitReceiptConfig;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseInvoiceEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    private final IJwtUtils jwtUtils;

    public void publish(PurchaseInvoiceOutboxEvent event) throws Exception {
        JsonNode payload = mapper.readTree(event.getPayload());
        CorrelationData correlation = new CorrelationData(event.getEventId());
        rabbitTemplate.convertAndSend(RabbitReceiptConfig.PURCHASE_INVOICE_EXCHANGE, "", payload, message -> {
            message.getMessageProperties().setMessageId(event.getEventId());
            message.getMessageProperties().setHeader("eventId", event.getEventId());
            message.getMessageProperties().setHeader("eventType", event.getEventType());
            message.getMessageProperties().setHeader("tenantId", event.getTenantId());
            message.getMessageProperties().setHeader("x-tenant-id", event.getTenantId());
            message.getMessageProperties().setHeader("x-jwt-token", jwtUtils.getToken());
            return message;
        }, correlation);
        CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
        if (!confirm.isAck()) {
            throw new IllegalStateException("Rabbit rechazó evento de compra: " + confirm.getReason());
        }
        if (correlation.getReturned() != null) {
            throw new IllegalStateException("Rabbit retornó evento de compra sin ruta");
        }
    }
}
