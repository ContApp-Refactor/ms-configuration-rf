package co.unicauca.edu.co.contables.keycloak.unitAudit.unitAuditSession;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.keycloak.audit.KeycloakSessionEventBuilder;
import co.unicauca.edu.co.contables.keycloak.audit.KeycloakSessionEventListener;
import co.unicauca.edu.co.contables.keycloak.audit.KeycloakSessionEventPublisher;
import co.unicauca.edu.co.contables.keycloak.audit.event.UserLoggedInEvent;
import co.unicauca.edu.co.contables.keycloak.audit.event.UserLoggedOutEvent;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AuditSessionEventListenerTest {

    @Mock
    private KeycloakSessionEventBuilder sessionEventBuilder;

    @Mock
    private KeycloakSessionEventPublisher sessionEventPublisher;

    @Mock
    private HttpServletRequest request;

    private KeycloakSessionEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new KeycloakSessionEventListener(sessionEventBuilder, sessionEventPublisher);
    }

    @Test
    @DisplayName("handleUserLoggedIn - construye y publica evento de login")
    void handleUserLoggedIn_publicaEvento() {
        UserLoggedInEvent event = new UserLoggedInEvent("jwt-login", request);
        SessionEventDto dto = sessionEventDto("LOGIN");

        when(sessionEventBuilder.buildLoginEvent("jwt-login", request)).thenReturn(dto);

        listener.handleUserLoggedIn(event);

        verify(sessionEventBuilder).buildLoginEvent("jwt-login", request);
        verify(sessionEventPublisher).publishLoginEvent(dto);
    }

    @Test
    @DisplayName("handleUserLoggedIn - si falla no lanza excepción")
    void handleUserLoggedIn_errorNoLanzaExcepcion() {
        UserLoggedInEvent event = new UserLoggedInEvent("jwt-login", request);

        when(sessionEventBuilder.buildLoginEvent("jwt-login", request))
                .thenThrow(new RuntimeException("error"));

        assertDoesNotThrow(() -> listener.handleUserLoggedIn(event));

        verify(sessionEventPublisher, never()).publishLoginEvent(any());
    }

    @Test
    @DisplayName("handleUserLoggedOut - construye y publica evento de logout")
    void handleUserLoggedOut_publicaEvento() {
        UserLoggedOutEvent event = new UserLoggedOutEvent("jwt-logout", request);
        SessionEventDto dto = sessionEventDto("LOGOUT");

        when(sessionEventBuilder.buildLogoutEvent("jwt-logout", request)).thenReturn(dto);

        listener.handleUserLoggedOut(event);

        verify(sessionEventBuilder).buildLogoutEvent("jwt-logout", request);
        verify(sessionEventPublisher).publishLogoutEvent(dto);
    }

    @Test
    @DisplayName("handleUserLoggedOut - si falla no lanza excepción")
    void handleUserLoggedOut_errorNoLanzaExcepcion() {
        UserLoggedOutEvent event = new UserLoggedOutEvent("jwt-logout", request);

        when(sessionEventBuilder.buildLogoutEvent("jwt-logout", request))
                .thenThrow(new RuntimeException("error"));

        assertDoesNotThrow(() -> listener.handleUserLoggedOut(event));

        verify(sessionEventPublisher, never()).publishLogoutEvent(any());
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
