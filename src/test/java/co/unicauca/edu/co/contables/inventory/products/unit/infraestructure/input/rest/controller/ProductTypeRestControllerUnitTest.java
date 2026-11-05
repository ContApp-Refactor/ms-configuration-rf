package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller.ProductTypeRestController;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductTypeRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ProductTypeResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.IProductTypeRestMapper;
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
class ProductTypeRestControllerUnitTest {

    @Mock
    private IProductTypeServicePort productTypeService;

    @Mock
    private IProductTypeRestMapper productTypeMapper;

    @InjectMocks
    private ProductTypeRestController productTypeRestController;

    private ProductType productType;
    private ProductTypeRequest productTypeRequest;
    private ProductTypeResponse productTypeResponse;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final Long PRODUCT_TYPE_ID = 1L;

    @BeforeEach
    void setUp() {
        productType = ProductType.builder()
                .id(PRODUCT_TYPE_ID)
                .enterpriseId(ENTERPRISE_ID)
                .name("Electrónicos")
                .state(true)
                .build();

        productTypeRequest = ProductTypeRequest.builder()
                .enterpriseId(ENTERPRISE_ID)
                .name("Electrónicos")
                .build();

        productTypeResponse = new ProductTypeResponse();
        productTypeResponse.setId(PRODUCT_TYPE_ID);
        productTypeResponse.setEnterpriseId(ENTERPRISE_ID);
        productTypeResponse.setName("Electrónicos");
        productTypeResponse.setState(true);
    }

    // ==================== Tests de createProductType ====================

