package co.unicauca.edu.co.contables.audit.unitAuditSession;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogSessionRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditSessionCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.SpringEventsAuditSessionListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.SessionEventMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SpringEventsAuditSessionListenerTest {

    @Mock
    private LogAuditSessionCommand logAuditSessionPort;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsAuditSessionListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsAuditSessionListener(
                logAuditSessionPort,
                new SessionEventMapper(),
                messageErrorHandlingPort,
                new ObjectMapper());
    }

    @Test
    @DisplayName("onSessionEvent - evento válido ejecuta el caso de uso")
    void onSessionEvent_eventoValidoEjecutaCasoDeUso() {
        SessionEventDto dto = sessionEventDto("LOGIN");

        listener.onSessionEvent(dto);

        verify(logAuditSessionPort).execute(any(LogSessionRequest.class));
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("onSessionEvent - evento inválido persiste error de validación sin lanzar")
    void onSessionEvent_eventoInvalidoPersisteError() {
        SessionEventDto dto = sessionEventDto("LOGIN");
        dto.setSessionId(null);

        assertDoesNotThrow(() -> listener.onSessionEvent(dto));

        verify(logAuditSessionPort, never()).execute(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("LOGIN"),
                eq("Validation failed: sessionId missing"),
                anyString(),
                eq("Session"),
                eq("VALIDATION"));
    }

    @Test
    @DisplayName("onSessionEvent - si el caso de uso falla persiste el error y no lanza")
    void onSessionEvent_errorProcesamientoPersisteError() {
        SessionEventDto dto = sessionEventDto("LOGIN");

        doThrow(new RuntimeException("audit down"))
                .when(logAuditSessionPort).execute(any(LogSessionRequest.class));

        assertDoesNotThrow(() -> listener.onSessionEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("LOGIN"),
                contains("audit down"),
                anyString(),
                eq("Session"),
                eq("VALIDATION"));
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
