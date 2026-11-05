package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.CategoryPersistenceAdapter;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.ICategoryPersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.ICategoryRepository;
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
class CategoryPersistenceAdapterUnitTest {

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private ICategoryPersistenceMapper categoryPersistenceMapper;

    @InjectMocks
    private CategoryPersistenceAdapter categoryPersistenceAdapter;

    private Category category;
    private CategoryEntity categoryEntity;
    private static final Long CATEGORY_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String CATEGORY_NAME = "Electrónicos";
    private static final String DESCRIPTION = "Productos electrónicos";

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(CATEGORY_ID)
                .name(CATEGORY_NAME)
                .description(DESCRIPTION)
                .enterpriseId(ENTERPRISE_ID)
                .inventoryId(1L)
                .costId(2L)
                .saleId(3L)
                .returnId(4L)
                .taxes(List.of(1L, 2L))
                .state(true)
                .build();

        categoryEntity = new CategoryEntity();
        categoryEntity.setId(CATEGORY_ID);
        categoryEntity.setName(CATEGORY_NAME);
        categoryEntity.setDescription(DESCRIPTION);
        categoryEntity.setEnterpriseId(ENTERPRISE_ID);
        categoryEntity.setInventoryId(1L);
        categoryEntity.setCostId(2L);
        categoryEntity.setSaleId(3L);
        categoryEntity.setReturnId(4L);
        categoryEntity.setTaxes(List.of(1L, 2L));
        categoryEntity.setState(true);
    }

    // ==================== Tests de findByIdAndEnterpriseId ====================

    @Test
    @DisplayName("Debe encontrar categoría por ID y enterpriseId")
    void testFindByIdAndEnterpriseId_ReturnsCategory() {
        // Arrange
        when(categoryRepository.findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(categoryEntity));
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Optional<Category> result = categoryPersistenceAdapter.findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(CATEGORY_ID, result.get().getId());
        assertEquals(CATEGORY_NAME, result.get().getName());
        verify(categoryRepository).findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID);
        verify(categoryPersistenceMapper).toCategory(categoryEntity);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no encuentra categoría")
    void testFindByIdAndEnterpriseId_ReturnsEmpty() {
        // Arrange
        when(categoryRepository.findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID))
                .thenReturn(Optional.empty());

        // Act
        Optional<Category> result = categoryPersistenceAdapter.findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(categoryRepository).findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID);
        verify(categoryPersistenceMapper, never()).toCategory(any());
    }

    // ==================== Tests de create ====================

    @Test
    @DisplayName("Debe crear nueva categoría sin ID")
    void testCreate_WithoutId_CreatesNewCategory() {
        // Arrange
        Category newCategory = Category.builder()
                .name(CATEGORY_NAME)
                .description(DESCRIPTION)
                .enterpriseId(ENTERPRISE_ID)
                .taxes(List.of(1L, 2L))
                .state(true)
                .build();

        CategoryEntity newEntity = new CategoryEntity();
        newEntity.setName(CATEGORY_NAME);
        
        CategoryEntity savedEntity = new CategoryEntity();
        savedEntity.setId(CATEGORY_ID);
        savedEntity.setName(CATEGORY_NAME);

        when(categoryPersistenceMapper.toCategoryEntity(newCategory)).thenReturn(newEntity);
        when(categoryRepository.save(newEntity)).thenReturn(savedEntity);
        when(categoryPersistenceMapper.toCategory(savedEntity)).thenReturn(category);

        // Act
        Category result = categoryPersistenceAdapter.create(newCategory);

        // Assert
        assertNotNull(result);
        assertEquals(CATEGORY_ID, result.getId());
        verify(categoryRepository).save(newEntity);
        verify(categoryPersistenceMapper).toCategory(savedEntity);
    }

    @Test
    @DisplayName("Debe actualizar categoría existente con ID")
    void testCreate_WithId_UpdatesExistingCategory() {
        // Arrange
        CategoryEntity existingEntity = new CategoryEntity();
        existingEntity.setId(CATEGORY_ID);
        existingEntity.setName("Nombre Antiguo");
        existingEntity.setTaxes(List.of(5L));

        when(categoryPersistenceMapper.toCategoryEntity(category)).thenReturn(categoryEntity);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingEntity));
        when(categoryRepository.save(existingEntity)).thenReturn(existingEntity);
        when(categoryPersistenceMapper.toCategory(existingEntity)).thenReturn(category);

        // Act
        Category result = categoryPersistenceAdapter.create(category);

        // Assert
        assertNotNull(result);
        assertEquals(List.of(1L, 2L), existingEntity.getTaxes());
        verify(categoryRepository).findById(CATEGORY_ID);
        verify(categoryRepository).save(existingEntity);
    }

    @Test
    @DisplayName("Debe crear categoría cuando ID existe pero no se encuentra en BD")
    void testCreate_WithIdNotFound_CreatesNewCategory() {
        // Arrange
        when(categoryPersistenceMapper.toCategoryEntity(category)).thenReturn(categoryEntity);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Category result = categoryPersistenceAdapter.create(category);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findById(CATEGORY_ID);
        verify(categoryRepository).save(categoryEntity);
    }

    // ==================== Tests de deleteById ====================

    @Test
    @DisplayName("Debe eliminar categoría por ID")
    void testDeleteById_DeletesCategory() {
        // Arrange
        doNothing().when(categoryRepository).deleteById(CATEGORY_ID);

        // Act
        categoryPersistenceAdapter.deleteById(CATEGORY_ID);

        // Assert
        verify(categoryRepository).deleteById(CATEGORY_ID);
    }

    // ==================== Tests de existsByNameAndEnterpriseId ====================

    @Test
    @DisplayName("Debe retornar true cuando existe categoría con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsTrue() {
        // Arrange
        when(categoryRepository.existsByNameAndEnterpriseId(CATEGORY_NAME, ENTERPRISE_ID))
                .thenReturn(true);

        // Act
        boolean result = categoryPersistenceAdapter.existsByNameAndEnterpriseId(CATEGORY_NAME, ENTERPRISE_ID);

        // Assert
        assertTrue(result);
        verify(categoryRepository).existsByNameAndEnterpriseId(CATEGORY_NAME, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe categoría con nombre y enterpriseId")
    void testExistsByNameAndEnterpriseId_ReturnsFalse() {
        // Arrange
        when(categoryRepository.existsByNameAndEnterpriseId(CATEGORY_NAME, ENTERPRISE_ID))
                .thenReturn(false);

        // Act
        boolean result = categoryPersistenceAdapter.existsByNameAndEnterpriseId(CATEGORY_NAME, ENTERPRISE_ID);

        // Assert
        assertFalse(result);
        verify(categoryRepository).existsByNameAndEnterpriseId(CATEGORY_NAME, ENTERPRISE_ID);
    }

    // ==================== Tests de existsByNameAndEnterpriseIdAndIdNot ====================

    @Test
    @DisplayName("Debe retornar true cuando existe otra categoría con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsTrue() {
        // Arrange
        when(categoryRepository.existsByNameAndEnterpriseIdAndIdNot(CATEGORY_NAME, ENTERPRISE_ID, CATEGORY_ID))
                .thenReturn(true);

        // Act
        boolean result = categoryPersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                CATEGORY_NAME, ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        assertTrue(result);
        verify(categoryRepository).existsByNameAndEnterpriseIdAndIdNot(CATEGORY_NAME, ENTERPRISE_ID, CATEGORY_ID);
    }

    @Test
    @DisplayName("Debe retornar false cuando no existe otra categoría con mismo nombre")
    void testExistsByNameAndEnterpriseIdAndIdNot_ReturnsFalse() {
        // Arrange
        when(categoryRepository.existsByNameAndEnterpriseIdAndIdNot(CATEGORY_NAME, ENTERPRISE_ID, CATEGORY_ID))
                .thenReturn(false);

        // Act
        boolean result = categoryPersistenceAdapter.existsByNameAndEnterpriseIdAndIdNot(
                CATEGORY_NAME, ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        assertFalse(result);
        verify(categoryRepository).existsByNameAndEnterpriseIdAndIdNot(CATEGORY_NAME, ENTERPRISE_ID, CATEGORY_ID);
    }

    // ==================== Tests de getAllCategoriesBy ====================

    @Test
    @DisplayName("Debe obtener todas las categorías paginadas")
    void testGetAllCategoriesBy_ReturnsPaginatedCategories() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.getCategoriesBy(ENTERPRISE_ID, pageable)).thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesBy(ENTERPRISE_ID, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(CATEGORY_NAME, result.getContent().get(0).getName());
        verify(categoryRepository).getCategoriesBy(ENTERPRISE_ID, pageable);
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay categorías")
    void testGetAllCategoriesBy_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(categoryRepository.getCategoriesBy(ENTERPRISE_ID, pageable)).thenReturn(emptyPage);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesBy(ENTERPRISE_ID, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== Tests de getAllCategoriesByState ====================

    @Test
    @DisplayName("Debe obtener categorías activas paginadas")
    void testGetAllCategoriesByState_ReturnsActiveCategories() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.getCategoriesByEnterpriseIdAndState(ENTERPRISE_ID, true, pageable))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesByState(ENTERPRISE_ID, true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
        verify(categoryRepository).getCategoriesByEnterpriseIdAndState(ENTERPRISE_ID, true, pageable);
    }

    @Test
    @DisplayName("Debe obtener categorías inactivas paginadas")
    void testGetAllCategoriesByState_ReturnsInactiveCategories() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        categoryEntity.setState(false);
        category = Category.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .enterpriseId(category.getEnterpriseId())
                .inventoryId(category.getInventoryId())
                .costId(category.getCostId())
                .saleId(category.getSaleId())
                .returnId(category.getReturnId())
                .taxes(category.getTaxes())
                .state(false)
                .build();
        
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.getCategoriesByEnterpriseIdAndState(ENTERPRISE_ID, false, pageable))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesByState(ENTERPRISE_ID, false, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).isState());
    }

    // ==================== Tests de findByEnterpriseIdAndSearch ====================

    @Test
    @DisplayName("Debe buscar categorías por texto")
    void testFindByEnterpriseIdAndSearch_ReturnsMatchingCategories() {
        // Arrange
        String searchTerm = "Elec";
        Pageable pageable = PageRequest.of(0, 10);
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.findByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm, pageable))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.findByEnterpriseIdAndSearch(
                ENTERPRISE_ID, searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository).findByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm, pageable);
    }

    // ==================== Tests de countByEnterpriseIdAndSearch ====================

    @Test
    @DisplayName("Debe contar categorías por búsqueda")
    void testCountByEnterpriseIdAndSearch_ReturnsCount() {
        // Arrange
        String searchTerm = "Elec";
        when(categoryRepository.countByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm))
                .thenReturn(5L);

        // Act
        long result = categoryPersistenceAdapter.countByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm);

        // Assert
        assertEquals(5L, result);
        verify(categoryRepository).countByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm);
    }

    // ==================== Tests de getAllCategoriesByWithSort ====================

    @Test
    @DisplayName("Debe obtener categorías ordenadas por nombre ascendente")
    void testGetAllCategoriesByWithSort_OrdersByNameAsc() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository).getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener categorías ordenadas por descripción descendente")
    void testGetAllCategoriesByWithSort_OrdersByDescriptionDesc() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("description").descending());
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesByWithSort(
                ENTERPRISE_ID, 0, 10, "description", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe usar ordenamiento por defecto para campo desconocido")
    void testGetAllCategoriesByWithSort_UsesDefaultSortForUnknownField() {
        // Arrange
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 1);

        when(categoryRepository.getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getAllCategoriesByWithSort(
                ENTERPRISE_ID, 0, 10, "invalidField", "asc");

        // Assert
        assertNotNull(result);
        verify(categoryRepository).getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    // ==================== Tests de countByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar categorías por enterpriseId")
    void testCountByEnterpriseId_ReturnsCount() {
        // Arrange
        when(categoryRepository.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(10L);

        // Act
        long result = categoryPersistenceAdapter.countByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(10L, result);
        verify(categoryRepository).countByEnterpriseId(ENTERPRISE_ID);
    }

    // ==================== Tests de getActiveCategoriesBy ====================

    @Test
    @DisplayName("Debe obtener categorías activas ordenadas")
    void testGetActiveCategoriesBy_ReturnsActiveSortedCategories() {
        // Arrange
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 1);

        when(categoryRepository.getActiveCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getActiveCategoriesBy(
                ENTERPRISE_ID, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository).getActiveCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener categorías activas ordenadas por estado descendente")
    void testGetActiveCategoriesBy_OrdersByStateDesc() {
        // Arrange
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 1);

        when(categoryRepository.getActiveCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act
        Page<Category> result = categoryPersistenceAdapter.getActiveCategoriesBy(
                ENTERPRISE_ID, 0, 10, "state", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ==================== Tests de countActiveByEnterpriseId ====================

    @Test
    @DisplayName("Debe contar categorías activas por enterpriseId")
    void testCountActiveByEnterpriseId_ReturnsCount() {
        // Arrange
        when(categoryRepository.countActiveByEnterpriseId(ENTERPRISE_ID)).thenReturn(8L);

        // Act
        long result = categoryPersistenceAdapter.countActiveByEnterpriseId(ENTERPRISE_ID);

        // Assert
        assertEquals(8L, result);
        verify(categoryRepository).countActiveByEnterpriseId(ENTERPRISE_ID);
    }

    // ==================== Tests de mapeo de campos de ordenamiento ====================

    @Test
    @DisplayName("Debe mapear correctamente todos los campos de ordenamiento válidos")
    void testSortFieldMapping_MapsAllValidFields() {
        // Arrange
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 1);

        when(categoryRepository.getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(categoryEntity)).thenReturn(category);

        // Act & Assert - name
        Page<Category> result1 = categoryPersistenceAdapter.getAllCategoriesByWithSort(
                ENTERPRISE_ID, 0, 10, "name", "asc");
        assertNotNull(result1);

        // Act & Assert - description
        Page<Category> result2 = categoryPersistenceAdapter.getAllCategoriesByWithSort(
                ENTERPRISE_ID, 0, 10, "description", "asc");
        assertNotNull(result2);

        // Act & Assert - state
        Page<Category> result3 = categoryPersistenceAdapter.getAllCategoriesByWithSort(
                ENTERPRISE_ID, 0, 10, "state", "asc");
        assertNotNull(result3);

        verify(categoryRepository, times(3)).getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    // ==================== Tests de integración de métodos ====================

    @Test
    @DisplayName("Debe invocar mapper en todos los métodos que retornan categorías")
    void testMapperInvocation_InAllReturnMethods() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CategoryEntity> entities = List.of(categoryEntity);
        Page<CategoryEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(categoryRepository.findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID))
                .thenReturn(Optional.of(categoryEntity));
        when(categoryRepository.getCategoriesBy(eq(ENTERPRISE_ID), any(Pageable.class)))
                .thenReturn(entityPage);
        when(categoryPersistenceMapper.toCategory(any(CategoryEntity.class))).thenReturn(category);

        // Act
        categoryPersistenceAdapter.findByIdAndEnterpriseId(CATEGORY_ID, ENTERPRISE_ID);
        categoryPersistenceAdapter.getAllCategoriesBy(ENTERPRISE_ID, pageable);

        // Assert
        verify(categoryPersistenceMapper, atLeast(2)).toCategory(any(CategoryEntity.class));
    }
}
