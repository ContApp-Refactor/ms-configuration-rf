package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.ProductQueryAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IProductEntityMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductQueryAdapterUnitTest {


    @Mock
    private IProductRepository productRepository;
    
    @Mock
    private IProductEntityMapper productEntityMapper;

    @InjectMocks
    private ProductQueryAdapter productQueryAdapter;

    private Product mockProduct;
    private ProductEntity mockProductEntity;
    private List<Product> mockProducts;
    private List<ProductEntity> mockProductEntities;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        mockProduct = createMockProduct(1L, 100L);
        mockProductEntity = createMockProductEntity(1L, 100L);
        mockProducts = createMockProducts();
        mockProductEntities = createMockProductEntities();
    }

    // ==================== getProductByProductId() ====================
    @Test
    @DisplayName("Should get product by product ID when exists")
    void testGetProductByProductId_ProductExists_ReturnsOptionalWithProduct() {
        // Arrange
        Long productId = 100L;
        when(productRepository.findByProductId(productId))
            .thenReturn(Optional.of(mockProductEntity));
        when(productEntityMapper.toDomain(mockProductEntity))
            .thenReturn(mockProduct);

        // Act
        Optional<Product> result = productQueryAdapter.getProductByProductId(productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getProductId());
        assertEquals("Product 100", result.get().getName());
        verify(productRepository).findByProductId(productId);
        verify(productEntityMapper).toDomain(mockProductEntity);
    }

    @Test
    @DisplayName("Should return empty optional when product not found")
    void testGetProductByProductId_ProductNotFound_ReturnsEmpty() {
        // Arrange
        Long productId = 999L;
        when(productRepository.findByProductId(productId))
            .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productQueryAdapter.getProductByProductId(productId);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository).findByProductId(productId);
        verify(productEntityMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle null product ID")
    void testGetProductByProductId_NullId_ReturnsEmpty() {
        // Arrange
        when(productRepository.findByProductId(null))
            .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productQueryAdapter.getProductByProductId(null);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository).findByProductId(null);
    }

    @Test
    @DisplayName("Should correctly map entity to domain")
    void testGetProductByProductId_ValidEntity_MapsCorrectly() {
        // Arrange
        Long productId = 100L;
        ProductEntity entity = createMockProductEntity(5L, productId);
        Product expectedProduct = createMockProduct(5L, productId);
        
        when(productRepository.findByProductId(productId))
            .thenReturn(Optional.of(entity));
        when(productEntityMapper.toDomain(entity))
            .thenReturn(expectedProduct);

        // Act
        Optional<Product> result = productQueryAdapter.getProductByProductId(productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedProduct.getId(), result.get().getId());
        assertEquals(expectedProduct.getProductId(), result.get().getProductId());
    }

    // ==================== findAll() ====================
    @Test
    @DisplayName("Should find all products by enterprise ID")
    void testFindAll_ValidEnterpriseId_ReturnsAllProducts() {
        // Arrange
        String enterpriseId = "ENT-001";
        when(productRepository.findAllByEnterpriseId(enterpriseId))
            .thenReturn(mockProductEntities);
        when(productEntityMapper.toDomain(any(ProductEntity.class)))
            .thenAnswer(invocation -> {
                ProductEntity entity = invocation.getArgument(0);
                return createMockProduct(entity.getId(), entity.getProductId());
            });

        // Act
        List<Product> result = productQueryAdapter.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(productRepository).findAllByEnterpriseId(enterpriseId);
        verify(productEntityMapper, times(3)).toDomain(any(ProductEntity.class));
    }

    @Test
    @DisplayName("Should return empty list when no products found")
    void testFindAll_NoProducts_ReturnsEmptyList() {
        // Arrange
        String enterpriseId = "ENT-999";
        when(productRepository.findAllByEnterpriseId(enterpriseId))
            .thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryAdapter.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAllByEnterpriseId(enterpriseId);
        verify(productEntityMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find single product for enterprise")
    void testFindAll_SingleProduct_ReturnsSingleProductList() {
        // Arrange
        String enterpriseId = "ENT-001";
        ProductEntity singleEntity = createMockProductEntity(1L, 100L);
        
        when(productRepository.findAllByEnterpriseId(enterpriseId))
            .thenReturn(List.of(singleEntity));
        when(productEntityMapper.toDomain(singleEntity))
            .thenReturn(mockProduct);

        // Act
        List<Product> result = productQueryAdapter.findAll(enterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockProduct.getProductId(), result.get(0).getProductId());
        verify(productRepository).findAllByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Should handle null enterprise ID")
    void testFindAll_NullEnterpriseId_CallsRepository() {
        // Arrange
        when(productRepository.findAllByEnterpriseId(null))
            .thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryAdapter.findAll(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAllByEnterpriseId(null);
    }

    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testFindAll_EmptyEnterpriseId_CallsRepository() {
        // Arrange
        String emptyEnterpriseId = "";
        when(productRepository.findAllByEnterpriseId(emptyEnterpriseId))
            .thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productQueryAdapter.findAll(emptyEnterpriseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAllByEnterpriseId(emptyEnterpriseId);
    }

    @Test
    @DisplayName("Should map all products correctly")
    void testFindAll_MultipleProducts_MapsAllCorrectly() {
        // Arrange
        String enterpriseId = "ENT-001";
        List<ProductEntity> entities = new ArrayList<>();
        entities.add(createMockProductEntity(1L, 100L));
        entities.add(createMockProductEntity(2L, 101L));
        entities.add(createMockProductEntity(3L, 102L));
        
        when(productRepository.findAllByEnterpriseId(enterpriseId))
            .thenReturn(entities);
        when(productEntityMapper.toDomain(any(ProductEntity.class)))
            .thenAnswer(invocation -> {
                ProductEntity entity = invocation.getArgument(0);
                return createMockProduct(entity.getId(), entity.getProductId());
            });

        // Act
        List<Product> result = productQueryAdapter.findAll(enterpriseId);

        // Assert
        assertEquals(3, result.size());
        assertEquals(100L, result.get(0).getProductId());
        assertEquals(101L, result.get(1).getProductId());
        assertEquals(102L, result.get(2).getProductId());
    }

    @Test
    @DisplayName("Should handle large list of products")
    void testFindAll_LargeList_ReturnsAllProducts() {
        // Arrange
        String enterpriseId = "ENT-001";
        List<ProductEntity> largeList = new ArrayList<>();
        for (long i = 1; i <= 1000; i++) {
            largeList.add(createMockProductEntity(i, 100L + i));
        }
        
        when(productRepository.findAllByEnterpriseId(enterpriseId))
            .thenReturn(largeList);
        when(productEntityMapper.toDomain(any(ProductEntity.class)))
            .thenAnswer(invocation -> {
                ProductEntity entity = invocation.getArgument(0);
                return createMockProduct(entity.getId(), entity.getProductId());
            });

        // Act
        List<Product> result = productQueryAdapter.findAll(enterpriseId);

        // Assert
        assertEquals(1000, result.size());
        verify(productRepository).findAllByEnterpriseId(enterpriseId);
        verify(productEntityMapper, times(1000)).toDomain(any(ProductEntity.class));
    }

    // ==================== Integration Scenarios ====================
    @Test
    @DisplayName("Should handle products with different states")
    void testFindAll_MixedStates_ReturnsAllProducts() {
        // Arrange
        String enterpriseId = "ENT-001";
        List<ProductEntity> entities = new ArrayList<>();
        
        ProductEntity activeEntity = createMockProductEntity(1L, 100L);
        activeEntity.setState(true);
        
        ProductEntity inactiveEntity = createMockProductEntity(2L, 101L);
        inactiveEntity.setState(false);
        
        entities.add(activeEntity);
        entities.add(inactiveEntity);
        
        when(productRepository.findAllByEnterpriseId(enterpriseId))
            .thenReturn(entities);
        when(productEntityMapper.toDomain(any(ProductEntity.class)))
            .thenAnswer(invocation -> {
                ProductEntity entity = invocation.getArgument(0);
                Product product = createMockProduct(entity.getId(), entity.getProductId());
                product.setState(entity.isState());
                return product;
            });

        // Act
        List<Product> result = productQueryAdapter.findAll(enterpriseId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).isActive());
        assertFalse(result.get(1).isActive());
    }

    // ==================== Helper Methods ====================
    private Product createMockProduct(Long id, Long productId) {
        Product product = new Product();
        product.setId(id);
        product.setProductId(productId);
        product.setName("Product " + productId);
        product.setReference("REF-" + productId);
        product.setPresentation("Box");
        product.setState(true);
        product.setEnterpriseId("ENT-001");
        return product;
    }

    private ProductEntity createMockProductEntity(Long id, Long productId) {
        ProductEntity entity = new ProductEntity();
        entity.setId(id);
        entity.setProductId(productId);
        entity.setName("Product " + productId);
        entity.setReference("REF-" + productId);
        entity.setPresentation("Box");
        entity.setState(true);
        entity.setEnterpriseId("ENT-001");
        return entity;
    }

    private List<Product> createMockProducts() {
        List<Product> products = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            products.add(createMockProduct(i, 100L + i));
        }
        return products;
    }

    private List<ProductEntity> createMockProductEntities() {
        List<ProductEntity> entities = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            entities.add(createMockProductEntity(i, 100L + i));
        }
        return entities;
    }
    
}
