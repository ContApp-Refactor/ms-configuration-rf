package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.ProductPersistenceAdapter;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.IProductPersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductPersistenceAdapterUnitTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductPersistenceMapper productPersistenceMapper;

    @InjectMocks
    private ProductPersistenceAdapter productPersistenceAdapter;

    private Product product;
    private ProductEntity productEntity;
    private static final Long PRODUCT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String PRODUCT_NAME = "Laptop HP";
    private static final String PRODUCT_CODE = "LAP-001";
    private static final String REFERENCE = "REF-001";
    private static final Long CATEGORY_ID = 1L;
    private static final Long UNIT_MEASURE_ID = 1L;
    private static final Long PRODUCT_TYPE_ID = 1L;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(PRODUCT_ID)
                .code(PRODUCT_CODE)
                .name(PRODUCT_NAME)
                .description("Laptop para trabajo")
                .quantity(10)
                .unitOfMeasureId(UNIT_MEASURE_ID)
                .categoryId(CATEGORY_ID)
                .enterpriseId(ENTERPRISE_ID)
                .cost(1500.00)
                .state(true)
                .reference(REFERENCE)
                .productTypeId(PRODUCT_TYPE_ID)
                .presentation("Caja")
                .usageCount(0)
                .build();

        productEntity = new ProductEntity();
        productEntity.setId(PRODUCT_ID);
        productEntity.setCode(PRODUCT_CODE);
        productEntity.setName(PRODUCT_NAME);
        productEntity.setDescription("Laptop para trabajo");
        productEntity.setQuantity(10);
        productEntity.setUnitOfMeasureId(UNIT_MEASURE_ID);
        productEntity.setCategoryId(CATEGORY_ID);
        productEntity.setEnterpriseId(ENTERPRISE_ID);
        productEntity.setCost(1500.00);
        productEntity.setState(true);
        productEntity.setReference(REFERENCE);
        productEntity.setProductTypeId(PRODUCT_TYPE_ID);
        productEntity.setPresentation("Caja");
        productEntity.setUsageCount(0);
    }

    // ==================== Tests de findByIdAndEnterpriseId ====================

    @Test
    @DisplayName("Debe encontrar producto por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsProduct() {
        // Arrange
        when(productRepository.findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(productEntity));
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Optional<Product> result = productPersistenceAdapter.findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(PRODUCT_ID, result.get().getId());
        assertEquals(PRODUCT_NAME, result.get().getName());
        verify(productRepository).findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID);
        verify(productPersistenceMapper).toProduct(productEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no encuentra producto por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsEmpty() {
        // Arrange
        when(productRepository.findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productPersistenceAdapter.findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository).findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID);
        verify(productPersistenceMapper, never()).toProduct(any());
    }

    // ==================== Tests de findById ====================

    @Test
    @DisplayName("Debe encontrar producto por ID")
    void testFindById_ReturnsProduct() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productEntity));
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Optional<Product> result = productPersistenceAdapter.findById(PRODUCT_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(PRODUCT_ID, result.get().getId());
        verify(productRepository).findById(PRODUCT_ID);
        verify(productPersistenceMapper).toProduct(productEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no encuentra producto por ID")
    void testFindById_ReturnsEmpty() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productPersistenceAdapter.findById(PRODUCT_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository).findById(PRODUCT_ID);
        verify(productPersistenceMapper, never()).toProduct(any());
    }

    // ==================== Tests de create ====================

    @Test
    @DisplayName("Debe crear producto correctamente")
    void testCreate_CreatesProduct() {
        // Arrange
        when(productPersistenceMapper.toProductEntity(product)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(productEntity);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Product result = productPersistenceAdapter.create(product);

        // Assert
        assertNotNull(result);
        assertEquals(PRODUCT_ID, result.getId());
        assertEquals(PRODUCT_NAME, result.getName());
        verify(productRepository).save(productEntity);
        verify(productPersistenceMapper).toProductEntity(product);
        verify(productPersistenceMapper).toProduct(productEntity);
    }

    // ==================== Tests de saveAll ====================

    @Test
    @DisplayName("Debe guardar múltiples productos correctamente")
    void testSaveAll_SavesAllProducts() {
        // Arrange
        Product product2 = Product.builder()
                .id(2L)
                .name("Mouse Logitech")
                .build();
        
        ProductEntity entity2 = new ProductEntity();
        entity2.setId(2L);
        entity2.setName("Mouse Logitech");

        List<Product> products = List.of(product, product2);
        List<ProductEntity> entities = List.of(productEntity, entity2);

        when(productPersistenceMapper.toProductEntity(product)).thenReturn(productEntity);
        when(productPersistenceMapper.toProductEntity(product2)).thenReturn(entity2);
        when(productRepository.saveAll(anyList())).thenReturn(entities);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);
        when(productPersistenceMapper.toProduct(entity2)).thenReturn(product2);

        // Act
        List<Product> result = productPersistenceAdapter.saveAll(products);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).saveAll(anyList());
        verify(productPersistenceMapper, times(2)).toProductEntity(any());
        verify(productPersistenceMapper, times(2)).toProduct(any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía al guardar lista vacía")
    void testSaveAll_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        List<Product> emptyList = List.of();
        when(productRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        List<Product> result = productPersistenceAdapter.saveAll(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).saveAll(anyList());
    }

    // ==================== Tests de deleteById ====================

    @Test
    @DisplayName("Debe eliminar producto por ID")
    void testDeleteById_DeletesProduct() {
        // Arrange
        doNothing().when(productRepository).deleteById(PRODUCT_ID);

        // Act
        productPersistenceAdapter.deleteById(PRODUCT_ID);

        // Assert
        verify(productRepository).deleteById(PRODUCT_ID);
    }

    // ==================== Tests de findActivatedWithPagination ====================

    @Test
    @DisplayName("Debe encontrar productos activados con paginación")
    void testFindActivatedWithPagination_ReturnsActivatedProducts() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductEntity> entities = List.of(productEntity);
        Page<ProductEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productRepository.findByEnterpriseIdAndState(ENTERPRISE_ID, true, pageRequest))
                .thenReturn(entityPage);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Page<Product> result = productPersistenceAdapter.findActivatedWithPagination(ENTERPRISE_ID, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
        verify(productRepository).findByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(true), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay productos activados")
    void testFindActivatedWithPagination_ReturnsEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productRepository.findByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(true), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        Page<Product> result = productPersistenceAdapter.findActivatedWithPagination(ENTERPRISE_ID, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de findByEnterpriseIdAndState ====================

    @Test
    @DisplayName("Debe encontrar productos por enterpriseId y estado activo")
    void testFindByEnterpriseIdAndState_WithActiveState_ReturnsProducts() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductEntity> entities = List.of(productEntity);
        Page<ProductEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productRepository.findByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(true), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Page<Product> result = productPersistenceAdapter.findByEnterpriseIdAndState(ENTERPRISE_ID, true, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
    }

    @Test
    @DisplayName("Debe encontrar productos por enterpriseId y estado inactivo")
    void testFindByEnterpriseIdAndState_WithInactiveState_ReturnsProducts() {
        // Arrange
        productEntity.setState(false);
        product.setState(false);
        
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductEntity> entities = List.of(productEntity);
        Page<ProductEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productRepository.findByEnterpriseIdAndState(eq(ENTERPRISE_ID), eq(false), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Page<Product> result = productPersistenceAdapter.findByEnterpriseIdAndState(ENTERPRISE_ID, false, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).isState());
    }

    // ==================== Tests de findByCategoryId ====================

    @Test
    @DisplayName("Debe encontrar productos por categoryId")
    void testFindByCategoryId_ReturnsProducts() {
        // Arrange
        List<ProductEntity> entities = List.of(productEntity);
        when(productRepository.findByCategoryId(CATEGORY_ID)).thenReturn(entities);
        when(productPersistenceMapper.toProductList(entities)).thenReturn(List.of(product));

        // Act
        List<Product> result = productPersistenceAdapter.findByCategoryId(CATEGORY_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CATEGORY_ID, result.get(0).getCategoryId());
        verify(productRepository).findByCategoryId(CATEGORY_ID);
        verify(productPersistenceMapper).toProductList(entities);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos con categoryId")
    void testFindByCategoryId_ReturnsEmptyList() {
        // Arrange
        when(productRepository.findByCategoryId(CATEGORY_ID)).thenReturn(List.of());
        when(productPersistenceMapper.toProductList(anyList())).thenReturn(List.of());

        // Act
        List<Product> result = productPersistenceAdapter.findByCategoryId(CATEGORY_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests de findByUnitOfMeasureId ====================

    @Test
    @DisplayName("Debe encontrar productos por unitOfMeasureId")
    void testFindByUnitOfMeasureId_ReturnsProducts() {
        // Arrange
        List<ProductEntity> entities = List.of(productEntity);
        when(productRepository.findByUnitOfMeasureId(UNIT_MEASURE_ID)).thenReturn(entities);
        when(productPersistenceMapper.toProductList(entities)).thenReturn(List.of(product));

        // Act
        List<Product> result = productPersistenceAdapter.findByUnitOfMeasureId(UNIT_MEASURE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UNIT_MEASURE_ID, result.get(0).getUnitOfMeasureId());
        verify(productRepository).findByUnitOfMeasureId(UNIT_MEASURE_ID);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos con unitOfMeasureId")
    void testFindByUnitOfMeasureId_ReturnsEmptyList() {
        // Arrange
        when(productRepository.findByUnitOfMeasureId(UNIT_MEASURE_ID)).thenReturn(List.of());
        when(productPersistenceMapper.toProductList(anyList())).thenReturn(List.of());

        // Act
        List<Product> result = productPersistenceAdapter.findByUnitOfMeasureId(UNIT_MEASURE_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests de findByProductTypeId ====================

    @Test
    @DisplayName("Debe encontrar productos por productTypeId")
    void testFindByProductTypeId_ReturnsProducts() {
        // Arrange
        List<ProductEntity> entities = List.of(productEntity);
        when(productRepository.findByProductTypeId(PRODUCT_TYPE_ID)).thenReturn(entities);
        when(productPersistenceMapper.toProductList(entities)).thenReturn(List.of(product));

        // Act
        List<Product> result = productPersistenceAdapter.findByProductTypeId(PRODUCT_TYPE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PRODUCT_TYPE_ID, result.get(0).getProductTypeId());
        verify(productRepository).findByProductTypeId(PRODUCT_TYPE_ID);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay productos con productTypeId")
    void testFindByProductTypeId_ReturnsEmptyList() {
        // Arrange
        when(productRepository.findByProductTypeId(PRODUCT_TYPE_ID)).thenReturn(List.of());
        when(productPersistenceMapper.toProductList(anyList())).thenReturn(List.of());

        // Act
        List<Product> result = productPersistenceAdapter.findByProductTypeId(PRODUCT_TYPE_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests de existsByNameAndEnterpriseId ====================

    @Test
    @DisplayName("Debe retornar true cuando existe producto con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByNameAndEnterpriseId(PRODUCT_NAME, ENTERPRISE_ID))
                .thenReturn(true);

        // Act
        boolean result = productPersistenceAdapter.existsByNameAndEnterpriseId(PRODUCT_NAME, ENTERPRISE_ID);

        // Assert
        assertTrue(result);
        verify(productRepository).existsByNameAndEnterpriseId(PRODUCT_NAME, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe producto con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByNameAndEnterpriseId(PRODUCT_NAME, ENTERPRISE_ID))
                .thenReturn(false);

        // Act
        boolean result = productPersistenceAdapter.existsByNameAndEnterpriseId(PRODUCT_NAME, ENTERPRISE_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByReferenceAndEnterpriseId ====================

    @Test
    @DisplayName("Debe retornar true cuando existe producto con referencia y enterpriseId")
    void testExistsByReferenceAndEnterpriseId_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByReferenceAndEnterpriseId(REFERENCE, ENTERPRISE_ID))
                .thenReturn(true);

        // Act
        boolean result = productPersistenceAdapter.existsByReferenceAndEnterpriseId(REFERENCE, ENTERPRISE_ID);

        // Assert
        assertTrue(result);
        verify(productRepository).existsByReferenceAndEnterpriseId(REFERENCE, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe producto con referencia y enterpriseId")
    void testExistsByReferenceAndEnterpriseId_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByReferenceAndEnterpriseId(REFERENCE, ENTERPRISE_ID))
                .thenReturn(false);

        // Act
        boolean result = productPersistenceAdapter.existsByReferenceAndEnterpriseId(REFERENCE, ENTERPRISE_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByNameAndEnterpriseIdAndIdNot ====================

    @Test
    @DisplayName("Debe retornar true cuando existe otro producto con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByNameAndEnterpriseIdAndIdNot(PRODUCT_NAME, ENTERPRISE_ID, PRODUCT_ID))
                .thenReturn(true);

        // Act
        boolean result = productPersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                PRODUCT_NAME, ENTERPRISE_ID, PRODUCT_ID);

        // Assert
        assertTrue(result);
        verify(productRepository).existsByNameAndEnterpriseIdAndIdNot(PRODUCT_NAME, ENTERPRISE_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe otro producto con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByNameAndEnterpriseIdAndIdNot(PRODUCT_NAME, ENTERPRISE_ID, PRODUCT_ID))
                .thenReturn(false);

        // Act
        boolean result = productPersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                PRODUCT_NAME, ENTERPRISE_ID, PRODUCT_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByReferenceAndEnterpriseIdAndIdNot ====================

    @Test
    @DisplayName("Debe retornar true cuando existe otro producto con misma referencia")
    void testExistsByReferenceAndEnterpriseIdAndIdNot_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByReferenceAndEnterpriseIdAndIdNot(REFERENCE, ENTERPRISE_ID, PRODUCT_ID))
                .thenReturn(true);

        // Act
        boolean result = productPersistenceAdapter.existsByReferenceAndEnterpriseIdAndIdNot(
                REFERENCE, ENTERPRISE_ID, PRODUCT_ID);

        // Assert
        assertTrue(result);
        verify(productRepository).existsByReferenceAndEnterpriseIdAndIdNot(REFERENCE, ENTERPRISE_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe otro producto con misma referencia")
    void testExistsByReferenceAndEnterpriseIdAndIdNot_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByReferenceAndEnterpriseIdAndIdNot(REFERENCE, ENTERPRISE_ID, PRODUCT_ID))
                .thenReturn(false);

        // Act
        boolean result = productPersistenceAdapter.existsByReferenceAndEnterpriseIdAndIdNot(
                REFERENCE, ENTERPRISE_ID, PRODUCT_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de findByEnterpriseIdWithFilters ====================

    @Test
    @DisplayName("Debe buscar productos con filtros ordenados ascendente")
    void testFindByEnterpriseIdWithFilters_WithAscOrder_ReturnsProducts() {
        // Arrange
        String search = "Laptop";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductEntity> entities = List.of(productEntity);
        Page<ProductEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productRepository.findByEnterpriseIdWithFilters(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Page<Product> result = productPersistenceAdapter.findByEnterpriseIdWithFilters(
                ENTERPRISE_ID, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByEnterpriseIdWithFilters(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe buscar productos con filtros ordenados descendente")
    void testFindByEnterpriseIdWithFilters_WithDescOrder_ReturnsProducts() {
        // Arrange
        String search = "Laptop";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").descending());
        List<ProductEntity> entities = List.of(productEntity);
        Page<ProductEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productRepository.findByEnterpriseIdWithFilters(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productPersistenceMapper.toProduct(productEntity)).thenReturn(product);

        // Act
        Page<Product> result = productPersistenceAdapter.findByEnterpriseIdWithFilters(
                ENTERPRISE_ID, search, 0, 10, "name", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe buscar productos sin resultados")
    void testFindByEnterpriseIdWithFilters_ReturnsEmptyPage() {
        // Arrange
        String search = "NonExistent";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productRepository.findByEnterpriseIdWithFilters(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        Page<Product> result = productPersistenceAdapter.findByEnterpriseIdWithFilters(
                ENTERPRISE_ID, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countByEnterpriseIdWithFilters ====================

    @Test
    @DisplayName("Debe contar productos con filtros")
    void testCountByEnterpriseIdWithFilters_ReturnsCount() {
        // Arrange
        String search = "Laptop";
        when(productRepository.countByEnterpriseIdWithFilters(ENTERPRISE_ID, search))
                .thenReturn(5L);

        // Act
        long result = productPersistenceAdapter.countByEnterpriseIdWithFilters(ENTERPRISE_ID, search);

        // Assert
        assertEquals(5L, result);
        verify(productRepository).countByEnterpriseIdWithFilters(ENTERPRISE_ID, search);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay productos con filtros")
    void testCountByEnterpriseIdWithFilters_ReturnsZero() {
        // Arrange
        String search = "NonExistent";
        when(productRepository.countByEnterpriseIdWithFilters(ENTERPRISE_ID, search))
                .thenReturn(0L);

        // Act
        long result = productPersistenceAdapter.countByEnterpriseIdWithFilters(ENTERPRISE_ID, search);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de countByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar productos por enterpriseId")
    void testCountByEnterpriseId_ReturnsCount() {
        // Arrange
        when(productRepository.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(20L);

        // Act
        long result = productPersistenceAdapter.countByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(20L, result);
        verify(productRepository).countByEnterpriseId(ENTERPRISE_ID);
    }

    // ==================== Tests de countActivatedByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar productos activados por enterpriseId")
    void testCountActivatedByEnterpriseId_ReturnsCount() {
        // Arrange
        when(productRepository.countByEnterpriseIdAndState(ENTERPRISE_ID, true))
                .thenReturn(15L);

        // Act
        long result = productPersistenceAdapter.countActivatedByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(15L, result);
        verify(productRepository).countByEnterpriseIdAndState(ENTERPRISE_ID, true);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay productos activados")
    void testCountActivatedByEnterpriseId_ReturnsZero() {
        // Arrange
        when(productRepository.countByEnterpriseIdAndState(ENTERPRISE_ID, true))
                .thenReturn(0L);

        // Act
        long result = productPersistenceAdapter.countActivatedByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de integración ====================

    @Test
    @DisplayName("Debe invocar mapper correctamente en todos los métodos que retornan productos")
    void testMapperInvocation_InAllReturnMethods() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productEntity));
        when(productRepository.findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(productEntity));
        when(productPersistenceMapper.toProduct(any(ProductEntity.class))).thenReturn(product);

        // Act
        productPersistenceAdapter.findById(PRODUCT_ID);
        productPersistenceAdapter.findByIdAndEnterpriseId(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        verify(productPersistenceMapper, times(2)).toProduct(any(ProductEntity.class));
    }
}
