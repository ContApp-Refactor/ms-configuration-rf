package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;

/**
 * @brief Unit tests for ProductSyncController
 * 
 * Tests the REST controller for product synchronization operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSyncController Tests")
public class ProductSyncControllerUnitTest {
    
    @Mock
    private IProductSyncCommandPort productCommandPort;
    
    @InjectMocks
    private co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.ProductSyncController productSyncController;
    
    private String enterpriseId;
    private String successMessage;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        successMessage = "Synchronization completed successfully";
    }
    
    @Test
    @DisplayName("Should sync products successfully")
    void testSyncProducts_Success() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<String> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Synchronization completed", body.getMessage());
        assertEquals(successMessage, body.getData());
        
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(enterpriseId);
    }
    
    @Test
    @DisplayName("Should sync products with different enterprise IDs")
    void testSyncProducts_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-999";
        when(productCommandPort.syncProductsByEnterpriseId(differentEnterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(differentEnterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(differentEnterpriseId);
    }
    
    @Test
    @DisplayName("Should handle sync with custom message")
    void testSyncProducts_CustomMessage() {
        // Arrange
        String customMessage = "10 products synchronized successfully for ENT-001";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(customMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(customMessage, response.getBody().getData());
    }
    
    @Test
    @DisplayName("Should verify response structure")
    void testSyncProducts_ResponseStructure() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should verify HTTP status is OK")
    void testSyncProducts_HttpStatus() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should verify synchronization message")
    void testSyncProducts_Message() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertEquals("Synchronization completed", response.getBody().getMessage());
    }
    
    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testSyncProducts_EmptyEnterpriseId() {
        // Arrange
        String emptyEnterpriseId = "";
        when(productCommandPort.syncProductsByEnterpriseId(emptyEnterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(emptyEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(emptyEnterpriseId);
    }
    
    @Test
    @DisplayName("Should verify port is called exactly once")
    void testSyncProducts_PortVerification() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        productSyncController.syncProducts(enterpriseId);
        
        // Assert
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(eq(enterpriseId));
    }
    
    @Test
    @DisplayName("Should handle multiple sequential synchronizations")
    void testMultipleSequentialSyncs() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";
        String enterprise3 = "ENT-003";
        
        when(productCommandPort.syncProductsByEnterpriseId(enterprise1)).thenReturn("Enterprise 1 synced");
        when(productCommandPort.syncProductsByEnterpriseId(enterprise2)).thenReturn("Enterprise 2 synced");
        when(productCommandPort.syncProductsByEnterpriseId(enterprise3)).thenReturn("Enterprise 3 synced");
        
        // Act
        ResponseEntity<ResponseDTO<String>> response1 = productSyncController.syncProducts(enterprise1);
        ResponseEntity<ResponseDTO<String>> response2 = productSyncController.syncProducts(enterprise2);
        ResponseEntity<ResponseDTO<String>> response3 = productSyncController.syncProducts(enterprise3);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        
        assertEquals("Enterprise 1 synced", response1.getBody().getData());
        assertEquals("Enterprise 2 synced", response2.getBody().getData());
        assertEquals("Enterprise 3 synced", response3.getBody().getData());
        
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(enterprise1);
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(enterprise2);
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(enterprise3);
    }
    
    @Test
    @DisplayName("Should handle sync with no products message")
    void testSyncProducts_NoProducts() {
        // Arrange
        String noProductsMessage = "No products found to synchronize";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(noProductsMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(noProductsMessage, response.getBody().getData());
    }
    
    @Test
    @DisplayName("Should handle sync with products count message")
    void testSyncProducts_WithCount() {
        // Arrange
        String countMessage = "Successfully synchronized 25 products";
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(countMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(countMessage, response.getBody().getData());
    }
    
    @Test
    @DisplayName("Should handle sync with special characters in enterprise ID")
    void testSyncProducts_SpecialCharactersInEnterpriseId() {
        // Arrange
        String specialEnterpriseId = "ENT-@#$-001";
        when(productCommandPort.syncProductsByEnterpriseId(specialEnterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(specialEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(specialEnterpriseId);
    }
    
    @Test
    @DisplayName("Should handle sync with long enterprise ID")
    void testSyncProducts_LongEnterpriseId() {
        // Arrange
        String longEnterpriseId = "ENT-" + "A".repeat(100);
        when(productCommandPort.syncProductsByEnterpriseId(longEnterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(longEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(productCommandPort, times(1)).syncProductsByEnterpriseId(longEnterpriseId);
    }
    
    @Test
    @DisplayName("Should return ResponseEntity with OK status")
    void testSyncProducts_ResponseEntityStatus() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(ResponseEntity.ok(response.getBody()).getStatusCode(), response.getStatusCode());
    }
    
    @Test
    @DisplayName("Should handle same enterprise ID multiple times")
    void testSyncProducts_SameEnterpriseMultipleTimes() {
        // Arrange
        when(productCommandPort.syncProductsByEnterpriseId(enterpriseId))
            .thenReturn("First sync")
            .thenReturn("Second sync")
            .thenReturn("Third sync");
        
        // Act
        ResponseEntity<ResponseDTO<String>> response1 = productSyncController.syncProducts(enterpriseId);
        ResponseEntity<ResponseDTO<String>> response2 = productSyncController.syncProducts(enterpriseId);
        ResponseEntity<ResponseDTO<String>> response3 = productSyncController.syncProducts(enterpriseId);
        
        // Assert
        assertEquals("First sync", response1.getBody().getData());
        assertEquals("Second sync", response2.getBody().getData());
        assertEquals("Third sync", response3.getBody().getData());
        
        verify(productCommandPort, times(3)).syncProductsByEnterpriseId(enterpriseId);
    }
}

