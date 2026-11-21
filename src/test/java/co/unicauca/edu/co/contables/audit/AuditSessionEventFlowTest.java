package co.unicauca.edu.co.contables.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogSessionRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditSessionCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.AuditSessionListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.SessionEventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.keycloak.audit.KeycloakSessionEventListener;
import co.unicauca.edu.co.contables.keycloak.audit.KeycloakSessionEventPublisher;
import co.unicauca.edu.co.contables.keycloak.audit.SpringEventsSessionEventPublisher;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class AuditSessionEventFlowTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private LogAuditSessionCommand logAuditSessionCommand;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private SpringEventsSessionEventPublisher publisher;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("login - el evento en proceso llega al caso de uso de auditoría")
    void login_llegaAlCasoDeUso() {
        publisher.publishLoginEvent(sessionEventDto("LOGIN"));

        org.mockito.ArgumentCaptor<LogSessionRequest> captor =
                org.mockito.ArgumentCaptor.forClass(LogSessionRequest.class);
        verify(logAuditSessionCommand).execute(captor.capture());
        assertEquals(UserAction.LOGIN, captor.getValue().getAction());
        assertEquals("SESSION-1", captor.getValue().getSessionId());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("logout - el evento en proceso llega al caso de uso con acción LOGOUT")
    void logout_llegaAlCasoDeUso() {
        publisher.publishLogoutEvent(sessionEventDto("LOGOUT"));

        org.mockito.ArgumentCaptor<LogSessionRequest> captor =
                org.mockito.ArgumentCaptor.forClass(LogSessionRequest.class);
        verify(logAuditSessionCommand).execute(captor.capture());
        assertEquals(UserAction.LOGOUT, captor.getValue().getAction());
        assertEquals("SESSION-1", captor.getValue().getSessionId());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("logout - si el caso de uso falla, el error se persiste y no se propaga")
    void logout_errorPersistidoSinPropagar() {
        doThrow(new RuntimeException("audit down"))
                .when(logAuditSessionCommand).execute(any());

        assertDoesNotThrow(() -> publisher.publishLogoutEvent(sessionEventDto("LOGOUT")));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("LOGOUT"),
                contains("audit down"),
                anyString(),
                eq("Session"),
                eq("VALIDATION"));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("invalidSessionFields")
    @DisplayName("evento inválido - persiste el error de validación sin lanzar")
    void eventoInvalido_persisteError(String field, String expectedMessage, String expectedEventType) {
        SessionEventDto dto = sessionEventDto("LOGIN");
        switch (field) {
            case "sessionId" -> dto.setSessionId(null);
            case "userId" -> dto.setUserId(null);
            case "userName" -> dto.setUserName(null);
            case "userRole" -> dto.setUserRole(List.of());
            case "action" -> dto.setAction(null);
            case "actionAt" -> dto.setActionAt(null);
            default -> throw new IllegalArgumentException("Campo no soportado: " + field);
        }

        assertDoesNotThrow(() -> publisher.publishLoginEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq(expectedEventType),
                eq("Validation failed: " + expectedMessage),
                anyString(),
                eq("Session"),
                eq("VALIDATION"));
        verifyNoInteractions(logAuditSessionCommand);
    }

    @Test
    @DisplayName("perfil por defecto - nuevo publicador activo, publicador y listener Rabbit ausentes")
    void perfilPorDefecto_soloNuevoPublicador() {
        assertNotNull(applicationContext.getBean(SpringEventsSessionEventPublisher.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(KeycloakSessionEventPublisher.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(KeycloakSessionEventListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(AuditSessionListener.class));
    }

    private static Stream<Arguments> invalidSessionFields() {
        return Stream.of(
                Arguments.of("sessionId", "sessionId missing", "LOGIN"),
                Arguments.of("userId", "userId missing", "LOGIN"),
                Arguments.of("userName", "userName missing", "LOGIN"),
                Arguments.of("userRole", "userRole missing", "LOGIN"),
                Arguments.of("action", "action missing", "UNKNOWN"),
                Arguments.of("actionAt", "actionAt missing", "LOGIN"));
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
