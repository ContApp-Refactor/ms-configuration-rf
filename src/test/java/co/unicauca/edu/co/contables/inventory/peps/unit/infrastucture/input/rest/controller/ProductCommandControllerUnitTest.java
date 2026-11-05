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

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IProductCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.ProductCommandController;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;

/**
 * @brief Unit tests for ProductCommandController
 * 
 * Tests the REST controller for product command operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCommandController Tests")
public class ProductCommandControllerUnitTest {
    
    @Mock
    private IProductCommandPort productCommandPort;
    
    @InjectMocks
    private ProductCommandController productCommandController;
    
    private Long productId;
    private String enterpriseId;
    private String successMessage;
    
    @BeforeEach
    void setUp() {
        productId = 1L;
        enterpriseId = "ENT-001";
        successMessage = "Operation completed successfully";
    }
    
    @Test
    @DisplayName("Should delete product by ID successfully")
    void testDeleteProduct_Success() {
        // Arrange
        when(productCommandPort.deleteById(productId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteProduct(productId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<String> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Product deleted successfully", body.getMessage());
        assertEquals(successMessage, body.getData());
        
        verify(productCommandPort, times(1)).deleteById(productId);
    }
    
    @Test
    @DisplayName("Should delete all products by enterprise ID successfully")
    void testDeleteAllProducts_Success() {
        // Arrange
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<String> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Products deleted successfully", body.getMessage());
        assertEquals(successMessage, body.getData());
        
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(enterpriseId);
    }
    
    @Test
    @DisplayName("Should delete product with different product IDs")
    void testDeleteProduct_DifferentProductIds() {
        // Arrange
        Long differentProductId = 999L;
        when(productCommandPort.deleteById(differentProductId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteProduct(differentProductId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productCommandPort, times(1)).deleteById(differentProductId);
    }
    
    @Test
    @DisplayName("Should delete all products with different enterprise IDs")
    void testDeleteAllProducts_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-999";
        when(productCommandPort.deleteAllByEnterpriseId(differentEnterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteAllProducts(differentEnterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(differentEnterpriseId);
    }
    
    @Test
    @DisplayName("Should handle product deletion with custom message")
    void testDeleteProduct_CustomMessage() {
        // Arrange
        String customMessage = "Product 123 has been removed from the system";
        when(productCommandPort.deleteById(productId)).thenReturn(customMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteProduct(productId);
        
        // Assert
        assertNotNull(response);
        assertEquals(customMessage, response.getBody().getData());
    }
    
    @Test
    @DisplayName("Should handle delete all products with custom message")
    void testDeleteAllProducts_CustomMessage() {
        // Arrange
        String customMessage = "All products for enterprise ENT-001 have been deleted";
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(customMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(customMessage, response.getBody().getData());
    }
    
    @Test
    @DisplayName("Should verify correct response structure for deleteProduct")
    void testDeleteProduct_ResponseStructure() {
        // Arrange
        when(productCommandPort.deleteById(productId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteProduct(productId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should verify correct response structure for deleteAllProducts")
    void testDeleteAllProducts_ResponseStructure() {
        // Arrange
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should verify messages for both operations")
    void testBothOperations_MessageVerification() {
        // Arrange
        when(productCommandPort.deleteById(productId)).thenReturn(successMessage);
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> deleteProductResponse = 
            productCommandController.deleteProduct(productId);
        ResponseEntity<ResponseDTO<String>> deleteAllResponse = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertEquals("Product deleted successfully", deleteProductResponse.getBody().getMessage());
        assertEquals("Products deleted successfully", deleteAllResponse.getBody().getMessage());
    }
    
    @Test
    @DisplayName("Should verify HTTP status for both operations")
    void testBothOperations_HttpStatusVerification() {
        // Arrange
        when(productCommandPort.deleteById(productId)).thenReturn(successMessage);
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> deleteProductResponse = 
            productCommandController.deleteProduct(productId);
        ResponseEntity<ResponseDTO<String>> deleteAllResponse = 
            productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        assertEquals(HttpStatus.OK, deleteProductResponse.getStatusCode());
        assertEquals(200, deleteProductResponse.getBody().getStatus());
        assertEquals(HttpStatus.OK, deleteAllResponse.getStatusCode());
        assertEquals(200, deleteAllResponse.getBody().getStatus());
    }
    
    @Test
    @DisplayName("Should delete product with zero ID")
    void testDeleteProduct_ZeroId() {
        // Arrange
        Long zeroId = 0L;
        when(productCommandPort.deleteById(zeroId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteProduct(zeroId);
        
        // Assert
        assertNotNull(response);
        verify(productCommandPort, times(1)).deleteById(zeroId);
    }
    
    @Test
    @DisplayName("Should delete product with large ID")
    void testDeleteProduct_LargeId() {
        // Arrange
        Long largeId = 999999999L;
        when(productCommandPort.deleteById(largeId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteProduct(largeId);
        
        // Assert
        assertNotNull(response);
        verify(productCommandPort, times(1)).deleteById(largeId);
    }
    
    @Test
    @DisplayName("Should delete all products with empty string enterprise ID")
    void testDeleteAllProducts_EmptyEnterpriseId() {
        // Arrange
        String emptyEnterpriseId = "";
        when(productCommandPort.deleteAllByEnterpriseId(emptyEnterpriseId)).thenReturn(successMessage);
        
        // Act
        ResponseEntity<ResponseDTO<String>> response = 
            productCommandController.deleteAllProducts(emptyEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(emptyEnterpriseId);
    }
    
    @Test
    @DisplayName("Should verify port is called exactly once for deleteProduct")
    void testDeleteProduct_PortVerification() {
        // Arrange
        when(productCommandPort.deleteById(productId)).thenReturn(successMessage);
        
        // Act
        productCommandController.deleteProduct(productId);
        
        // Assert
        verify(productCommandPort, times(1)).deleteById(eq(productId));
    }
    
    @Test
    @DisplayName("Should verify port is called exactly once for deleteAllProducts")
    void testDeleteAllProducts_PortVerification() {
        // Arrange
        when(productCommandPort.deleteAllByEnterpriseId(enterpriseId)).thenReturn(successMessage);
        
        // Act
        productCommandController.deleteAllProducts(enterpriseId);
        
        // Assert
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(eq(enterpriseId));
    }
    
    @Test
    @DisplayName("Should handle multiple sequential delete operations")
    void testMultipleSequentialDeletes() {
        // Arrange
        Long product1 = 1L;
        Long product2 = 2L;
        Long product3 = 3L;
        
        when(productCommandPort.deleteById(product1)).thenReturn("Product 1 deleted");
        when(productCommandPort.deleteById(product2)).thenReturn("Product 2 deleted");
        when(productCommandPort.deleteById(product3)).thenReturn("Product 3 deleted");
        
        // Act
        ResponseEntity<ResponseDTO<String>> response1 = productCommandController.deleteProduct(product1);
        ResponseEntity<ResponseDTO<String>> response2 = productCommandController.deleteProduct(product2);
        ResponseEntity<ResponseDTO<String>> response3 = productCommandController.deleteProduct(product3);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        
        verify(productCommandPort, times(1)).deleteById(product1);
        verify(productCommandPort, times(1)).deleteById(product2);
        verify(productCommandPort, times(1)).deleteById(product3);
    }
    
    @Test
    @DisplayName("Should handle multiple sequential deleteAll operations")
    void testMultipleSequentialDeleteAlls() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";
        
        when(productCommandPort.deleteAllByEnterpriseId(enterprise1)).thenReturn("Enterprise 1 products deleted");
        when(productCommandPort.deleteAllByEnterpriseId(enterprise2)).thenReturn("Enterprise 2 products deleted");
        
        // Act
        ResponseEntity<ResponseDTO<String>> response1 = productCommandController.deleteAllProducts(enterprise1);
        ResponseEntity<ResponseDTO<String>> response2 = productCommandController.deleteAllProducts(enterprise2);
        
        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(enterprise1);
        verify(productCommandPort, times(1)).deleteAllByEnterpriseId(enterprise2);
    }
}

