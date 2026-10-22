package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.ProductTypePersistenceAdapter;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.IProductTypePersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductTypeRepository;
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
class ProductTypePersistenceAdapterUnitTest {

    @Mock
    private IProductTypeRepository productTypeRepository;

    @Mock
    private IProductTypePersistenceMapper productTypePersistenceMapper;

    @InjectMocks
    private ProductTypePersistenceAdapter productTypePersistenceAdapter;

    private ProductType productType;
    private ProductTypeEntity productTypeEntity;
    private static final Long PRODUCT_TYPE_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String PRODUCT_TYPE_NAME = "Electrónico";

    @BeforeEach
    void setUp() {
        productType = ProductType.builder()
                .id(PRODUCT_TYPE_ID)
                .name(PRODUCT_TYPE_NAME)
                .enterpriseId(ENTERPRISE_ID)
                .state(true)
                .build();

        productTypeEntity = new ProductTypeEntity();
        productTypeEntity.setId(PRODUCT_TYPE_ID);
        productTypeEntity.setName(PRODUCT_TYPE_NAME);
        productTypeEntity.setEnterpriseId(ENTERPRISE_ID);
        productTypeEntity.setState(true);
    }

    // ==================== Tests de save ====================

    @Test
    @DisplayName("Debe guardar tipo de producto correctamente")
    void testSave_SavesProductType() {
        // Arrange
        when(productTypePersistenceMapper.toProductTypeEntity(productType)).thenReturn(productTypeEntity);
        when(productTypeRepository.save(productTypeEntity)).thenReturn(productTypeEntity);
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        ProductType result = productTypePersistenceAdapter.save(productType);

        // Assert
        assertNotNull(result);
        assertEquals(PRODUCT_TYPE_ID, result.getId());
        assertEquals(PRODUCT_TYPE_NAME, result.getName());
        verify(productTypeRepository).save(productTypeEntity);
        verify(productTypePersistenceMapper).toProductTypeEntity(productType);
        verify(productTypePersistenceMapper).toProductType(productTypeEntity);
    }

    // ==================== Tests de update ====================

