package co.unicauca.edu.co.contables.inventory.kardex.unit.application.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.product.query.ProductQueryService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductQueryRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceUnitTest {
    
    @Mock
    private IProductQueryRepositoryPort productQueryRepository;
    
    @InjectMocks
    private ProductQueryService productQueryService;
    
    private Product product1;
    private Product product2;
    private Product product3;
    private String enterpriseId;
    private List<Product> productList;
    
    @BeforeEach
    void setUp() {
        enterpriseId = "ENT001";
        
        // Setup product 1
        product1 = Product.builder()
            .id(1L)
            .productId(101L)
            .reference("REF001")
            .name("Laptop Dell")
            .presentation("Unit")
            .enterpriseId(enterpriseId)
            .state(true)
            .build();
        
        // Setup product 2
        product2 = Product.builder()
            .id(2L)
            .productId(102L)
            .reference("REF002")
            .name("Mouse Logitech")
            .presentation("Box of 10")
            .enterpriseId(enterpriseId)
            .state(true)
            .build();
        
        // Setup product 3
        product3 = Product.builder()
            .id(3L)
            .productId(103L)
            .reference("REF003")
            .name("Keyboard HP")
            .presentation("Unit")
            .enterpriseId(enterpriseId)
            .state(false)
            .build();
        
        productList = Arrays.asList(product1, product2, product3);
    }
    
    @Test
    @DisplayName("Should find all products for enterprise successfully")
    void testFindAllProductsSuccess() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Laptop Dell", result.get(0).getName());
        assertEquals("Mouse Logitech", result.get(1).getName());
        assertEquals("Keyboard HP", result.get(2).getName());
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should return empty list when no products exist for enterprise")
    void testFindAllWithNoProducts() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(Collections.emptyList());
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should find products with all fields populated")
    void testFindAllProductsWithCompleteData() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify first product
        Product firstProduct = result.get(0);
        assertEquals(1L, firstProduct.getId());
        assertEquals(101L, firstProduct.getProductId());
        assertEquals("REF001", firstProduct.getReference());
        assertEquals("Laptop Dell", firstProduct.getName());
        assertEquals("Unit", firstProduct.getPresentation());
        assertEquals(enterpriseId, firstProduct.getEnterpriseId());
        assertTrue(firstProduct.isState());
        
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should find products including inactive ones")
    void testFindAllIncludesInactiveProducts() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify that inactive product is included
        Product inactiveProduct = result.stream()
            .filter(p -> !p.isState())
            .findFirst()
            .orElse(null);
        
        assertNotNull(inactiveProduct);
        assertEquals("Keyboard HP", inactiveProduct.getName());
        assertFalse(inactiveProduct.isState());
        
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should find products for different enterprises independently")
    void testFindAllForDifferentEnterprises() {
        // Arrange
        String enterprise1 = "ENT001";
        String enterprise2 = "ENT002";
        List<Product> products1 = Arrays.asList(product1);
        List<Product> products2 = Arrays.asList(product2, product3);
        
        when(productQueryRepository.findAll(enterprise1)).thenReturn(products1);
        when(productQueryRepository.findAll(enterprise2)).thenReturn(products2);
        
        // Act
        List<Product> result1 = productQueryService.findAll(enterprise1);
        List<Product> result2 = productQueryService.findAll(enterprise2);
        
        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.size());
        assertEquals(2, result2.size());
        
        verify(productQueryRepository).findAll(enterprise1);
        verify(productQueryRepository).findAll(enterprise2);
    }
    
    @Test
    @DisplayName("Should handle multiple consecutive queries")
    void testMultipleConsecutiveQueries() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result1 = productQueryService.findAll(enterpriseId);
        List<Product> result2 = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());
        assertEquals(3, result1.size());
        
        verify(productQueryRepository, times(2)).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should return only active products when filtering")
    void testFindAllActiveProducts() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        List<Product> activeProducts = result.stream()
            .filter(Product::isState)
            .toList();
        
        // Assert
        assertNotNull(activeProducts);
        assertEquals(2, activeProducts.size());
        assertTrue(activeProducts.stream().allMatch(Product::isState));
        
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should find products with various presentations")
    void testFindAllWithDifferentPresentations() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify different presentations
        List<String> presentations = result.stream()
            .map(Product::getPresentation)
            .toList();
        
        assertTrue(presentations.contains("Unit"));
        assertTrue(presentations.contains("Box of 10"));
        
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should handle null enterprise id gracefully")
    void testFindAllWithNullEnterpriseId() {
        // Arrange
        String nullEnterpriseId = null;
        when(productQueryRepository.findAll(nullEnterpriseId)).thenReturn(Collections.emptyList());
        
        // Act
        List<Product> result = productQueryService.findAll(nullEnterpriseId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productQueryRepository).findAll(nullEnterpriseId);
    }
    
    @Test
    @DisplayName("Should return products with unique product ids")
    void testFindAllWithUniqueProductIds() {
        // Arrange
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(productList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        List<Long> productIds = result.stream()
            .map(Product::getProductId)
            .toList();
        
        assertEquals(3, productIds.size());
        assertEquals(3, productIds.stream().distinct().count()); // All unique
        
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should find single product for enterprise")
    void testFindAllWithSingleProduct() {
        // Arrange
        List<Product> singleProductList = Arrays.asList(product1);
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(singleProductList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop Dell", result.get(0).getName());
        
        verify(productQueryRepository).findAll(enterpriseId);
    }
    
    @Test
    @DisplayName("Should find large number of products efficiently")
    void testFindAllWithManyProducts() {
        // Arrange
        List<Product> largeProductList = Arrays.asList(
            product1, product2, product3, product1, product2, product3,
            product1, product2, product3, product1
        );
        when(productQueryRepository.findAll(enterpriseId)).thenReturn(largeProductList);
        
        // Act
        List<Product> result = productQueryService.findAll(enterpriseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());
        verify(productQueryRepository).findAll(enterpriseId);
    }
}
