package co.unicauca.edu.co.contables.inventory.products.unit.application.service.category;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.category.CategoryService;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceUnitTest {

    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private String enterpriseId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        categoryId = 1L;

        category = new Category();
        category.setId(categoryId);
        category.setName("Electrónicos");
        category.setDescription("Productos electrónicos");
        category.setEnterpriseId(enterpriseId);
        category.setInventoryId(100L);
        category.setCostId(200L);
        category.setSaleId(300L);
        category.setReturnId(400L);
        category.setState(true);
    }

    @Test
    @DisplayName("Debe encontrar categoría por ID y empresa exitosamente")
    void testFindByIdSuccess() {
        // Arrange
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.findById(enterpriseId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals(enterpriseId, result.getEnterpriseId());
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando categoría no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, 
                () -> categoryService.findById(enterpriseId, categoryId));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
    }

    @Test
    @DisplayName("Debe crear categoría con nombre normalizado exitosamente")
    void testCreateSuccess() {
        // Arrange
        category.setName("  Electrónicos  ");
        when(categoryPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(false);
        when(categoryPersistencePort.create(any(Category.class)))
                .thenReturn(category);

        // Act
        Category result = categoryService.create(category);

        // Assert
        assertNotNull(result);
        assertNotEquals("  Electrónicos  ", result.getName());
        verify(categoryPersistencePort).existsByNameAndEnterpriseId(anyString(), eq(enterpriseId));
        verify(categoryPersistencePort).create(any(Category.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando nombre de categoría ya existe al crear")
    void testCreateWithDuplicateName() {
        // Arrange
        when(categoryPersistencePort.existsByNameAndEnterpriseId(anyString(), eq(enterpriseId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(CategoryNameAlreadyExistsException.class, 
                () -> categoryService.create(category));
        verify(categoryPersistencePort).existsByNameAndEnterpriseId(anyString(), eq(enterpriseId));
        verify(categoryPersistencePort, never()).create(any(Category.class));
    }

    @Test
    @DisplayName("Debe actualizar categoría exitosamente")
    void testUpdateSuccess() {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Nombre Anterior");
        existingCategory.setEnterpriseId(enterpriseId);

        Category updateData = new Category();
        updateData.setName("Nuevo Nombre");
        updateData.setDescription("Nueva descripción");
        updateData.setEnterpriseId(enterpriseId);

        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(existingCategory));
        when(productService.findAllByCategory(categoryId))
                .thenReturn(Collections.emptyList());
        when(categoryPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(categoryId)))
                .thenReturn(false);
        when(categoryPersistencePort.create(any(Category.class)))
                .thenReturn(existingCategory);

        // Act
        Category result = categoryService.update(enterpriseId, categoryId, updateData);

        // Assert
        assertNotNull(result);
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(productService).findAllByCategory(categoryId);
        verify(categoryPersistencePort).existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(categoryId));
        verify(categoryPersistencePort).create(any(Category.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar categoría no encontrada")
    void testUpdateNotFound() {
        // Arrange
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, 
                () -> categoryService.update(enterpriseId, categoryId, category));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar categoría con productos en uso")
    void testUpdateWithProductsInUse() {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setEnterpriseId(enterpriseId);

        Product productInUse = new Product();
        productInUse.setUsageCount(5);

        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(existingCategory));
        when(productService.findAllByCategory(categoryId))
                .thenReturn(List.of(productInUse));

        // Act & Assert
        assertThrows(CategoryInUseException.class, 
                () -> categoryService.update(enterpriseId, categoryId, category));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(productService).findAllByCategory(categoryId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar con nombre duplicado")
    void testUpdateWithDuplicateName() {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setEnterpriseId(enterpriseId);

        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(existingCategory));
        when(productService.findAllByCategory(categoryId))
                .thenReturn(Collections.emptyList());
        when(categoryPersistencePort.existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(categoryId)))
                .thenReturn(true);

        // Act & Assert
        assertThrows(CategoryNameAlreadyExistsException.class, 
                () -> categoryService.update(enterpriseId, categoryId, category));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(productService).findAllByCategory(categoryId);
        verify(categoryPersistencePort).existsByNameAndEnterpriseIdAndIdNot(anyString(), eq(enterpriseId), eq(categoryId));
    }

    @Test
    @DisplayName("Debe cambiar estado de categoría activa a inactiva")
    void testChangeStateFromActiveToInactive() {
        // Arrange
        category.setState(true);
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(category));
        when(categoryPersistencePort.create(any(Category.class)))
                .thenReturn(category);

        // Act
        categoryService.changeState(enterpriseId, categoryId);

        // Assert
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(categoryPersistencePort).create(argThat(cat -> !cat.isState()));
    }

    @Test
    @DisplayName("Debe cambiar estado de categoría inactiva a activa")
    void testChangeStateFromInactiveToActive() {
        // Arrange
        category.setState(false);
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(category));
        when(categoryPersistencePort.create(any(Category.class)))
                .thenReturn(category);

        // Act
        categoryService.changeState(enterpriseId, categoryId);

        // Assert
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(categoryPersistencePort).create(argThat(Category::isState));
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar estado de categoría no encontrada")
    void testChangeStateNotFound() {
        // Arrange
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, 
                () -> categoryService.changeState(enterpriseId, categoryId));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
    }

    @Test
    @DisplayName("Debe eliminar categoría sin productos asociados exitosamente")
    void testDeleteByIdSuccess() {
        // Arrange
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productService.findAllByCategory(categoryId))
                .thenReturn(Collections.emptyList());
        doNothing().when(categoryPersistencePort).deleteById(categoryId);

        // Act
        categoryService.deleteById(enterpriseId, categoryId);

        // Assert
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(productService).findAllByCategory(categoryId);
        verify(categoryPersistencePort).deleteById(categoryId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar categoría no encontrada")
    void testDeleteByIdNotFound() {
        // Arrange
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, 
                () -> categoryService.deleteById(enterpriseId, categoryId));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(categoryPersistencePort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar categoría con productos asociados")
    void testDeleteByIdWithAssociatedProducts() {
        // Arrange
        Product product = new Product();
        when(categoryPersistencePort.findByIdAndEnterpriseId(categoryId, enterpriseId))
                .thenReturn(Optional.of(category));
        when(productService.findAllByCategory(categoryId))
                .thenReturn(List.of(product));

        // Act & Assert
        assertThrows(CategoryAssociatedException.class, 
                () -> categoryService.deleteById(enterpriseId, categoryId));
        verify(categoryPersistencePort).findByIdAndEnterpriseId(categoryId, enterpriseId);
        verify(productService).findAllByCategory(categoryId);
        verify(categoryPersistencePort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe obtener todas las categorías paginadas")
    void testGetAllCategoriesBy() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryPersistencePort.getAllCategoriesBy(enterpriseId, pageable))
                .thenReturn(categoryPage);

        // Act
        Page<Category> result = categoryService.getAllCategoriesBy(enterpriseId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryPersistencePort).getAllCategoriesBy(enterpriseId, pageable);
    }

    @Test
    @DisplayName("Debe contar todas las categorías de una empresa")
    void testCountAllCategoriesByEntId() {
        // Arrange
        long expectedCount = 5L;
        when(categoryPersistencePort.countByEnterpriseId(enterpriseId))
                .thenReturn(expectedCount);

        // Act
        long result = categoryService.countAllCategoriesByEntId(enterpriseId);

        // Assert
        assertEquals(expectedCount, result);
        verify(categoryPersistencePort).countByEnterpriseId(enterpriseId);
    }

    @Test
    @DisplayName("Debe buscar categorías por empresa y término de búsqueda")
    void testFindByEntIdAndSearch() {
        // Arrange
        String search = "Electr";
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryPersistencePort.findByEnterpriseIdAndSearch(eq(enterpriseId), eq(search), any(Pageable.class)))
                .thenReturn(categoryPage);

        // Act
        Page<Category> result = categoryService.findByEntIdAndSearch(enterpriseId, search, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryPersistencePort).findByEnterpriseIdAndSearch(eq(enterpriseId), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe contar categorías por empresa y búsqueda")
    void testCountByEntIdAndSearch() {
        // Arrange
        String search = "Electr";
        long expectedCount = 3L;
        when(categoryPersistencePort.countByEnterpriseIdAndSearch(enterpriseId, search))
                .thenReturn(expectedCount);

        // Act
        long result = categoryService.countByEntIdAndSearch(enterpriseId, search);

        // Assert
        assertEquals(expectedCount, result);
        verify(categoryPersistencePort).countByEnterpriseIdAndSearch(enterpriseId, search);
    }

    @Test
    @DisplayName("Debe obtener categorías con ordenamiento personalizado")
    void testGetAllCategoriesByWithSort() {
        // Arrange
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryPersistencePort.getAllCategoriesByWithSort(enterpriseId, 0, 10, "name", "asc"))
                .thenReturn(categoryPage);

        // Act
        Page<Category> result = categoryService.getAllCategoriesByWithSort(enterpriseId, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(categoryPersistencePort).getAllCategoriesByWithSort(enterpriseId, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe obtener solo categorías activas con ordenamiento")
    void testGetAllActiveCategoriesByWithSort() {
        // Arrange
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryPersistencePort.getActiveCategoriesBy(enterpriseId, 0, 10, "name", "asc"))
                .thenReturn(categoryPage);

        // Act
        Page<Category> result = categoryService.getAllActiveCategoriesByWithSort(enterpriseId, 0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isState());
        verify(categoryPersistencePort).getActiveCategoriesBy(enterpriseId, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("Debe contar categorías activas por empresa")
    void testCountActiveCategoriesByEntId() {
        // Arrange
        long expectedCount = 4L;
        when(categoryPersistencePort.countActiveByEnterpriseId(enterpriseId))
                .thenReturn(expectedCount);

        // Act
        long result = categoryService.countActiveCategoriesByEntId(enterpriseId);

        // Assert
        assertEquals(expectedCount, result);
        verify(categoryPersistencePort).countActiveByEnterpriseId(enterpriseId);
    }
}
