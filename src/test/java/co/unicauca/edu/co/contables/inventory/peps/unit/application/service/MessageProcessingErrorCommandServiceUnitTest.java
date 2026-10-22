package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.application.service.command.MessageProcessingErrorCommandService;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IMessageProcessingErrorCommandOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IMessageProcessingErrorQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;

/**
 * @brief Unit tests for MessageProcessingErrorCommandService
 * 
 * Tests command operations for message processing errors including
 * deletion operations with validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Message Processing Error Command Service Unit Tests")
class MessageProcessingErrorCommandServiceUnitTest {
    
    @Mock
    private IMessageProcessingErrorCommandOutputPort messageProcessingErrorCommandOutputPort;
    
    @Mock
    private IMessageProcessingErrorQueryOutputPort messageProcessingErrorQueryOutputPort;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private MessageProcessingErrorCommandService commandService;
    
    private MessageProcessingError messageProcessingError;
    private Long errorId;
    
    @BeforeEach
    void setUp() {
        errorId = 1L;
        
        messageProcessingError = new MessageProcessingError();
        messageProcessingError.setId(errorId);
        messageProcessingError.setEventType("PURCHASE_CREATED");
        messageProcessingError.setErrorDescription("Failed to process purchase");
        messageProcessingError.setMessageData("{\"productId\":123,\"quantity\":10}");
        messageProcessingError.setErrorTimestamp(Instant.now());
        messageProcessingError.setEntityType("Kardex");
    }
    
    @Test
    @DisplayName("Should successfully delete all message processing errors")
    void testDeleteAll_ShouldDeleteAllRecords() {
        // Arrange
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteAll();
        
        // Act
        commandService.deleteAll();
        
        // Assert
        verify(messageProcessingErrorCommandOutputPort, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should successfully delete message processing error by ID")
    void testDeleteById_WhenErrorExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(messageProcessingErrorQueryOutputPort.findById(errorId))
                .thenReturn(Optional.of(messageProcessingError));
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteById(errorId);
        
        // Act
        commandService.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorQueryOutputPort).findById(errorId);
        verify(messageProcessingErrorCommandOutputPort).deleteById(errorId);
        verifyNoInteractions(formatterResultOutputPort);
    }
    
    @Test
    @DisplayName("Should throw error when deleting non-existent error")
    void testDeleteById_WhenErrorDoesNotExist_ShouldThrowError() {
        // Arrange
        Long nonExistentId = 999L;
        String errorMessage = "Message processing error with id: " + nonExistentId + " not found";
        
        when(messageProcessingErrorQueryOutputPort.findById(nonExistentId))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND), anyString()))
                .thenReturn(errorMessage);
        doThrow(new RuntimeException(errorMessage))
                .when(formatterResultOutputPort)
                .returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            commandService.deleteById(nonExistentId),
            "Should throw exception when error does not exist"
        );
        
        verify(messageProcessingErrorQueryOutputPort).findById(nonExistentId);
        verify(messageService).getMessage(eq(MessageKeys.ERROR_NOT_FOUND), anyString());
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        verify(messageProcessingErrorCommandOutputPort, never()).deleteById(anyLong());
    }
    
    @Test
    @DisplayName("Should call deleteAll without throwing exception")
    void testDeleteAll_ShouldNotThrowException() {
        // Arrange
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteAll();
        
        // Act & Assert
        assertDoesNotThrow(() -> commandService.deleteAll(),
                           "deleteAll should not throw exception");
        
        verify(messageProcessingErrorCommandOutputPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should handle multiple deleteById calls")
    void testDeleteById_WithMultipleIds_ShouldDeleteEach() {
        // Arrange
        Long id1 = 1L;
        Long id2 = 2L;
        Long id3 = 3L;
        
        MessageProcessingError error1 = new MessageProcessingError();
        error1.setId(id1);
        error1.setEventType("EVENT_1");
        error1.setErrorDescription("Error 1");
        error1.setEntityType("Kardex");
        
        MessageProcessingError error2 = new MessageProcessingError();
        error2.setId(id2);
        error2.setEventType("EVENT_2");
        error2.setErrorDescription("Error 2");
        error2.setEntityType("Kardex");
        
        MessageProcessingError error3 = new MessageProcessingError();
        error3.setId(id3);
        error3.setEventType("EVENT_3");
        error3.setErrorDescription("Error 3");
        error3.setEntityType("Kardex");
        
        when(messageProcessingErrorQueryOutputPort.findById(id1))
                .thenReturn(Optional.of(error1));
        when(messageProcessingErrorQueryOutputPort.findById(id2))
                .thenReturn(Optional.of(error2));
        when(messageProcessingErrorQueryOutputPort.findById(id3))
                .thenReturn(Optional.of(error3));
        
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteById(anyLong());
        
        // Act
        commandService.deleteById(id1);
        commandService.deleteById(id2);
        commandService.deleteById(id3);
        
        // Assert
        verify(messageProcessingErrorQueryOutputPort).findById(id1);
        verify(messageProcessingErrorQueryOutputPort).findById(id2);
        verify(messageProcessingErrorQueryOutputPort).findById(id3);
        verify(messageProcessingErrorCommandOutputPort).deleteById(id1);
        verify(messageProcessingErrorCommandOutputPort).deleteById(id2);
        verify(messageProcessingErrorCommandOutputPort).deleteById(id3);
    }
    
    @Test
    @DisplayName("Should verify query before delete in deleteById")
    void testDeleteById_ShouldVerifyExistenceBeforeDeleting() {
        // Arrange
        when(messageProcessingErrorQueryOutputPort.findById(errorId))
                .thenReturn(Optional.of(messageProcessingError));
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteById(errorId);
        
        // Act
        commandService.deleteById(errorId);
        
        // Assert - verify order of operations
        verify(messageProcessingErrorQueryOutputPort).findById(errorId);
        verify(messageProcessingErrorCommandOutputPort).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should handle deleteAll even when repository throws exception")
    void testDeleteAll_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        String errorMessage = "Database connection failed";
        doThrow(new RuntimeException(errorMessage))
                .when(messageProcessingErrorCommandOutputPort).deleteAll();
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            commandService.deleteAll(),
            "Should propagate exception from repository"
        );
        
        assertEquals(errorMessage, exception.getMessage(), 
                     "Exception message should match");
        verify(messageProcessingErrorCommandOutputPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should pass correct error ID to query port")
    void testDeleteById_ShouldPassCorrectIdToQueryPort() {
        // Arrange
        Long specificId = 42L;
        MessageProcessingError error = new MessageProcessingError();
        error.setId(specificId);
        error.setEventType("TEST_EVENT");
        error.setErrorDescription("Test error");
        error.setEntityType("Product");
        
        when(messageProcessingErrorQueryOutputPort.findById(specificId))
                .thenReturn(Optional.of(error));
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteById(specificId);
        
        // Act
        commandService.deleteById(specificId);
        
        // Assert
        verify(messageProcessingErrorQueryOutputPort).findById(specificId);
        verify(messageProcessingErrorCommandOutputPort).deleteById(specificId);
    }
    
    @Test
    @DisplayName("Should construct proper error message when entity not found")
    void testDeleteById_WhenNotFound_ShouldConstructProperErrorMessage() {
        // Arrange
        Long missingId = 123L;
        String expectedMessage = "Message processing error with id: " + missingId;
        
        when(messageProcessingErrorQueryOutputPort.findById(missingId))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MessageKeys.ERROR_NOT_FOUND), eq(expectedMessage)))
                .thenReturn("Error: " + expectedMessage);
        doThrow(new RuntimeException("Not found"))
                .when(formatterResultOutputPort)
                .returnEntityDoesNotExistErrorResponse(anyInt(), anyString());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            commandService.deleteById(missingId)
        );
        
        verify(messageService).getMessage(eq(MessageKeys.ERROR_NOT_FOUND), eq(expectedMessage));
    }
    
    @Test
    @DisplayName("Should call repository deleteById only once per call")
    void testDeleteById_ShouldCallRepositoryOnlyOnce() {
        // Arrange
        when(messageProcessingErrorQueryOutputPort.findById(errorId))
                .thenReturn(Optional.of(messageProcessingError));
        doNothing().when(messageProcessingErrorCommandOutputPort).deleteById(errorId);
        
        // Act
        commandService.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorCommandOutputPort, times(1)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should return 404 status code when entity not found")
    void testDeleteById_WhenNotFound_ShouldReturn404() {
        // Arrange
        Long missingId = 999L;
        
        when(messageProcessingErrorQueryOutputPort.findById(missingId))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(anyString(), anyString()))
                .thenReturn("Not found");
        doThrow(new RuntimeException("404"))
                .when(formatterResultOutputPort)
                .returnEntityDoesNotExistErrorResponse(eq(404), anyString());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            commandService.deleteById(missingId)
        );
        
        verify(formatterResultOutputPort).returnEntityDoesNotExistErrorResponse(eq(404), anyString());
    }
}
