package co.unicauca.edu.co.contables.audit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditOperationCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.AuditOperationListener;
import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitOperationConfig;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.commons.audit.publisher.SpringEventsAuditEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class AuditOperationEventFlowTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private LogAuditOperationCommand logAuditOperationCommand;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private SpringEventsAuditEventPublisher publisher;

    @Autowired
    private ApplicationContext applicationContext;

    @ParameterizedTest(name = "{0}")
    @MethodSource("happyOperations")
    @DisplayName("operación válida - el evento en proceso llega al caso de uso de auditoría")
    void operacionValida_llegaAlCasoDeUso(String operationType, OperationType expectedType,
            Map<String, Object> dataObject) {
        publisher.publish(operationEventDto(operationType, dataObject));

        ArgumentCaptor<LogOperationRequest> captor = ArgumentCaptor.forClass(LogOperationRequest.class);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(logAuditOperationCommand).execute(captor.capture());
            LogOperationRequest request = captor.getValue();
            assertEquals(expectedType, request.getOperationType());
            assertEquals("ENT-001", request.getEnterpriseId());
            assertEquals("1", request.getRegisterId());
            assertNotNull(request.getDataObject());
            switch (expectedType) {
                case CREATE, DELETE -> assertFalse(request.getDataObject().getEntity().isEmpty());
                case UPDATE -> {
                    var change = request.getDataObject().getChanges().get("thirdTypeName");
                    assertNotNull(change);
                    assertEquals("Cliente integración", change.getBefore());
                    assertEquals("Cliente integración actualizado", change.getAfter());
                }
                case ACTIVATE -> {
                    var change = request.getDataObject().getChanges().get("state");
                    assertNotNull(change);
                    assertEquals(Boolean.FALSE, change.getBefore());
                    assertEquals(Boolean.TRUE, change.getAfter());
                }
                case INACTIVATE -> {
                    var change = request.getDataObject().getChanges().get("state");
                    assertNotNull(change);
                    assertEquals(Boolean.TRUE, change.getBefore());
                    assertEquals(Boolean.FALSE, change.getAfter());
                }
            }
        });
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("operación con error - el error se persiste y no se propaga")
    void operacionConError_errorPersistidoSinPropagar() {
        doThrow(new RuntimeException("audit down"))
                .when(logAuditOperationCommand).execute(any());

        assertDoesNotThrow(() -> publisher.publish(operationEventDto("CREATE", createDataObject())));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(
                () -> verify(messageErrorHandlingPort).saveProcessingError(
                        eq("CREATE"),
                        contains("audit down"),
                        anyString(),
                        eq("Operation"),
                        eq("VALIDATION")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidOperationFields")
    @DisplayName("evento inválido - persiste el error de validación sin lanzar")
    void eventoInvalido_persisteError(String field, String expectedMessage, String expectedEventType) {
        OperationEventDto dto = operationEventDto("CREATE", createDataObject());
        switch (field) {
            case "enterpriseId" -> dto.setEnterpriseId(null);
            case "userId" -> dto.setUserId(null);
            case "operationType" -> dto.setOperationType(null);
            case "moduleName" -> dto.setModuleName(null);
            case "affectedTable" -> dto.setAffectedTable(null);
            case "registerId" -> dto.setRegisterId(null);
            case "dataObject" -> dto.setDataObject(null);
            default -> throw new IllegalArgumentException("Campo no soportado: " + field);
        }

        assertDoesNotThrow(() -> publisher.publish(dto));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(
                () -> verify(messageErrorHandlingPort).saveProcessingError(
                        eq(expectedEventType),
                        eq("Validation failed: " + expectedMessage),
                        anyString(),
                        eq("Operation"),
                        eq("VALIDATION")));
        verifyNoInteractions(logAuditOperationCommand);
    }

    @Test
    @DisplayName("perfil por defecto - sólo el nuevo publicador y el nuevo listener están activos")
    void perfilPorDefecto_soloNuevosBeans() {
        Map<String, AuditEventPublisher> publishers = applicationContext.getBeansOfType(AuditEventPublisher.class);
        assertEquals(1, publishers.size());
        assertInstanceOf(SpringEventsAuditEventPublisher.class, publishers.values().iterator().next());

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("auditEventPublisher"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(AuditOperationListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(RabbitOperationConfig.class));
    }

    private static Stream<Arguments> happyOperations() {
        return Stream.of(
                Arguments.of("CREATE", OperationType.CREATE, createDataObject()),
                Arguments.of("UPDATE", OperationType.UPDATE, updateDataObject()),
                Arguments.of("DELETE", OperationType.DELETE, deleteDataObject()),
                Arguments.of("ACTIVATE", OperationType.ACTIVATE, stateChangeDataObject(false, true)),
                Arguments.of("INACTIVATE", OperationType.INACTIVATE, stateChangeDataObject(true, false)));
    }

    private static Stream<Arguments> invalidOperationFields() {
        return Stream.of(
                Arguments.of("enterpriseId", "enterpriseId missing", "CREATE"),
                Arguments.of("userId", "userId missing", "CREATE"),
                Arguments.of("operationType", "operationType missing", "UNKNOWN"),
                Arguments.of("moduleName", "moduleName missing", "CREATE"),
                Arguments.of("affectedTable", "affectedTable missing", "CREATE"),
                Arguments.of("registerId", "registerId missing", "CREATE"),
                Arguments.of("dataObject", "dataObject missing", "CREATE"));
    }

    private static Map<String, Object> createDataObject() {
        return Map.of("entity", Map.of(
                "id", "1",
                "thirdTypeName", "Cliente integración",
                "state", true));
    }

    private static Map<String, Object> deleteDataObject() {
        return Map.of("entity", Map.of(
                "id", "1",
                "thirdTypeName", "Cliente integración eliminado",
                "state", true));
    }

    private static Map<String, Object> updateDataObject() {
        return Map.of(
                "context", Map.of("thirdTypeName", "Cliente integración"),
                "changes", Map.of("thirdTypeName", Map.of(
                        "before", "Cliente integración",
                        "after", "Cliente integración actualizado")));
    }

    private static Map<String, Object> stateChangeDataObject(boolean before, boolean after) {
        return Map.of(
                "context", Map.of("thirdTypeName", "Cliente integración"),
                "changes", Map.of("state", Map.of("before", before, "after", after)));
    }

    private OperationEventDto operationEventDto(String operationType, Map<String, Object> dataObject) {
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
                .dataObject(dataObject)
                .build();
    }
}
