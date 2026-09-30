package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueAsyncImportProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueImportJobTracker;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueImportService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueFileValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueImportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueImportServiceUnitTest {

    @Mock
    private AccountCatalogueImportJobTracker jobTracker;

    @Mock
    private AccountCatalogueAsyncImportProcessor asyncImportProcessor;

    @Mock
    private AccountCatalogueFileValidationService fileValidationService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AccountCatalogueImportService importService;

    private String entId;
    private String fileName;
    private String jobId;
    private byte[] fileBytes;
    private AccountCatalogueImportRequest importRequest;

    @BeforeEach
    void setUp() throws IOException {
        entId = "ENT-001";
        fileName = "catalogo_cuentas.xlsx";
        jobId = "JOB-001";
        fileBytes = new byte[]{1, 2, 3, 4, 5};

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        importRequest = AccountCatalogueImportRequest.builder()
                .entId(entId)
                .excelFile(multipartFile)
                .build();
    }

    @Test
    @DisplayName("Debe importar catálogo de cuentas de forma asíncrona exitosamente")
    void testImportAccountCatalogueAsyncSuccess() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);
        doNothing().when(asyncImportProcessor).processImportAsync(any(), eq(jobId), eq(fileBytes));

        // Act
        String result = importService.importAccountCatalogueAsync(importRequest);

        // Assert
        assertNotNull(result);
        assertEquals(jobId, result);
    }

    @Test
    @DisplayName("Debe validar archivo antes de crear el trabajo")
    void testImportValidatesFileFirst() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        importService.importAccountCatalogueAsync(importRequest);

        // Assert
        verify(fileValidationService).validate(multipartFile);
    }

    @Test
    @DisplayName("Debe crear trabajo de importación con entId y fileName")
    void testImportCreatesJobWithCorrectParameters() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        importService.importAccountCatalogueAsync(importRequest);

        // Assert
        verify(jobTracker).createJob(entId, fileName);
    }

    @Test
    @DisplayName("Debe iniciar procesamiento asíncrono con parámetros correctos")
    void testImportStartsAsyncProcessing() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        importService.importAccountCatalogueAsync(importRequest);

        // Assert
        verify(asyncImportProcessor).processImportAsync(eq(importRequest), eq(jobId), eq(fileBytes));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando falla la lectura del archivo")
    void testImportThrowsRuntimeExceptionOnIOException() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);
        when(multipartFile.getBytes()).thenThrow(new IOException("Error de lectura"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> importService.importAccountCatalogueAsync(importRequest));
        
        assertTrue(exception.getMessage().contains("Error al leer el archivo"));
    }

    @Test
    @DisplayName("Debe propagar excepción de validación de archivo")
    void testImportPropagatesValidationException() {
        // Arrange
        doThrow(new IllegalArgumentException("Archivo inválido"))
                .when(fileValidationService).validate(multipartFile);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> importService.importAccountCatalogueAsync(importRequest));
    }

    @Test
    @DisplayName("No debe crear trabajo si validación falla")
    void testImportDoesNotCreateJobIfValidationFails() {
        // Arrange
        doThrow(new IllegalArgumentException("Archivo inválido"))
                .when(fileValidationService).validate(multipartFile);

        // Act
        try {
            importService.importAccountCatalogueAsync(importRequest);
        } catch (IllegalArgumentException e) {
            // Esperado
        }

        // Assert
        verify(jobTracker, never()).createJob(anyString(), anyString());
    }

    @Test
    @DisplayName("No debe iniciar procesamiento asíncrono si validación falla")
    void testImportDoesNotStartAsyncProcessingIfValidationFails() {
        // Arrange
        doThrow(new IllegalArgumentException("Archivo inválido"))
                .when(fileValidationService).validate(multipartFile);

        // Act
        try {
            importService.importAccountCatalogueAsync(importRequest);
        } catch (IllegalArgumentException e) {
            // Esperado
        }

        // Assert
        verify(asyncImportProcessor, never()).processImportAsync(any(), anyString(), any());
    }

    @Test
    @DisplayName("Debe obtener estado de importación existente")
    void testGetImportStatusReturnsExistingStatus() {
        // Arrange
        ImportJobStatus jobStatus = ImportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.PROCESSING)
                .progress(50)
                .startTime(LocalDateTime.now())
                .build();
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.of(jobStatus));

        // Act
        Optional<ImportJobStatus> result = importService.getImportStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jobId, result.get().getJobId());
        assertEquals(ImportStatus.PROCESSING, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío para jobId inexistente")
    void testGetImportStatusReturnsEmptyForNonExistentJob() {
        // Arrange
        when(jobTracker.getJobStatus("non-existent")).thenReturn(Optional.empty());

        // Act
        Optional<ImportJobStatus> result = importService.getImportStatus("non-existent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe delegar getImportStatus a jobTracker")
    void testGetImportStatusDelegatesToJobTracker() {
        // Arrange
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.empty());

        // Act
        importService.getImportStatus(jobId);

        // Assert
        verify(jobTracker).getJobStatus(jobId);
    }

    @Test
    @DisplayName("Debe obtener estado completado con métricas")
    void testGetImportStatusWithCompletedMetrics() {
        // Arrange
        ImportJobStatus jobStatus = ImportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.COMPLETED)
                .progress(100)
                .totalRecords(100)
                .successfulImports(90)
                .failedImports(5)
                .duplicatesSkipped(5)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.of(jobStatus));

        // Act
        Optional<ImportJobStatus> result = importService.getImportStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.COMPLETED, result.get().getStatus());
        assertEquals(100, result.get().getTotalRecords());
        assertEquals(90, result.get().getSuccessfulImports());
        assertEquals(5, result.get().getFailedImports());
        assertEquals(5, result.get().getDuplicatesSkipped());
    }

    @Test
    @DisplayName("Debe obtener estado fallido con mensaje de error")
    void testGetImportStatusWithFailedStatus() {
        // Arrange
        ImportJobStatus jobStatus = ImportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.FAILED)
                .progress(25)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
        when(jobTracker.getJobStatus(jobId)).thenReturn(Optional.of(jobStatus));

        // Act
        Optional<ImportJobStatus> result = importService.getImportStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.FAILED, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe manejar nombre de archivo null")
    void testImportWithNullFileName() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, null)).thenReturn(jobId);

        // Act
        String result = importService.importAccountCatalogueAsync(importRequest);

        // Assert
        assertNotNull(result);
        verify(jobTracker).createJob(entId, null);
    }

    @Test
    @DisplayName("Debe obtener bytes del archivo correctamente")
    void testImportGetsFileBytesCorrectly() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        importService.importAccountCatalogueAsync(importRequest);

        // Assert
        verify(multipartFile).getBytes();
    }

    @Test
    @DisplayName("Debe obtener nombre original del archivo")
    void testImportGetsOriginalFilename() throws IOException {
        // Arrange
        doNothing().when(fileValidationService).validate(multipartFile);
        when(jobTracker.createJob(entId, fileName)).thenReturn(jobId);

        // Act
        importService.importAccountCatalogueAsync(importRequest);

        // Assert
        verify(multipartFile).getOriginalFilename();
    }
}
