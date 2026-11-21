package co.unicauca.edu.co.contables.audit.unitAuditOperation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditOperationCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.SpringEventsAuditOperationListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.OperationEventMapper;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SpringEventsAuditOperationListenerTest {

    @Mock
    private LogAuditOperationCommand logOperationPort;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsAuditOperationListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsAuditOperationListener(
                logOperationPort,
                new OperationEventMapper(),
                messageErrorHandlingPort,
                new ObjectMapper());
    }

    @Test
    @DisplayName("onOperationEvent - evento válido ejecuta el caso de uso con los campos mapeados")
    void onOperationEvent_eventoValidoEjecutaCasoDeUso() {
        OperationEventDto dto = operationEventDto("CREATE");

        listener.onOperationEvent(dto);

        ArgumentCaptor<LogOperationRequest> captor = ArgumentCaptor.forClass(LogOperationRequest.class);
        verify(logOperationPort).execute(captor.capture());
        assertEquals("ENT-001", captor.getValue().getEnterpriseId());
        assertEquals("user-123", captor.getValue().getUserId());
        assertEquals(OperationType.CREATE, captor.getValue().getOperationType());
        assertEquals("DOCUMENT_CLASS", captor.getValue().getAffectedTable());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("onOperationEvent - evento inválido persiste error de validación sin lanzar")
    void onOperationEvent_eventoInvalidoPersisteError() {
        OperationEventDto dto = operationEventDto("CREATE");
        dto.setEnterpriseId(null);

        assertDoesNotThrow(() -> listener.onOperationEvent(dto));

        verify(logOperationPort, never()).execute(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATE"),
                eq("Validation failed: enterpriseId missing"),
                anyString(),
                eq("Operation"),
                eq("VALIDATION"));
    }

    @Test
    @DisplayName("onOperationEvent - tipo de operación inválido persiste el error de mapeo y no lanza")
    void onOperationEvent_mapeoInvalidoPersisteError() {
        OperationEventDto dto = operationEventDto("BOGUS");

        assertDoesNotThrow(() -> listener.onOperationEvent(dto));

        verify(logOperationPort, never()).execute(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("BOGUS"),
                contains("Invalid operation type"),
                anyString(),
                eq("Operation"),
                eq("VALIDATION"));
    }

    @Test
    @DisplayName("onOperationEvent - si el caso de uso falla persiste el error y no lanza")
    void onOperationEvent_errorProcesamientoPersisteError() {
        OperationEventDto dto = operationEventDto("CREATE");
        doThrow(new RuntimeException("audit down"))
                .when(logOperationPort).execute(any(LogOperationRequest.class));

        assertDoesNotThrow(() -> listener.onOperationEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATE"),
                contains("audit down"),
                anyString(),
                eq("Operation"),
                eq("VALIDATION"));
    }

    private OperationEventDto operationEventDto(String operationType) {
        return OperationEventDto.builder()
                .enterpriseId("ENT-001")
                .userId("user-123")
                .userName("jdoe")
                .userRole(List.of("Administrador"))
                .operationType(operationType)
                .operationAt(Instant.now())
                .moduleName("CLASSES_OF_DOCUMENTS")
                .affectedTable("DOCUMENT_CLASS")
                .registerId("1")
                .dataObject(Map.of("entity", Map.of("id", 1L)))
                .build();
    }
}
