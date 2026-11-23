package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.ProductCommandAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IProductEntityCommandMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;

/**
 * @brief Unit tests for ProductCommandAdapter
 * 
 * Tests the write operations for Product records including
 * save, update, delete, and batch operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCommandAdapter Tests")
class ProductCommandAdapterUnitTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductEntityCommandMapper productEntityCommandMapper;

    @InjectMocks
    private ProductCommandAdapter productCommandAdapter;

    private Product product1;
    private Product product2;
    private ProductEntity productEntity1;
    private ProductEntity productEntity2;

    @BeforeEach
    void setUp() {
        // Create test products
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
    }

    @Test
    @DisplayName("Should save single product successfully")
    void testSave_Success() {
        // Arrange
        when(productEntityCommandMapper.toEntity(product1)).thenReturn(productEntity1);
        when(productRepository.save(productEntity1)).thenReturn(productEntity1);

        // Act
        String result = productCommandAdapter.save(product1);

        // Assert
        assertEquals("Product saved successfully.", result);
        verify(productEntityCommandMapper, times(1)).toEntity(product1);
        verify(productRepository, times(1)).save(productEntity1);
    }

    @Test
    @DisplayName("Should handle exception when saving product")
    void testSave_Exception() {
        // Arrange
        when(productEntityCommandMapper.toEntity(product1)).thenReturn(productEntity1);
        when(productRepository.save(productEntity1)).thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.save(product1);

        // Assert
        assertTrue(result.contains("An error occurred while saving the product"));
        verify(productRepository, times(1)).save(productEntity1);
    }

    @Test
    @DisplayName("Should update existing product successfully")
    void testUpdate_Success() {
        // Arrange
        when(productRepository.existsByProductId(100L)).thenReturn(true);
        when(productRepository.getReferenceByProductId(100L)).thenReturn(productEntity1);
        doNothing().when(productEntityCommandMapper).updateEntityFromProduct(product1, productEntity1);
        when(productRepository.save(productEntity1)).thenReturn(productEntity1);

        // Act
        String result = productCommandAdapter.update(product1);

        // Assert
        assertEquals("Product updated successfully.", result);
        verify(productRepository).existsByProductId(100L);
        verify(productRepository).getReferenceByProductId(100L);
        verify(productEntityCommandMapper).updateEntityFromProduct(product1, productEntity1);
        verify(productRepository).save(productEntity1);
    }

    @Test
    @DisplayName("Should return not found when updating non-existent product")
    void testUpdate_NotFound() {
        // Arrange
        when(productRepository.existsByProductId(100L)).thenReturn(false);

        // Act
        String result = productCommandAdapter.update(product1);

        // Assert
        assertEquals("Product not found for update.", result);
        verify(productRepository).existsByProductId(100L);
        verify(productRepository, never()).getReferenceByProductId(anyLong());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle exception when updating product")
    void testUpdate_Exception() {
        // Arrange
        when(productRepository.existsByProductId(100L)).thenReturn(true);
        when(productRepository.getReferenceByProductId(100L)).thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.update(product1);

        // Assert
        assertTrue(result.contains("An error occurred while updating the product"));
    }

    @Test
    @DisplayName("Should delete product by ID successfully")
    void testdelete_Success() {
        // Arrange
        when(productRepository.deleteByProductId(100L)).thenReturn(1);

        // Act
        String result = productCommandAdapter.delete(100L);

        // Assert
        assertEquals("Product deleted successfully.", result);
        verify(productRepository).deleteByProductId(100L);
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent product")
    void testdelete_NotFound() {
        // Arrange
        when(productRepository.deleteByProductId(999L)).thenReturn(0);

        // Act
        String result = productCommandAdapter.delete(999L);

        // Assert
        assertEquals("Product not found.", result);
        verify(productRepository).deleteByProductId(999L);
    }

    @Test
    @DisplayName("Should handle exception when deleting product")
    void testdelete_Exception() {
        // Arrange
        when(productRepository.deleteByProductId(100L))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.delete(100L);

        // Assert
        assertTrue(result.contains("An error occurred while deleting the product"));
    }

    @Test
    @DisplayName("Should delete all products by enterprise ID successfully")
    void testDeleteAllByEnterpriseId_Success() {
        // Arrange
        when(productRepository.deleteByEnterpriseId("ENT-001")).thenReturn(5);

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId("ENT-001");

        // Assert
        assertEquals("Deleted 5 products successfully.", result);
        verify(productRepository).deleteByEnterpriseId("ENT-001");
    }

    @Test
    @DisplayName("Should handle no products to delete by enterprise ID")
    void testDeleteAllByEnterpriseId_NoProducts() {
        // Arrange
        when(productRepository.deleteByEnterpriseId("ENT-999")).thenReturn(0);

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId("ENT-999");

        // Assert
        assertEquals("Deleted 0 products successfully.", result);
        verify(productRepository).deleteByEnterpriseId("ENT-999");
    }

    @Test
    @DisplayName("Should handle exception when deleting all products")
    void testDeleteAllByEnterpriseId_Exception() {
        // Arrange
        when(productRepository.deleteByEnterpriseId("ENT-001"))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.deleteAllByEnterpriseId("ENT-001");

        // Assert
        assertTrue(result.contains("An error occurred while deleting products"));
    }

    @Test
    @DisplayName("Should save all new products successfully")
    void testSaveAll_NewProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        List<Long> productIds = Arrays.asList(100L, 200L);
        List<ProductEntity> entities = Arrays.asList(productEntity1, productEntity2);

        when(productRepository.findProductsIdByProductIdIn(productIds)).thenReturn(Collections.emptyList());
        when(productEntityCommandMapper.toEntity(products)).thenReturn(entities);
        when(productRepository.saveAll(entities)).thenReturn(entities);

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Products processed: 2 new, 0 updated"));
        verify(productRepository).findProductsIdByProductIdIn(productIds);
        verify(productEntityCommandMapper).toEntity(products);
        verify(productRepository).saveAll(entities);
    }

    @Test
    @DisplayName("Should update all existing products successfully")
    void testSaveAll_ExistingProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        List<Long> productIds = Arrays.asList(100L, 200L);
        List<ProductEntity> entities = Arrays.asList(productEntity1, productEntity2);

        when(productRepository.findProductsIdByProductIdIn(productIds)).thenReturn(productIds);
        when(productRepository.findByProductIdIn(productIds)).thenReturn(entities);
        doNothing().when(productEntityCommandMapper).updateEntityFromProduct(any(), any());
        when(productRepository.saveAll(entities)).thenReturn(entities);

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Products processed: 0 new, 2 updated"));
        verify(productRepository).findProductsIdByProductIdIn(productIds);
        verify(productRepository).findByProductIdIn(productIds);
        verify(productRepository).saveAll(entities);
    }

    @Test
    @DisplayName("Should handle mix of new and existing products")
    void testSaveAll_MixedProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        List<Long> productIds = Arrays.asList(100L, 200L);
        List<Long> existingIds = Arrays.asList(100L);
        List<ProductEntity> existingEntities = Arrays.asList(productEntity1);
        List<ProductEntity> newEntities = Arrays.asList(productEntity2);

        when(productRepository.findProductsIdByProductIdIn(productIds)).thenReturn(existingIds);
        when(productEntityCommandMapper.toEntity(Arrays.asList(product2))).thenReturn(newEntities);
        when(productRepository.saveAll(newEntities)).thenReturn(newEntities);
        when(productRepository.findByProductIdIn(existingIds)).thenReturn(existingEntities);
        doNothing().when(productEntityCommandMapper).updateEntityFromProduct(any(), any());
        when(productRepository.saveAll(existingEntities)).thenReturn(existingEntities);

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Products processed: 1 new, 1 updated"));
    }

    @Test
    @DisplayName("Should handle empty product list in saveAll")
    void testSaveAll_EmptyList() {
        // Arrange
        List<Product> emptyList = Collections.emptyList();

        // Act
        String result = productCommandAdapter.saveAll(emptyList);

        // Assert
        assertEquals("No products to process.", result);
        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle exception in saveAll")
    void testSaveAll_Exception() {
        // Arrange
        List<Product> products = Arrays.asList(product1);
        when(productRepository.findProductsIdByProductIdIn(anyList()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Error:"));
    }

    @Test
    @DisplayName("Should handle single product in saveAll")
    void testSaveAll_SingleProduct() {
        // Arrange
        List<Product> products = Arrays.asList(product1);
        List<Long> productIds = Arrays.asList(100L);
        List<ProductEntity> entities = Arrays.asList(productEntity1);

        when(productRepository.findProductsIdByProductIdIn(productIds)).thenReturn(Collections.emptyList());
        when(productEntityCommandMapper.toEntity(products)).thenReturn(entities);
        when(productRepository.saveAll(entities)).thenReturn(entities);

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Products processed: 1 new, 0 updated"));
    }

    @Test
    @DisplayName("Should handle large batch of products")
    void testSaveAll_LargeBatch() {
        // Arrange
        Product product3 = Product.builder().productId(300L).name("Product 3").build();
        Product product4 = Product.builder().productId(400L).name("Product 4").build();
        Product product5 = Product.builder().productId(500L).name("Product 5").build();
        
        List<Product> products = Arrays.asList(product1, product2, product3, product4, product5);
        List<Long> productIds = Arrays.asList(100L, 200L, 300L, 400L, 500L);

        when(productRepository.findProductsIdByProductIdIn(productIds)).thenReturn(Collections.emptyList());
        when(productEntityCommandMapper.toEntity(products)).thenReturn(Collections.emptyList());
        when(productRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        String result = productCommandAdapter.saveAll(products);

        // Assert
        assertTrue(result.contains("Products processed: 5 new, 0 updated"));
    }

    @Test
    @DisplayName("Should map entity correctly during save")
    void testSave_EntityMapping() {
        // Arrange
        when(productEntityCommandMapper.toEntity(product1)).thenReturn(productEntity1);
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity1);

        // Act
        productCommandAdapter.save(product1);

        // Assert
        verify(productEntityCommandMapper).toEntity(product1);
        verify(productRepository).save(argThat(entity ->
            entity.getProductId().equals(100L) &&
            entity.getName().equals("Product 1") &&
            entity.getReference().equals("REF-001")
        ));
    }

    @Test
    @DisplayName("Should update entity fields correctly")
    void testUpdate_EntityFieldsUpdate() {
        // Arrange
        when(productRepository.existsByProductId(100L)).thenReturn(true);
        when(productRepository.getReferenceByProductId(100L)).thenReturn(productEntity1);
        doNothing().when(productEntityCommandMapper).updateEntityFromProduct(any(), any());
        when(productRepository.save(any())).thenReturn(productEntity1);

        // Act
        productCommandAdapter.update(product1);

        // Assert
        verify(productEntityCommandMapper).updateEntityFromProduct(eq(product1), eq(productEntity1));
    }
}