    @Test
    @DisplayName("Debe actualizar tipo de producto correctamente")
    void testUpdate_UpdatesProductType() {
        // Arrange
        ProductType updatedProductType = ProductType.builder()
                .name("Electrónico Actualizado")
                .enterpriseId(ENTERPRISE_ID)
                .state(true)
                .build();

        ProductTypeEntity updatedEntity = new ProductTypeEntity();
        updatedEntity.setId(PRODUCT_TYPE_ID);
        updatedEntity.setName("Electrónico Actualizado");
        updatedEntity.setEnterpriseId(ENTERPRISE_ID);
        updatedEntity.setState(true);

        when(productTypePersistenceMapper.toProductTypeEntity(any(ProductType.class))).thenReturn(updatedEntity);
        when(productTypeRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(productTypePersistenceMapper.toProductType(updatedEntity)).thenReturn(updatedProductType);

        // Act
        ProductType result = productTypePersistenceAdapter.update(PRODUCT_TYPE_ID, updatedProductType);

        // Assert
        assertNotNull(result);
        assertEquals("Electrónico Actualizado", result.getName());
        verify(productTypeRepository).save(any(ProductTypeEntity.class));
    }

    @Test
    @DisplayName("Debe asignar ID correctamente al actualizar")
    void testUpdate_AssignsIdCorrectly() {
        // Arrange
        ProductType productTypeWithoutId = ProductType.builder()
                .name(PRODUCT_TYPE_NAME)
                .enterpriseId(ENTERPRISE_ID)
                .state(true)
                .build();

        when(productTypePersistenceMapper.toProductTypeEntity(any(ProductType.class))).thenReturn(productTypeEntity);
        when(productTypeRepository.save(any(ProductTypeEntity.class))).thenReturn(productTypeEntity);
        when(productTypePersistenceMapper.toProductType(any(ProductTypeEntity.class))).thenReturn(productType);

        // Act
        ProductType result = productTypePersistenceAdapter.update(PRODUCT_TYPE_ID, productTypeWithoutId);

        // Assert
        assertNotNull(result);
        verify(productTypeRepository).save(any(ProductTypeEntity.class));
    }

    // ==================== Tests de delete ====================

    @Test
    @DisplayName("Debe eliminar tipo de producto por ID")
    void testDelete_DeletesProductType() {
        // Arrange
        doNothing().when(productTypeRepository).deleteById(PRODUCT_TYPE_ID);

        // Act
        productTypePersistenceAdapter.delete(PRODUCT_TYPE_ID);

        // Assert
        verify(productTypeRepository).deleteById(PRODUCT_TYPE_ID);
    }

    // ==================== Tests de findById ====================

    @Test
    @DisplayName("Debe encontrar tipo de producto por ID")
    void testFindById_ReturnsProductType() {
        // Arrange
        when(productTypeRepository.findById(PRODUCT_TYPE_ID)).thenReturn(Optional.of(productTypeEntity));
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Optional<ProductType> result = productTypePersistenceAdapter.findById(PRODUCT_TYPE_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(PRODUCT_TYPE_ID, result.get().getId());
        assertEquals(PRODUCT_TYPE_NAME, result.get().getName());
        verify(productTypeRepository).findById(PRODUCT_TYPE_ID);
        verify(productTypePersistenceMapper).toProductType(productTypeEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no encuentra tipo de producto por ID")
    void testFindById_ReturnsEmpty() {
        // Arrange
        when(productTypeRepository.findById(PRODUCT_TYPE_ID)).thenReturn(Optional.empty());

        // Act
        Optional<ProductType> result = productTypePersistenceAdapter.findById(PRODUCT_TYPE_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(productTypeRepository).findById(PRODUCT_TYPE_ID);
        verify(productTypePersistenceMapper, never()).toProductType(any());
    }

    // ==================== Tests de findByIdAndEnterpriseId ====================

    @Test
    @DisplayName("Debe encontrar tipo de producto por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsProductType() {
        // Arrange
        when(productTypeRepository.findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(productTypeEntity));
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Optional<ProductType> result = productTypePersistenceAdapter.findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(PRODUCT_TYPE_ID, result.get().getId());
        assertEquals(ENTERPRISE_ID, result.get().getEnterpriseId());
        verify(productTypeRepository).findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no encuentra tipo de producto por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsEmpty() {
        // Arrange
        when(productTypeRepository.findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID))
                .thenReturn(Optional.empty());

        // Act
        Optional<ProductType> result = productTypePersistenceAdapter.findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(productTypeRepository).findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);
        verify(productTypePersistenceMapper, never()).toProductType(any());
    }

    // ==================== Tests de existsByNameAndEnterpriseId ====================

    @Test
    @DisplayName("Debe retornar true cuando existe tipo de producto con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsTrue() {
        // Arrange
        when(productTypeRepository.existsByNameAndEnterpriseId(PRODUCT_TYPE_NAME, ENTERPRISE_ID))
                .thenReturn(true);

        // Act
        boolean result = productTypePersistenceAdapter.existsByNameAndEnterpriseId(PRODUCT_TYPE_NAME, ENTERPRISE_ID);

        // Assert
        assertTrue(result);
        verify(productTypeRepository).existsByNameAndEnterpriseId(PRODUCT_TYPE_NAME, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe tipo de producto con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsFalse() {
        // Arrange
        when(productTypeRepository.existsByNameAndEnterpriseId(PRODUCT_TYPE_NAME, ENTERPRISE_ID))
                .thenReturn(false);

        // Act
        boolean result = productTypePersistenceAdapter.existsByNameAndEnterpriseId(PRODUCT_TYPE_NAME, ENTERPRISE_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de existsByNameAndEnterpriseIdAndIdNot ====================

    @Test
    @DisplayName("Debe retornar true cuando existe otro tipo de producto con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsTrue() {
        // Arrange
        when(productTypeRepository.existsByNameAndEnterpriseIdAndIdNot(PRODUCT_TYPE_NAME, ENTERPRISE_ID, PRODUCT_TYPE_ID))
                .thenReturn(true);

        // Act
        boolean result = productTypePersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                PRODUCT_TYPE_NAME, ENTERPRISE_ID, PRODUCT_TYPE_ID);

        // Assert
        assertTrue(result);
        verify(productTypeRepository).existsByNameAndEnterpriseIdAndIdNot(PRODUCT_TYPE_NAME, ENTERPRISE_ID, PRODUCT_TYPE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe otro tipo de producto con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsFalse() {
        // Arrange
        when(productTypeRepository.existsByNameAndEnterpriseIdAndIdNot(PRODUCT_TYPE_NAME, ENTERPRISE_ID, PRODUCT_TYPE_ID))
                .thenReturn(false);

        // Act
        boolean result = productTypePersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                PRODUCT_TYPE_NAME, ENTERPRISE_ID, PRODUCT_TYPE_ID);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests de getAllProductTypesByWithSort ====================

    @Test
    @DisplayName("Debe obtener tipos de producto ordenados ascendente")
    void testGetAllProductTypesByWithSort_OrderAsc_ReturnsProductTypes() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductTypeEntity> entities = List.of(productTypeEntity);
        Page<ProductTypeEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productTypeRepository.getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.getAllProductTypesByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(PRODUCT_TYPE_NAME, result.getContent().get(0).getName());
        verify(productTypeRepository).getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe obtener tipos de producto ordenados descendente")
    void testGetAllProductTypesByWithSort_OrderDesc_ReturnsProductTypes() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").descending());
        List<ProductTypeEntity> entities = List.of(productTypeEntity);
        Page<ProductTypeEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productTypeRepository.getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.getAllProductTypesByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe usar ordenamiento por defecto cuando campo es null")
    void testGetAllProductTypesByWithSort_NullSortField_UsesDefault() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductTypeEntity> entityPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productTypeRepository.getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(entityPage);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.getAllProductTypesByWithSort(
                ENTERPRISE_ID, 0, 10, null, "asc");

        // Assert
        assertNotNull(result);
        verify(productTypeRepository).getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe usar ordenamiento por defecto cuando campo es vacío")
    void testGetAllProductTypesByWithSort_EmptySortField_UsesDefault() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductTypeEntity> entityPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productTypeRepository.getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(entityPage);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.getAllProductTypesByWithSort(
                ENTERPRISE_ID, 0, 10, "", "asc");

        // Assert
        assertNotNull(result);
        verify(productTypeRepository).getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe usar ordenamiento por defecto cuando campo es inválido")
    void testGetAllProductTypesByWithSort_InvalidSortField_UsesDefault() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductTypeEntity> entityPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productTypeRepository.getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(entityPage);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.getAllProductTypesByWithSort(
                ENTERPRISE_ID, 0, 10, "invalidField", "asc");

        // Assert
        assertNotNull(result);
        verify(productTypeRepository).getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay tipos de producto")
    void testGetAllProductTypesByWithSort_ReturnsEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductTypeEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productTypeRepository.getProductTypesBy(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.getAllProductTypesByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de findByEnterpriseIdAndSearch ====================

    @Test
    @DisplayName("Debe buscar tipos de producto con filtro ascendente")
    void testFindByEnterpriseIdAndSearch_OrderAsc_ReturnsProductTypes() {
        // Arrange
        String search = "Electr";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductTypeEntity> entities = List.of(productTypeEntity);
        Page<ProductTypeEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productTypeRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productTypeRepository).findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe buscar tipos de producto con filtro descendente")
    void testFindByEnterpriseIdAndSearch_OrderDesc_ReturnsProductTypes() {
        // Arrange
        String search = "Electr";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").descending());
        List<ProductTypeEntity> entities = List.of(productTypeEntity);
        Page<ProductTypeEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productTypeRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "name", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando búsqueda no encuentra resultados")
    void testFindByEnterpriseIdAndSearch_ReturnsEmptyPage() {
        // Arrange
        String search = "NoExiste";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductTypeEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productTypeRepository.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(search), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countByEnterpriseIdAndSearch ====================

    @Test
    @DisplayName("Debe contar tipos de producto con búsqueda")
    void testCountByEnterpriseIdAndSearch_ReturnsCount() {
        // Arrange
        String search = "Electr";
        when(productTypeRepository.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search))
                .thenReturn(5L);

        // Act
        long result = productTypePersistenceAdapter.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search);

        // Assert
        assertEquals(5L, result);
        verify(productTypeRepository).countByEnterpriseIdAndSearch(ENTERPRISE_ID, search);
    }

    @Test
    @DisplayName("Debe retornar cero cuando búsqueda no encuentra resultados")
    void testCountByEnterpriseIdAndSearch_ReturnsZero() {
        // Arrange
        String search = "NoExiste";
        when(productTypeRepository.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search))
                .thenReturn(0L);

        // Act
        long result = productTypePersistenceAdapter.countByEnterpriseIdAndSearch(ENTERPRISE_ID, search);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de countByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar tipos de producto por enterpriseId")
    void testCountByEnterpriseId_ReturnsCount() {
        // Arrange
        when(productTypeRepository.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(10L);

        // Act
        long result = productTypePersistenceAdapter.countByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(10L, result);
        verify(productTypeRepository).countByEnterpriseId(ENTERPRISE_ID);
    }

    // ==================== Tests de findActivatedByEnterpriseId ====================

    @Test
    @DisplayName("Debe encontrar tipos de producto activados")
    void testFindActivatedByEnterpriseId_ReturnsActivatedProductTypes() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<ProductTypeEntity> entities = List.of(productTypeEntity);
        Page<ProductTypeEntity> entityPage = new PageImpl<>(entities, pageRequest, 1);

        when(productTypeRepository.findActivatedByEnterpriseId(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(entityPage);
        when(productTypePersistenceMapper.toProductType(productTypeEntity)).thenReturn(productType);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.findActivatedByEnterpriseId(ENTERPRISE_ID, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
        verify(productTypeRepository).findActivatedByEnterpriseId(eq(ENTERPRISE_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay tipos activados")
    void testFindActivatedByEnterpriseId_ReturnsEmptyPage() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ProductTypeEntity> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(productTypeRepository.findActivatedByEnterpriseId(eq(ENTERPRISE_ID), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        Page<ProductType> result = productTypePersistenceAdapter.findActivatedByEnterpriseId(ENTERPRISE_ID, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de countActivatedByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar tipos de producto activados")
    void testCountActivatedByEnterpriseId_ReturnsCount() {
        // Arrange
        when(productTypeRepository.countActivatedByEnterpriseId(ENTERPRISE_ID)).thenReturn(7L);

        // Act
        long result = productTypePersistenceAdapter.countActivatedByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(7L, result);
        verify(productTypeRepository).countActivatedByEnterpriseId(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay tipos activados")
    void testCountActivatedByEnterpriseId_ReturnsZero() {
        // Arrange
        when(productTypeRepository.countActivatedByEnterpriseId(ENTERPRISE_ID)).thenReturn(0L);

        // Act
        long result = productTypePersistenceAdapter.countActivatedByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(0L, result);
    }

    // ==================== Tests de integración ====================

    @Test
    @DisplayName("Debe invocar mapper correctamente en métodos de consulta")
    void testMapperInvocation_InQueryMethods() {
        // Arrange
        when(productTypeRepository.findById(PRODUCT_TYPE_ID)).thenReturn(Optional.of(productTypeEntity));
        when(productTypeRepository.findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(productTypeEntity));
        when(productTypePersistenceMapper.toProductType(any(ProductTypeEntity.class))).thenReturn(productType);

        // Act
        productTypePersistenceAdapter.findById(PRODUCT_TYPE_ID);
        productTypePersistenceAdapter.findByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(productTypePersistenceMapper, times(2)).toProductType(any(ProductTypeEntity.class));
    }

    @Test
    @DisplayName("Debe invocar mapper correctamente en métodos de escritura")
    void testMapperInvocation_InWriteMethods() {
        // Arrange
        when(productTypePersistenceMapper.toProductTypeEntity(any(ProductType.class))).thenReturn(productTypeEntity);
        when(productTypeRepository.save(any(ProductTypeEntity.class))).thenReturn(productTypeEntity);
        when(productTypePersistenceMapper.toProductType(any(ProductTypeEntity.class))).thenReturn(productType);

        // Act
        productTypePersistenceAdapter.save(productType);
        productTypePersistenceAdapter.update(PRODUCT_TYPE_ID, productType);

        // Assert
        verify(productTypePersistenceMapper, times(2)).toProductTypeEntity(any(ProductType.class));
        verify(productTypePersistenceMapper, times(2)).toProductType(any(ProductTypeEntity.class));
    }
}
