package co.unicauca.edu.co.contables.keycloak.unitAudit.unitAuditSession;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitSessionConfig;
import co.unicauca.edu.co.contables.commons.config.RabbitAuditPublisherConfig;
import co.unicauca.edu.co.contables.keycloak.audit.KeycloakSessionEventPublisher;

/**
 * Pruebas de {@link KeycloakSessionEventPublisher} (D-12.4).
 * <p>
 * El origen publicaba con su propio {@code SessionEventPublisherImpl} y sus
 * configs Rabbit; aquí se reutilizan el {@code SessionEventDto} del módulo
 * audit, el exchange de commons y la routing key de
 * {@code RabbitSessionConfig}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AuditSessionEventPublisherImplTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    private KeycloakSessionEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KeycloakSessionEventPublisher(amqpTemplate);
    }

    @Test
    @DisplayName("publishLoginEvent - debe publicar evento de login")
    void publishLoginEvent_publicaEvento() {
        SessionEventDto dto = sessionEventDto("LOGIN");

        publisher.publishLoginEvent(dto);

        verify(amqpTemplate).convertAndSend(
                RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                RabbitSessionConfig.SESSION_EVENT_ROUTING_KEY,
                dto);
    }

    @Test
    @DisplayName("publishLoginEvent - si Rabbit falla no lanza excepción")
    void publishLoginEvent_errorRabbitNoLanzaExcepcion() {
        SessionEventDto dto = sessionEventDto("LOGIN");

        doThrow(new RuntimeException("Rabbit caído"))
                .when(amqpTemplate)
                .convertAndSend(
                        RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                        RabbitSessionConfig.SESSION_EVENT_ROUTING_KEY,
                        dto);

        assertDoesNotThrow(() -> publisher.publishLoginEvent(dto));
    }

    @Test
    @DisplayName("publishLogoutEvent - debe publicar evento de logout")
    void publishLogoutEvent_publicaEvento() {
        SessionEventDto dto = sessionEventDto("LOGOUT");

        publisher.publishLogoutEvent(dto);

        verify(amqpTemplate).convertAndSend(
                RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                RabbitSessionConfig.SESSION_EVENT_ROUTING_KEY,
                dto);
    }

    @Test
    @DisplayName("publishLogoutEvent - si Rabbit falla no lanza excepción")
    void publishLogoutEvent_errorRabbitNoLanzaExcepcion() {
        SessionEventDto dto = sessionEventDto("LOGOUT");

        doThrow(new RuntimeException("Rabbit caído"))
                .when(amqpTemplate)
                .convertAndSend(
                        RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                        RabbitSessionConfig.SESSION_EVENT_ROUTING_KEY,
                        dto);

        assertDoesNotThrow(() -> publisher.publishLogoutEvent(dto));
    }

    private SessionEventDto sessionEventDto(String action) {
        return SessionEventDto.builder()
                .sessionId("SESSION-1")
                .userId("USER-1")
                .userName("maria")
                .userRole(List.of("Administrador"))
                .action(action)
                .actionAt(Instant.now())
                .ipAddress("127.0.0.1")
                .build();
    }
}
