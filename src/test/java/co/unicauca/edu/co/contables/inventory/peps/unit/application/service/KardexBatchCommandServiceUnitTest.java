package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.application.service.command.KardexBatchCommandService;
import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IKardexExternalClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDTO;

/**
 * @brief Unit tests for KardexBatchCommandService
 * 
 * Tests batch processing logic including transaction management,
 * error handling, and rollback scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kardex Batch Command Service Unit Tests")
class KardexBatchCommandServiceUnitTest {
    
    @Mock
    private IKardexExternalClientPort kardexExternalClientPort;
    
    @Mock
    private IKardexCommandPort kardexCommandPort;
    
    @InjectMocks
    private KardexBatchCommandService batchCommandService;
    
    private String enterpriseId;
    private Product product1;
    private Product product2;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        
        product1 = Product.builder()
                .productId(1L)
                .name("Product 1")
                .build();
        
        product2 = Product.builder()
                .productId(2L)
                .name("Product 2")
                .build();
    }
    
    @Test
    @DisplayName("Should successfully process batch with multiple records")
    void testProcessBatchFromExternalService_WithMultipleRecords_ShouldProcessSuccessfully() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(3);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccess(), "Processing should be successful");
        assertEquals(3, result.getTotalRecords(), "Total records should be 3");
        assertEquals(3, result.getProcessedRecords(), "Processed records should be 3");
        assertEquals(0, result.getFailedRecords(), "Failed records should be 0");
        assertTrue(result.getErrors().isEmpty(), "Errors list should be empty");
        
        verify(kardexCommandPort).deleteAll();
        verify(kardexCommandPort, times(3)).registerPurchase(any(Kardex.class));
        verify(kardexExternalClientPort).findKardexByEnterpriseId(enterpriseId);
    }
    
    @Test
    @DisplayName("Should handle empty kardex list from external service")
    void testProcessBatchFromExternalService_WithEmptyList_ShouldReturnZeroRecords() {
        // Arrange
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(Collections.emptyList());
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccess(), "Processing should be successful");
        assertEquals(0, result.getTotalRecords(), "Total records should be 0");
        assertEquals(0, result.getProcessedRecords(), "Processed records should be 0");
        assertEquals(0, result.getFailedRecords(), "Failed records should be 0");
        assertTrue(result.getErrors().isEmpty(), "Errors list should be empty");
        
        verify(kardexCommandPort).deleteAll();
        verify(kardexCommandPort, never()).registerPurchase(any(Kardex.class));
    }
    
    @Test
    @DisplayName("Should handle null kardex list from external service")
    void testProcessBatchFromExternalService_WithNullList_ShouldReturnZeroRecords() {
        // Arrange
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(null);
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccess(), "Processing should be successful");
        assertEquals(0, result.getTotalRecords(), "Total records should be 0");
        assertEquals(0, result.getProcessedRecords(), "Processed records should be 0");
        assertEquals(0, result.getFailedRecords(), "Failed records should be 0");
        
        verify(kardexCommandPort).deleteAll();
        verify(kardexCommandPort, never()).registerPurchase(any(Kardex.class));
    }
    
    @Test
    @DisplayName("Should rollback transaction when processing fails")
    void testProcessBatchFromExternalService_WhenProcessingFails_ShouldRollback() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(3);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        
        // First record succeeds, second fails
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenReturn(kardexList.get(0))  // First call succeeds
                .thenThrow(new RuntimeException("Database error")); // Second call fails
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Processing should fail");
        assertEquals(2, result.getTotalRecords(), "Total records should be 2");
        assertEquals(0, result.getProcessedRecords(), "Processed records should be 0 due to rollback");
        assertEquals(2, result.getFailedRecords(), "Failed records should be 2");
        assertFalse(result.getErrors().isEmpty(), "Errors list should not be empty");
        
        verify(kardexCommandPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should handle invalid movement type")
    void testProcessBatchFromExternalService_WithInvalidMovementType_ShouldFail() {
        // Arrange
        Kardex invalidKardex = Kardex.builder()
                .factCode("1001")
                .date(ZonedDateTime.now(ZoneId.of("America/Bogota")))
                .details("Invalid movement")
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(100))
                .type(MovementType.SALE) // Invalid type for batch processing
                .product(product1)
                .build();
        
        List<Kardex> kardexList = Collections.singletonList(invalidKardex);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Processing should fail");
        assertEquals(1, result.getTotalRecords(), "Total records should be 1");
        assertEquals(0, result.getProcessedRecords(), "Processed records should be 0");
        assertEquals(1, result.getFailedRecords(), "Failed records should be 1");
        assertFalse(result.getErrors().isEmpty(), "Errors list should not be empty");
        
        verify(kardexCommandPort).deleteAll();
        verify(kardexCommandPort, never()).registerPurchase(any(Kardex.class));
    }
    
    @Test
    @DisplayName("Should process single record successfully")
    void testProcessBatchFromExternalService_WithSingleRecord_ShouldProcessSuccessfully() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(1);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccess(), "Processing should be successful");
        assertEquals(1, result.getTotalRecords(), "Total records should be 1");
        assertEquals(1, result.getProcessedRecords(), "Processed records should be 1");
        assertEquals(0, result.getFailedRecords(), "Failed records should be 0");
        
        verify(kardexCommandPort).deleteAll();
        verify(kardexCommandPort, times(1)).registerPurchase(any(Kardex.class));
    }
    
    @Test
    @DisplayName("Should call deleteAll before processing")
    void testProcessBatchFromExternalService_ShouldCallDeleteAllFirst() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(2);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        verify(kardexCommandPort).deleteAll();
        verify(kardexExternalClientPort).findKardexByEnterpriseId(enterpriseId);
        verify(kardexCommandPort, times(2)).registerPurchase(any(Kardex.class));
    }
    
    @Test
    @DisplayName("Should handle exception from external client")
    void testProcessBatchFromExternalService_WhenExternalClientFails_ShouldReturnError() {
        // Arrange
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenThrow(new RuntimeException("External service unavailable"));
        doNothing().when(kardexCommandPort).deleteAll();
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Processing should fail");
        assertEquals(0, result.getTotalRecords(), "Total records should be 0");
        assertEquals(0, result.getProcessedRecords(), "Processed records should be 0");
        assertEquals(0, result.getFailedRecords(), "Failed records should be 0");
        
        verify(kardexCommandPort).deleteAll();
    }
    @Test
    @DisplayName("Should track error details when processing fails")
    void testProcessBatchFromExternalService_WhenFails_ShouldTrackErrorDetails() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(2);
        String errorMessage = "Validation failed";
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenThrow(new RuntimeException(errorMessage));
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Processing should fail");
        assertFalse(result.getErrors().isEmpty(), "Errors should be tracked");
        assertEquals(1, result.getErrors().size(), "Should have one error");
        assertEquals(errorMessage, result.getErrors().get(0).getErrorMessage(), 
                     "Error message should match");
        assertEquals(0, result.getErrors().get(0).getRecordIndex(), 
                     "Record index should be 0");
        
        verify(kardexCommandPort).deleteAll();
    }
    
    @Test
    @DisplayName("Should process large batch successfully")
    void testProcessBatchFromExternalService_WithLargeBatch_ShouldProcessSuccessfully() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(100);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccess(), "Processing should be successful");
        assertEquals(100, result.getTotalRecords(), "Total records should be 100");
        assertEquals(100, result.getProcessedRecords(), "Processed records should be 100");
        assertEquals(0, result.getFailedRecords(), "Failed records should be 0");
        
        verify(kardexCommandPort).deleteAll();
        verify(kardexCommandPort, times(100)).registerPurchase(any(Kardex.class));
    }
    @Test
    @DisplayName("Should handle null error message gracefully")
    void testProcessBatchFromExternalService_WithNullErrorMessage_ShouldUseDefaultMessage() {
        // Arrange
        List<Kardex> kardexList = createValidKardexList(1);
        
        when(kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId))
                .thenReturn(kardexList);
        doNothing().when(kardexCommandPort).deleteAll();
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenThrow(new RuntimeException((String) null));
        
        // Act
        KardexBatchProcessingResultDTO result = 
                batchCommandService.processBatchFromExternalService(enterpriseId);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isSuccess(), "Processing should fail");
        assertFalse(result.getErrors().isEmpty(), "Should have errors");
        assertEquals("Unknown error", result.getErrors().get(0).getErrorMessage(),
                     "Should use default error message");
    }
    
    // Helper methods
    
    /**
     * Creates a list of valid Kardex records for testing
     * 
     * @param count Number of records to create
     * @return List of Kardex objects
     */
    private List<Kardex> createValidKardexList(int count) {
        List<Kardex> kardexList = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Product product = (i % 2 == 0) ? product1 : product2;
            
            Kardex kardex = Kardex.builder()
                    .factCode(String.valueOf(1000 + i))
                    .date(ZonedDateTime.now(ZoneId.of("America/Bogota")))
                    .details("Purchase " + (i + 1))
                    .quantity(10 + i)
                    .unitPrice(BigDecimal.valueOf(100 + i * 10))
                    .type(MovementType.PURCHASE)
                    .product(product)
                    .build();
            
            kardexList.add(kardex);
        }
        
        return kardexList;
    }
}
