package co.unicauca.edu.co.contables.inventory.kardex.unit.application.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.product.command.ProductCommandService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.external.ISyncStateRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductClientPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductCommandRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class ProductCommandServiceUnitTest {
    
    @Mock
    private IProductCommandRepositoryPort productCommandRepositoryPort;
    
    @Mock
    private IProductClientPort productClient;
    
    @Mock
    private ISyncStateRepositoryPort syncStateRepository;
    
    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;
    
    @Mock
    private IMessageServicePort messageService;
    
    @InjectMocks
    private ProductCommandService productCommandService;
    
    private Product product1;
    private Product product2;
    private Product product3;
    private SyncState syncState;
    private String enterpriseId;
    private Instant lastSyncDate;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT001";
        lastSyncDate = Instant.parse("2024-01-01T00:00:00Z");
        
        // Setup products
        product1 = Product.builder()
            .id(1L)
            .productId(101L)
            .reference("REF001")
            .name("Product 1")
            .presentation("Box")
            .enterpriseId(enterpriseId)
            .state(true)
            .build();
            
        product2 = Product.builder()
            .id(2L)
            .productId(102L)
            .reference("REF002")
            .name("Product 2")
            .presentation("Unit")
            .enterpriseId(enterpriseId)
            .state(true)
            .build();
            
        product3 = Product.builder()
            .id(3L)
            .productId(103L)
            .reference("REF003")
            .name("Product 3")
            .presentation("Pack")
            .enterpriseId(enterpriseId)
            .state(false)
            .build();
        
        // Setup sync state
        syncState = new SyncState();
        syncState.setId(1L);
        syncState.setSyncType("products");
        syncState.setEnterpriseId(enterpriseId);
        syncState.setLastSyncDate(lastSyncDate);
        syncState.setCreatedAt(Instant.now());
    }
    
    @Test
    @DisplayName("Should sync products successfully with existing sync state")
    void testSyncProductsWithExistingSyncState() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2, product3);
        when(syncStateRepository.findLastSyncFor("products", enterpriseId))
            .thenReturn(Optional.of(lastSyncDate));
        when(productClient.findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class)))
            .thenReturn(products);
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("products", enterpriseId))
            .thenReturn(Optional.of(syncState));
        doNothing().when(syncStateRepository).save(any(SyncState.class));
        when(productCommandRepositoryPort.saveAll(anyList())).thenReturn("Products saved successfully");
        
        // Act
        String result = productCommandService.syncProductsByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Successful synchronization"));
        assertTrue(result.contains("3 products processed"));
        verify(syncStateRepository).findLastSyncFor("products", enterpriseId);
        verify(productClient).findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class));
        verify(productCommandRepositoryPort).saveAll(anyList());
        verify(syncStateRepository).save(any(SyncState.class));
    }
    
    @Test
    @DisplayName("Should sync products and create initial sync state when none exists")
    void testSyncProductsWithNoSyncState() {
        // Arrange
        List<Product> products = Arrays.asList(product1);
        when(syncStateRepository.findLastSyncFor("products", enterpriseId))
            .thenReturn(Optional.empty());
        doNothing().when(syncStateRepository).save(any(SyncState.class));
        when(productClient.findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class)))
            .thenReturn(products);
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("products", enterpriseId))
            .thenReturn(Optional.of(syncState));
        when(productCommandRepositoryPort.saveAll(anyList())).thenReturn("Products saved successfully");
        
        // Act
        String result = productCommandService.syncProductsByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Successful synchronization"));
        assertTrue(result.contains("1 products processed"));
        verify(syncStateRepository).findLastSyncFor("products", enterpriseId);
        verify(syncStateRepository, times(2)).save(any(SyncState.class)); // Once for initial, once for update
        verify(productClient).findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class));
    }
    
    @Test
    @DisplayName("Should handle empty product list from API")
    void testSyncProductsWithEmptyList() {
        // Arrange
        when(syncStateRepository.findLastSyncFor("products", enterpriseId))
            .thenReturn(Optional.of(lastSyncDate));
        when(productClient.findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class)))
            .thenReturn(Collections.emptyList());
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("products", enterpriseId))
            .thenReturn(Optional.of(syncState));
        doNothing().when(syncStateRepository).save(any(SyncState.class));
        
        // Act
        String result = productCommandService.syncProductsByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("0 products processed"));
        verify(productCommandRepositoryPort, never()).saveAll(anyList());
        verify(syncStateRepository).save(any(SyncState.class)); // Still updates sync state
    }
    
    @Test
    @DisplayName("Should throw exception when API call fails")
    void testSyncProductsWithApiError() {
        // Arrange
        when(syncStateRepository.findLastSyncFor("products", enterpriseId))
            .thenReturn(Optional.of(lastSyncDate));
        when(productClient.findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class)))
            .thenThrow(new RuntimeException("API connection failed"));
        when(messageService.getMessage(any(), any(), any())).thenReturn("Error message");
        doNothing().when(formatterResultOutputPort).returnBusinessRuleErrorResponse(eq(500), anyString());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productCommandService.syncProductsByEnterpriseId(enterpriseId);
        });
        
        assertEquals("API connection failed", exception.getMessage());
        verify(productClient).findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class));
        verify(formatterResultOutputPort).returnBusinessRuleErrorResponse(eq(500), anyString());
        verify(productCommandRepositoryPort, never()).saveAll(anyList());
        verify(syncStateRepository, never()).save(any(SyncState.class)); // Should not update on error
    }
    
    @Test
    @DisplayName("Should create new sync state when updating and none exists")
    void testUpdateSyncStateCreatesNew() {
        // Arrange
        List<Product> products = Arrays.asList(product1);
        when(syncStateRepository.findLastSyncFor("products", enterpriseId))
            .thenReturn(Optional.of(lastSyncDate));
        when(productClient.findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class)))
            .thenReturn(products);
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("products", enterpriseId))
            .thenReturn(Optional.empty()); // No existing state
        doNothing().when(syncStateRepository).save(any(SyncState.class));
        when(productCommandRepositoryPort.saveAll(anyList())).thenReturn("Products saved successfully");
        
        // Act
        String result = productCommandService.syncProductsByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        verify(syncStateRepository).save(any(SyncState.class));
    }
    
    @Test
    @DisplayName("Should delete product by id successfully")
    void testDeleteByIdSuccess() {
        // Arrange
        Long productId = 101L;
        String expectedMessage = "Product deleted successfully";
        when(productCommandRepositoryPort.delete(productId))
            .thenReturn(expectedMessage);
        
        // Act
        String result = productCommandService.deleteById(productId);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedMessage, result);
        verify(productCommandRepositoryPort).delete(productId);
    }
    
    @Test
    @DisplayName("Should delete all products by enterprise id successfully")
    void testDeleteAllByEnterpriseIdSuccess() {
        // Arrange
        String expectedMessage = "All products deleted successfully";
        when(productCommandRepositoryPort.deleteAllByEnterpriseId(enterpriseId))
            .thenReturn(expectedMessage);
        
        // Act
        String result = productCommandService.deleteAllByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedMessage, result);
        verify(productCommandRepositoryPort).deleteAllByEnterpriseId(enterpriseId);
    }
    
    @Test
    @DisplayName("Should handle multiple sync operations for different enterprises")
    void testMultipleSyncForDifferentEnterprises() {
        // Arrange
        String enterprise1 = "ENT001";
        String enterprise2 = "ENT002";
        List<Product> products = Arrays.asList(product1);
        
        when(syncStateRepository.findLastSyncFor("products", enterprise1))
            .thenReturn(Optional.of(lastSyncDate));
        when(syncStateRepository.findLastSyncFor("products", enterprise2))
            .thenReturn(Optional.of(lastSyncDate));
        when(productClient.findAllProductsByEnterpriseId(anyString(), any(Instant.class)))
            .thenReturn(products);
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(eq("products"), anyString()))
            .thenReturn(Optional.of(syncState));
        doNothing().when(syncStateRepository).save(any(SyncState.class));
        when(productCommandRepositoryPort.saveAll(anyList())).thenReturn("Products saved successfully");
        
        // Act
        String result1 = productCommandService.syncProductsByEnterpriseId(enterprise1);
        String result2 = productCommandService.syncProductsByEnterpriseId(enterprise2);
        
        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(productClient, times(2)).findAllProductsByEnterpriseId(anyString(), any(Instant.class));
        verify(productCommandRepositoryPort, times(2)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should preserve product data when mapping from DTO")
    void testProductDataMappingPreservesAllFields() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        when(syncStateRepository.findLastSyncFor("products", enterpriseId))
            .thenReturn(Optional.of(lastSyncDate));
        when(productClient.findAllProductsByEnterpriseId(eq(enterpriseId), any(Instant.class)))
            .thenReturn(products);
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("products", enterpriseId))
            .thenReturn(Optional.of(syncState));
        doNothing().when(syncStateRepository).save(any(SyncState.class));
        when(productCommandRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> {
            List<Product> savedProducts = invocation.getArgument(0);
            // Verify all fields are preserved
            assertEquals(2, savedProducts.size());
            assertEquals(101L, savedProducts.get(0).getProductId());
            assertEquals("REF001", savedProducts.get(0).getReference());
            assertEquals("Product 1", savedProducts.get(0).getName());
            assertEquals("Box", savedProducts.get(0).getPresentation());
            assertEquals(enterpriseId, savedProducts.get(0).getEnterpriseId());
            assertTrue(savedProducts.get(0).isState());
            return "Products saved successfully";
        });
        
        // Act
        String result = productCommandService.syncProductsByEnterpriseId(enterpriseId);
        
        // Assert
        assertNotNull(result);
        verify(productCommandRepositoryPort).saveAll(anyList());
    }
}
