package co.unicauca.edu.co.contables.configuration.unitAudit.auditCommon;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.SpringEventsAuditEventPublisher;

@ExtendWith(MockitoExtension.class)
class SpringEventsAuditEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsAuditEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsAuditEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("publish - debe publicar el DTO en el bus en proceso")
    void publish_publicaEvento() {
        OperationEventDto dto = buildDto();

        publisher.publish(dto);

        verify(eventPublisher).publishEvent(dto);
    }

    @Test
    @DisplayName("publish - no debe propagar excepción si el bus falla")
    void publish_errorBus_noLanzaExcepcion() {
        OperationEventDto dto = buildDto();
        doThrow(new RuntimeException("bus caído"))
                .when(eventPublisher).publishEvent(dto);

        assertDoesNotThrow(() -> publisher.publish(dto));
    }

    @Test
    @DisplayName("publish - debe publicar exactamente una vez")
    void publish_publicaExactamenteUnaVez() {
        OperationEventDto dto = buildDto();

        publisher.publish(dto);

        verify(eventPublisher, times(1)).publishEvent(dto);
    }

    @Test
    @DisplayName("publish - un DTO con campos nulos no rompe el publicador")
    void publish_dtoParcial_noLanzaExcepcion() {
        assertDoesNotThrow(() -> publisher.publish(new OperationEventDto()));
    }

    private OperationEventDto buildDto() {
        return OperationEventDto.builder()
                .enterpriseId("ENT-001")
                .userId("user-123")
                .userName("jdoe")
                .userRole(java.util.List.of("Administrador"))
                .operationType("CREATE")
                .operationAt(Instant.now())
                .moduleName("CLASSES_OF_DOCUMENTS")
                .affectedTable("DOCUMENT_CLASS")
                .registerId("1")
                .dataObject(Map.of("entity", Map.of("id", 1L)))
                .build();
    }
}