    @Test
    @DisplayName("Debe crear tipo de producto exitosamente")
    void testCreateProductType_CreatesProductType() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.createProductType(productType)).thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<ProductTypeResponse> result = productTypeRestController.createProductType(productTypeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(PRODUCT_TYPE_ID, result.getBody().getId());
        assertEquals("Electrónicos", result.getBody().getName());
        verify(productTypeMapper).toProductType(productTypeRequest);
        verify(productTypeService).createProductType(productType);
        verify(productTypeMapper).toProductTypeResponse(productType);
    }

    @Test
    @DisplayName("Debe retornar status 201 al crear tipo de producto")
    void testCreateProductType_ReturnsCreatedStatus() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.createProductType(productType)).thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<ProductTypeResponse> result = productTypeRestController.createProductType(productTypeRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de crear")
    void testCreateProductType_MapsRequestToDomain() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.createProductType(productType)).thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.createProductType(productTypeRequest);

        // Assert
        verify(productTypeMapper).toProductType(productTypeRequest);
        verify(productTypeService).createProductType(productType);
    }

    // ==================== Tests de findActivate ====================

    @Test
    @DisplayName("Debe obtener tipos de producto activos paginados")
    void testFindActivate_ReturnsActivatedProductTypes() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countActivatedByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.findActivatedWithPagination(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findActivate(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productTypeService).countActivatedByEnterpriseId(ENTERPRISE_ID);
        verify(productTypeService).findActivatedWithPagination(eq(ENTERPRISE_ID), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe usar paginación por defecto en findActivate")
    void testFindActivate_WithoutParams_UsesDefaults() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countActivatedByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.findActivatedWithPagination(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(productTypeService).countActivatedByEnterpriseId(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe mapear tipos de producto a response en findActivate")
    void testFindActivate_MapsProductTypesToResponse() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countActivatedByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.findActivatedWithPagination(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.findActivate(ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        verify(productTypeMapper).toProductTypeResponse(any(ProductType.class));
    }

    // ==================== Tests de findAll sin búsqueda ====================

    @Test
    @DisplayName("Debe obtener lista paginada de tipos de producto sin búsqueda")
    void testFindAll_WithoutSearch_ReturnsPaginatedList() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findAll(
                ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productTypeService).countByEnterpriseId(ENTERPRISE_ID);
        verify(productTypeService).getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"));
        verify(productTypeService, never()).countByEnterpriseIdAndSearch(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento personalizado en findAll")
    void testFindAll_AppliesCustomSorting() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("id"), eq("desc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.findAll(ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "id", "desc");

        // Assert
        verify(productTypeService).getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("id"), eq("desc"));
    }

    @Test
    @DisplayName("Debe usar parámetros por defecto en findAll")
    void testFindAll_WithoutParams_UsesDefaults() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findAll(
                ENTERPRISE_ID, null, Optional.empty(), Optional.empty(), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // ==================== Tests de findAll con búsqueda ====================

    @Test
    @DisplayName("Debe buscar tipos de producto con término de búsqueda")
    void testFindAll_WithSearch_ReturnsFilteredList() {
        // Arrange
        String searchTerm = "Electrónicos";
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm)).thenReturn(1L);
        when(productTypeService.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), anyInt(), 
                anyInt(), eq("name"), eq("asc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findAll(
                ENTERPRISE_ID, searchTerm, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productTypeService).countByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm);
        verify(productTypeService).findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), 
                anyInt(), anyInt(), eq("name"), eq("asc"));
        verify(productTypeService, never()).countByEnterpriseId(anyString());
    }

    @Test
    @DisplayName("Debe ignorar búsqueda con string vacío")
    void testFindAll_WithEmptySearch_IgnoresSearch() {
        // Arrange
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(1L);
        when(productTypeService.getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findAll(
                ENTERPRISE_ID, "   ", Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        verify(productTypeService).countByEnterpriseId(ENTERPRISE_ID);
        verify(productTypeService, never()).countByEnterpriseIdAndSearch(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento en búsqueda de tipos de producto")
    void testFindAll_WithSearch_AppliesSorting() {
        // Arrange
        String searchTerm = "Electrónicos";
        List<ProductType> productTypes = List.of(productType);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseIdAndSearch(ENTERPRISE_ID, searchTerm)).thenReturn(1L);
        when(productTypeService.findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), anyInt(), 
                anyInt(), eq("id"), eq("desc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.findAll(ENTERPRISE_ID, searchTerm, Optional.of(0), Optional.of(10), "id", "desc");

        // Assert
        verify(productTypeService).findByEnterpriseIdAndSearch(eq(ENTERPRISE_ID), eq(searchTerm), 
                anyInt(), anyInt(), eq("id"), eq("desc"));
    }

    // ==================== Tests de getProductTypeById ====================

    @Test
    @DisplayName("Debe obtener tipo de producto por ID")
    void testGetProductTypeById_ReturnsProductType() {
        // Arrange
        when(productTypeService.getProductTypeByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<ProductTypeResponse> result = productTypeRestController.getProductTypeById(
                PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(PRODUCT_TYPE_ID, result.getBody().getId());
        assertEquals("Electrónicos", result.getBody().getName());
        verify(productTypeService).getProductTypeByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);
        verify(productTypeMapper).toProductTypeResponse(productType);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en getProductTypeById")
    void testGetProductTypeById_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(productTypeService.getProductTypeByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.getProductTypeById(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(productTypeService, times(1)).getProductTypeByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de updateProductType ====================

    @Test
    @DisplayName("Debe actualizar tipo de producto exitosamente")
    void testUpdateProductType_UpdatesProductType() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<ProductTypeResponse> result = productTypeRestController.updateProductType(
                PRODUCT_TYPE_ID, productTypeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(PRODUCT_TYPE_ID, result.getBody().getId());
        assertEquals("Electrónicos", result.getBody().getName());
        verify(productTypeMapper).toProductType(productTypeRequest);
        verify(productTypeService).updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType);
        verify(productTypeMapper).toProductTypeResponse(productType);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en updateProductType")
    void testUpdateProductType_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.updateProductType(PRODUCT_TYPE_ID, productTypeRequest);

        // Assert
        verify(productTypeService).updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType);
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de actualizar")
    void testUpdateProductType_MapsRequestToDomain() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.updateProductType(PRODUCT_TYPE_ID, productTypeRequest);

        // Assert
        verify(productTypeMapper).toProductType(productTypeRequest);
    }

    // ==================== Tests de changeState ====================

    @Test
    @DisplayName("Debe cambiar estado de tipo de producto")
    void testChangeState_ChangesState() {
        // Arrange
        doNothing().when(productTypeService).changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Act
        productTypeRestController.changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(productTypeService).changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en changeState")
    void testChangeState_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(productTypeService).changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Act
        productTypeRestController.changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(productTypeService, times(1)).changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de deleteProductType ====================

    @Test
    @DisplayName("Debe eliminar tipo de producto exitosamente")
    void testDeleteProductType_DeletesProductType() {
        // Arrange
        doNothing().when(productTypeService).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Act
        ResponseEntity<Void> result = productTypeRestController.deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(productTypeService).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar status 204 al eliminar tipo de producto")
    void testDeleteProductType_ReturnsNoContentStatus() {
        // Arrange
        doNothing().when(productTypeService).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Act
        ResponseEntity<Void> result = productTypeRestController.deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en deleteProductType")
    void testDeleteProductType_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(productTypeService).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Act
        productTypeRestController.deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(productTypeService, times(1)).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de integración del controller ====================

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en create")
    void testCreateProductType_MaintainsMapperConsistency() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.createProductType(productType)).thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.createProductType(productTypeRequest);

        // Assert
        verify(productTypeMapper).toProductType(productTypeRequest);
        verify(productTypeMapper).toProductTypeResponse(productType);
    }

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en update")
    void testUpdateProductType_MaintainsMapperConsistency() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);

        // Act
        productTypeRestController.updateProductType(PRODUCT_TYPE_ID, productTypeRequest);

        // Assert
        verify(productTypeMapper).toProductType(productTypeRequest);
        verify(productTypeMapper).toProductTypeResponse(productType);
    }

    @Test
    @DisplayName("Debe manejar lista vacía de tipos de producto correctamente")
    void testFindAll_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<ProductType> emptyPage = Page.empty();
        
        when(productTypeService.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(0L);
        when(productTypeService.getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findAll(
                ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de tipos activos correctamente")
    void testFindActivate_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<ProductType> emptyPage = Page.empty();
        
        when(productTypeService.countActivatedByEnterpriseId(ENTERPRISE_ID)).thenReturn(0L);
        when(productTypeService.findActivatedWithPagination(eq(ENTERPRISE_ID), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe mapear múltiples tipos de producto en lista paginada")
    void testFindAll_MapsMultipleProductTypes() {
        // Arrange
        ProductType productType2 = ProductType.builder()
                .id(2L)
                .enterpriseId(ENTERPRISE_ID)
                .name("Alimentos")
                .state(true)
                .build();
        
        List<ProductType> productTypes = List.of(productType, productType2);
        Page<ProductType> productTypePage = new PageImpl<>(productTypes);
        
        when(productTypeService.countByEnterpriseId(ENTERPRISE_ID)).thenReturn(2L);
        when(productTypeService.getAllProductTypesByWithSort(eq(ENTERPRISE_ID), anyInt(), anyInt(), 
                eq("name"), eq("asc"))).thenReturn(productTypePage);
        when(productTypeMapper.toProductTypeResponse(any(ProductType.class))).thenReturn(productTypeResponse);

        // Act
        ResponseEntity<Page<ProductTypeResponse>> result = productTypeRestController.findAll(
                ENTERPRISE_ID, null, Optional.of(0), Optional.of(10), "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getBody().getTotalElements());
        verify(productTypeMapper, times(2)).toProductTypeResponse(any(ProductType.class));
    }

    @Test
    @DisplayName("Debe validar que todos los endpoints usen enterpriseId")
    void testAllEndpoints_UseEnterpriseId() {
        // Arrange
        when(productTypeMapper.toProductType(productTypeRequest)).thenReturn(productType);
        when(productTypeService.createProductType(productType)).thenReturn(productType);
        when(productTypeService.getProductTypeByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID))
                .thenReturn(productType);
        when(productTypeService.updateProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID, productType))
                .thenReturn(productType);
        when(productTypeMapper.toProductTypeResponse(productType)).thenReturn(productTypeResponse);
        doNothing().when(productTypeService).changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);
        doNothing().when(productTypeService).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        // Act & Assert
        productTypeRestController.createProductType(productTypeRequest);
        verify(productTypeService).createProductType(argThat(pt -> pt.getEnterpriseId().equals(ENTERPRISE_ID)));

        productTypeRestController.getProductTypeById(PRODUCT_TYPE_ID, ENTERPRISE_ID);
        verify(productTypeService).getProductTypeByIdAndEnterpriseId(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        productTypeRestController.updateProductType(PRODUCT_TYPE_ID, productTypeRequest);
        verify(productTypeService).updateProductType(eq(PRODUCT_TYPE_ID), eq(ENTERPRISE_ID), any(ProductType.class));

        productTypeRestController.changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);
        verify(productTypeService).changeState(PRODUCT_TYPE_ID, ENTERPRISE_ID);

        productTypeRestController.deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);
        verify(productTypeService).deleteProductType(PRODUCT_TYPE_ID, ENTERPRISE_ID);
    }
}
