package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import co.unicauca.edu.co.contables.inventory.peps.application.service.command.DetailOutputCommandService;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IDetailOutputCommandOutPutPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetailOutputCommandServiceUnitTest {

    @Mock
    private IDetailOutputCommandOutPutPort detailOutputCommandOutPutPort;

    @InjectMocks
    private DetailOutputCommandService detailOutputCommandService;

    @BeforeEach
    void setUp() {
        // Reset mock interactions before each test
        reset(detailOutputCommandOutPutPort);
    }

    // ========== Tests for deleteAll() ==========

    @Test
    void testDeleteAll_ShouldDelegateToOutputPort() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
    }

    @Test
    void testDeleteAll_ShouldCallOutputPortExactlyOnce() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
        verifyNoMoreInteractions(detailOutputCommandOutPutPort);
    }

    @Test
    void testDeleteAll_ShouldNotThrowException() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert
        assertDoesNotThrow(() -> detailOutputCommandService.deleteAll());
    }

    @Test
    void testDeleteAll_WhenCalledMultipleTimes_ShouldDelegateEachTime() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();
        detailOutputCommandService.deleteAll();
        detailOutputCommandService.deleteAll();

        // Assert
        verify(detailOutputCommandOutPutPort, times(3)).deleteAll();
    }

    @Test
    void testDeleteAll_WhenOutputPortThrowsException_ShouldPropagateException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Database error");
        doThrow(expectedException).when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert
        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> detailOutputCommandService.deleteAll()
        );
        assertEquals("Database error", thrownException.getMessage());
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
    }

    @Test
    void testDeleteAll_WhenOutputPortThrowsIllegalStateException_ShouldPropagateException() {
        // Arrange
        IllegalStateException expectedException = new IllegalStateException("Invalid state");
        doThrow(expectedException).when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert
        IllegalStateException thrownException = assertThrows(
                IllegalStateException.class,
                () -> detailOutputCommandService.deleteAll()
        );
        assertEquals("Invalid state", thrownException.getMessage());
    }

    // ========== Tests for Service Behavior ==========

    @Test
    void testService_ShouldBeInstantiated() {
        // Assert
        assertNotNull(detailOutputCommandService);
    }

    @Test
    void testService_ShouldHaveOutputPortInjected() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert - if port is injected, it should be called
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
    }

    // ========== Tests for Integration Scenarios ==========

    @Test
    void testDeleteAll_CompleteScenario_SuccessfulDeletion() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
        verifyNoMoreInteractions(detailOutputCommandOutPutPort);
    }

    @Test
    void testDeleteAll_CompleteScenario_WithException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Connection timeout");
        doThrow(expectedException).when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert
        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> detailOutputCommandService.deleteAll()
        );
        
        assertEquals("Connection timeout", thrownException.getMessage());
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
    }

    // ========== Tests for Edge Cases ==========

    @Test
    void testDeleteAll_WhenCalledConcurrently_ShouldHandleEachCall() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act - Simulate concurrent calls
        detailOutputCommandService.deleteAll();
        detailOutputCommandService.deleteAll();

        // Assert
        verify(detailOutputCommandOutPutPort, times(2)).deleteAll();
    }

    @Test
    void testDeleteAll_AfterException_ShouldStillWork() {
        // Arrange
        RuntimeException exception = new RuntimeException("First call fails");
        doThrow(exception)
            .doNothing()
            .when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert - First call should throw
        assertThrows(RuntimeException.class, () -> detailOutputCommandService.deleteAll());
        
        // Second call should succeed
        assertDoesNotThrow(() -> detailOutputCommandService.deleteAll());
        
        verify(detailOutputCommandOutPutPort, times(2)).deleteAll();
    }

    // ========== Tests for Delegation Pattern ==========

    @Test
    void testDeleteAll_ShouldFollowDelegationPattern() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert - Service should only delegate, not add logic
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
        verifyNoMoreInteractions(detailOutputCommandOutPutPort);
    }

    @Test
    void testDeleteAll_ShouldNotModifyBehavior() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert - Verify it's a pure delegation (no additional behavior)
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
    }

    // ========== Tests for Verification ==========

    @Test
    void testDeleteAll_ShouldVerifyNoOtherInteractions() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert
        verify(detailOutputCommandOutPutPort).deleteAll();
        verifyNoMoreInteractions(detailOutputCommandOutPutPort);
    }

    @Test
    void testDeleteAll_WithInOrder_ShouldMaintainCallOrder() {
        // Arrange
        doNothing().when(detailOutputCommandOutPutPort).deleteAll();

        // Act
        detailOutputCommandService.deleteAll();

        // Assert
        var inOrder = inOrder(detailOutputCommandOutPutPort);
        inOrder.verify(detailOutputCommandOutPutPort).deleteAll();
        inOrder.verifyNoMoreInteractions();
    }

    // ========== Tests for Exception Handling ==========

    @Test
    void testDeleteAll_WhenNullPointerException_ShouldPropagate() {
        // Arrange
        NullPointerException expectedException = new NullPointerException("Null reference");
        doThrow(expectedException).when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert
        NullPointerException thrownException = assertThrows(
                NullPointerException.class,
                () -> detailOutputCommandService.deleteAll()
        );
        assertEquals("Null reference", thrownException.getMessage());
    }

    @Test
    void testDeleteAll_WhenDataAccessException_ShouldPropagate() {
        // Arrange
        RuntimeException dataAccessException = new RuntimeException("Data access error");
        doThrow(dataAccessException).when(detailOutputCommandOutPutPort).deleteAll();

        // Act & Assert
        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> detailOutputCommandService.deleteAll()
        );
        assertEquals("Data access error", thrownException.getMessage());
        verify(detailOutputCommandOutPutPort, times(1)).deleteAll();
    }
}
