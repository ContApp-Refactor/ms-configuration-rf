package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingEventPublisherPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.EventPublicationException;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Publica un mensaje del outbox en RabbitMQ y espera la confirmacion del broker (EVT-4, EVT-5, D15).
 *
 * <p>El {@code rabbitTemplate} compartido tiene un conversor Jackson: enviar el JSON como
 * {@code String} lo codificaria dos veces. Por eso se arma un {@link Message} con los bytes UTF-8
 * del sobre y se usa {@code send(...)}, que no pasa por el conversor.
 *
 * <p>Headers: {@code eventId}, {@code eventType}, {@code tenantId}, {@code x-tenant-id}. NUNCA
 * {@code x-jwt-token}: el relay corre fuera de una peticion y no hay token que persistir (decision H0-2;
 * los consumidores con el aspecto JWT deben adaptarse, fuera de alcance).
 *
 * <p>IMPORTANTE (configuracion): la confirmacion solo llega si la conexion tiene
 * {@code spring.rabbitmq.publisher-confirm-type=correlated}; hoy solo el perfil {@code dev} lo
 * declara. Sin ese valor el future nunca se completa, cada publicacion espera
 * {@code billing.outbox.confirm-timeout} (5 s) y falla con una excepcion que lo senala, y las filas
 * terminan FAILED tras 10 intentos. Falla de forma explicita en lugar de dar por publicado un evento
 * sin confirmar. Se excluye del perfil {@code test}, donde el {@code rabbitTemplate} no existe.
 */
@Component("billingRabbitEventPublisher")
@Profile("!test")
public class BillingRabbitEventPublisher implements BillingEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final BillingProperties properties;

    public BillingRabbitEventPublisher(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
            BillingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(PendingOutboxMessage outbox) {
        Message message = buildMessage(outbox);
        CorrelationData correlation = new CorrelationData(outbox.eventId());
        long timeoutMs = properties.getOutbox().getConfirmTimeout().toMillis();
        CorrelationData.Confirm confirm;
        try {
            rabbitTemplate.send(outbox.exchange(), outbox.routingKey(), message, correlation);
            confirm = correlation.getFuture().get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new EventPublicationException("Espera de confirmacion interrumpida para el evento "
                    + outbox.eventId(), ex);
        } catch (TimeoutException ex) {
            throw new EventPublicationException("Sin confirmacion del broker en " + timeoutMs + " ms para el evento "
                    + outbox.eventId() + " (verifique spring.rabbitmq.publisher-confirm-type=correlated)", ex);
        } catch (ExecutionException ex) {
            throw new EventPublicationException("Fallo la confirmacion del evento " + outbox.eventId(), ex);
        } catch (AmqpException ex) {
            throw new EventPublicationException("Error AMQP publicando el evento " + outbox.eventId() + ": "
                    + ex.getMessage(), ex);
        }
        if (confirm == null || !confirm.isAck()) {
            throw new EventPublicationException("El broker rechazo el evento " + outbox.eventId() + ": "
                    + (confirm == null ? "sin confirmacion" : confirm.getReason()));
        }
        if (correlation.getReturned() != null) {
            throw new EventPublicationException("El broker devolvio el evento " + outbox.eventId()
                    + " sin ruta: " + correlation.getReturned().getReplyText());
        }
    }

    private static Message buildMessage(PendingOutboxMessage outbox) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding(StandardCharsets.UTF_8.name());
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setMessageId(outbox.eventId());
        props.setType(outbox.eventType());
        props.setHeader("eventId", outbox.eventId());
        props.setHeader("eventType", outbox.eventType());
        props.setHeader("tenantId", outbox.tenantId());
        props.setHeader("x-tenant-id", outbox.tenantId());
        return new Message(outbox.payloadJson().getBytes(StandardCharsets.UTF_8), props);
    }
}
