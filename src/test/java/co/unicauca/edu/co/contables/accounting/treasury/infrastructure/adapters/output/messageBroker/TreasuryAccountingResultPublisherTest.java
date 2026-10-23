package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher.AccountingResultReply;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config.TreasuryRabbitConfig;
import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;

/**
 * El receptor de resultados monta su TenantContext a partir del header
 * {@code x-jwt-token}. Si el resultado sale sin ese header, la consulta filtra
 * contra {@code tenant_id = null} y devuelve "Comprobante no encontrado".
 */
@ExtendWith(MockitoExtension.class)
class TreasuryAccountingResultPublisherTest {

    private static final String TENANT = "ebc7841e-c830-430a-8f66-0073254814d2";

    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private JwtTokenService jwtTokenService;

    private TreasuryAccountingResultPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new TreasuryAccountingResultPublisher(rabbitTemplate, jwtTokenService);
    }

    @Test
    @DisplayName("Reply carries x-jwt-token so the consumer resolves the same tenant as the voucher")
    void replyCarriesJwtHeader() {
        when(jwtTokenService.getToken()).thenReturn("jwt-de-prueba");

        publisher.publish(reply(true));

        Message message = captureReply();
        assertEquals("jwt-de-prueba", message.getMessageProperties().getHeaders().get("x-jwt-token"));
        assertEquals(TENANT, message.getMessageProperties().getHeaders().get("x-tenant-id"));
        assertEquals(TENANT, message.getMessageProperties().getHeaders().get("tenantId"));
    }

    @Test
    @DisplayName("Reply published without a JWT simply omits the header")
    void replyWithoutJwtStillPublishes() {
        when(jwtTokenService.getToken()).thenThrow(new IllegalStateException("sin token"));

        publisher.publish(reply(false));

        assertNull(captureReply().getMessageProperties().getHeaders().get("x-jwt-token"));
    }

    private Message captureReply() {
        ArgumentCaptor<MessagePostProcessor> captor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(
                eq(TreasuryRabbitConfig.RESULT_EXCHANGE), eq(""), any(), captor.capture());
        Message message = new Message(new byte[0], new MessageProperties());
        captor.getValue().postProcessMessage(message);
        return message;
    }

    private AccountingResultReply reply(boolean accepted) {
        return new AccountingResultReply("src-1", "PAYMENT_VOUCHER", TENANT,
                "55433c17-b40f-45a7-ba17-1a8dc71086e8", "corr-1", "CREATE",
                "PAYMENT_VOUCHER", 55L, accepted, 7L, null);
    }
}
