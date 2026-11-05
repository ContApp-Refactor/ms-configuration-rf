package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductAsyncExportProcessor;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExcelValidationService;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExportJobTracker;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductExportException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductExportRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductAsyncExportProcessorUnitTest {

    @Mock
    private IProductPersistencePort productPersistencePort;

    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @Mock
    private IProductTypePersistencePort productTypePersistencePort;

    @Mock
    private IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

    @Mock
    private ProductExportJobTracker jobTracker;

    @Mock
    private ProductExcelValidationService excelValidationService;

    @InjectMocks
    private ProductAsyncExportProcessor asyncExportProcessor;

    private String testEntId;
    private String testJobId;
    private ProductExportRequest exportRequest;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        testJobId = "JOB123";
        exportRequest = ProductExportRequest.builder()
                .entId(testEntId)
                .companyName("Test Company")
                .status(null)
                .build();
    }

    @Test
    @DisplayName("Debe procesar exportación exitosa con productos activos e inactivos")
    void testProcessExportAsyncSuccessful() {
        List<Product> products = createTestProducts(10);
        Page<Product> page = new PageImpl<>(products);

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page);
        when(categoryPersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateTotalRecords(testJobId, 10);
        verify(jobTracker).setFileData(eq(testJobId), any(byte[].class));
        verify(jobTracker, atLeastOnce()).updateProgress(eq(testJobId), anyInt());
    }

    @Test
    @DisplayName("Debe exportar solo productos activos cuando status es true")
    void testProcessExportAsyncOnlyActiveProducts() {
        exportRequest.setStatus(true);
        List<Product> activeProducts = createTestProducts(5);
        Page<Product> page = new PageImpl<>(activeProducts);

        when(productPersistencePort.findByEnterpriseIdAndState(eq(testEntId), eq(true), eq(0), anyInt()))
                .thenReturn(page);
        when(categoryPersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(productPersistencePort).findByEnterpriseIdAndState(eq(testEntId), eq(true), eq(0), anyInt());
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateTotalRecords(testJobId, 5);
    }

    @Test
    @DisplayName("Debe exportar solo productos inactivos cuando status es false")
    void testProcessExportAsyncOnlyInactiveProducts() {
        exportRequest.setStatus(false);
        List<Product> inactiveProducts = createTestProducts(3);
        Page<Product> page = new PageImpl<>(inactiveProducts);

        when(productPersistencePort.findByEnterpriseIdAndState(eq(testEntId), eq(false), eq(0), anyInt()))
                .thenReturn(page);
        when(categoryPersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(productPersistencePort).findByEnterpriseIdAndState(eq(testEntId), eq(false), eq(0), anyInt());
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateTotalRecords(testJobId, 3);
    }

    @Test
    @DisplayName("Debe manejar exportación sin datos")
    void testProcessExportAsyncNoData() {
        Page<Product> emptyPage = new PageImpl<>(new ArrayList<>());

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(emptyPage);

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).setErrorMessage(eq(testJobId), anyString());
    }

    @Test
    @DisplayName("Debe manejar exportación con múltiples páginas")
    void testProcessExportAsyncMultiplePages() {
        List<Product> page1Products = createTestProducts(1000);
        List<Product> page2Products = createTestProducts(500);
        Page<Product> page1 = new PageImpl<>(page1Products, PageRequest.of(0, 1000), 1500);
        Page<Product> page2 = new PageImpl<>(page2Products, PageRequest.of(1, 1000), 1500);

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page1);
        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(1), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page2);
        when(categoryPersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(productPersistencePort, atLeast(2)).findByEnterpriseIdWithFilters(eq(testEntId), isNull(), anyInt(), anyInt(), eq("name"), eq("asc"));
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateTotalRecords(testJobId, 1500);
    }

    @Test
    @DisplayName("Debe actualizar progreso durante la exportación")
    void testProcessExportAsyncProgressUpdates() {
        List<Product> products = createTestProducts(10);
        Page<Product> page = new PageImpl<>(products);

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page);
        when(categoryPersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(jobTracker).updateProgress(testJobId, 10);
        verify(jobTracker).updateProgress(testJobId, 50);
        verify(jobTracker).updateProgress(testJobId, 90);
        verify(jobTracker).updateProgress(testJobId, 100);
    }

    @Test
    @DisplayName("Debe manejar ProductExportException correctamente")
    void testProcessExportAsyncWithProductExportException() {
        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenThrow(new ProductExportException(ErrorCode.PRODUCT_EXPORT_ERROR, "Error de exportación"));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).setErrorMessage(eq(testJobId), contains("Error de exportación"));
    }

    @Test
    @DisplayName("Debe manejar excepción inesperada correctamente")
    void testProcessExportAsyncWithUnexpectedException() {
        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenThrow(new RuntimeException("Error inesperado"));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).setErrorMessage(eq(testJobId), contains("Error inesperado"));
    }

    @Test
    @DisplayName("Debe usar cache de entidades para evitar N+1 queries")
    void testProcessExportAsyncWithEntityCache() {
        List<Product> products = createTestProducts(100);
        Page<Product> page = new PageImpl<>(products);

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page);
        when(categoryPersistencePort.findByIdAndEnterpriseId(eq(1L), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(eq(1L), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(eq(1L), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(categoryPersistencePort, times(1)).findByIdAndEnterpriseId(eq(1L), eq(testEntId));
        verify(productTypePersistencePort, times(1)).findByIdAndEnterpriseId(eq(1L), eq(testEntId));
        verify(unitOfMeasurePersistencePort, times(1)).findByIdAndEnterpriseId(eq(1L), eq(testEntId));
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe manejar productos con campos opcionales null")
    void testProcessExportAsyncWithNullOptionalFields() {
        List<Product> products = new ArrayList<>();
        products.add(Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Product 1")
                .description("Description")
                .enterpriseId(testEntId)
                .categoryId(null)
                .productTypeId(null)
                .unitOfMeasureId(null)
                .reference(null)
                .presentation(null)
                .state(true)
                .build());

        Page<Product> page = new PageImpl<>(products);

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page);

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker).setFileData(eq(testJobId), any(byte[].class));
    }

    @Test
    @DisplayName("Debe aplicar validaciones a la hoja Excel generada")
    void testProcessExportAsyncAppliesValidations() {
        List<Product> products = createTestProducts(5);
        Page<Product> page = new PageImpl<>(products);

        when(productPersistencePort.findByEnterpriseIdWithFilters(eq(testEntId), isNull(), eq(0), anyInt(), eq("name"), eq("asc")))
                .thenReturn(page);
        when(categoryPersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createCategory(1L, "Category1")));
        when(productTypePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createProductType(1L, "Type1")));
        when(unitOfMeasurePersistencePort.findByIdAndEnterpriseId(anyLong(), eq(testEntId)))
                .thenReturn(Optional.of(createUnitOfMeasure(1L, "Unit1")));

        asyncExportProcessor.processExportAsync(exportRequest, testJobId);

        verify(excelValidationService).applyProductValidations(any(), eq(testEntId), eq(1), anyInt());
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
    }

    private List<Product> createTestProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            products.add(Product.builder()
                    .id((long) i)
                    .code("PRD" + String.format("%03d", i))
                    .name("Product " + i)
                    .description("Description " + i)
                    .enterpriseId(testEntId)
                    .categoryId(1L)
                    .productTypeId(1L)
                    .unitOfMeasureId(1L)
                    .cost(100.0 * i)
                    .quantity(10 * i)
                    .reference("REF" + i)
                    .presentation("Presentation " + i)
                    .state(i % 2 == 0)
                    .build());
        }
        return products;
    }

    private Category createCategory(Long id, String name) {
        return Category.builder()
                .id(id)
                .name(name)
                .state(true)
                .build();
    }

    private ProductType createProductType(Long id, String name) {
        return ProductType.builder()
                .id(id)
                .name(name)
                .state(true)
                .build();
    }

    private UnitOfMeasure createUnitOfMeasure(Long id, String name) {
        return UnitOfMeasure.builder()
                .id(id)
                .name(name)
                .abbreviation("ABV")
                .state(true)
                .build();
    }
}
