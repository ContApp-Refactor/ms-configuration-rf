package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductImportJobTracker;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductImportJobTrackerUnitTest {

    @InjectMocks
    private ProductImportJobTracker productImportJobTracker;

    private String testJobId;
    private String testEntId;
    private String testFileName;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        testFileName = "productos_import.xlsx";
    }

    @Test
    @DisplayName("Debe crear un nuevo job de importación exitosamente")
    void testCreateJobSuccess() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        assertNotNull(testJobId);
        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(testEntId, jobStatus.get().getEnterpriseId());
        assertEquals(testFileName, jobStatus.get().getFileName());
        assertEquals(ImportStatus.PENDING, jobStatus.get().getStatus());
        assertEquals(0, jobStatus.get().getProgress());
        assertEquals(0, jobStatus.get().getTotalRecords());
        assertEquals(0, jobStatus.get().getSuccessfulImports());
        assertEquals(0, jobStatus.get().getFailedImports());
        assertEquals(0, jobStatus.get().getDuplicatesSkipped());
    }

    @Test
    @DisplayName("Debe obtener el estado de un job existente")
    void testGetJobStatusExists() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);

        assertTrue(jobStatus.isPresent());
        assertEquals(testJobId, jobStatus.get().getJobId());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío para job inexistente")
    void testGetJobStatusNotExists() {
        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus("invalid-job-id");

        assertFalse(jobStatus.isPresent());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a PROCESSING")
    void testUpdateJobStatusToProcessing() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateJobStatus(testJobId, ImportStatus.PROCESSING);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.PROCESSING, jobStatus.get().getStatus());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a COMPLETED y establecer endTime y progreso 100")
    void testUpdateJobStatusToCompleted() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateJobStatus(testJobId, ImportStatus.COMPLETED);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.COMPLETED, jobStatus.get().getStatus());
        assertNotNull(jobStatus.get().getEndTime());
        assertEquals(100, jobStatus.get().getProgress());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a FAILED y establecer endTime")
    void testUpdateJobStatusToFailed() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateJobStatus(testJobId, ImportStatus.FAILED);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.FAILED, jobStatus.get().getStatus());
        assertNotNull(jobStatus.get().getEndTime());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a COMPLETED_WITH_ERRORS")
    void testUpdateJobStatusToCompletedWithErrors() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateJobStatus(testJobId, ImportStatus.COMPLETED_WITH_ERRORS);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.COMPLETED_WITH_ERRORS, jobStatus.get().getStatus());
        assertNotNull(jobStatus.get().getEndTime());
    }

    @Test
    @DisplayName("Debe actualizar las métricas del job")
    void testUpdateJobMetrics() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateJobMetrics(testJobId, 100, 80, 15, 5);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(100, jobStatus.get().getTotalRecords());
        assertEquals(80, jobStatus.get().getSuccessfulImports());
        assertEquals(15, jobStatus.get().getFailedImports());
        assertEquals(5, jobStatus.get().getDuplicatesSkipped());
    }

    @Test
    @DisplayName("Debe actualizar el progreso del job dentro del rango válido")
    void testUpdateProgressWithinRange() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateProgress(testJobId, 50);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(50, jobStatus.get().getProgress());
    }

    @Test
    @DisplayName("Debe limitar el progreso al máximo de 100")
    void testUpdateProgressMaxLimit() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateProgress(testJobId, 150);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(100, jobStatus.get().getProgress());
    }

    @Test
    @DisplayName("Debe limitar el progreso al mínimo de 0")
    void testUpdateProgressMinLimit() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.updateProgress(testJobId, -10);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(0, jobStatus.get().getProgress());
    }

    @Test
    @DisplayName("Debe agregar errores al job")
    void testAddErrors() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);
        List<ImportErrorDetail> errors = new ArrayList<>();
        errors.add(ImportErrorDetail.builder()
                .rowNumber(1)
                .errorCode("TEST_ERROR")
                .errorMessage("Error de prueba")
                .errorType(ImportErrorType.VALIDATION_ERROR)
                .build());

        productImportJobTracker.addErrors(testJobId, errors);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(1, jobStatus.get().getErrors().size());
        assertEquals("TEST_ERROR", jobStatus.get().getErrors().get(0).getErrorCode());
    }

    @Test
    @DisplayName("Debe agregar múltiples listas de errores al job")
    void testAddMultipleErrorLists() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);
        
        List<ImportErrorDetail> errors1 = List.of(
            ImportErrorDetail.builder()
                .rowNumber(1)
                .errorCode("ERROR_1")
                .errorMessage("Primer error")
                .errorType(ImportErrorType.VALIDATION_ERROR)
                .build()
        );
        
        List<ImportErrorDetail> errors2 = List.of(
            ImportErrorDetail.builder()
                .rowNumber(2)
                .errorCode("ERROR_2")
                .errorMessage("Segundo error")
                .errorType(ImportErrorType.FORMAT_ERROR)
                .build()
        );

        productImportJobTracker.addErrors(testJobId, errors1);
        productImportJobTracker.addErrors(testJobId, errors2);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(2, jobStatus.get().getErrors().size());
    }

    @Test
    @DisplayName("Debe eliminar un job del tracker")
    void testRemoveJob() {
        testJobId = productImportJobTracker.createJob(testEntId, testFileName);

        productImportJobTracker.removeJob(testJobId);

        Optional<ImportJobStatus> jobStatus = productImportJobTracker.getJobStatus(testJobId);
        assertFalse(jobStatus.isPresent());
    }

    @Test
    @DisplayName("Debe obtener el conteo de jobs activos")
    void testGetActiveJobsCount() {
        int initialCount = productImportJobTracker.getActiveJobsCount();

        String jobId1 = productImportJobTracker.createJob(testEntId, "file1.xlsx");
        assertEquals(initialCount + 1, productImportJobTracker.getActiveJobsCount());

        String jobId2 = productImportJobTracker.createJob(testEntId, "file2.xlsx");
        assertEquals(initialCount + 2, productImportJobTracker.getActiveJobsCount());

        productImportJobTracker.removeJob(jobId1);
        assertEquals(initialCount + 1, productImportJobTracker.getActiveJobsCount());

        productImportJobTracker.removeJob(jobId2);
        assertEquals(initialCount, productImportJobTracker.getActiveJobsCount());
    }


}
