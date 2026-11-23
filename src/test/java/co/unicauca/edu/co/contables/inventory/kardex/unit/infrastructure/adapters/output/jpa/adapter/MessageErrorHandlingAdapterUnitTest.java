package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.MessageErrorHandlingAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IMessageProcessingErrorRepository;

/**
 * @brief Unit tests for MessageErrorHandlingAdapter
 * 
 * Tests the error handling operations for message processing failures.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageErrorHandlingAdapter Tests")
class MessageErrorHandlingAdapterUnitTest {

    @Mock
    private IMessageProcessingErrorRepository errorRepository;

    @InjectMocks
    private MessageErrorHandlingAdapter messageErrorHandlingAdapter;

    private MessageProcessingErrorEntity errorEntity;

    @BeforeEach
    void setUp() {
        errorEntity = new MessageProcessingErrorEntity();
        errorEntity.setId(1L);
        errorEntity.setEventType("PRODUCT_CREATED");
        errorEntity.setErrorDescription("Failed to process message");
        errorEntity.setMessageData("{\"productId\": 1}");
        errorEntity.setEntityType("Product");
    }

    @Test
    @DisplayName("Should save processing error successfully")
    void testSaveProcessingError_Success() {
        // Arrange
        String eventType = "PRODUCT_CREATED";
        String errorDescription = "Failed to process message";
        String messageData = "{\"productId\": 1}";
        String entityType = "Product";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(argThat(entity ->
            entity.getEventType().equals(eventType) &&
            entity.getErrorDescription().equals(errorDescription) &&
            entity.getMessageData().equals(messageData) &&
            entity.getEntityType().equals(entityType)
        ));
    }

    @Test
    @DisplayName("Should save processing error with null event type")
    void testSaveProcessingError_NullEventType() {
        // Arrange
        String eventType = null;
        String errorDescription = "Failed to process message";
        String messageData = "{\"productId\": 1}";
        String entityType = "Product";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(argThat(entity ->
            entity.getEventType().equals("null_event_type") &&
            entity.getErrorDescription().equals(errorDescription) &&
            entity.getMessageData().equals(messageData) &&
            entity.getEntityType().equals(entityType)
        ));
    }

    @Test
    @DisplayName("Should handle exception when saving error")
    void testSaveProcessingError_ExceptionHandling() {
        // Arrange
        String eventType = "PRODUCT_CREATED";
        String errorDescription = "Failed to process message";
        String messageData = "{\"productId\": 1}";
        String entityType = "Product";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act - Should not throw exception
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(any(MessageProcessingErrorEntity.class));
    }

    @Test
    @DisplayName("Should save error with all valid parameters")
    void testSaveProcessingError_AllValidParameters() {
        // Arrange
        String eventType = "INVOICE_CREATED";
        String errorDescription = "Connection timeout";
        String messageData = "{\"invoiceId\": 123, \"amount\": 1000}";
        String entityType = "Invoice";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(any(MessageProcessingErrorEntity.class));
    }

    @Test
    @DisplayName("Should save error with empty message data")
    void testSaveProcessingError_EmptyMessageData() {
        // Arrange
        String eventType = "PRODUCT_UPDATED";
        String errorDescription = "Invalid format";
        String messageData = "";
        String entityType = "Product";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(argThat(entity ->
            entity.getMessageData().equals(messageData)
        ));
    }

    @Test
    @DisplayName("Should save error with null error description")
    void testSaveProcessingError_NullErrorDescription() {
        // Arrange
        String eventType = "PRODUCT_DELETED";
        String errorDescription = null;
        String messageData = "{\"productId\": 1}";
        String entityType = "Product";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(argThat(entity ->
            entity.getErrorDescription() == null
        ));
    }

    @Test
    @DisplayName("Should save error with long message data")
    void testSaveProcessingError_LongMessageData() {
        // Arrange
        String eventType = "BATCH_PROCESS";
        String errorDescription = "Processing failed";
        String messageData = "{\"data\": \"" + "x".repeat(1000) + "\"}";
        String entityType = "Batch";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(argThat(entity ->
            entity.getMessageData().equals(messageData)
        ));
    }

    @Test
    @DisplayName("Should save error for different entity types")
    void testSaveProcessingError_DifferentEntityTypes() {
        // Arrange
        String[] entityTypes = {"Product", "Invoice", "Kardex", "Stock", "Customer"};

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act & Assert
        for (String entityType : entityTypes) {
            messageErrorHandlingAdapter.saveProcessingError(
                "TEST_EVENT",
                "Test error",
                "{\"test\": \"data\"}",
                entityType
            );
        }

        verify(errorRepository, times(entityTypes.length)).save(any(MessageProcessingErrorEntity.class));
    }

    @Test
    @DisplayName("Should create entity with correct field mappings")
    void testSaveProcessingError_FieldMappings() {
        // Arrange
        String eventType = "KARDEX_UPDATED";
        String errorDescription = "Validation error: quantity cannot be negative";
        String messageData = "{\"kardexId\": 456, \"quantity\": -10}";
        String entityType = "Kardex";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(argThat(entity ->
            entity.getEventType().equals(eventType) &&
            entity.getErrorDescription().equals(errorDescription) &&
            entity.getMessageData().equals(messageData) &&
            entity.getEntityType().equals(entityType)
        ));
    }

    @Test
    @DisplayName("Should handle special characters in error data")
    void testSaveProcessingError_SpecialCharacters() {
        // Arrange
        String eventType = "SPECIAL_EVENT";
        String errorDescription = "Error with symbols: @#$%^&*()";
        String messageData = "{\"data\": \"Test with 'quotes' and \\\"escapes\\\"\"}";
        String entityType = "Test";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class))).thenReturn(errorEntity);

        // Act
        messageErrorHandlingAdapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository, times(1)).save(any(MessageProcessingErrorEntity.class));
    }
}
