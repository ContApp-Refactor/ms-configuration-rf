package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.ProductCommandAdapter;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IProductEntityMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductCommandAdapterUnitTest {
    

    @Mock private IProductRepository productRepository;
    @Mock private IProductEntityMapper productEntityMapper;

    @InjectMocks
    private ProductCommandAdapter productCommandAdapter;

    private Product mockProduct;
    private ProductEntity mockProductEntity;
    private List<ProductEntity> mockProductEntities;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        mockProduct = createMockProduct(1L, 100L);
        mockProductEntity = createMockProductEntity(1L, 100L);
        mockProductEntities = createMockProductEntities();
    }

    // ==================== save() - Single Product ====================
    @Test
    @DisplayName("Should save single product successfully")
    void testSave_ValidProduct_SavesSuccessfully() {
        // Arrange
        when(productEntityMapper.toEntity(mockProduct)).thenReturn(mockProductEntity);
        when(productRepository.save(mockProductEntity)).thenReturn(mockProductEntity);

        // Act
        String result = productCommandAdapter.save(mockProduct);

        // Assert
        assertEquals("Product saved successfully.", result);
        verify(productEntityMapper).toEntity(mockProduct);
        verify(productRepository).save(mockProductEntity);
    }

    @Test
    @DisplayName("Should handle exception when saving single product")
    void testSave_RepositoryThrowsException_ReturnsErrorMessage() {
        // Arrange
        when(productEntityMapper.toEntity(mockProduct)).thenReturn(mockProductEntity);
        when(productRepository.save(mockProductEntity))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.save(mockProduct);

        // Assert
        assertTrue(result.contains("An error occurred"));
        assertTrue(result.contains("Database error"));
        verify(productRepository).save(mockProductEntity);
    }

    // ==================== saveAll() - Empty List ====================
    @Test
    @DisplayName("Should handle empty product list")
    void testSaveAll_EmptyList_ReturnsNoProductsMessage() {
        // Arrange
        List<Product> emptyList = Collections.emptyList();

        // Act
        String result = productCommandAdapter.saveAll(emptyList);

        // Assert
        assertEquals("No products to process.", result);
        verify(productRepository, never()).saveAll(anyList());
    }

    // ==================== saveAll() - Only New Products ====================
    @Test
    @DisplayName("Should save only new products when all are new")
    void testSaveAll_OnlyNewProducts_SavesAll() {
        // Arrange
        List<Product> newProducts = createNewProducts(3);
        List<Long> productIds = Arrays.asList(200L, 201L, 202L);
        
        when(productRepository.findProductsIdByProductIdIn(productIds))
            .thenReturn(Collections.emptyList()); // No existing products
        when(productEntityMapper.toEntity(anyList())).thenReturn(mockProductEntities);
        when(productRepository.saveAll(anyList())).thenReturn(mockProductEntities);

        // Act
        String result = productCommandAdapter.saveAll(newProducts);

        // Assert
        assertTrue(result.contains("3 new"));
        assertTrue(result.contains("0 updated"));
        verify(productRepository).saveAll(anyList());
    }

    // ==================== saveAll() - Only Existing Products ====================
    @Test
    @DisplayName("Should update only existing products when all exist")
    void testSaveAll_OnlyExistingProducts_UpdatesAll() {
        // Arrange
        List<Product> existingProducts = createExistingProducts(3);
        List<Long> productIds = Arrays.asList(100L, 101L, 102L);
        
        when(productRepository.findProductsIdByProductIdIn(productIds))
            .thenReturn(productIds); // All exist
        when(productRepository.findByProductIdIn(productIds))
            .thenReturn(mockProductEntities);
        doNothing().when(productEntityMapper).updateEntityFromProduct(any(Product.class), any(ProductEntity.class));
        when(productRepository.saveAll(anyList())).thenReturn(mockProductEntities);

        // Act
        String result = productCommandAdapter.saveAll(existingProducts);

        // Assert
        assertTrue(result.contains("0 new"));
        assertTrue(result.contains("3 updated"));
        verify(productRepository).findByProductIdIn(productIds);
        verify(productRepository).saveAll(anyList());
    }

    // ==================== saveAll() - Mixed New and Existing ====================
    @Test
    @DisplayName("Should handle mixed new and existing products")
    void testSaveAll_MixedProducts_SavesAndUpdates() {
        // Arrange
        List<Product> mixedProducts = new ArrayList<>();
        mixedProducts.add(createMockProduct(1L, 100L)); // Existing
        mixedProducts.add(createMockProduct(null, 200L)); // New
        mixedProducts.add(createMockProduct(2L, 101L)); // Existing
        mixedProducts.add(createMockProduct(null, 201L)); // New
        
        List<Long> allProductIds = Arrays.asList(100L, 200L, 101L, 201L);
        List<Long> existingIds = Arrays.asList(100L, 101L);
        
        when(productRepository.findProductsIdByProductIdIn(allProductIds))
            .thenReturn(existingIds);
        when(productEntityMapper.toEntity(anyList())).thenReturn(Arrays.asList(
            createMockProductEntity(null, 200L),
            createMockProductEntity(null, 201L)
        ));
        when(productRepository.findByProductIdIn(existingIds))
            .thenReturn(Arrays.asList(
                createMockProductEntity(1L, 100L),
                createMockProductEntity(2L, 101L)
            ));
        doNothing().when(productEntityMapper).updateEntityFromProduct(any(Product.class), any(ProductEntity.class));
        when(productRepository.saveAll(anyList())).thenReturn(mockProductEntities);

        // Act
        String result = productCommandAdapter.saveAll(mixedProducts);

        // Assert
        assertTrue(result.contains("2 new"));
        assertTrue(result.contains("2 updated"));
        verify(productRepository, times(2)).saveAll(anyList());
    }

    // ==================== saveAll() - Update Logic ====================
    @Test
    @DisplayName("Should correctly update entity properties from product")
    void testSaveAll_ExistingProduct_UpdatesEntityProperties() {
    // Arrange
    List<Product> existingProducts = List.of(createMockProduct(1L, 100L));
    List<Long> productIds = List.of(100L);
    ProductEntity entity = createMockProductEntity(1L, 100L);
    
    when(productRepository.findProductsIdByProductIdIn(productIds))
        .thenReturn(productIds);
    when(productRepository.findByProductIdIn(productIds))
        .thenReturn(List.of(entity));
    doNothing().when(productEntityMapper).updateEntityFromProduct(any(Product.class), eq(entity));
    when(productRepository.saveAll(anyList())).thenReturn(List.of(entity));

    // Act
    productCommandAdapter.saveAll(existingProducts);

    // Assert
    verify(productEntityMapper).updateEntityFromProduct(any(Product.class), eq(entity));
    
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ProductEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(productRepository).saveAll(captor.capture());
    
    List<ProductEntity> savedEntities = captor.getValue();
    assertEquals(1, savedEntities.size());
    assertEquals(entity, savedEntities.get(0));
    }



    @Test
    @DisplayName("Should handle exception during saveAll and return error message")
    void testSaveAll_ExceptionThrown_ReturnsErrorMessage() {
        // Arrange
        List<Product> products = createNewProducts(2);
        when(productRepository.findProductsIdByProductIdIn(anyList()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Error:"));
        assertTrue(result.contains("Database connection failed"));
    }

    @Test
    @DisplayName("Should handle exception during mapper conversion")
    void testSaveAll_MapperThrowsException_ReturnsErrorMessage() {
        // Arrange
        List<Product> newProducts = createNewProducts(2);
        when(productRepository.findProductsIdByProductIdIn(anyList()))
            .thenReturn(Collections.emptyList());
        when(productEntityMapper.toEntity(anyList()))
            .thenThrow(new RuntimeException("Mapping error"));

        // Act
        String result = productCommandAdapter.saveAll(newProducts);

        // Assert
        assertTrue(result.contains("Error:"));
        assertTrue(result.contains("Mapping error"));
    }

    // ==================== Edge Cases ====================
    @Test
    @DisplayName("Should handle single new product in list")
    void testSaveAll_SingleNewProduct_SavesSuccessfully() {
        // Arrange
        List<Product> singleProduct = List.of(createMockProduct(null, 200L));
        when(productRepository.findProductsIdByProductIdIn(anyList()))
            .thenReturn(Collections.emptyList());
        when(productEntityMapper.toEntity(anyList()))
            .thenReturn(List.of(createMockProductEntity(null, 200L)));
        when(productRepository.saveAll(anyList())).thenReturn(List.of(mockProductEntity));

        // Act
        String result = productCommandAdapter.saveAll(singleProduct);

        // Assert
        assertTrue(result.contains("1 new"));
        assertTrue(result.contains("0 updated"));
    }

    @Test
    @DisplayName("Should handle single existing product in list")
    void testSaveAll_SingleExistingProduct_UpdatesSuccessfully() {
        // Arrange
        List<Product> singleProduct = List.of(createMockProduct(1L, 100L));
        when(productRepository.findProductsIdByProductIdIn(anyList()))
            .thenReturn(List.of(100L));
        when(productRepository.findByProductIdIn(anyList()))
            .thenReturn(List.of(createMockProductEntity(1L, 100L)));
        doNothing().when(productEntityMapper).updateEntityFromProduct(any(), any());
        when(productRepository.saveAll(anyList())).thenReturn(List.of(mockProductEntity));

        // Act
        String result = productCommandAdapter.saveAll(singleProduct);

        // Assert
        assertTrue(result.contains("0 new"));
        assertTrue(result.contains("1 updated"));
    }

    @Test
    @DisplayName("Should handle large list of products")
    void testSaveAll_LargeList_ProcessesSuccessfully() {
        // Arrange
        List<Product> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(createMockProduct(null, (long) (200 + i)));
        }
        
        when(productRepository.findProductsIdByProductIdIn(anyList()))
            .thenReturn(Collections.emptyList());
        when(productEntityMapper.toEntity(anyList())).thenReturn(mockProductEntities);
        when(productRepository.saveAll(anyList())).thenReturn(mockProductEntities);

        // Act
        String result = productCommandAdapter.saveAll(largeList);

        // Assert
        assertTrue(result.contains("1000 new"));
        verify(productRepository).saveAll(anyList());
    }

    // ==================== Helper Methods ====================
    private Product createMockProduct(Long id, Long productId) {
        Product product = new Product();
        product.setId(id);
        product.setProductId(productId);
        product.setName("Product " + productId);
        product.setReference("REF-" + productId);
        product.setState(true);
        return product;
    }

    private ProductEntity createMockProductEntity(Long id, Long productId) {
        ProductEntity entity = new ProductEntity();
        entity.setId(id);
        entity.setProductId(productId);
        entity.setName("Product " + productId);
        entity.setReference("REF-" + productId);
        return entity;
    }

    private List<ProductEntity> createMockProductEntities() {
        List<ProductEntity> entities = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            entities.add(createMockProductEntity(i, 100L + i));
        }
        return entities;
    }

    private List<Product> createNewProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(createMockProduct(null, 200L + i));
        }
        return products;
    }

    private List<Product> createExistingProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            products.add(createMockProduct(i + 1, 100L + i));
        }
        return products;
    }

    
    // ==================== delete() ====================
    @Test
    @DisplayName("Should delete product by ID successfully")
    void testDelete_ExistingProduct_DeletesSuccessfully() {
        // Arrange
        Long productId = 100L;
        when(productRepository.deleteByProductId(productId)).thenReturn(1);

        // Act
        String result = productCommandAdapter.delete(productId);

        // Assert
        assertEquals("Product deleted successfully.", result);
        verify(productRepository).deleteByProductId(productId);
    }

    @Test
    @DisplayName("Should return not found message when deleting non-existing product")
    void testDelete_NonExistingProduct_ReturnsNotFoundMessage() {
        // Arrange
        Long productId = 999L;
        when(productRepository.deleteByProductId(productId)).thenReturn(0);

        // Act
        String result = productCommandAdapter.delete(productId);

        // Assert
        assertEquals("Product not found.", result);
        verify(productRepository).deleteByProductId(productId);
    }

    @Test
    @DisplayName("Should handle exception when deleting product")
    void testDelete_RepositoryThrowsException_ReturnsErrorMessage() {
        // Arrange
        Long productId = 100L;
        when(productRepository.deleteByProductId(productId))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.delete(productId);

        // Assert
        assertTrue(result.contains("An error occurred"));
        assertTrue(result.contains("Database error"));
    }

    @Test
    @DisplayName("Should handle null productId when deleting")
    void testDelete_NullProductId_HandlesGracefully() {
        // Arrange
        when(productRepository.deleteByProductId(null))
            .thenThrow(new IllegalArgumentException("Product ID cannot be null"));

        // Act
        String result = productCommandAdapter.delete(null);

        // Assert
        assertTrue(result.contains("An error occurred"));
    }

    @Test
    @DisplayName("Should delete multiple products with different IDs")
    void testDelete_MultipleProducts_DeletesEachCorrectly() {
        // Arrange
        when(productRepository.deleteByProductId(100L)).thenReturn(1);
        when(productRepository.deleteByProductId(101L)).thenReturn(1);
        when(productRepository.deleteByProductId(102L)).thenReturn(1);

        // Act
        String result1 = productCommandAdapter.delete(100L);
        String result2 = productCommandAdapter.delete(101L);
        String result3 = productCommandAdapter.delete(102L);

        // Assert
        assertEquals("Product deleted successfully.", result1);
        assertEquals("Product deleted successfully.", result2);
        assertEquals("Product deleted successfully.", result3);
        verify(productRepository, times(3)).deleteByProductId(any());
    }

    // ==================== deleteAllByEnterpriseId() ====================
    @Test
    @DisplayName("Should delete all products by enterprise ID successfully")
    void testDeleteAllByEnterpriseId_ExistingProducts_DeletesAll() {
        // Arrange
        String enterpriseId = "ENT-001";
        when(productRepository.deleteByEnterpriseId(enterpriseId)).thenReturn(5);

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId(enterpriseId);

        // Assert
        assertEquals("Deleted 5 products successfully.", result);
        verify(productRepository).deleteByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Should return zero count when no products exist for enterprise")
    void testDeleteAllByEnterpriseId_NoProducts_ReturnsZeroCount() {
        // Arrange
        String enterpriseId = "ENT-999";
        when(productRepository.deleteByEnterpriseId(enterpriseId)).thenReturn(0);

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId(enterpriseId);

        // Assert
        assertEquals("Deleted 0 products successfully.", result);
        verify(productRepository).deleteByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Should handle exception when deleting by enterprise ID")
    void testDeleteAllByEnterpriseId_RepositoryThrowsException_ReturnsErrorMessage() {
        // Arrange
        String enterpriseId = "ENT-001";
        when(productRepository.deleteByEnterpriseId(enterpriseId))
            .thenThrow(new RuntimeException("Database connection lost"));

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId(enterpriseId);

        // Assert
        assertTrue(result.contains("An error occurred"));
        assertTrue(result.contains("Database connection lost"));
    }

    @Test
    @DisplayName("Should handle null enterprise ID")
    void testDeleteAllByEnterpriseId_NullEnterpriseId_HandlesGracefully() {
        // Arrange
        when(productRepository.deleteByEnterpriseId(null))
            .thenThrow(new IllegalArgumentException("Enterprise ID cannot be null"));

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId(null);

        // Assert
        assertTrue(result.contains("An error occurred"));
    }

    @Test
    @DisplayName("Should delete large number of products for enterprise")
    void testDeleteAllByEnterpriseId_LargeCount_DeletesSuccessfully() {
        // Arrange
        String enterpriseId = "ENT-001";
        when(productRepository.deleteByEnterpriseId(enterpriseId)).thenReturn(1000);

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId(enterpriseId);

        // Assert
        assertEquals("Deleted 1000 products successfully.", result);
        verify(productRepository).deleteByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Should handle different enterprise IDs correctly")
    void testDeleteAllByEnterpriseId_DifferentEnterprises_DeletesCorrectly() {
        // Arrange
        when(productRepository.deleteByEnterpriseId("ENT-001")).thenReturn(10);
        when(productRepository.deleteByEnterpriseId("ENT-002")).thenReturn(20);
        when(productRepository.deleteByEnterpriseId("ENT-003")).thenReturn(5);

        // Act
        String result1 = productCommandAdapter.deleteAllByEnterpriseId("ENT-001");
        String result2 = productCommandAdapter.deleteAllByEnterpriseId("ENT-002");
        String result3 = productCommandAdapter.deleteAllByEnterpriseId("ENT-003");

        // Assert
        assertTrue(result1.contains("10 products"));
        assertTrue(result2.contains("20 products"));
        assertTrue(result3.contains("5 products"));
        verify(productRepository, times(3)).deleteByEnterpriseId(any());
    }

    @Test
    @DisplayName("Should handle empty enterprise ID")
    void testDeleteAllByEnterpriseId_EmptyEnterpriseId_CallsRepository() {
        // Arrange
        String emptyEnterpriseId = "";
        when(productRepository.deleteByEnterpriseId(emptyEnterpriseId)).thenReturn(0);

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId(emptyEnterpriseId);

        // Assert
        assertEquals("Deleted 0 products successfully.", result);
        verify(productRepository).deleteByEnterpriseId(emptyEnterpriseId);
    }

}

