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
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.keycloak.audit.SpringEventsSessionEventPublisher;

@ExtendWith(MockitoExtension.class)
class SpringEventsSessionEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsSessionEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsSessionEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("publishLoginEvent - debe publicar el evento en el bus")
    void publishLoginEvent_publicaEvento() {
        SessionEventDto dto = sessionEventDto("LOGIN");

        publisher.publishLoginEvent(dto);

        verify(eventPublisher).publishEvent(dto);
    }

    @Test
    @DisplayName("publishLoginEvent - si el bus falla no lanza excepción")
    void publishLoginEvent_errorNoLanzaExcepcion() {
        SessionEventDto dto = sessionEventDto("LOGIN");

        doThrow(new RuntimeException("bus caído"))
                .when(eventPublisher).publishEvent(dto);

        assertDoesNotThrow(() -> publisher.publishLoginEvent(dto));
    }

    @Test
    @DisplayName("publishLogoutEvent - debe publicar el evento en el bus")
    void publishLogoutEvent_publicaEvento() {
        SessionEventDto dto = sessionEventDto("LOGOUT");

        publisher.publishLogoutEvent(dto);

        verify(eventPublisher).publishEvent(dto);
    }

    @Test
    @DisplayName("publishLogoutEvent - si el bus falla no lanza excepción")
    void publishLogoutEvent_errorNoLanzaExcepcion() {
        SessionEventDto dto = sessionEventDto("LOGOUT");

        doThrow(new RuntimeException("bus caído"))
                .when(eventPublisher).publishEvent(dto);

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
