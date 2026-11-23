package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.messajeBroker.recovery;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.recovery.ProductRecoveryActionPort;

@ExtendWith(MockitoExtension.class)
public class ProductRecoveryActionPortUnitTest {
    
     @InjectMocks
    private ProductRecoveryActionPort productRecoveryActionPort;

    @Mock
    private IProductSyncCommandPort productSyncCommandPort;

    private EventDto<ProductAsyncDto, EventProductType> event;
    private ProductAsyncDto productAsyncDto;

    @BeforeEach
    void setUp() {
        productAsyncDto = new ProductAsyncDto();
        event = new EventDto<>();
        event.setData(productAsyncDto);
    }

    // ==================== Tests para executeRecoveryAction ====================

    @Test
    @DisplayName("executeRecoveryAction - Con enterpriseId válido ejecuta sync exitosamente y retorna true")
    void executeRecoveryAction_WithValidEnterpriseId_ExecutesSyncSuccessfullyAndReturnsTrue() {
        // Arrange
        String enterpriseId = "enterprise-123";
        productAsyncDto.setEnterpriseId(enterpriseId);
        String syncResult = "Sync completed successfully";
        
        when(productSyncCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(syncResult);
        
        // Act
        boolean result = productRecoveryActionPort.executeRecoveryAction(event);
        
        // Assert
        assertTrue(result);
        verify(productSyncCommandPort, times(1)).syncProductsByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("executeRecoveryAction - Cuando sync falla con excepción retorna false")
    void executeRecoveryAction_WhenSyncFailsWithException_ReturnsFalse() {
        // Arrange
        String enterpriseId = "enterprise-456";
        productAsyncDto.setEnterpriseId(enterpriseId);
        
        when(productSyncCommandPort.syncProductsByEnterpriseId(enterpriseId))
            .thenThrow(new RuntimeException("Sync failed"));
        
        // Act
        boolean result = productRecoveryActionPort.executeRecoveryAction(event);
        
        // Assert
        assertFalse(result);
        verify(productSyncCommandPort, times(1)).syncProductsByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("executeRecoveryAction - Con enterpriseId null captura excepción y retorna false")
    void executeRecoveryAction_WithNullEnterpriseId_CatchesExceptionAndReturnsFalse() {
        // Arrange
        productAsyncDto.setEnterpriseId(null);
        
        // Act
        boolean result = productRecoveryActionPort.executeRecoveryAction(event);
        
        // Assert
        assertFalse(result);
        verify(productSyncCommandPort, never()).syncProductsByEnterpriseId(any());
    }

    @Test
    @DisplayName("executeRecoveryAction - Con event data null captura excepción y retorna false")
    void executeRecoveryAction_WithNullEventData_CatchesExceptionAndReturnsFalse() {
        // Arrange
        event.setData(null);
        
        // Act
        boolean result = productRecoveryActionPort.executeRecoveryAction(event);
        
        // Assert
        assertFalse(result);
        verify(productSyncCommandPort, never()).syncProductsByEnterpriseId(any());
    }

    @Test
    @DisplayName("executeRecoveryAction - Con múltiples enterpriseIds ejecuta sync para cada uno correctamente")
    void executeRecoveryAction_WithDifferentEnterpriseIds_ExecutesSyncCorrectly() {
        // Arrange
        String enterpriseId1 = "enterprise-001";
        String enterpriseId2 = "enterprise-002";
        
        productAsyncDto.setEnterpriseId(enterpriseId1);
        when(productSyncCommandPort.syncProductsByEnterpriseId(enterpriseId1)).thenReturn("Success 1");
        
        // Act - First call
        boolean result1 = productRecoveryActionPort.executeRecoveryAction(event);
        
        // Arrange second call
        productAsyncDto.setEnterpriseId(enterpriseId2);
        when(productSyncCommandPort.syncProductsByEnterpriseId(enterpriseId2)).thenReturn("Success 2");
        
        // Act - Second call
        boolean result2 = productRecoveryActionPort.executeRecoveryAction(event);
        
        // Assert
        assertTrue(result1);
        assertTrue(result2);
        verify(productSyncCommandPort, times(1)).syncProductsByEnterpriseId(enterpriseId1);
        verify(productSyncCommandPort, times(1)).syncProductsByEnterpriseId(enterpriseId2);
    }

    // ==================== Tests para canHandle ====================

    @Test
    @DisplayName("canHandle - Con event y data válidos retorna true")
    void canHandle_WithValidEventAndData_ReturnsTrue() {
        // Arrange
        productAsyncDto.setEnterpriseId("enterprise-789");
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("canHandle - Con event null retorna false")
    void canHandle_WithNullEvent_ReturnsFalse() {
        // Act
        boolean result = productRecoveryActionPort.canHandle(null);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("canHandle - Con event data null retorna false")
    void canHandle_WithNullEventData_ReturnsFalse() {
        // Arrange
        event.setData(null);
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("canHandle - Con enterpriseId null retorna false")
    void canHandle_WithNullEnterpriseId_ReturnsFalse() {
        // Arrange
        productAsyncDto.setEnterpriseId(null);
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("canHandle - Con enterpriseId vacío retorna false")
    void canHandle_WithEmptyEnterpriseId_ReturnsFalse() {
        // Arrange
        productAsyncDto.setEnterpriseId("");
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("canHandle - Con enterpriseId solo espacios retorna false")
    void canHandle_WithBlankEnterpriseId_ReturnsFalse() {
        // Arrange
        productAsyncDto.setEnterpriseId("   ");
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("canHandle - Con enterpriseId válido no vacío retorna true")
    void canHandle_WithValidNonEmptyEnterpriseId_ReturnsTrue() {
        // Arrange
        productAsyncDto.setEnterpriseId("valid-enterprise-id");
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("canHandle - Con enterpriseId con espacios pero contenido válido retorna true")
    void canHandle_WithEnterpriseIdWithSpacesButValidContent_ReturnsTrue() {
        // Arrange
        productAsyncDto.setEnterpriseId("  enterprise-with-spaces  ");
        
        // Act
        boolean result = productRecoveryActionPort.canHandle(event);
        
        // Assert
        assertTrue(result);
    }
}
