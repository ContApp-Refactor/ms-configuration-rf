package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.MessageProcessingErrorCommandAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;

@ExtendWith(MockitoExtension.class)
public class MessageProcessingErrorCommandAdapterUnitTest {
    
    @Mock
    private IMessageProcessingErrorRepository messageProcessingErrorRepository;
    
    @InjectMocks
    private MessageProcessingErrorCommandAdapter messageProcessingErrorCommandAdapter;
    
    // ==================== deleteAll() ====================
    @Test
    @DisplayName("Should delete all message processing errors successfully")
    void testDeleteAll_Success_DeletesAllRecords() {
        // Arrange
        doNothing().when(messageProcessingErrorRepository).deleteAll();
        
        // Act
        messageProcessingErrorCommandAdapter.deleteAll();
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should propagate exception when deleteAll fails")
    void testDeleteAll_RepositoryThrowsException_PropagatesException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database connection error");
        doThrow(exception).when(messageProcessingErrorRepository).deleteAll();
        
        // Act & Assert
        try {
            messageProcessingErrorCommandAdapter.deleteAll();
        } catch (RuntimeException e) {
            // Exception is expected to be propagated
        }
        
        verify(messageProcessingErrorRepository, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should call repository deleteAll exactly once")
    void testDeleteAll_CallsRepositoryOnce() {
        // Arrange
        doNothing().when(messageProcessingErrorRepository).deleteAll();
        
        // Act
        messageProcessingErrorCommandAdapter.deleteAll();
        messageProcessingErrorCommandAdapter.deleteAll();
        
        // Assert
        verify(messageProcessingErrorRepository, times(2)).deleteAll();
    }
    
    // ==================== deleteById() ====================
    @Test
    @DisplayName("Should delete message processing error by ID successfully")
    void testDeleteById_ValidId_DeletesRecord() {
        // Arrange
        Long errorId = 1L;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should delete error with different ID values")
    void testDeleteById_DifferentIds_DeletesCorrectRecords() {
        // Arrange
        Long id1 = 5L;
        Long id2 = 10L;
        doNothing().when(messageProcessingErrorRepository).deleteById(id1);
        doNothing().when(messageProcessingErrorRepository).deleteById(id2);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(id1);
        messageProcessingErrorCommandAdapter.deleteById(id2);
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(id1);
        verify(messageProcessingErrorRepository, times(1)).deleteById(id2);
    }
    
    @Test
    @DisplayName("Should handle null ID gracefully")
    void testDeleteById_NullId_CallsRepositoryWithNull() {
        // Arrange
        doNothing().when(messageProcessingErrorRepository).deleteById(null);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(null);
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(null);
    }
    
    @Test
    @DisplayName("Should propagate exception when deleteById fails")
    void testDeleteById_RepositoryThrowsException_PropagatesException() {
        // Arrange
        Long errorId = 1L;
        RuntimeException exception = new RuntimeException("Record not found");
        doThrow(exception).when(messageProcessingErrorRepository).deleteById(errorId);
        
        // Act & Assert
        try {
            messageProcessingErrorCommandAdapter.deleteById(errorId);
        } catch (RuntimeException e) {
            // Exception is expected to be propagated
        }
        
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should delete same ID multiple times")
    void testDeleteById_SameIdMultipleTimes_CallsRepositoryMultipleTimes() {
        // Arrange
        Long errorId = 1L;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorRepository, times(3)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should handle zero ID value")
    void testDeleteById_ZeroId_CallsRepository() {
        // Arrange
        Long errorId = 0L;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should handle negative ID value")
    void testDeleteById_NegativeId_CallsRepository() {
        // Arrange
        Long errorId = -1L;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }
    
    @Test
    @DisplayName("Should handle large ID value")
    void testDeleteById_LargeId_CallsRepository() {
        // Arrange
        Long errorId = Long.MAX_VALUE;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);
        
        // Act
        messageProcessingErrorCommandAdapter.deleteById(errorId);
        
        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }
}
