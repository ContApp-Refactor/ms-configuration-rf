package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueImportJobTracker;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueImportJobTrackerUnitTest {

    private AccountCatalogueImportJobTracker jobTracker;

    private String entId;
    private String fileName;

    @BeforeEach
    void setUp() {
        jobTracker = new AccountCatalogueImportJobTracker();
        entId = "ENT-001";
        fileName = "catalogo_import.xlsx";
    }

    @Test
    @DisplayName("Debe crear job y retornar jobId único")
    void testCreateJobReturnsUniqueJobId() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);

        // Assert
        assertNotNull(jobId);
        assertFalse(jobId.isEmpty());
    }

    @Test
    @DisplayName("Debe crear jobs con IDs diferentes")
    void testCreateJobGeneratesUniqueIds() {
        // Act
        String jobId1 = jobTracker.createJob(entId, fileName);
        String jobId2 = jobTracker.createJob(entId, fileName);

        // Assert
        assertNotEquals(jobId1, jobId2);
    }

    @Test
    @DisplayName("Debe crear job con estado PENDING inicial")
    void testCreateJobWithPendingStatus() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.PENDING, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe crear job con progreso inicial en 0")
    void testCreateJobWithZeroProgress() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getProgress());
    }

    @Test
    @DisplayName("Debe crear job con totalRecords inicial en 0")
    void testCreateJobWithZeroTotalRecords() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getTotalRecords());
    }

    @Test
    @DisplayName("Debe crear job con métricas iniciales en 0")
    void testCreateJobWithZeroMetrics() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getSuccessfulImports());
        assertEquals(0, result.get().getFailedImports());
        assertEquals(0, result.get().getDuplicatesSkipped());
    }

    @Test
    @DisplayName("Debe crear job con entId y fileName correctos")
    void testCreateJobWithCorrectEntIdAndFileName() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(entId, result.get().getEntId());
        assertEquals(fileName, result.get().getFileName());
    }

    @Test
    @DisplayName("Debe crear job con startTime establecido")
    void testCreateJobWithStartTime() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getStartTime());
    }

    @Test
    @DisplayName("Debe obtener estado de job existente")
    void testGetJobStatusReturnsExistingJob() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jobId, result.get().getJobId());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío para jobId inexistente")
    void testGetJobStatusReturnsEmptyForNonExistent() {
        // Act
        Optional<ImportJobStatus> result = jobTracker.getJobStatus("non-existent-job-id");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe actualizar estado a PROCESSING")
    void testUpdateJobStatusToProcessing() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.PROCESSING, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe establecer endTime cuando estado es COMPLETED")
    void testUpdateJobStatusToCompletedSetsEndTime() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobStatus(jobId, ImportStatus.COMPLETED);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.COMPLETED, result.get().getStatus());
        assertNotNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("Debe establecer endTime cuando estado es COMPLETED_WITH_ERRORS")
    void testUpdateJobStatusToCompletedWithErrorsSetsEndTime() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobStatus(jobId, ImportStatus.COMPLETED_WITH_ERRORS);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.COMPLETED_WITH_ERRORS, result.get().getStatus());
        assertNotNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("Debe establecer endTime cuando estado es FAILED")
    void testUpdateJobStatusToFailedSetsEndTime() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.FAILED, result.get().getStatus());
        assertNotNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("No debe establecer endTime para estado PROCESSING")
    void testUpdateJobStatusToProcessingDoesNotSetEndTime() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("No debe hacer nada si jobId no existe al actualizar estado")
    void testUpdateJobStatusDoesNothingForNonExistentJob() {
        // Act & Assert
        assertDoesNotThrow(() -> jobTracker.updateJobStatus("non-existent", ImportStatus.COMPLETED));
    }

    @Test
    @DisplayName("Debe actualizar progreso correctamente")
    void testUpdateProgressSuccessfully() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateProgress(jobId, 50);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(50, result.get().getProgress());
    }

    @Test
    @DisplayName("Debe actualizar progreso a 100")
    void testUpdateProgressTo100() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateProgress(jobId, 100);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getProgress());
    }

    @Test
    @DisplayName("No debe hacer nada si jobId no existe al actualizar progreso")
    void testUpdateProgressDoesNothingForNonExistentJob() {
        // Act & Assert
        assertDoesNotThrow(() -> jobTracker.updateProgress("non-existent", 50));
    }

    @Test
    @DisplayName("Debe actualizar métricas del trabajo")
    void testUpdateJobMetricsSuccessfully() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobMetrics(jobId, 100, 80, 15, 5);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getTotalRecords());
        assertEquals(80, result.get().getSuccessfulImports());
        assertEquals(15, result.get().getFailedImports());
        assertEquals(5, result.get().getDuplicatesSkipped());
    }

    @Test
    @DisplayName("Debe actualizar métricas con todos los valores en 0")
    void testUpdateJobMetricsWithAllZeros() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobMetrics(jobId, 0, 0, 0, 0);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getTotalRecords());
        assertEquals(0, result.get().getSuccessfulImports());
        assertEquals(0, result.get().getFailedImports());
        assertEquals(0, result.get().getDuplicatesSkipped());
    }

    @Test
    @DisplayName("No debe hacer nada si jobId no existe al actualizar métricas")
    void testUpdateJobMetricsDoesNothingForNonExistentJob() {
        // Act & Assert
        assertDoesNotThrow(() -> jobTracker.updateJobMetrics("non-existent", 100, 80, 15, 5));
    }

    @Test
    @DisplayName("Debe agregar errores al trabajo")
    void testAddErrorsSuccessfully() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);
        List<ImportErrorDetail> errors = new ArrayList<>();
        errors.add(ImportErrorDetail.builder()
                .rowNumber(2)
                .errorMessage("Código duplicado")
                .build());
        errors.add(ImportErrorDetail.builder()
                .rowNumber(5)
                .errorMessage("Naturaleza inválida")
                .build());

        // Act
        jobTracker.addErrors(jobId, errors);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getErrors().size());
    }

    @Test
    @DisplayName("Debe acumular errores en múltiples llamadas")
    void testAddErrorsAccumulatesErrors() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);
        List<ImportErrorDetail> firstErrors = new ArrayList<>();
        firstErrors.add(ImportErrorDetail.builder()
                .rowNumber(2)
                .errorMessage("Error 1")
                .build());
        List<ImportErrorDetail> secondErrors = new ArrayList<>();
        secondErrors.add(ImportErrorDetail.builder()
                .rowNumber(3)
                .errorMessage("Error 2")
                .build());
        secondErrors.add(ImportErrorDetail.builder()
                .rowNumber(4)
                .errorMessage("Error 3")
                .build());

        // Act
        jobTracker.addErrors(jobId, firstErrors);
        jobTracker.addErrors(jobId, secondErrors);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(3, result.get().getErrors().size());
    }

    @Test
    @DisplayName("No debe hacer nada si jobId no existe al agregar errores")
    void testAddErrorsDoesNothingForNonExistentJob() {
        // Arrange
        List<ImportErrorDetail> errors = new ArrayList<>();
        errors.add(ImportErrorDetail.builder()
                .rowNumber(2)
                .errorMessage("Error")
                .build());

        // Act & Assert
        assertDoesNotThrow(() -> jobTracker.addErrors("non-existent", errors));
    }

    @Test
    @DisplayName("Debe eliminar trabajo existente")
    void testRemoveJobSuccessfully() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.removeJob(jobId);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("No debe lanzar excepción al eliminar job inexistente")
    void testRemoveJobDoesNothingForNonExistentJob() {
        // Act & Assert
        assertDoesNotThrow(() -> jobTracker.removeJob("non-existent"));
    }

    @Test
    @DisplayName("Debe manejar múltiples jobs independientes")
    void testMultipleJobsAreIndependent() {
        // Arrange
        String jobId1 = jobTracker.createJob("ENT-001", "file1.xlsx");
        String jobId2 = jobTracker.createJob("ENT-002", "file2.xlsx");

        // Act
        jobTracker.updateJobStatus(jobId1, ImportStatus.COMPLETED);
        jobTracker.updateProgress(jobId2, 50);

        Optional<ImportJobStatus> result1 = jobTracker.getJobStatus(jobId1);
        Optional<ImportJobStatus> result2 = jobTracker.getJobStatus(jobId2);

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(ImportStatus.COMPLETED, result1.get().getStatus());
        assertEquals(ImportStatus.PENDING, result2.get().getStatus());
        assertEquals(0, result1.get().getProgress());
        assertEquals(50, result2.get().getProgress());
    }

    @Test
    @DisplayName("Debe mantener jobId correcto en el estado del trabajo")
    void testJobStatusContainsCorrectJobId() {
        // Act
        String jobId = jobTracker.createJob(entId, fileName);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jobId, result.get().getJobId());
    }

    @Test
    @DisplayName("Debe permitir actualizar estado múltiples veces")
    void testUpdateStatusMultipleTimes() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);

        // Act
        jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
        jobTracker.updateJobStatus(jobId, ImportStatus.COMPLETED);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.COMPLETED, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe mantener datos del job tras actualizar progreso")
    void testUpdateProgressPreservesOtherData() {
        // Arrange
        String jobId = jobTracker.createJob(entId, fileName);
        jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);

        // Act
        jobTracker.updateProgress(jobId, 75);
        Optional<ImportJobStatus> result = jobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(entId, result.get().getEntId());
        assertEquals(fileName, result.get().getFileName());
        assertEquals(ImportStatus.PROCESSING, result.get().getStatus());
        assertEquals(75, result.get().getProgress());
    }
}
