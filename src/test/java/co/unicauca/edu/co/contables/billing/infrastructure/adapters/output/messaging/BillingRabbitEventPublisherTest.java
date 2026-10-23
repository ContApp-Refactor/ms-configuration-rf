package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.TENANT_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.unicauca.edu.co.contables.billing.application.ports.output.model.EventPublicationException;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * EVT-4, EVT-5, D15: el publicador manda el sobre como BYTES crudos (el {@code rabbitTemplate}
 * compartido usa un conversor Jackson y codificaria doble un String), con los headers acordados y
 * SIN {@code x-jwt-token} (H0-2), y espera la confirmacion del broker (5 s por defecto). Cualquier
 * nack, timeout, devolucion o error AMQP se convierte en {@link EventPublicationException}.
 * Mockito puro: el {@code CorrelationData} se completa a mano.
 */
class BillingRabbitEventPublisherTest {

    private static final String PAYLOAD = "{\"eventId\":\"ev-1\",\"nombre\":\"Facturación señor Ñandú\"}";
    private static final PendingOutboxMessage MESSAGE = new PendingOutboxMessage(7L, "ev-1", "FacturaVentaEmitida",
            TENANT_A, "billing.documents.exchange", "sale-invoice.emitted.v1", PAYLOAD, 0, NOW);

    private RabbitTemplate rabbitTemplate;
    private BillingProperties properties;
    private BillingRabbitEventPublisher publisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        properties = new BillingProperties();
        properties.getOutbox().setConfirmTimeout(Duration.ofMillis(200));
        publisher = new BillingRabbitEventPublisher(rabbitTemplate, properties);
    }

    // ---------- helpers ----------

    /** El broker "responde" dentro del propio send. */
    private void brokerConfirms(boolean ack, String reason) {
        doAnswer(inv -> {
            CorrelationData correlation = inv.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(ack, reason));
            return null;
        }).when(rabbitTemplate).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
    }

    private Message sentMessage() {
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate).send(eq("billing.documents.exchange"), eq("sale-invoice.emitted.v1"), captor.capture(),
                any(CorrelationData.class));
        return captor.getValue();
    }

    // ---------- envio ----------

    @Test
    void D15_sends_the_payload_as_raw_utf8_bytes_to_the_row_exchange_and_routing_key() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);

        Message sent = sentMessage();
        assertThat(sent.getBody()).isEqualTo(PAYLOAD.getBytes(StandardCharsets.UTF_8));
        assertThat(new String(sent.getBody(), StandardCharsets.UTF_8)).isEqualTo(PAYLOAD);
    }

    @Test
    void D15_never_goes_through_the_converting_convertAndSend() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class),
                any(CorrelationData.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void EVT_5_message_properties_are_json_utf8_persistent_with_message_id_and_type() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);

        MessageProperties props = sentMessage().getMessageProperties();
        assertThat(props.getContentType()).isEqualTo("application/json");
        assertThat(props.getContentEncoding()).isEqualTo("UTF-8");
        assertThat(props.getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(props.getMessageId()).isEqualTo("ev-1");
        assertThat(props.getType()).isEqualTo("FacturaVentaEmitida");
    }

    @Test
    void EVT_5_headers_are_exactly_eventId_eventType_tenantId_and_x_tenant_id() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);

        Map<String, Object> headers = sentMessage().getMessageProperties().getHeaders();
        assertThat(headers).containsOnlyKeys("eventId", "eventType", "tenantId", "x-tenant-id");
        assertThat(headers).containsEntry("eventId", "ev-1")
                .containsEntry("eventType", "FacturaVentaEmitida")
                .containsEntry("tenantId", TENANT_A)
                .containsEntry("x-tenant-id", TENANT_A);
    }

    @Test
    void H0_2_no_jwt_header_is_ever_sent() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);

        assertThat(sentMessage().getMessageProperties().getHeaders()).doesNotContainKey("x-jwt-token");
    }

    @Test
    void EVT_4_the_correlation_id_is_the_event_id() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);

        ArgumentCaptor<CorrelationData> captor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).send(anyString(), anyString(), any(Message.class), captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("ev-1");
    }

    // ---------- confirmacion ----------

    @Test
    void EVT_4_an_ack_completes_without_error() {
        brokerConfirms(true, null);

        publisher.publish(MESSAGE);
    }

    @Test
    void EVT_4_a_nack_is_a_publication_failure_carrying_the_broker_reason() {
        brokerConfirms(false, "exchange sin permisos");

        assertThatThrownBy(() -> publisher.publish(MESSAGE))
                .isInstanceOf(EventPublicationException.class)
                .hasMessageContaining("exchange sin permisos");
    }

    @Test
    void EVT_4_no_confirmation_within_the_timeout_is_a_publication_failure_that_names_the_confirm_setting() {
        properties.getOutbox().setConfirmTimeout(Duration.ofMillis(50));

        assertThatThrownBy(() -> publisher.publish(MESSAGE))
                .isInstanceOf(EventPublicationException.class)
                .hasMessageContaining("publisher-confirm-type=correlated");
    }

    @Test
    void EVT_4_a_returned_message_is_a_publication_failure_even_if_acked() {
        doAnswer(inv -> {
            CorrelationData correlation = inv.getArgument(3);
            correlation.setReturned(new ReturnedMessage(inv.getArgument(2), 312, "NO_ROUTE",
                    "billing.documents.exchange", "sale-invoice.emitted.v1"));
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));

        assertThatThrownBy(() -> publisher.publish(MESSAGE))
                .isInstanceOf(EventPublicationException.class)
                .hasMessageContaining("NO_ROUTE");
    }

    @Test
    void EVT_4_an_amqp_exception_while_sending_is_wrapped_keeping_the_cause() {
        AmqpException boom = new AmqpException("conexion rechazada");
        doThrow(boom).when(rabbitTemplate).send(anyString(), anyString(), any(Message.class),
                any(CorrelationData.class));

        assertThatThrownBy(() -> publisher.publish(MESSAGE))
                .isInstanceOf(EventPublicationException.class)
                .hasCause(boom);
    }

    @Test
    void EVT_4_a_future_completed_exceptionally_is_a_publication_failure() {
        IllegalStateException cause = new IllegalStateException("canal cerrado");
        doAnswer(inv -> {
            CorrelationData correlation = inv.getArgument(3);
            correlation.getFuture().completeExceptionally(cause);
            return null;
        }).when(rabbitTemplate).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));

        assertThatThrownBy(() -> publisher.publish(MESSAGE))
                .isInstanceOf(EventPublicationException.class)
                .hasRootCause(cause);
    }

    @Test
    void EVT_4_an_interrupted_wait_is_a_publication_failure_and_keeps_the_interrupt_flag() {
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> publisher.publish(MESSAGE)).isInstanceOf(EventPublicationException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void EVT_4_the_default_confirmation_timeout_is_five_seconds() {
        assertThat(new BillingProperties().getOutbox().getConfirmTimeout()).isEqualTo(Duration.ofSeconds(5));
    }
}
