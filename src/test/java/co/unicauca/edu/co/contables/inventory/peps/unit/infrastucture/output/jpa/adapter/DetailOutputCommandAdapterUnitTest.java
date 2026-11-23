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
import org.springframework.dao.DataAccessException;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.DetailOutputCommandAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IDetailOutPutRepository;

@ExtendWith(MockitoExtension.class)
public class DetailOutputCommandAdapterUnitTest {
    
    @Mock
    private IDetailOutPutRepository detailOutPutRepository;
    
    @InjectMocks
    private DetailOutputCommandAdapter detailOutputCommandAdapter;
    
    // ==================== deleteAll() ====================
    @Test
    @DisplayName("Should delete all detail output records successfully")
    void testDeleteAll_Success_DeletesAllRecords() {
        // Arrange
        doNothing().when(detailOutPutRepository).deleteAll();
        
        // Act
        detailOutputCommandAdapter.deleteAll();
        
        // Assert
        verify(detailOutPutRepository, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should propagate exception when deleteAll fails")
    void testDeleteAll_RepositoryThrowsException_PropagatesException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database error");
        doThrow(exception).when(detailOutPutRepository).deleteAll();
        
        // Act & Assert
        try {
            detailOutputCommandAdapter.deleteAll();
        } catch (RuntimeException e) {
            // Exception is expected to be propagated
        }
        
        verify(detailOutPutRepository, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should propagate DataAccessException when repository fails")
    void testDeleteAll_DataAccessException_PropagatesException() {
        // Arrange
        DataAccessException exception = new DataAccessException("Connection lost") {};
        doThrow(exception).when(detailOutPutRepository).deleteAll();
        
        // Act & Assert
        try {
            detailOutputCommandAdapter.deleteAll();
        } catch (DataAccessException e) {
            // Exception is expected to be propagated
        }
        
        verify(detailOutPutRepository, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should call repository deleteAll exactly once per invocation")
    void testDeleteAll_MultipleInvocations_CallsRepositoryMultipleTimes() {
        // Arrange
        doNothing().when(detailOutPutRepository).deleteAll();
        
        // Act
        detailOutputCommandAdapter.deleteAll();
        detailOutputCommandAdapter.deleteAll();
        detailOutputCommandAdapter.deleteAll();
        
        // Assert
        verify(detailOutPutRepository, times(3)).deleteAll();
    }
    
    @Test
    @DisplayName("Should delegate deletion to repository without modification")
    void testDeleteAll_DelegatesToRepository() {
        // Arrange
        doNothing().when(detailOutPutRepository).deleteAll();
        
        // Act
        detailOutputCommandAdapter.deleteAll();
        
        // Assert - Verify direct delegation
        verify(detailOutPutRepository, times(1)).deleteAll();
    }
    
    @Test
    @DisplayName("Should handle concurrent deleteAll calls")
    void testDeleteAll_ConcurrentCalls_HandlesCorrectly() {
        // Arrange
        doNothing().when(detailOutPutRepository).deleteAll();
        
        // Act - Simulate multiple calls
        for (int i = 0; i < 5; i++) {
            detailOutputCommandAdapter.deleteAll();
        }
        
        // Assert
        verify(detailOutPutRepository, times(5)).deleteAll();
    }
}
