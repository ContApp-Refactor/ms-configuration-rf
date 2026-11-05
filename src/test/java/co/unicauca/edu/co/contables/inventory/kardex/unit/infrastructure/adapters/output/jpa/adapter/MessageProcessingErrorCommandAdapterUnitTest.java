package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.MessageProcessingErrorCommandAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;

/**
 * @brief Unit tests for MessageProcessingErrorCommandAdapter
 * 
 * Tests the write operations for message processing error records.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageProcessingErrorCommandAdapter Tests")
class MessageProcessingErrorCommandAdapterUnitTest {

    @Mock
    private IMessageProcessingErrorRepository messageProcessingErrorRepository;

    @InjectMocks
    private MessageProcessingErrorCommandAdapter commandAdapter;

    @Test
    @DisplayName("Should delete all message processing errors successfully")
    void testDeleteAll_Success() {
        // Arrange
        doNothing().when(messageProcessingErrorRepository).deleteAll();

        // Act
        commandAdapter.deleteAll();

        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Should delete message processing error by ID successfully")
    void testDeleteById_Success() {
        // Arrange
        Long errorId = 1L;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);

        // Act
        commandAdapter.deleteById(errorId);

        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }

    @Test
    @DisplayName("Should delete multiple errors by ID")
    void testDeleteById_MultipleIds() {
        // Arrange
        Long[] errorIds = {1L, 2L, 3L, 4L, 5L};
        doNothing().when(messageProcessingErrorRepository).deleteById(anyLong());

        // Act
        for (Long errorId : errorIds) {
            commandAdapter.deleteById(errorId);
        }

        // Assert
        verify(messageProcessingErrorRepository, times(errorIds.length)).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle delete by ID with large ID value")
    void testDeleteById_LargeIdValue() {
        // Arrange
        Long largeId = 999999999L;
        doNothing().when(messageProcessingErrorRepository).deleteById(largeId);

        // Act
        commandAdapter.deleteById(largeId);

        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(largeId);
    }

    @Test
    @DisplayName("Should invoke repository deleteAll only once")
    void testDeleteAll_SingleInvocation() {
        // Arrange
        doNothing().when(messageProcessingErrorRepository).deleteAll();

        // Act
        commandAdapter.deleteAll();

        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteAll();
        verifyNoMoreInteractions(messageProcessingErrorRepository);
    }

    @Test
    @DisplayName("Should invoke repository deleteById only once per call")
    void testDeleteById_SingleInvocation() {
        // Arrange
        Long errorId = 42L;
        doNothing().when(messageProcessingErrorRepository).deleteById(errorId);

        // Act
        commandAdapter.deleteById(errorId);

        // Assert
        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
        verifyNoMoreInteractions(messageProcessingErrorRepository);
    }

    @Test
    @DisplayName("Should delete different IDs correctly")
    void testDeleteById_DifferentIds() {
        // Arrange
        Long firstId = 10L;
        Long secondId = 20L;
        Long thirdId = 30L;

        doNothing().when(messageProcessingErrorRepository).deleteById(anyLong());

        // Act
        commandAdapter.deleteById(firstId);
        commandAdapter.deleteById(secondId);
        commandAdapter.deleteById(thirdId);

        // Assert
        verify(messageProcessingErrorRepository).deleteById(firstId);
        verify(messageProcessingErrorRepository).deleteById(secondId);
        verify(messageProcessingErrorRepository).deleteById(thirdId);
    }

    @Test
    @DisplayName("Should handle sequential delete operations")
    void testSequentialDeleteOperations() {
        // Arrange
        doNothing().when(messageProcessingErrorRepository).deleteAll();
        doNothing().when(messageProcessingErrorRepository).deleteById(anyLong());

        // Act
        commandAdapter.deleteById(1L);
        commandAdapter.deleteById(2L);
        commandAdapter.deleteAll();
        commandAdapter.deleteById(3L);

        // Assert
        verify(messageProcessingErrorRepository, times(3)).deleteById(anyLong());
        verify(messageProcessingErrorRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Should handle exception during deleteAll gracefully")
    void testDeleteAll_WithException() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(messageProcessingErrorRepository).deleteAll();

        // Act & Assert
        try {
            commandAdapter.deleteAll();
        } catch (RuntimeException e) {
            // Exception is expected
        }

        verify(messageProcessingErrorRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Should handle exception during deleteById gracefully")
    void testDeleteById_WithException() {
        // Arrange
        Long errorId = 1L;
        doThrow(new RuntimeException("Database error")).when(messageProcessingErrorRepository).deleteById(errorId);

        // Act & Assert
        try {
            commandAdapter.deleteById(errorId);
        } catch (RuntimeException e) {
            // Exception is expected
        }

        verify(messageProcessingErrorRepository, times(1)).deleteById(errorId);
    }
}
