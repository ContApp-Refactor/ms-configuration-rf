package co.unicauca.edu.co.contables.keycloak.audit;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitSessionConfig;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.commons.config.RabbitAuditPublisherConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publicador de eventos de sesión (login/logout) del módulo keycloak.
 * <p>
 * Equivale al {@code SessionEventPublisherImpl} del origen (D-12.4): usa el
 * {@code SessionEventDto} del módulo audit y las constantes de
 * {@code RabbitAuditPublisherConfig} / {@code RabbitSessionConfig} en lugar de
 * migrar las configs Rabbit del origen, que colisionarían con los beans ya
 * declarados en commons y en audit (R-12.1). No se migra la interfaz
 * {@code ISessionEventPublisher}: no aporta una segunda implementación.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakSessionEventPublisher {

    private final AmqpTemplate amqpTemplate;

    /**
     * Publica un evento de login en la cola de sesiones de auditoría.
     *
     * @param sessionEventDto evento de sesión a publicar.
     */
    public void publishLoginEvent(SessionEventDto sessionEventDto) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                    RabbitSessionConfig.SESSION_EVENT_ROUTING_KEY,
                    sessionEventDto);
        } catch (Exception e) {
            log.error("Failed to publish login event: {}", e.getMessage());
        }
    }

    /**
     * Publica un evento de logout en la cola de sesiones de auditoría.
     *
     * @param sessionEventDto evento de sesión a publicar.
     */
    public void publishLogoutEvent(SessionEventDto sessionEventDto) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                    RabbitSessionConfig.SESSION_EVENT_ROUTING_KEY,
                    sessionEventDto);
        } catch (Exception e) {
            log.error("Error publishing logout event for user: {}", sessionEventDto.getUserName(), e);
        }
    }
}
