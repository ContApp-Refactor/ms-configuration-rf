package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductAsyncExportProcessor;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExcelValidationService;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExportJobTracker;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExportService;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductExportRequest;
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
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductExportServiceUnitTest {

    @Mock
    private ProductExcelValidationService excelValidationService;

    @Mock
    private ProductExportJobTracker exportJobTracker;

    @Mock
    private ProductAsyncExportProcessor asyncExportProcessor;

    @Mock
    private ExcelFileNameGenerator fileNameGenerator;

    @InjectMocks
    private ProductExportService productExportService;

    private String testEntId;
    private ProductExportRequest exportRequest;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        exportRequest = ProductExportRequest.builder()
            .entId(testEntId)
            .companyName("Empresa Test")
            .status(true)
            .build();
    }

    @Test
    @DisplayName("Debe exportar plantilla con validaciones exitosamente")
    void testExportProductTemplateWithValidations() {
        when(excelValidationService.getCategoryOptions(testEntId)).thenReturn(List.of("Categoría 1"));
        when(excelValidationService.getProductTypeOptions(testEntId)).thenReturn(List.of("Tipo 1"));
        when(excelValidationService.getUnitOfMeasureOptions(testEntId)).thenReturn(List.of("Unidad 1"));
        when(excelValidationService.getStatusOptions()).thenReturn(List.of("ACTIVO", "INACTIVO"));

        Resource resource = productExportService.exportProductTemplateWithValidations(testEntId);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    @DisplayName("Debe iniciar exportación asíncrona exitosamente")
    void testExportProductsAsyncSuccess() {
        String expectedJobId = "job-123";
        String expectedFileName = "productos_export.xlsx";
        
        when(fileNameGenerator.generateExportFileName(testEntId, "Empresa Test", true))
            .thenReturn(expectedFileName);
        when(exportJobTracker.createJob(testEntId, expectedFileName)).thenReturn(expectedJobId);
        doNothing().when(asyncExportProcessor).processExportAsync(exportRequest, expectedJobId);

        String actualJobId = productExportService.exportProductsAsync(exportRequest);

        assertNotNull(actualJobId);
        assertEquals(expectedJobId, actualJobId);
        verify(fileNameGenerator, times(1)).generateExportFileName(testEntId, "Empresa Test", true);
        verify(exportJobTracker, times(1)).createJob(testEntId, expectedFileName);
        verify(asyncExportProcessor, times(1)).processExportAsync(exportRequest, expectedJobId);
    }

    @Test
    @DisplayName("Debe obtener el estado de exportación exitosamente")
    void testGetExportStatusSuccess() {
        String testJobId = "job-123";
        ExportJobStatus expectedStatus = ExportJobStatus.builder()
            .jobId(testJobId)
            .entId(testEntId)
            .fileName("productos.xlsx")
            .status(ImportStatus.COMPLETED)
            .build();
        
        when(exportJobTracker.getJobStatus(testJobId)).thenReturn(Optional.of(expectedStatus));

        Optional<ExportJobStatus> actualStatus = productExportService.getExportStatus(testJobId);

        assertTrue(actualStatus.isPresent());
        assertEquals(testJobId, actualStatus.get().getJobId());
        assertEquals(ImportStatus.COMPLETED, actualStatus.get().getStatus());
        verify(exportJobTracker, times(1)).getJobStatus(testJobId);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el job no existe")
    void testGetExportStatusNotFound() {
        String testJobId = "non-existent-job";
        
        when(exportJobTracker.getJobStatus(testJobId)).thenReturn(Optional.empty());

        Optional<ExportJobStatus> actualStatus = productExportService.getExportStatus(testJobId);

        assertFalse(actualStatus.isPresent());
        verify(exportJobTracker, times(1)).getJobStatus(testJobId);
    }

    @Test
    @DisplayName("Debe manejar exportación con filtro de estado null")
    void testExportProductsAsyncWithNullStatus() {
        exportRequest = ProductExportRequest.builder()
            .entId(testEntId)
            .companyName("Empresa Test")
            .status(null)
            .build();

        String expectedJobId = "job-123";
        String expectedFileName = "productos_export.xlsx";
        
        when(fileNameGenerator.generateExportFileName(testEntId, "Empresa Test", null))
            .thenReturn(expectedFileName);
        when(exportJobTracker.createJob(testEntId, expectedFileName)).thenReturn(expectedJobId);
        doNothing().when(asyncExportProcessor).processExportAsync(exportRequest, expectedJobId);

        String actualJobId = productExportService.exportProductsAsync(exportRequest);

        assertNotNull(actualJobId);
        verify(fileNameGenerator, times(1)).generateExportFileName(testEntId, "Empresa Test", null);
    }

    @Test
    @DisplayName("Debe manejar exportación con estado inactivo")
    void testExportProductsAsyncWithInactiveStatus() {
        exportRequest = ProductExportRequest.builder()
            .entId(testEntId)
            .companyName("Empresa Test")
            .status(false)
            .build();

        String expectedJobId = "job-123";
        String expectedFileName = "productos_inactivos.xlsx";
        
        when(fileNameGenerator.generateExportFileName(testEntId, "Empresa Test", false))
            .thenReturn(expectedFileName);
        when(exportJobTracker.createJob(testEntId, expectedFileName)).thenReturn(expectedJobId);
        doNothing().when(asyncExportProcessor).processExportAsync(exportRequest, expectedJobId);

        String actualJobId = productExportService.exportProductsAsync(exportRequest);

        assertNotNull(actualJobId);
        verify(fileNameGenerator, times(1)).generateExportFileName(testEntId, "Empresa Test", false);
    }

    @Test
    @DisplayName("Debe exportar plantilla incluso sin opciones de validación")
    void testExportTemplateWithEmptyValidationOptions() {
        when(excelValidationService.getCategoryOptions(testEntId)).thenReturn(List.of());
        when(excelValidationService.getProductTypeOptions(testEntId)).thenReturn(List.of());
        when(excelValidationService.getUnitOfMeasureOptions(testEntId)).thenReturn(List.of());
        when(excelValidationService.getStatusOptions()).thenReturn(List.of("ACTIVO", "INACTIVO"));

        Resource resource = productExportService.exportProductTemplateWithValidations(testEntId);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }
}
