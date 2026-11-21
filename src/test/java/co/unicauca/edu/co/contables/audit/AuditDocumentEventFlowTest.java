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
import java.time.LocalDate;
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

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentEventDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.publisher.FactureAuditEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.audit.SpringEventsFactureAuditEventPublisher;
import co.unicauca.edu.co.contables.audit.application.dto.request.LogDocumentEventRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditDocumentEventCommand;
import co.unicauca.edu.co.contables.audit.application.port.output.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.AuditDocumentEventDtoListener;
import co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig.RabbitDocumentConfig;
import co.unicauca.edu.co.contables.commons.config.RabbitAuditPublisherConfig;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class AuditDocumentEventFlowTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private LogAuditDocumentEventCommand logAuditDocumentEventCommand;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private SpringEventsFactureAuditEventPublisher publisher;

    @Autowired
    private ApplicationContext applicationContext;

    @ParameterizedTest(name = "{0}")
    @MethodSource("happyDocuments")
    @DisplayName("documento válido - el evento en proceso llega al caso de uso de auditoría")
    void documentoValido_llegaAlCasoDeUso(String operationType, DocumentOperationType expectedType,
            Map<String, Object> documentData) {
        publisher.publish(documentEventDto(operationType, documentData));

        ArgumentCaptor<LogDocumentEventRequest> captor = ArgumentCaptor.forClass(LogDocumentEventRequest.class);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(logAuditDocumentEventCommand).execute(captor.capture());
            LogDocumentEventRequest request = captor.getValue();
            assertEquals(expectedType, request.getOperationType());
            assertEquals("FAC-1", request.getDocumentCode());
            assertEquals("jdoe", request.getUserName());
            assertNotNull(request.getDocumentData());
            assertFalse(request.getDocumentData().getDetails().isEmpty());
            assertEquals(expectedType.name(), request.getDocumentData().getMetadata().get("operationType").toString());
        });
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("documento con error - el error se persiste y no se propaga")
    void documentoConError_errorPersistidoSinPropagar() {
        doThrow(new RuntimeException("audit down"))
                .when(logAuditDocumentEventCommand).execute(any());

        assertDoesNotThrow(() -> publisher.publish(documentEventDto("CREATE", createDocumentData())));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(
                () -> verify(messageErrorHandlingPort).saveProcessingError(
                        eq("CREATE"),
                        contains("audit down"),
                        anyString(),
                        eq("Document_event"),
                        eq("VALIDATION")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDocumentFields")
    @DisplayName("evento inválido - persiste el error de validación sin lanzar")
    void eventoInvalido_persisteError(String field, String expectedMessage, String expectedEventType) {
        DocumentEventDto dto = documentEventDto("CREATE", createDocumentData());
        switch (field) {
            case "documentId" -> dto.setDocumentId(null);
            case "documentCode" -> dto.setDocumentCode(null);
            case "operationType" -> dto.setOperationType(null);
            case "documentDate" -> dto.setDocumentDate(null);
            case "userRoles" -> dto.setUserRoles(List.of());
            case "documentData" -> dto.setDocumentData(null);
            case "userName" -> dto.setUserName(null);
            case "moduleName" -> dto.setModuleName(null);
            case "operationAt" -> dto.setOperationAt(null);
            default -> throw new IllegalArgumentException("Campo no soportado: " + field);
        }

        assertDoesNotThrow(() -> publisher.publish(dto));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(
                () -> verify(messageErrorHandlingPort).saveProcessingError(
                        eq(expectedEventType),
                        eq("Validation failed: " + expectedMessage),
                        anyString(),
                        eq("Document_event"),
                        eq("VALIDATION")));
        verifyNoInteractions(logAuditDocumentEventCommand);
    }

    @Test
    @DisplayName("perfil por defecto - sólo el nuevo publicador y el nuevo listener están activos")
    void perfilPorDefecto_soloNuevosBeans() {
        Map<String, FactureAuditEventPublisher> publishers = applicationContext
                .getBeansOfType(FactureAuditEventPublisher.class);
        assertEquals(1, publishers.size());
        assertInstanceOf(SpringEventsFactureAuditEventPublisher.class, publishers.values().iterator().next());

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("factureAuditEventPublisher"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(AuditDocumentEventDtoListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(RabbitDocumentConfig.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(RabbitAuditPublisherConfig.class));
    }

    private static Stream<Arguments> happyDocuments() {
        return Stream.of(
                Arguments.of("CREATE", DocumentOperationType.CREATE, createDocumentData()),
                Arguments.of("APPROVE", DocumentOperationType.APPROVE, approveDocumentData()),
                Arguments.of("VOID", DocumentOperationType.VOID, voidDocumentData()));
    }

    private static Stream<Arguments> invalidDocumentFields() {
        return Stream.of(
                Arguments.of("documentId", "documentId missing", "CREATE"),
                Arguments.of("documentCode", "documentCode missing", "CREATE"),
                Arguments.of("operationType", "operationType missing", "UNKNOWN"),
                Arguments.of("documentDate", "documentDate missing", "CREATE"),
                Arguments.of("userRoles", "userRoles missing", "CREATE"),
                Arguments.of("documentData", "documentData missing", "CREATE"),
                Arguments.of("userName", "userName missing", "CREATE"),
                Arguments.of("moduleName", "moduleName missing", "CREATE"),
                Arguments.of("operationAt", "operationAt missing", "CREATE"));
    }

    private static Map<String, Object> createDocumentData() {
        return Map.of(
                "header", Map.of("code", "FAC-1", "thirdId", "10"),
                "details", List.of(Map.of(
                        "invoiceId", 1,
                        "amountWrittenOff", 1000000,
                        "accountingAccount", 13050513)),
                "totals", Map.of("totalAmount", 1000000),
                "metadata", Map.of("operationType", "CREATE"));
    }

    private static Map<String, Object> approveDocumentData() {
        return Map.of(
                "header", Map.of("code", "FAC-1", "thirdId", "10"),
                "details", List.of(Map.of(
                        "invoiceId", 1,
                        "changes", Map.of(
                                "invoiceStatus", Map.of(
                                        "before", "PENDING_WRITTEN_OFF",
                                        "after", "WRITTEN_OFF"),
                                "pendingValue", Map.of(
                                        "before", 1000000,
                                        "after", 0)))),
                "totals", Map.of("totalAmountWrittenOff", 1000000),
                "metadata", Map.of("operationType", "APPROVE"));
    }

    private static Map<String, Object> voidDocumentData() {
        return Map.of(
                "header", Map.of("code", "FAC-1", "thirdId", "10"),
                "details", List.of(Map.of(
                        "invoiceId", 1,
                        "changes", Map.of(
                                "invoiceStatus", Map.of(
                                        "before", "WRITTEN_OFF",
                                        "after", "PENDING"),
                                "pendingValue", Map.of(
                                        "before", 0,
                                        "after", 1000000)))),
                "totals", Map.of("totalAmountRestored", 1000000),
                "metadata", Map.of("operationType", "VOID"));
    }

    private DocumentEventDto documentEventDto(String operationType, Map<String, Object> documentData) {
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
                .documentData(documentData)
                .build();
    }
}
