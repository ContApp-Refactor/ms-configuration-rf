package co.unicauca.edu.co.contables.audit.unitAuditDocument;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentEventDto;
import co.unicauca.edu.co.contables.audit.application.dto.request.LogDocumentEventRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditDocumentEventCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.SpringEventsAuditDocumentListener;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper.DocumentEventMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SpringEventsAuditDocumentListenerTest {

    @Mock
    private LogAuditDocumentEventCommand logDocumentEventPort;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsAuditDocumentListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsAuditDocumentListener(
                logDocumentEventPort,
                new DocumentEventMapper(),
                messageErrorHandlingPort,
                new ObjectMapper());
    }

    @Test
    @DisplayName("onDocumentEvent - evento válido ejecuta el caso de uso con los campos mapeados")
    void onDocumentEvent_eventoValidoEjecutaCasoDeUso() {
        DocumentEventDto dto = documentEventDto("CREATE");

        listener.onDocumentEvent(dto);

        ArgumentCaptor<LogDocumentEventRequest> captor = ArgumentCaptor.forClass(LogDocumentEventRequest.class);
        verify(logDocumentEventPort).execute(captor.capture());
        assertEquals("ENT-001", captor.getValue().getEnterpriseId());
        assertEquals("1", captor.getValue().getDocumentId());
        assertEquals("FAC-1", captor.getValue().getDocumentCode());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("onDocumentEvent - evento inválido persiste error de validación sin lanzar")
    void onDocumentEvent_eventoInvalidoPersisteError() {
        DocumentEventDto dto = documentEventDto("CREATE");
        dto.setDocumentCode(null);

        assertDoesNotThrow(() -> listener.onDocumentEvent(dto));

        verify(logDocumentEventPort, never()).execute(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATE"),
                eq("Validation failed: documentCode missing"),
                anyString(),
                eq("Document_event"),
                eq("VALIDATION"));
    }

    @Test
    @DisplayName("onDocumentEvent - operación inválida persiste el error de mapeo y no lanza")
    void onDocumentEvent_mapeoInvalidoPersisteError() {
        DocumentEventDto dto = documentEventDto("BOGUS");

        assertDoesNotThrow(() -> listener.onDocumentEvent(dto));

        verify(logDocumentEventPort, never()).execute(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("BOGUS"),
                contains("Invalid operation type"),
                anyString(),
                eq("Document_event"),
                eq("VALIDATION"));
    }

    @Test
    @DisplayName("onDocumentEvent - si el caso de uso falla persiste el error y no lanza")
    void onDocumentEvent_errorProcesamientoPersisteError() {
        DocumentEventDto dto = documentEventDto("CREATE");
        doThrow(new RuntimeException("audit down"))
                .when(logDocumentEventPort).execute(any(LogDocumentEventRequest.class));

        assertDoesNotThrow(() -> listener.onDocumentEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATE"),
                contains("audit down"),
                anyString(),
                eq("Document_event"),
                eq("VALIDATION"));
    }

    private DocumentEventDto documentEventDto(String operationType) {
        return DocumentEventDto.builder()
                .enterpriseId("ENT-001")
                .documentId("1")
                .documentCode("FAC-1")
                .documentType("SALE")
                .documentDate(LocalDate.now())
                .userId("user-123")
                .userName("jdoe")
                .userRoles(List.of("Administrador"))
                .operationType(operationType)
                .thirdPartyId("10")
                .thirdPartyName("Cliente 10")
                .moduleName("FACTURE")
                .operationAt(Instant.now())
                .documentData(Map.<String, Object>of(
                        "header", Map.of("factCode", 1L),
                        "details", List.of(Map.of("productId", 1L)),
                        "totals", Map.of("total", 100)))
                .build();
    }
}
