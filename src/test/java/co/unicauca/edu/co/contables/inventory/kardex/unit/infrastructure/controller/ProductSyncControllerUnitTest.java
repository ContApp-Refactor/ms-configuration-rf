package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller.ProductSyncController;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;

@ExtendWith(MockitoExtension.class)
public class ProductSyncControllerUnitTest {
    
    @Mock
    private IProductSyncCommandPort productCommandPort;
    
    @InjectMocks
    private ProductSyncController productSyncController;
    
    private String enterpriseId;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should sync products successfully")
    void testSyncProducts() {
        // Arrange
        String expectedResult = "Synchronization successful";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Synchronization completed", response.getBody().getMessage());
        assertEquals(expectedResult, response.getBody().getData());
        
        verify(productCommandPort).syncProductsByEnterpriseId(enterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should sync products for different enterprises")
    void testSyncProductsForDifferentEnterprises() {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-002";
        String result1 = "10 products synchronized for ENT-001";
        String result2 = "5 products synchronized for ENT-002";
        
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId1)).thenReturn(result1);
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId2)).thenReturn(result2);
        
        // Act
        ResponseEntity<ResponseDto<String>> response1 = 
            productSyncController.syncProducts(enterpriseId1);
        ResponseEntity<ResponseDto<String>> response2 = 
            productSyncController.syncProducts(enterpriseId2);
        
        // Assert
        assertEquals(result1, response1.getBody().getData());
        assertEquals(result2, response2.getBody().getData());
        
        verify(productCommandPort).syncProductsByEnterpriseId(enterpriseId1);
        verify(productCommandPort).syncProductsByEnterpriseId(enterpriseId2);
    }
    
    @Test
    @DisplayName("Should verify syncProductsByEnterpriseId is called only once")
    void testSyncProductsVerifyOnce() {
        // Arrange
        String result = "Products synchronized";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        productSyncController.syncProducts(enterpriseId);
        
        // Assert
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(enterpriseId);
        verifyNoMoreInteractions(productCommandPort);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify response structure")
    void testSyncProductsResponseStructure() {
        // Arrange
        String result = "Synchronization successful";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getStatus() > 0);
        assertTrue(response.getBody().getData() instanceof String);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should return OK status code")
    void testSyncProductsReturnsOkStatus() {
        // Arrange
        String result = "Products synchronized";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().getStatus());
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sync with special characters in enterprise ID")
    void testSyncProductsWithSpecialEnterpriseId() {
        // Arrange
        String specialEnterpriseId = "ENT-001-ABC_123";
        String result = "Synchronization completed for special enterprise";
        when(productCommandPort.syncProductsByEnterpriseId(specialEnterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(specialEnterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        assertEquals(result, response.getBody().getData());
        
        verify(productCommandPort).syncProductsByEnterpriseId(specialEnterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sync with numeric enterprise ID")
    void testSyncProductsWithNumericEnterpriseId() {
        // Arrange
        String numericEnterpriseId = "12345";
        String result = "Products synchronized";
        when(productCommandPort.syncProductsByEnterpriseId(numericEnterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(numericEnterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        
        verify(productCommandPort).syncProductsByEnterpriseId(numericEnterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should verify sync returns proper message")
    void testSyncProductsMessage() {
        // Arrange
        String result = "Products synchronized";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertEquals("Synchronization completed", response.getBody().getMessage());
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sync with empty result string")
    void testSyncProductsWithEmptyResult() {
        // Arrange
        String emptyResult = "";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(emptyResult);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getBody().getStatus());
        assertEquals(emptyResult, response.getBody().getData());
        
        verify(productCommandPort).syncProductsByEnterpriseId(enterpriseId);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle sync with detailed result message")
    void testSyncProductsWithDetailedMessage() {
        // Arrange
        String detailedResult = "Successfully synchronized 150 products: 100 updated, 50 created, 0 deleted";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(detailedResult);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertEquals(detailedResult, response.getBody().getData());
        verify(productCommandPort).syncProductsByEnterpriseId(enterpriseId);
    }
    
    @Test
    @DisplayName("Should verify ResponseEntity is properly constructed")
    void testSyncProductsResponseEntityConstruction() {
        // Arrange
        String result = "Sync completed";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        ResponseEntity<ResponseDto<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.hasBody());
        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    @DisplayName("Should handle multiple consecutive syncs")
    void testMultipleConsecutiveSyncs() {
        // Arrange
        String result = "Products synchronized";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(result);
        
        // Act
        productSyncController.syncProducts(enterpriseId);
        productSyncController.syncProducts(enterpriseId);
        productSyncController.syncProducts(enterpriseId);
        
        // Assert
        verify(productCommandPort, times(3)).syncProductsByEnterpriseId(enterpriseId);
    }
}
