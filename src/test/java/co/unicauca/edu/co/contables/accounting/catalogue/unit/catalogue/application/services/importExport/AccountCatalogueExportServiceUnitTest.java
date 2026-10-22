package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueAsyncExportProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueExportJobTracker;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueExportService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueExcelValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ExportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueExportRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.utils.ExcelFileNameGenerator;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueExportServiceUnitTest {

    @Mock
    private AccountCatalogueExcelValidationService excelValidationService;

    @Mock
    private AccountCatalogueExportJobTracker jobTracker;

    @Mock
    private AccountCatalogueAsyncExportProcessor asyncExportProcessor;

    @Mock
    private ExcelFileNameGenerator fileNameGenerator;

    @InjectMocks
    private AccountCatalogueExportService exportService;

    private String entId;
    private String companyName;
    private Boolean status;
    private String jobId;
    private String fileName;
    private AccountCatalogueExportRequest exportRequest;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        companyName = "Empresa Test";
        status = true;
        jobId = "JOB-001";
        fileName = "catalogo_export_ENT-001.xlsx";

        exportRequest = AccountCatalogueExportRequest.builder()
                .entId(entId)
                .companyName(companyName)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Debe generar plantilla Excel correctamente")
    void testExportAccountCatalogueTemplateSuccess() throws Exception {
        // Arrange
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        Resource result = exportService.exportAccountCatalogueTemplate(entId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
    }

    @Test
    @DisplayName("Debe retornar recurso con contenido válido")
    void testExportAccountCatalogueTemplateReturnsValidResource() throws Exception {
        // Arrange
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        Resource result = exportService.exportAccountCatalogueTemplate(entId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getInputStream());
    }

    @Test
    @DisplayName("Debe iniciar exportación asíncrona y retornar jobId")
    void testExportAccountCatalogueAsyncReturnsJobId() {
        // Arrange
        when(fileNameGenerator.generateExportFileName(entId, companyName, status)).thenReturn(fileName);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);
        doNothing().when(asyncExportProcessor).processExportAsync(entId, status, jobId);

        // Act
        String result = exportService.exportAccountCatalogueAsync(exportRequest);

        // Assert
        assertNotNull(result);
        assertEquals(jobId, result);
    }

    @Test
    @DisplayName("Debe generar nombre de archivo usando fileNameGenerator")
    void testExportAccountCatalogueAsyncUsesFileNameGenerator() {
        // Arrange
        when(fileNameGenerator.generateExportFileName(entId, companyName, status)).thenReturn(fileName);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        exportService.exportAccountCatalogueAsync(exportRequest);

        // Assert
        verify(fileNameGenerator).generateExportFileName(entId, companyName, status);
    }

    @Test
    @DisplayName("Debe crear job en jobTracker")
    void testExportAccountCatalogueAsyncCreatesJob() {
        // Arrange
        when(fileNameGenerator.generateExportFileName(entId, companyName, status)).thenReturn(fileName);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        exportService.exportAccountCatalogueAsync(exportRequest);

        // Assert
        verify(jobTracker).createJob(entId, fileName);
    }

    @Test
    @DisplayName("Debe invocar procesador asíncrono con parámetros correctos")
    void testExportAccountCatalogueAsyncInvokesAsyncProcessor() {
        // Arrange
        when(fileNameGenerator.generateExportFileName(entId, companyName, status)).thenReturn(fileName);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        exportService.exportAccountCatalogueAsync(exportRequest);

        // Assert
        verify(asyncExportProcessor).processExportAsync(entId, status, jobId);
    }

    @Test
    @DisplayName("Debe obtener estado de exportación existente")
    void testGetExportStatusReturnsStatusWhenExists() {
        // Arrange
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.PROCESSING)
                .progress(50)
                .startTime(LocalDateTime.now())
                .build();
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.of(jobStatus));

        // Act
        Optional<ExportJobStatus> result = exportService.getExportStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jobId, result.get().getJobId());
        assertEquals(ImportStatus.PROCESSING, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío para jobId inexistente")
    void testGetExportStatusReturnsEmptyWhenNotExists() {
        // Arrange
        when(jobTracker.getJobStatus("non-existent")).thenReturn(Optional.empty());

        // Act
        Optional<ExportJobStatus> result = exportService.getExportStatus("non-existent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe delegar getExportStatus a jobTracker")
    void testGetExportStatusDelegatesToJobTracker() {
        // Arrange
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.empty());

        // Act
        exportService.getExportStatus(jobId);

        // Assert
        verify(jobTracker).getJobStatus(jobId);
    }

    @Test
    @DisplayName("Debe aplicar validaciones a la plantilla")
    void testExportAccountCatalogueTemplateAppliesValidations() throws Exception {
        // Arrange
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        exportService.exportAccountCatalogueTemplate(entId);

        // Assert
        verify(excelValidationService).applyAccountCatalogueValidations(any(), eq(1), eq(1000));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando falla generación de plantilla")
    void testExportAccountCatalogueTemplateThrowsRuntimeExceptionOnError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error interno")).when(excelValidationService)
                .applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> exportService.exportAccountCatalogueTemplate(entId));
    }

    @Test
    @DisplayName("Debe manejar exportación con status null")
    void testExportAccountCatalogueAsyncWithNullStatus() {
        // Arrange
        AccountCatalogueExportRequest requestWithNullStatus = AccountCatalogueExportRequest.builder()
                .entId(entId)
                .companyName(companyName)
                .status(null)
                .build();
        
        when(fileNameGenerator.generateExportFileName(entId, companyName, null)).thenReturn(fileName);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        String result = exportService.exportAccountCatalogueAsync(requestWithNullStatus);

        // Assert
        assertNotNull(result);
        verify(asyncExportProcessor).processExportAsync(entId, null, jobId);
    }

    @Test
    @DisplayName("Debe manejar exportación con status false")
    void testExportAccountCatalogueAsyncWithFalseStatus() {
        // Arrange
        AccountCatalogueExportRequest requestWithFalseStatus = AccountCatalogueExportRequest.builder()
                .entId(entId)
                .companyName(companyName)
                .status(false)
                .build();
        
        when(fileNameGenerator.generateExportFileName(entId, companyName, false)).thenReturn(fileName);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        String result = exportService.exportAccountCatalogueAsync(requestWithFalseStatus);

        // Assert
        assertNotNull(result);
        verify(asyncExportProcessor).processExportAsync(entId, false, jobId);
    }

    @Test
    @DisplayName("Debe obtener estado completado con datos de archivo")
    void testGetExportStatusWithFileData() {
        // Arrange
        byte[] fileData = new byte[]{1, 2, 3, 4, 5};
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.COMPLETED)
                .progress(100)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .fileData(fileData)
                .build();
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.of(jobStatus));

        // Act
        Optional<ExportJobStatus> result = exportService.getExportStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.COMPLETED, result.get().getStatus());
        assertArrayEquals(fileData, result.get().getFileData());
    }

    @Test
    @DisplayName("Debe obtener estado fallido con mensaje de error")
    void testGetExportStatusWithErrorMessage() {
        // Arrange
        String errorMessage = "Error durante la exportación";
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.FAILED)
                .progress(50)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.of(jobStatus));

        // Act
        Optional<ExportJobStatus> result = exportService.getExportStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.FAILED, result.get().getStatus());
        assertEquals(errorMessage, result.get().getErrorMessage());
    }
}
