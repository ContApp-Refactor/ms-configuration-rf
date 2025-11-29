package co.unicauca.edu.co.contables.configuration.unit.typesOfDocuments.domain.messageBroker;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.DocumentTypeUsageListener;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.DocumentTypeUsageDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeUsage;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Message;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentTypeUsageListenerUnitTest {

    @Mock
    private IDocumentTypeUsage documentTypeUsage;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private DocumentTypeUsageListener documentTypeUsageListener;

    private static final Long DOCUMENT_TYPE_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final Integer QUANTITY_USED = 5;
    private static final long DELIVERY_TAG = 1L;

    private DocumentTypeUsageDto validUsageDto;
    private EventDto<DocumentTypeUsageDto, EventUsageType> validEvent;

    @BeforeEach
    void setUp() {
        validUsageDto = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, QUANTITY_USED);
        validEvent = new EventDto<>(validUsageDto, EventUsageType.USED);
    }

    // ========== HANDLE DOCUMENT TYPE EVENT TESTS ==========

    @Test
    @DisplayName("handleDocumentTypeEvent - Debe procesar evento válido e incrementar uso")
    void testHandleDocumentTypeEventWithValidEvent() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage).incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleDocumentTypeEvent - Debe hacer acknowledge cuando evento es inválido")
    void testHandleDocumentTypeEventWithInvalidEvent() throws IOException {
        // Arrange
        EventDto<DocumentTypeUsageDto, EventUsageType> invalidEvent = new EventDto<>(null, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(invalidEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleDocumentTypeEvent - Debe hacer acknowledge cuando evento es null")
    void testHandleDocumentTypeEventWithNullEvent() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(null, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ========== IS VALID EVENT TESTS (protected method via handleDocumentTypeEvent) ==========

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando tipo de evento es null")
    void testIsValidEventReturnsFalseWhenEventTypeNull() throws IOException {
        // Arrange
        EventDto<DocumentTypeUsageDto, EventUsageType> eventWithNullType = new EventDto<>(validUsageDto, null);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(eventWithNullType, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando documentTypeId es null")
    void testIsValidEventReturnsFalseWhenDocumentTypeIdNull() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithNullId = new DocumentTypeUsageDto(null, ENTERPRISE_ID, QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNullId, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando enterpriseId es null")
    void testIsValidEventReturnsFalseWhenEnterpriseIdNull() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithNullEnterprise = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, null, QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNullEnterprise, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando enterpriseId está vacío")
    void testIsValidEventReturnsFalseWhenEnterpriseIdEmpty() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithEmptyEnterprise = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, "", QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithEmptyEnterprise, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando enterpriseId tiene solo espacios")
    void testIsValidEventReturnsFalseWhenEnterpriseIdOnlySpaces() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithSpacesEnterprise = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, "   ", QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithSpacesEnterprise, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando quantityUsed es null")
    void testIsValidEventReturnsFalseWhenQuantityUsedNull() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithNullQuantity = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, null);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNullQuantity, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando quantityUsed es cero")
    void testIsValidEventReturnsFalseWhenQuantityUsedZero() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithZeroQuantity = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 0);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithZeroQuantity, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando quantityUsed es negativo")
    void testIsValidEventReturnsFalseWhenQuantityUsedNegative() throws IOException {
        // Arrange
        DocumentTypeUsageDto dtoWithNegativeQuantity = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, -1);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNegativeQuantity, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    // ========== PROCESS EVENT TESTS ==========

    @Test
    @DisplayName("processEvent - Debe llamar incrementUsageCount con datos correctos")
    void testProcessEventCallsIncrementUsageCount() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage).incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("processEvent - Debe procesar evento con quantityUsed mayor a 1")
    void testProcessEventWithQuantityGreaterThanOne() throws IOException {
        // Arrange
        DocumentTypeUsageDto dto = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 100);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dto, EventUsageType.USED);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(event, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage).incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("processEvent - Debe manejar excepción del servicio")
    void testProcessEventHandlesServiceException() throws IOException {
        // Arrange
        doThrow(new RuntimeException("Error de servicio")).when(documentTypeUsage)
                .incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);

        // Act & Assert
        assertDoesNotThrow(() -> documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG));
    }

    // ========== EXTRACT EVENT TYPE TESTS ==========

    @Test
    @DisplayName("extractEventType - Debe extraer tipo USED correctamente")
    void testExtractEventTypeReturnsUsedType() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(documentTypeUsage).incrementUsageCount(any(), any());
    }

    // ========== GET ENTITY TYPE TESTS ==========

    @Test
    @DisplayName("getEntityType - Debe procesar mensaje con tipo de entidad DocumentType")
    void testGetEntityTypeReturnsDocumentType() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ========== ACKNOWLEDGMENT TESTS ==========

    @Test
    @DisplayName("handleMessage - Debe hacer acknowledge después de procesar exitosamente")
    void testHandleMessageAcknowledgesAfterSuccess() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("handleMessage - Debe manejar error de acknowledge sin propagar excepción")
    void testHandleMessageHandlesAckError() throws IOException {
        // Arrange
        doThrow(new IOException("Error de canal")).when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        assertDoesNotThrow(() -> documentTypeUsageListener.handleDocumentTypeEvent(validEvent, message, channel, DELIVERY_TAG));
    }
}
