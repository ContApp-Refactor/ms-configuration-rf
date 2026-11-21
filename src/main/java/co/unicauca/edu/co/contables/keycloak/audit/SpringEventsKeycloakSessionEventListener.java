package co.unicauca.edu.co.contables.keycloak.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.keycloak.audit.event.UserLoggedInEvent;
import co.unicauca.edu.co.contables.keycloak.audit.event.UserLoggedOutEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsKeycloakSessionEventListener {

    private final KeycloakSessionEventBuilder sessionEventBuilder;
    private final SpringEventsSessionEventPublisher sessionEventPublisher;

    @Async
    @EventListener
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        try {
            SessionEventDto sessionEvent = sessionEventBuilder.buildLoginEvent(
                    event.jwtToken(),
                    event.request());
            sessionEventPublisher.publishLoginEvent(sessionEvent);
        } catch (Exception e) {
            log.error("Failed to process UserLoggedInEvent", e);
        }
    }

    @Async
    @EventListener
    public void handleUserLoggedOut(UserLoggedOutEvent event) {
        try {
            SessionEventDto sessionEvent = sessionEventBuilder.buildLogoutEvent(
                    event.jwtToken(),
                    event.request());

            sessionEventPublisher.publishLogoutEvent(sessionEvent);
        } catch (Exception e) {
            log.error("Error handling UserLoggedOutEvent", e);
        }
    }
}
