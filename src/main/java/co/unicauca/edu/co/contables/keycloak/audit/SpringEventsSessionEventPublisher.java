package co.unicauca.edu.co.contables.keycloak.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class SpringEventsSessionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishLoginEvent(SessionEventDto sessionEventDto) {
        try {
            eventPublisher.publishEvent(sessionEventDto);
        } catch (Exception e) {
            log.error("Failed to publish login event: {}", e.getMessage());
        }
    }

    public void publishLogoutEvent(SessionEventDto sessionEventDto) {
        try {
            eventPublisher.publishEvent(sessionEventDto);
        } catch (Exception e) {
            log.error("Error publishing logout event for user: {}", sessionEventDto.getUserName(), e);
        }
    }
}
