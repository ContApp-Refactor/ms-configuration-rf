package co.unicauca.edu.co.contables.inventory.peps.unit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import co.unicauca.edu.co.contables.inventory.peps.application.service.query.ProductQueryService;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductQueryServiceUnitTest {
    
    @Mock
    private IProductQueryOutputPort productQueryOutputPort;
    
    @Mock
    private IMessageServicePort messageServicePort;

    @InjectMocks
    private ProductQueryService productQueryService;

    private String enterpriseId;
    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        enterpriseId = "ENT-001";
        mockProducts = createMockProducts();
        
        // Mock message service default behavior
        when(messageServicePort.getMessage(anyString(), any()))
            .thenReturn("Mocked log message");
    }

    // ==================== findAll() ====================
    @Test
    @DisplayName("Should return all products for valid enterprise ID")
    void testFindAll_ValidEnterpriseId_ReturnsAllProducts() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId))
            .thenReturn(mockProducts);

        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
        assertEquals("Product 3", result.get(2).getName());
        verify(productQueryOutputPort).findAll(enterpriseId);
        verify(messageServicePort).getMessage(anyString(), eq(enterpriseId));
    }

    @Test
    @DisplayName("Should return empty list when no products found")
    void testFindAll_NoProducts_ReturnsEmptyList() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId))
            .thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productQueryOutputPort).findAll(enterpriseId);
    }

    @Test
    @DisplayName("Should return single product when only one exists")
    void testFindAll_SingleProduct_ReturnsSingleProductList() {
        // Arrange
        Product singleProduct = createProduct(1L, "Single Product");
        when(productQueryOutputPort.findAll(enterpriseId))
            .thenReturn(List.of(singleProduct));

        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Single Product", result.get(0).getName());
        verify(productQueryOutputPort).findAll(enterpriseId);
    }

    @Test
    @DisplayName("Should handle different enterprise IDs correctly")
    void testFindAll_DifferentEnterpriseIds_ReturnsDifferentProducts() {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-002";
        
        List<Product> products1 = List.of(createProduct(1L, "Product A"));
        List<Product> products2 = List.of(createProduct(2L, "Product B"));
        
        when(productQueryOutputPort.findAll(enterpriseId1)).thenReturn(products1);
        when(productQueryOutputPort.findAll(enterpriseId2)).thenReturn(products2);

        // Act
        List<Product> result1 = productQueryService.findAll(enterpriseId1);
        List<Product> result2 = productQueryService.findAll(enterpriseId2);

        // Assert
        assertEquals(1, result1.size());
        assertEquals("Product A", result1.get(0).getName());
        
        assertEquals(1, result2.size());
        assertEquals("Product B", result2.get(0).getName());
        
        verify(productQueryOutputPort).findAll(enterpriseId1);
        verify(productQueryOutputPort).findAll(enterpriseId2);
    }

    @Test
    @DisplayName("Should return products with correct attributes")
    void testFindAll_ValidProducts_ReturnsProductsWithCorrectAttributes() {
        // Arrange
        List<Product> products = new ArrayList<>();
        
        Product product1 = new Product();
        product1.setProductId(1L);
        product1.setName("Test Product 1");
        product1.setReference("REF-001");
        product1.setPresentation("Box");
        product1.setEnterpriseId(enterpriseId);
        product1.setState(true);
        products.add(product1);
        
        Product product2 = new Product();
        product2.setProductId(2L);
        product2.setName("Test Product 2");
        product2.setReference("REF-002");
        product2.setPresentation("Bottle");
        product2.setEnterpriseId(enterpriseId);
        product2.setState(false);
        products.add(product2);
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(products);

        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);

        // Assert
        assertEquals(2, result.size());
        
        Product firstProduct = result.get(0);
        assertEquals(1L, firstProduct.getProductId());
        assertEquals("Test Product 1", firstProduct.getName());
        assertEquals("REF-001", firstProduct.getReference());
        assertEquals("Box", firstProduct.getPresentation());
        assertTrue(firstProduct.isActive());
        
        Product secondProduct = result.get(1);
        assertEquals(2L, secondProduct.getProductId());
        assertEquals("Test Product 2", secondProduct.getName());
        assertEquals("REF-002", secondProduct.getReference());
        assertEquals("Bottle", secondProduct.getPresentation());
        assertFalse(secondProduct.isActive());
        
        verify(productQueryOutputPort).findAll(enterpriseId);
    }

    @Test
    @DisplayName("Should return both active and inactive products")
    void testFindAll_MixedActiveInactive_ReturnsAllProducts() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(createActiveProduct(1L, "Active Product"));
        products.add(createInactiveProduct(2L, "Inactive Product"));
        products.add(createActiveProduct(3L, "Another Active"));
        
        when(productQueryOutputPort.findAll(enterpriseId)).thenReturn(products);

        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.get(0).isActive());
        assertFalse(result.get(1).isActive());
        assertTrue(result.get(2).isActive());
        verify(productQueryOutputPort).findAll(enterpriseId);
    }

    // ==================== Logging Verification ====================
    @Test
    @DisplayName("Should log query attempt with correct message key")
    void testFindAll_ValidRequest_LogsQuery() {
        // Arrange
        when(productQueryOutputPort.findAll(enterpriseId))
            .thenReturn(mockProducts);

        // Act
        productQueryService.findAll(enterpriseId);

        // Assert
        verify(messageServicePort).getMessage(anyString(), eq(enterpriseId));
    }

    // ==================== Error Handling ====================
    @Test
    @DisplayName("Should propagate exception from output port")
    void testFindAll_OutputPortThrowsException_PropagatesException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Database connection error");
        when(productQueryOutputPort.findAll(enterpriseId))
            .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> productQueryService.findAll(enterpriseId));
        
        assertEquals("Database connection error", thrown.getMessage());
        verify(productQueryOutputPort).findAll(enterpriseId);
    }

    @Test
    @DisplayName("Should handle null enterprise ID gracefully")
    void testFindAll_NullEnterpriseId_PropagatesException() {
        // Arrange
        when(productQueryOutputPort.findAll(null))
            .thenThrow(new IllegalArgumentException("Enterprise ID cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> productQueryService.findAll(null));
        
        verify(productQueryOutputPort).findAll(null);
    }

    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testFindAll_EmptyEnterpriseId_CallsOutputPort() {
        // Arrange
        String emptyEnterpriseId = "";
        when(productQueryOutputPort.findAll(emptyEnterpriseId))
            .thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryService.findAll(emptyEnterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productQueryOutputPort).findAll(emptyEnterpriseId);
    }

    // ==================== Large Dataset Tests ====================
    @Test
    @DisplayName("Should handle large list of products")
    void testFindAll_LargeDataset_ReturnsAllProducts() {
        // Arrange
        List<Product> largeProductList = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeProductList.add(createProduct((long) i, "Product " + i));
        }
        
        when(productQueryOutputPort.findAll(enterpriseId))
            .thenReturn(largeProductList);

        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.size());
        verify(productQueryOutputPort).findAll(enterpriseId);
    }

    // ==================== Helper Methods ====================
    private List<Product> createMockProducts() {
        List<Product> products = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Product " + i);
            product.setReference("REF-00" + i);
            product.setPresentation("Box");
            product.setEnterpriseId(enterpriseId);
            product.setState(true);
            products.add(product);
        }
        
        return products;
    }

    private Product createProduct(Long productId, String name) {
        Product product = new Product();
        product.setProductId(productId);
        product.setName(name);
        product.setReference("REF-" + productId);
        product.setPresentation("Box");
        product.setEnterpriseId(enterpriseId);
        product.setState(true);
        return product;
    }

    private Product createActiveProduct(Long productId, String name) {
        Product product = createProduct(productId, name);
        product.setState(true);
        return product;
    }

    private Product createInactiveProduct(Long productId, String name) {
        Product product = createProduct(productId, name);
        product.setState(false);
        return product;
    }
}
