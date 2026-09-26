package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IMessageProcessingErrorRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.MessageErrorHandlingAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for MessageErrorHandlingAdapter")
public class MessageErrorHandlingAdapterUnitTest {

    @Mock
    private IMessageProcessingErrorRepository errorRepository;

    @InjectMocks
    private MessageErrorHandlingAdapter adapter;

    @Captor
    private ArgumentCaptor<MessageProcessingErrorEntity> entityCaptor;

    @Test
    @DisplayName("saveProcessingError should persist error entity with all fields")
    void shouldSaveProcessingErrorWithAllFields() {
        // Arrange
        String eventType = "INVOICE_CREATED";
        String errorDescription = "Failed to process invoice creation";
        String messageData = "{\"invoiceId\": 123, \"amount\": 1000}";
        String entityType = "Invoice";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        adapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(entityCaptor.capture());

        MessageProcessingErrorEntity captured = entityCaptor.getValue();
        assertThat(captured.getEventType()).isEqualTo(eventType);
        assertThat(captured.getErrorDescription()).isEqualTo(errorDescription);
        assertThat(captured.getMessageData()).isEqualTo(messageData);
        assertThat(captured.getEntityType()).isEqualTo(entityType);
    }

    @Test
    @DisplayName("saveProcessingError should handle null eventType by setting default value")
    void shouldHandleNullEventType() {
        // Arrange
        String eventType = null;
        String errorDescription = "Unknown error";
        String messageData = "{}";
        String entityType = "Unknown";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        adapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(entityCaptor.capture());

        MessageProcessingErrorEntity captured = entityCaptor.getValue();
        assertThat(captured.getEventType()).isEqualTo("null_event_type");
        assertThat(captured.getErrorDescription()).isEqualTo(errorDescription);
    }

    @Test
    @DisplayName("saveProcessingError should not throw exception when repository fails")
    void shouldNotThrowExceptionWhenRepositoryFails() {
        // Arrange
        String eventType = "TEST_EVENT";
        String errorDescription = "Test error";
        String messageData = "test data";
        String entityType = "Test";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act - should not throw exception
        adapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(any(MessageProcessingErrorEntity.class));
        // The method should catch and log the exception without rethrowing
    }

    @Test
    @DisplayName("saveProcessingError should handle very long error descriptions")
    void shouldHandleLongErrorDescriptions() {
        // Arrange
        String eventType = "COMPLEX_EVENT";
        String errorDescription = "A".repeat(5000); // Very long error description
        String messageData = "large payload";
        String entityType = "ComplexEntity";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        adapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(entityCaptor.capture());

        MessageProcessingErrorEntity captured = entityCaptor.getValue();
        assertThat(captured.getErrorDescription()).hasSize(5000);
        assertThat(captured.getEventType()).isEqualTo(eventType);
    }

    @Test
    @DisplayName("saveProcessingError should persist multiple errors independently")
    void shouldPersistMultipleErrorsIndependently() {
        // Arrange
        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act - save multiple errors
        adapter.saveProcessingError("EVENT_1", "Error 1", "data 1", "Entity1");
        adapter.saveProcessingError("EVENT_2", "Error 2", "data 2", "Entity2");
        adapter.saveProcessingError("EVENT_3", "Error 3", "data 3", "Entity3");

        // Assert
        verify(errorRepository, times(3)).save(any(MessageProcessingErrorEntity.class));
    }

    @Test
    @DisplayName("saveProcessingError should handle empty strings")
    void shouldHandleEmptyStrings() {
        // Arrange
        String eventType = "";
        String errorDescription = "";
        String messageData = "";
        String entityType = "";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        adapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(entityCaptor.capture());

        MessageProcessingErrorEntity captured = entityCaptor.getValue();
        assertThat(captured.getEventType()).isEmpty();
        assertThat(captured.getErrorDescription()).isEmpty();
        assertThat(captured.getMessageData()).isEmpty();
        assertThat(captured.getEntityType()).isEmpty();
    }

    @Test
    @DisplayName("saveProcessingError should handle special characters in data")
    void shouldHandleSpecialCharactersInData() {
        // Arrange
        String eventType = "SPECIAL_EVENT";
        String errorDescription = "Error with special chars: <>&\"'";
        String messageData = "{\"field\": \"value with \\\"quotes\\\" and \\n newlines\"}";
        String entityType = "SpecialEntity";

        when(errorRepository.save(any(MessageProcessingErrorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        adapter.saveProcessingError(eventType, errorDescription, messageData, entityType);

        // Assert
        verify(errorRepository).save(entityCaptor.capture());

        MessageProcessingErrorEntity captured = entityCaptor.getValue();
        assertThat(captured.getErrorDescription()).contains("<>&\"'");
        assertThat(captured.getMessageData()).contains("\\\"quotes\\\"");
    }
}
