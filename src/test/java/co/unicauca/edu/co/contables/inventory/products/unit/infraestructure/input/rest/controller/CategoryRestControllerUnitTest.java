package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller.CategoryRestController;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.CategoryCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.CategoryResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.ICategoryRestMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryRestControllerUnitTest {

    @Mock
    private ICategoryServicePort categoryServicePort;

    @Mock
    private ICategoryRestMapper categoryRestMapper;

    @InjectMocks
    private CategoryRestController categoryRestController;

    private Category category;
    private CategoryCreateRequest categoryCreateRequest;
    private CategoryResponse categoryResponse;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final Long CATEGORY_ID = 1L;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(CATEGORY_ID)
                .enterpriseId(ENTERPRISE_ID)
                .name("Electrónica")
                .state(true)
                .build();

        categoryCreateRequest = CategoryCreateRequest.builder()
                .enterpriseId(ENTERPRISE_ID)
                .name("Electrónica")
                .build();

        categoryResponse = CategoryResponse.builder()
                .id(CATEGORY_ID)
                .enterpriseId(ENTERPRISE_ID)
                .name("Electrónica")
                .state(true)
                .build();
    }

    // ==================== Tests de findById ====================

    @Test
    @DisplayName("Debe encontrar categoría por ID exitosamente")
    void testFindById_ReturnsCategory() {
        // Arrange
        when(categoryServicePort.findById(ENTERPRISE_ID, CATEGORY_ID)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryRestController.findById(ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        assertNotNull(result);
        assertEquals(CATEGORY_ID, result.getId());
        assertEquals(ENTERPRISE_ID, result.getEnterpriseId());
        assertEquals("Electrónica", result.getName());
        verify(categoryServicePort).findById(ENTERPRISE_ID, CATEGORY_ID);
        verify(categoryRestMapper).toCategoryResponse(category);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en findById")
    void testFindById_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(categoryServicePort.findById(ENTERPRISE_ID, CATEGORY_ID)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        categoryRestController.findById(ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        verify(categoryServicePort).findById(ENTERPRISE_ID, CATEGORY_ID);
    }

    // ==================== Tests de getCategoriesList sin búsqueda ====================

    @Test
    @DisplayName("Debe obtener lista paginada de categorías sin búsqueda")
    void testGetCategoriesList_WithoutSearch_ReturnsPaginatedList() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countAllCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(categoryServicePort).countAllCategoriesByEntId(ENTERPRISE_ID);
        verify(categoryServicePort).getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("Debe usar parámetros de paginación por defecto cuando no se proporcionan")
    void testGetCategoriesList_WithoutPaginationParams_UsesDefaults() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countAllCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(categoryServicePort).countAllCategoriesByEntId(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento personalizado en lista de categorías")
    void testGetCategoriesList_AppliesCustomSorting() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countAllCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("id"), eq("desc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "id", "desc", null);

        // Assert
        assertNotNull(result);
        verify(categoryServicePort).getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("id"), eq("desc"));
    }

    // ==================== Tests de getCategoriesList con búsqueda ====================

    @Test
    @DisplayName("Debe buscar categorías con término de búsqueda")
    void testGetCategoriesList_WithSearch_ReturnsFilteredList() {
        // Arrange
        String searchTerm = "Electrónica";
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countByEntIdAndSearch(ENTERPRISE_ID, searchTerm)).thenReturn(1L);
        when(categoryServicePort.findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(categoryServicePort).countByEntIdAndSearch(ENTERPRISE_ID, searchTerm);
        verify(categoryServicePort).findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), 
                anyInt(), anyInt(), eq("name"), eq("asc"));
        verify(categoryServicePort, never()).countAllCategoriesByEntId(anyString());
    }

    @Test
    @DisplayName("Debe ignorar búsqueda con string vacío")
    void testGetCategoriesList_WithEmptySearch_IgnoresSearch() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countAllCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", "   ");

        // Assert
        assertNotNull(result);
        verify(categoryServicePort).countAllCategoriesByEntId(ENTERPRISE_ID);
        verify(categoryServicePort, never()).countByEntIdAndSearch(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento en búsqueda de categorías")
    void testGetCategoriesList_WithSearch_AppliesSorting() {
        // Arrange
        String searchTerm = "Electrónica";
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countByEntIdAndSearch(ENTERPRISE_ID, searchTerm)).thenReturn(1L);
        when(categoryServicePort.findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), anyInt(), 
                anyInt(), eq("id"), eq("desc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "id", "desc", searchTerm);

        // Assert
        verify(categoryServicePort).findByEntIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), 
                anyInt(), anyInt(), eq("id"), eq("desc"));
    }

    // ==================== Tests de findActivate ====================

    @Test
    @DisplayName("Debe obtener lista de categorías activas")
    void testFindActivate_ReturnsActiveCategories() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countActiveCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllActiveCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.findActivate(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(categoryServicePort).countActiveCategoriesByEntId(ENTERPRISE_ID);
        verify(categoryServicePort).getAllActiveCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), 
                anyInt(), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("Debe usar paginación por defecto en categorías activas")
    void testFindActivate_WithoutPaginationParams_UsesDefaults() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countActiveCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllActiveCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(categoryServicePort).countActiveCategoriesByEntId(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe ordenar categorías activas por nombre ascendente")
    void testFindActivate_SortsByNameAscending() {
        // Arrange
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countActiveCategoriesByEntId(ENTERPRISE_ID)).thenReturn(1L);
        when(categoryServicePort.getAllActiveCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        categoryRestController.findActivate(ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        verify(categoryServicePort).getAllActiveCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), 
                anyInt(), eq("name"), eq("asc"));
    }

    // ==================== Tests de create ====================

    @Test
    @DisplayName("Debe crear categoría exitosamente")
    void testCreate_CreatesCategory() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.create(category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        ResponseEntity<CategoryResponse> result = categoryRestController.create(categoryCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(CATEGORY_ID, result.getBody().getId());
        assertEquals("Electrónica", result.getBody().getName());
        verify(categoryRestMapper).toCategory(categoryCreateRequest);
        verify(categoryServicePort).create(category);
        verify(categoryRestMapper).toCategoryResponse(category);
    }

    @Test
    @DisplayName("Debe retornar status 201 al crear categoría")
    void testCreate_ReturnsCreatedStatus() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.create(category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        ResponseEntity<CategoryResponse> result = categoryRestController.create(categoryCreateRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de crear")
    void testCreate_MapsRequestToDomain() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.create(category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        categoryRestController.create(categoryCreateRequest);

        // Assert
        verify(categoryRestMapper).toCategory(categoryCreateRequest);
        verify(categoryServicePort).create(category);
    }

    // ==================== Tests de update ====================

    @Test
    @DisplayName("Debe actualizar categoría exitosamente")
    void testUpdate_UpdatesCategory() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.update(ENTERPRISE_ID, CATEGORY_ID, category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryRestController.update(ENTERPRISE_ID, CATEGORY_ID, categoryCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(CATEGORY_ID, result.getId());
        assertEquals("Electrónica", result.getName());
        verify(categoryRestMapper).toCategory(categoryCreateRequest);
        verify(categoryServicePort).update(ENTERPRISE_ID, CATEGORY_ID, category);
        verify(categoryRestMapper).toCategoryResponse(category);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en update")
    void testUpdate_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.update(ENTERPRISE_ID, CATEGORY_ID, category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        categoryRestController.update(ENTERPRISE_ID, CATEGORY_ID, categoryCreateRequest);

        // Assert
        verify(categoryServicePort).update(ENTERPRISE_ID, CATEGORY_ID, category);
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de actualizar")
    void testUpdate_MapsRequestToDomain() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.update(ENTERPRISE_ID, CATEGORY_ID, category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        categoryRestController.update(ENTERPRISE_ID, CATEGORY_ID, categoryCreateRequest);

        // Assert
        verify(categoryRestMapper).toCategory(categoryCreateRequest);
    }

    // ==================== Tests de changeState ====================

    @Test
    @DisplayName("Debe cambiar estado de categoría")
    void testChangeState_ChangesState() {
        // Arrange
        doNothing().when(categoryServicePort).changeState(ENTERPRISE_ID, CATEGORY_ID);

        // Act
        categoryRestController.changeState(ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        verify(categoryServicePort).changeState(ENTERPRISE_ID, CATEGORY_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en changeState")
    void testChangeState_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(categoryServicePort).changeState(ENTERPRISE_ID, CATEGORY_ID);

        // Act
        categoryRestController.changeState(ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        verify(categoryServicePort, times(1)).changeState(ENTERPRISE_ID, CATEGORY_ID);
    }

    // ==================== Tests de deleteById ====================

    @Test
    @DisplayName("Debe eliminar categoría por ID")
    void testDeleteById_DeletesCategory() {
        // Arrange
        doNothing().when(categoryServicePort).deleteById(ENTERPRISE_ID, CATEGORY_ID);

        // Act
        categoryRestController.deleteById(ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        verify(categoryServicePort).deleteById(ENTERPRISE_ID, CATEGORY_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en deleteById")
    void testDeleteById_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(categoryServicePort).deleteById(ENTERPRISE_ID, CATEGORY_ID);

        // Act
        categoryRestController.deleteById(ENTERPRISE_ID, CATEGORY_ID);

        // Assert
        verify(categoryServicePort, times(1)).deleteById(ENTERPRISE_ID, CATEGORY_ID);
    }

    // ==================== Tests de integración del controller ====================

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en create")
    void testCreate_MaintainsMapperConsistency() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.create(category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        categoryRestController.create(categoryCreateRequest);

        // Assert
        verify(categoryRestMapper).toCategory(categoryCreateRequest);
        verify(categoryRestMapper).toCategoryResponse(category);
    }

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en update")
    void testUpdate_MaintainsMapperConsistency() {
        // Arrange
        when(categoryRestMapper.toCategory(categoryCreateRequest)).thenReturn(category);
        when(categoryServicePort.update(ENTERPRISE_ID, CATEGORY_ID, category)).thenReturn(category);
        when(categoryRestMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        // Act
        categoryRestController.update(ENTERPRISE_ID, CATEGORY_ID, categoryCreateRequest);

        // Assert
        verify(categoryRestMapper).toCategory(categoryCreateRequest);
        verify(categoryRestMapper).toCategoryResponse(category);
    }

    @Test
    @DisplayName("Debe manejar lista vacía de categorías correctamente")
    void testGetCategoriesList_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<Category> emptyPage = Page.empty();
        
        when(categoryServicePort.countAllCategoriesByEntId(ENTERPRISE_ID)).thenReturn(0L);
        when(categoryServicePort.getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de categorías activas correctamente")
    void testFindActivate_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<Category> emptyPage = Page.empty();
        
        when(categoryServicePort.countActiveCategoriesByEntId(ENTERPRISE_ID)).thenReturn(0L);
        when(categoryServicePort.getAllActiveCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe mapear múltiples categorías en lista paginada")
    void testGetCategoriesList_MapsMultipleCategories() {
        // Arrange
        Category category2 = Category.builder()
                .id(2L)
                .enterpriseId(ENTERPRISE_ID)
                .name("Hogar")
                .state(true)
                .build();
        
        List<Category> categories = List.of(category, category2);
        Page<Category> categoryPage = new PageImpl<>(categories);
        
        when(categoryServicePort.countAllCategoriesByEntId(ENTERPRISE_ID)).thenReturn(2L);
        when(categoryServicePort.getAllCategoriesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(categoryPage);
        when(categoryRestMapper.toCategoryResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        ResponseEntity<Page<CategoryResponse>> result = categoryRestController.getCategoriesList(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getBody().getTotalElements());
        verify(categoryRestMapper, times(2)).toCategoryResponse(any(Category.class));
    }
}
