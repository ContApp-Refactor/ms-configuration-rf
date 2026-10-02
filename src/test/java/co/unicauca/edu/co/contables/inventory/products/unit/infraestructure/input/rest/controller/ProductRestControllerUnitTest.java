package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductExportUseCase;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductImportUseCase;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller.ProductRestController;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductExportRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductImportRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ProductResponse;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces.IProductRestMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.ExcelFileNameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductRestControllerUnitTest {

    @Mock
    private IProductServicePort productServicePort;

    @Mock
    private IProductRestMapper productRestMapper;

    @Mock
    private IProductExportUseCase productExportUseCase;

    @Mock
    private IProductImportUseCase productImportUseCase;

    @Mock
    private ExcelFileNameGenerator fileNameGenerator;

    @InjectMocks
    private ProductRestController productRestController;

    private Product product;
    private ProductCreateRequest productCreateRequest;
    private ProductResponse productResponse;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final Long PRODUCT_ID = 1L;
    private static final String JOB_ID = "job-123";

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(PRODUCT_ID)
                .enterpriseId(ENTERPRISE_ID)
                .name("Laptop HP")
                .code("LAP001")
                .state(true)
                .build();

        productCreateRequest = ProductCreateRequest.builder()
                .enterpriseId(ENTERPRISE_ID)
                .name("Laptop HP")
                .code("LAP001")
                .build();

        productResponse = ProductResponse.builder()
                .id(PRODUCT_ID)
                .enterpriseId(ENTERPRISE_ID)
                .name("Laptop HP")
                .code("LAP001")
                .state(true)
                .build();
    }

    // ==================== Tests de findAll ====================

    @Test
    @DisplayName("Debe obtener lista paginada de productos")
    void testFindAll_ReturnsPaginatedList() {
        // Arrange
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("name"), eq("asc"), any())).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findAll(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productServicePort).findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("name"), eq("asc"), any());
    }

    @Test
    @DisplayName("Debe aplicar búsqueda en lista de productos")
    void testFindAll_WithSearch_AppliesSearchFilter() {
        // Arrange
        String searchTerm = "Laptop";
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("name"), eq("asc"), eq(Optional.of(searchTerm)))).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findAll(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBody().getTotalElements());
        verify(productServicePort).findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("name"), eq("asc"), eq(Optional.of(searchTerm)));
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento personalizado en findAll")
    void testFindAll_AppliesCustomSorting() {
        // Arrange
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("code"), eq("desc"), any())).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        productRestController.findAll(ENTERPRISE_ID, Optional.of(0), Optional.of(10), "code", "desc", null);

        // Assert
        verify(productServicePort).findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("code"), eq("desc"), any());
    }

    @Test
    @DisplayName("Debe usar parámetros por defecto en findAll")
    void testFindAll_WithoutParams_UsesDefaults() {
        // Arrange
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findAllPaginated(eq(ENTERPRISE_ID), eq(Optional.empty()), 
                eq(Optional.empty()), eq("name"), eq("asc"), any())).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findAll(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // ==================== Tests de findById ====================

    @Test
    @DisplayName("Debe encontrar producto por ID")
    void testFindById_ReturnsProduct() {
        // Arrange
        when(productServicePort.findById(PRODUCT_ID, ENTERPRISE_ID)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productRestController.findById(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(PRODUCT_ID, result.getId());
        assertEquals(ENTERPRISE_ID, result.getEnterpriseId());
        assertEquals("Laptop HP", result.getName());
        verify(productServicePort).findById(PRODUCT_ID, ENTERPRISE_ID);
        verify(productRestMapper).toProductResponse(product);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en findById")
    void testFindById_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(productServicePort.findById(PRODUCT_ID, ENTERPRISE_ID)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        productRestController.findById(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        verify(productServicePort, times(1)).findById(PRODUCT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de findActivate ====================

    @Test
    @DisplayName("Debe obtener productos activos paginados")
    void testFindActivate_ReturnsActivatedProducts() {
        // Arrange
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findActivatedPaginated(eq(ENTERPRISE_ID), any(), 
                any())).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findActivate(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10));

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productServicePort).findActivatedPaginated(eq(ENTERPRISE_ID), any(), 
                any());
    }

    @Test
    @DisplayName("Debe usar paginación por defecto en findActivate")
    void testFindActivate_WithoutParams_UsesDefaults() {
        // Arrange
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findActivatedPaginated(eq(ENTERPRISE_ID), eq(Optional.empty()), 
                eq(Optional.empty()))).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findActivate(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // ==================== Tests de create ====================

    @Test
    @DisplayName("Debe crear producto exitosamente")
    void testCreate_CreatesProduct() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.create(product)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        ResponseEntity<ProductResponse> result = productRestController.create(productCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(PRODUCT_ID, result.getBody().getId());
        assertEquals("Laptop HP", result.getBody().getName());
        verify(productRestMapper).toProduct(productCreateRequest);
        verify(productServicePort).create(product);
        verify(productRestMapper).toProductResponse(product);
    }

    @Test
    @DisplayName("Debe retornar status 201 al crear producto")
    void testCreate_ReturnsCreatedStatus() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.create(product)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        ResponseEntity<ProductResponse> result = productRestController.create(productCreateRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear request a domain antes de crear")
    void testCreate_MapsRequestToDomain() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.create(product)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        productRestController.create(productCreateRequest);

        // Assert
        verify(productRestMapper).toProduct(productCreateRequest);
        verify(productServicePort).create(product);
    }

    // ==================== Tests de update ====================

    @Test
    @DisplayName("Debe actualizar producto exitosamente")
    void testUpdate_UpdatesProduct() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.update(PRODUCT_ID, product, ENTERPRISE_ID)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productRestController.update(PRODUCT_ID, productCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(PRODUCT_ID, result.getId());
        assertEquals("Laptop HP", result.getName());
        verify(productRestMapper).toProduct(productCreateRequest);
        verify(productServicePort).update(PRODUCT_ID, product, ENTERPRISE_ID);
        verify(productRestMapper).toProductResponse(product);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en update")
    void testUpdate_InvokesServiceWithCorrectParameters() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.update(PRODUCT_ID, product, ENTERPRISE_ID)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        productRestController.update(PRODUCT_ID, productCreateRequest);

        // Assert
        verify(productServicePort).update(PRODUCT_ID, product, ENTERPRISE_ID);
    }

    // ==================== Tests de changeState ====================

    @Test
    @DisplayName("Debe cambiar estado de producto")
    void testChangeState_ChangesState() {
        // Arrange
        doNothing().when(productServicePort).changeState(PRODUCT_ID, ENTERPRISE_ID);

        // Act
        productRestController.changeState(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        verify(productServicePort).changeState(PRODUCT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en changeState")
    void testChangeState_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(productServicePort).changeState(PRODUCT_ID, ENTERPRISE_ID);

        // Act
        productRestController.changeState(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        verify(productServicePort, times(1)).changeState(PRODUCT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de deleteById ====================

    @Test
    @DisplayName("Debe eliminar producto por ID")
    void testDeleteById_DeletesProduct() {
        // Arrange
        doNothing().when(productServicePort).deleteById(PRODUCT_ID, ENTERPRISE_ID);

        // Act
        productRestController.deleteById(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        verify(productServicePort).deleteById(PRODUCT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en deleteById")
    void testDeleteById_InvokesServiceWithCorrectParameters() {
        // Arrange
        doNothing().when(productServicePort).deleteById(PRODUCT_ID, ENTERPRISE_ID);

        // Act
        productRestController.deleteById(PRODUCT_ID, ENTERPRISE_ID);

        // Assert
        verify(productServicePort, times(1)).deleteById(PRODUCT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de exportProductTemplate ====================

    @Test
    @DisplayName("Debe exportar plantilla de productos")
    void testExportProductTemplate_ReturnsTemplate() {
        // Arrange
        Resource mockResource = new ByteArrayResource(new byte[0]);
        String templateFileName = "plantilla_productos.xlsx";
        
        when(productExportUseCase.exportProductTemplateWithValidations(ENTERPRISE_ID)).thenReturn(mockResource);
        when(fileNameGenerator.generateTemplateFileName()).thenReturn(templateFileName);

        // Act
        ResponseEntity<Resource> result = productRestController.exportProductTemplate(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().getContentDisposition().toString().contains(templateFileName));
        verify(productExportUseCase).exportProductTemplateWithValidations(ENTERPRISE_ID);
        verify(fileNameGenerator).generateTemplateFileName();
    }

    @Test
    @DisplayName("Debe configurar headers correctos para plantilla Excel")
    void testExportProductTemplate_SetsCorrectHeaders() {
        // Arrange
        Resource mockResource = new ByteArrayResource(new byte[0]);
        String templateFileName = "plantilla_productos.xlsx";
        
        when(productExportUseCase.exportProductTemplateWithValidations(ENTERPRISE_ID)).thenReturn(mockResource);
        when(fileNameGenerator.generateTemplateFileName()).thenReturn(templateFileName);

        // Act
        ResponseEntity<Resource> result = productRestController.exportProductTemplate(ENTERPRISE_ID);

        // Assert
        assertTrue(result.getHeaders().getContentDisposition().toString().contains("attachment"));
        assertNotNull(result.getHeaders().getContentType());
    }

    // ==================== Tests de exportProductsAsync ====================

    @Test
    @DisplayName("Debe iniciar exportación asíncrona de productos")
    void testExportProductsAsync_StartsExport() {
        // Arrange
        when(productExportUseCase.exportProductsAsync(any(ProductExportRequest.class))).thenReturn(JOB_ID);

        // Act
        ResponseEntity<Map<String, String>> result = productRestController.exportProductsAsync(
                ENTERPRISE_ID, "Mi Empresa", true);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(JOB_ID, result.getBody().get("jobId"));
        assertEquals("Exportación iniciada correctamente", result.getBody().get("message"));
        assertEquals("PENDING", result.getBody().get("status"));
        verify(productExportUseCase).exportProductsAsync(any(ProductExportRequest.class));
    }

    @Test
    @DisplayName("Debe crear request con parámetros opcionales en exportación")
    void testExportProductsAsync_WithOptionalParams_CreatesRequest() {
        // Arrange
        when(productExportUseCase.exportProductsAsync(any(ProductExportRequest.class))).thenReturn(JOB_ID);

        // Act
        productRestController.exportProductsAsync(ENTERPRISE_ID, null, null);

        // Assert
        verify(productExportUseCase).exportProductsAsync(any(ProductExportRequest.class));
    }

    @Test
    @DisplayName("Debe retornar status 202 al iniciar exportación")
    void testExportProductsAsync_ReturnsAcceptedStatus() {
        // Arrange
        when(productExportUseCase.exportProductsAsync(any(ProductExportRequest.class))).thenReturn(JOB_ID);

        // Act
        ResponseEntity<Map<String, String>> result = productRestController.exportProductsAsync(
                ENTERPRISE_ID, "Mi Empresa", true);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
    }

    // ==================== Tests de getExportStatus ====================

    @Test
    @DisplayName("Debe obtener estado de exportación existente")
    void testGetExportStatus_ReturnsStatus() {
        // Arrange
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.PROCESSING)
                .build();
        
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.of(jobStatus));

        // Act
        ResponseEntity<?> result = productRestController.getExportStatus(JOB_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(productExportUseCase).getExportStatus(JOB_ID);
    }

    @Test
    @DisplayName("Debe retornar 404 cuando exportación no existe")
    void testGetExportStatus_WhenNotFound_Returns404() {
        // Arrange
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = productRestController.getExportStatus(JOB_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(productExportUseCase).getExportStatus(JOB_ID);
    }

    // ==================== Tests de downloadExportedFile ====================

    @Test
    @DisplayName("Debe descargar archivo exportado completado")
    void testDownloadExportedFile_WhenCompleted_ReturnsFile() {
        // Arrange
        byte[] fileData = "test data".getBytes();
        String fileName = "productos_export.xlsx";
        
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.COMPLETED)
                .fileData(fileData)
                .fileName(fileName)
                .build();
        
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.of(jobStatus));

        // Act
        ResponseEntity<Resource> result = productRestController.downloadExportedFile(JOB_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().getContentDisposition().toString().contains(fileName));
        verify(productExportUseCase).getExportStatus(JOB_ID);
    }

    @Test
    @DisplayName("Debe retornar 404 cuando exportación no existe para descarga")
    void testDownloadExportedFile_WhenNotFound_Returns404() {
        // Arrange
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Resource> result = productRestController.downloadExportedFile(JOB_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 400 cuando exportación no está completada")
    void testDownloadExportedFile_WhenNotCompleted_Returns400() {
        // Arrange
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.PROCESSING)
                .build();
        
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.of(jobStatus));

        // Act
        ResponseEntity<Resource> result = productRestController.downloadExportedFile(JOB_ID);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar 500 cuando no hay datos de archivo")
    void testDownloadExportedFile_WhenNoFileData_Returns500() {
        // Arrange
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.COMPLETED)
                .fileData(null)
                .build();
        
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.of(jobStatus));

        // Act
        ResponseEntity<Resource> result = productRestController.downloadExportedFile(JOB_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    // ==================== Tests de importProductsFromExcel ====================

    @Test
    @DisplayName("Debe iniciar importación asíncrona desde Excel")
    void testImportProductsFromExcel_StartsImport() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("excelFile", "productos.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test data".getBytes());
        
        when(productImportUseCase.importProductsAsync(any(ProductImportRequest.class))).thenReturn(JOB_ID);

        // Act
        ResponseEntity<Map<String, String>> result = productRestController.importProductsFromExcel(
                ENTERPRISE_ID, file);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(JOB_ID, result.getBody().get("jobId"));
        assertEquals("Importación iniciada correctamente", result.getBody().get("message"));
        assertEquals("PENDING", result.getBody().get("status"));
        verify(productImportUseCase).importProductsAsync(any(ProductImportRequest.class));
    }

    @Test
    @DisplayName("Debe retornar status 202 al iniciar importación")
    void testImportProductsFromExcel_ReturnsAcceptedStatus() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("excelFile", "productos.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test data".getBytes());
        
        when(productImportUseCase.importProductsAsync(any(ProductImportRequest.class))).thenReturn(JOB_ID);

        // Act
        ResponseEntity<Map<String, String>> result = productRestController.importProductsFromExcel(
                ENTERPRISE_ID, file);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe crear request desde archivo en importación")
    void testImportProductsFromExcel_CreatesRequestFromFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("excelFile", "productos.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test data".getBytes());
        
        when(productImportUseCase.importProductsAsync(any(ProductImportRequest.class))).thenReturn(JOB_ID);

        // Act
        productRestController.importProductsFromExcel(ENTERPRISE_ID, file);

        // Assert
        verify(productImportUseCase).importProductsAsync(any(ProductImportRequest.class));
    }

    // ==================== Tests de getImportStatus ====================

    @Test
    @DisplayName("Debe obtener estado de importación existente")
    void testGetImportStatus_ReturnsStatus() {
        // Arrange
        ImportJobStatus jobStatus = ImportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.PROCESSING)
                .build();
        
        when(productImportUseCase.getImportStatus(JOB_ID)).thenReturn(Optional.of(jobStatus));

        // Act
        ResponseEntity<?> result = productRestController.getImportStatus(JOB_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(productImportUseCase).getImportStatus(JOB_ID);
    }

    @Test
    @DisplayName("Debe retornar 404 cuando importación no existe")
    void testGetImportStatus_WhenNotFound_Returns404() {
        // Arrange
        when(productImportUseCase.getImportStatus(JOB_ID)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = productRestController.getImportStatus(JOB_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(productImportUseCase).getImportStatus(JOB_ID);
    }

    // ==================== Tests de integración del controller ====================

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en create")
    void testCreate_MaintainsMapperConsistency() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.create(product)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        productRestController.create(productCreateRequest);

        // Assert
        verify(productRestMapper).toProduct(productCreateRequest);
        verify(productRestMapper).toProductResponse(product);
    }

    @Test
    @DisplayName("Debe mantener consistencia entre mappers en update")
    void testUpdate_MaintainsMapperConsistency() {
        // Arrange
        when(productRestMapper.toProduct(productCreateRequest)).thenReturn(product);
        when(productServicePort.update(PRODUCT_ID, product, ENTERPRISE_ID)).thenReturn(product);
        when(productRestMapper.toProductResponse(product)).thenReturn(productResponse);

        // Act
        productRestController.update(PRODUCT_ID, productCreateRequest);

        // Assert
        verify(productRestMapper).toProduct(productCreateRequest);
        verify(productRestMapper).toProductResponse(product);
    }

    @Test
    @DisplayName("Debe manejar lista vacía de productos correctamente")
    void testFindAll_WithEmptyList_ReturnsEmptyPage() {
        // Arrange
        Page<Product> emptyPage = Page.empty();
        
        when(productServicePort.findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("name"), eq("asc"), any())).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findAll(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Debe manejar múltiples productos en lista paginada")
    void testFindAll_MapsMultipleProducts() {
        // Arrange
        Product product2 = Product.builder()
                .id(2L)
                .enterpriseId(ENTERPRISE_ID)
                .name("Mouse Logitech")
                .code("MOU001")
                .state(true)
                .build();
        
        List<Product> products = List.of(product, product2);
        Page<Product> productPage = new PageImpl<>(products);
        
        when(productServicePort.findAllPaginated(eq(ENTERPRISE_ID), any(), 
                any(), eq("name"), eq("asc"), any())).thenReturn(productPage);
        when(productRestMapper.toProductResponse(any(Product.class))).thenReturn(productResponse);

        // Act
        ResponseEntity<Page<ProductResponse>> result = productRestController.findAll(
                ENTERPRISE_ID, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getBody().getTotalElements());
        verify(productRestMapper, times(2)).toProductResponse(any(Product.class));
    }

    @Test
    @DisplayName("Debe validar todos los estados posibles de exportación")
    void testDownloadExportedFile_ValidatesAllStatuses() {
        // Arrange
        ExportJobStatus pendingStatus = ExportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.PENDING)
                .build();
        
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.of(pendingStatus));

        // Act
        ResponseEntity<Resource> result = productRestController.downloadExportedFile(JOB_ID);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Debe configurar headers correctos para descarga de Excel")
    void testDownloadExportedFile_SetsCorrectHeaders() {
        // Arrange
        byte[] fileData = "test data".getBytes();
        String fileName = "productos_export.xlsx";
        
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(JOB_ID)
                .status(ImportStatus.COMPLETED)
                .fileData(fileData)
                .fileName(fileName)
                .build();
        
        when(productExportUseCase.getExportStatus(JOB_ID)).thenReturn(Optional.of(jobStatus));

        // Act
        ResponseEntity<Resource> result = productRestController.downloadExportedFile(JOB_ID);

        // Assert
        assertTrue(result.getHeaders().getContentDisposition().toString().contains("attachment"));
        assertTrue(result.getHeaders().getContentDisposition().toString().contains(fileName));
        assertNotNull(result.getHeaders().getContentType());
    }
}
