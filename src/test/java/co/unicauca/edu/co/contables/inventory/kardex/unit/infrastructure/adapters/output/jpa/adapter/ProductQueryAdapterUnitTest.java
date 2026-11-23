package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.ProductQueryAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IProductEntityQueryMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

/**
 * @brief Unit tests for ProductQueryAdapter
 * 
 * Tests the query operations for Product records including
 * finding by enterprise and existence checks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductQueryAdapter Tests")
class ProductQueryAdapterUnitTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductEntityQueryMapper productEntityMapper;

    @InjectMocks
    private ProductQueryAdapter productQueryAdapter;

    private ProductEntity productEntity1;
    private ProductEntity productEntity2;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Create test entities
        productEntity1 = new ProductEntity();
        productEntity1.setId(1L);
        productEntity1.setProductId(100L);
        productEntity1.setReference("REF-001");
        productEntity1.setName("Product 1");
        productEntity1.setPresentation("Box");
        productEntity1.setEnterpriseId("ENT-001");
        productEntity1.setState(true);

        productEntity2 = new ProductEntity();
        productEntity2.setId(2L);
        productEntity2.setProductId(200L);
        productEntity2.setReference("REF-002");
        productEntity2.setName("Product 2");
        productEntity2.setPresentation("Unit");
        productEntity2.setEnterpriseId("ENT-001");
        productEntity2.setState(true);

        // Create test domain objects
        product1 = Product.builder()
                .id(1L)
                .productId(100L)
                .reference("REF-001")
                .name("Product 1")
                .presentation("Box")
                .enterpriseId("ENT-001")
                .state(true)
                .build();

        product2 = Product.builder()
                .id(2L)
                .productId(200L)
                .reference("REF-002")
                .name("Product 2")
                .presentation("Unit")
                .enterpriseId("ENT-001")
                .state(true)
                .build();
    }

    @Test
    @DisplayName("Should find all products by enterprise ID successfully")
    void testFindAll_Success() {
        // Arrange
        Collection<ProductEntity> entities = Arrays.asList(productEntity1, productEntity2);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);
        when(productEntityMapper.toDomain(productEntity2)).thenReturn(product2);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
        assertEquals("ENT-001", result.get(0).getEnterpriseId());

        verify(productRepository, times(1)).findAllByEnterpriseId("ENT-001");
        verify(productEntityMapper, times(2)).toDomain(any(ProductEntity.class));
    }

    @Test
    @DisplayName("Should return empty list when no products found for enterprise")
    void testFindAll_EmptyResult() {
        // Arrange
        when(productRepository.findAllByEnterpriseId("ENT-999")).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-999");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository, times(1)).findAllByEnterpriseId("ENT-999");
        verify(productEntityMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should check if product exists by product ID - exists")
    void testExistsByProductId_True() {
        // Arrange
        when(productRepository.existsByProductId(100L)).thenReturn(true);

        // Act
        boolean result = productQueryAdapter.existsByProductId(100L);

        // Assert
        assertTrue(result);
        verify(productRepository, times(1)).existsByProductId(100L);
    }

    @Test
    @DisplayName("Should check if product exists by product ID - not exists")
    void testExistsByProductId_False() {
        // Arrange
        when(productRepository.existsByProductId(999L)).thenReturn(false);

        // Act
        boolean result = productQueryAdapter.existsByProductId(999L);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).existsByProductId(999L);
    }

    @Test
    @DisplayName("Should find single product by enterprise ID")
    void testFindAll_SingleProduct() {
        // Arrange
        Collection<ProductEntity> entities = Arrays.asList(productEntity1);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProductId());
        assertEquals("REF-001", result.get(0).getReference());

        verify(productRepository, times(1)).findAllByEnterpriseId("ENT-001");
        verify(productEntityMapper, times(1)).toDomain(productEntity1);
    }

    @Test
    @DisplayName("Should map entities to domain objects correctly")
    void testFindAll_MappingCorrectness() {
        // Arrange
        Collection<ProductEntity> entities = Arrays.asList(productEntity1);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-001");

        // Assert
        Product mappedProduct = result.get(0);
        assertEquals(productEntity1.getProductId(), mappedProduct.getProductId());
        assertEquals(productEntity1.getName(), mappedProduct.getName());
        assertEquals(productEntity1.getReference(), mappedProduct.getReference());
        assertEquals(productEntity1.getPresentation(), mappedProduct.getPresentation());
        assertEquals(productEntity1.getEnterpriseId(), mappedProduct.getEnterpriseId());
        assertEquals(productEntity1.isState(), mappedProduct.isState());
    }

    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testFindAll_DifferentEnterpriseIds() {
        // Arrange
        String[] enterpriseIds = {"ENT-001", "ENT-002", "ENT-003"};
        
        for (String enterpriseId : enterpriseIds) {
            when(productRepository.findAllByEnterpriseId(enterpriseId))
                .thenReturn(Collections.emptyList());
        }

        // Act & Assert
        for (String enterpriseId : enterpriseIds) {
            List<Product> result = productQueryAdapter.findAll(enterpriseId);
            assertNotNull(result);
            verify(productRepository).findAllByEnterpriseId(enterpriseId);
        }

        verify(productRepository, times(3)).findAllByEnterpriseId(anyString());
    }

    @Test
    @DisplayName("Should find products with different states")
    void testFindAll_DifferentStates() {
        // Arrange
        productEntity1.setState(true);
        productEntity2.setState(false);

        Collection<ProductEntity> entities = Arrays.asList(productEntity1, productEntity2);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);
        when(productEntityMapper.toDomain(productEntity2)).thenReturn(product2);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-001");

        // Assert
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAllByEnterpriseId("ENT-001");
    }

    @Test
    @DisplayName("Should handle multiple existence checks")
    void testExistsByProductId_MultipleCalls() {
        // Arrange
        when(productRepository.existsByProductId(100L)).thenReturn(true);
        when(productRepository.existsByProductId(200L)).thenReturn(true);
        when(productRepository.existsByProductId(300L)).thenReturn(false);

        // Act
        boolean exists1 = productQueryAdapter.existsByProductId(100L);
        boolean exists2 = productQueryAdapter.existsByProductId(200L);
        boolean exists3 = productQueryAdapter.existsByProductId(300L);

        // Assert
        assertTrue(exists1);
        assertTrue(exists2);
        assertFalse(exists3);

        verify(productRepository).existsByProductId(100L);
        verify(productRepository).existsByProductId(200L);
        verify(productRepository).existsByProductId(300L);
    }

    @Test
    @DisplayName("Should find large number of products")
    void testFindAll_LargeResult() {
        // Arrange
        ProductEntity product3 = new ProductEntity();
        product3.setProductId(300L);
        product3.setName("Product 3");
        product3.setEnterpriseId("ENT-001");

        ProductEntity product4 = new ProductEntity();
        product4.setProductId(400L);
        product4.setName("Product 4");
        product4.setEnterpriseId("ENT-001");

        ProductEntity product5 = new ProductEntity();
        product5.setProductId(500L);
        product5.setName("Product 5");
        product5.setEnterpriseId("ENT-001");

        Collection<ProductEntity> entities = Arrays.asList(
            productEntity1, productEntity2, product3, product4, product5
        );

        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(any(ProductEntity.class)))
            .thenReturn(product1, product2, product1, product1, product1);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-001");

        // Assert
        assertEquals(5, result.size());
        verify(productRepository, times(1)).findAllByEnterpriseId("ENT-001");
        verify(productEntityMapper, times(5)).toDomain(any(ProductEntity.class));
    }

    @Test
    @DisplayName("Should preserve product details in mapping")
    void testFindAll_PreserveDetails() {
        // Arrange
        productEntity1.setPresentation("Special Edition Box");
        product1.setPresentation("Special Edition Box");

        Collection<ProductEntity> entities = Arrays.asList(productEntity1);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-001");

        // Assert
        assertEquals("Special Edition Box", result.get(0).getPresentation());
    }

    @Test
    @DisplayName("Should handle null enterprise ID gracefully")
    void testFindAll_NullEnterpriseId() {
        // Arrange
        when(productRepository.findAllByEnterpriseId(null)).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryAdapter.findAll(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAllByEnterpriseId(null);
    }

    @Test
    @DisplayName("Should verify repository interaction count")
    void testFindAll_RepositoryInteraction() {
        // Arrange
        Collection<ProductEntity> entities = Arrays.asList(productEntity1);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);

        // Act
        productQueryAdapter.findAll("ENT-001");

        // Assert
        verify(productRepository, times(1)).findAllByEnterpriseId("ENT-001");
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should verify mapper interaction for each entity")
    void testFindAll_MapperInteraction() {
        // Arrange
        Collection<ProductEntity> entities = Arrays.asList(productEntity1, productEntity2);
        when(productRepository.findAllByEnterpriseId("ENT-001")).thenReturn(entities);
        when(productEntityMapper.toDomain(any())).thenReturn(product1, product2);

        // Act
        productQueryAdapter.findAll("ENT-001");

        // Assert
        verify(productEntityMapper, times(1)).toDomain(productEntity1);
        verify(productEntityMapper, times(1)).toDomain(productEntity2);
    }

    @Test
    @DisplayName("Should handle products with same enterprise ID")
    void testFindAll_SameEnterpriseId() {
        // Arrange
        productEntity1.setEnterpriseId("ENT-SHARED");
        productEntity2.setEnterpriseId("ENT-SHARED");
        product1.setEnterpriseId("ENT-SHARED");
        product2.setEnterpriseId("ENT-SHARED");

        Collection<ProductEntity> entities = Arrays.asList(productEntity1, productEntity2);
        when(productRepository.findAllByEnterpriseId("ENT-SHARED")).thenReturn(entities);
        when(productEntityMapper.toDomain(productEntity1)).thenReturn(product1);
        when(productEntityMapper.toDomain(productEntity2)).thenReturn(product2);

        // Act
        List<Product> result = productQueryAdapter.findAll("ENT-SHARED");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getEnterpriseId().equals("ENT-SHARED")));
    }
}
