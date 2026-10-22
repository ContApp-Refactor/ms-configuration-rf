package co.unicauca.edu.co.contables.inventory.kardex.unit.application.messageProcessingError;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.messageProcessingError.command.MessageProcessingErrorCommandService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError.IMessageProcessingErrorCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.messageProcessingError.IMessageProcessingErrorQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

@ExtendWith(MockitoExtension.class)
public class MessageProcessingErrorCommandServiceUnitTest {
    
    @Mock
    private IMessageProcessingErrorCommandRepositoryPort messageProcessingErrorCommandRepositoryPort;
    
    @Mock
    private IMessageProcessingErrorQueryRepositoryPort messageProcessingErrorQueryRepositoryPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private MessageProcessingErrorCommandService messageProcessingErrorCommandService;
    
    private MessageProcessingError messageProcessingError;
    
    @BeforeEach
    void setUp() {
        messageProcessingError = new MessageProcessingError();
        messageProcessingError.setId(1L);
        messageProcessingError.setEventType("TEST_EVENT");
        messageProcessingError.setErrorDescription("Test error message");
        messageProcessingError.setMessageData("Test message data");
        messageProcessingError.setErrorTimestamp(java.time.Instant.now());
        messageProcessingError.setEntityType("TEST_ENTITY");
    }
    
    @Test
    @DisplayName("Should delete all message processing errors successfully")
    void testDeleteAll() {
        // Arrange
        doNothing().when(messageProcessingErrorCommandRepositoryPort).deleteAll();
        
        // Act
        messageProcessingErrorCommandService.deleteAll();
        
        // Assert
        verify(messageProcessingErrorCommandRepositoryPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should delete message processing error by id successfully")
    void testDeleteByIdSuccess() {
        // Arrange
        Long errorId = 1L;
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId))
            .thenReturn(Optional.of(messageProcessingError));
        doNothing().when(messageProcessingErrorCommandRepositoryPort).deleteById(errorId);
        
        // Act
        messageProcessingErrorCommandService.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId);
        verify(messageProcessingErrorCommandRepositoryPort).deleteById(errorId);
        verify(formatterResultOutputPort, never()).returnEntityDoesNotExistErrorResponse(anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Should throw error when deleting non-existent message processing error")
    void testDeleteByIdNotFound() {
        // Arrange
        Long errorId = 999L;
        String errorMessage = "Message processing error with id: " + errorId;
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, errorMessage))
            .thenReturn("Error not found: " + errorMessage);
        doThrow(new RuntimeException("Error not found"))
            .when(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(404, 
                "Error not found: " + errorMessage);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageProcessingErrorCommandService.deleteById(errorId);
        });
        
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId);
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        verify(messageProcessingErrorCommandRepositoryPort, never()).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should handle multiple delete operations")
    void testMultipleDeletes() {
        // Arrange
        Long errorId1 = 1L;
        Long errorId2 = 2L;
        MessageProcessingError error1 = new MessageProcessingError();
        error1.setId(errorId1);
        MessageProcessingError error2 = new MessageProcessingError();
        error2.setId(errorId2);
        
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId1))
            .thenReturn(Optional.of(error1));
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId2))
            .thenReturn(Optional.of(error2));
        doNothing().when(messageProcessingErrorCommandRepositoryPort).deleteById(anyLong());
        
        // Act
        messageProcessingErrorCommandService.deleteById(errorId1);
        messageProcessingErrorCommandService.deleteById(errorId2);
        
        // Assert
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId1);
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId2);
        verify(messageProcessingErrorCommandRepositoryPort).deleteById(errorId1);
        verify(messageProcessingErrorCommandRepositoryPort).deleteById(errorId2);
    }
    
    @Test
    @DisplayName("Should validate existence before deletion")
    void testValidateExistenceBeforeDeletion() {
        // Arrange
        Long errorId = 1L;
        when(messageProcessingErrorQueryRepositoryPort.findById(errorId))
            .thenReturn(Optional.of(messageProcessingError));
        
        // Act
        messageProcessingErrorCommandService.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorQueryRepositoryPort).findById(errorId);
        verify(messageProcessingErrorCommandRepositoryPort).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should handle deleteAll with no errors present")
    void testDeleteAllWithNoErrors() {
        // Arrange
        doNothing().when(messageProcessingErrorCommandRepositoryPort).deleteAll();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            messageProcessingErrorCommandService.deleteAll();
        });
        
        verify(messageProcessingErrorCommandRepositoryPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should delete error with null id gracefully")
    void testDeleteByIdWithNullId() {
        // Arrange
        Long nullId = null;
        when(messageProcessingErrorQueryRepositoryPort.findById(nullId))
            .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND), anyString()))
            .thenReturn("Error not found");
        doThrow(new RuntimeException("Error not found"))
            .when(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageProcessingErrorCommandService.deleteById(nullId);
        });
        
        verify(messageProcessingErrorQueryRepositoryPort).findById(nullId);
        verify(messageProcessingErrorCommandRepositoryPort, never()).deleteById(nullId);
    }
}
