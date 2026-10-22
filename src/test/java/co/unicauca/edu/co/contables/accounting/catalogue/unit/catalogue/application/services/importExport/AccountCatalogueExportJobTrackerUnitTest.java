package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueExportJobTracker;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ExportJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueExportJobTrackerUnitTest {

    private AccountCatalogueExportJobTracker exportJobTracker;
    private String entId;
    private String fileName;

    @BeforeEach
    void setUp() {
        exportJobTracker = new AccountCatalogueExportJobTracker();
        entId = "ENT-001";
        fileName = "catalogo_export.xlsx";
    }

    @Test
    @DisplayName("Debe crear un nuevo trabajo de exportación con jobId único")
    void testCreateJobReturnsUniqueJobId() {
        // Arrange & Act
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Assert
        assertNotNull(jobId);
        assertFalse(jobId.isEmpty());
    }

    @Test
    @DisplayName("Debe crear trabajo con estado inicial PENDING")
    void testCreateJobInitializesWithPendingStatus() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.PENDING, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe crear trabajo con progreso inicial en cero")
    void testCreateJobInitializesWithZeroProgress() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getProgress());
    }

    @Test
    @DisplayName("Debe crear trabajo con totalRecords inicial en cero")
    void testCreateJobInitializesWithZeroTotalRecords() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(0, result.get().getTotalRecords());
    }

    @Test
    @DisplayName("Debe crear trabajo con fecha de inicio asignada")
    void testCreateJobAssignsStartTime() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getStartTime());
    }

    @Test
    @DisplayName("Debe almacenar entId correctamente")
    void testCreateJobStoresEntId() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(entId, result.get().getEntId());
    }

    @Test
    @DisplayName("Debe almacenar fileName correctamente")
    void testCreateJobStoresFileName() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(fileName, result.get().getFileName());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío para jobId inexistente")
    void testGetJobStatusReturnsEmptyForNonExistentJob() {
        // Arrange & Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus("non-existent-job-id");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Debe actualizar estado del trabajo correctamente")
    void testUpdateJobStatusChangesStatus() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ImportStatus.PROCESSING, result.get().getStatus());
    }

    @Test
    @DisplayName("Debe asignar endTime cuando estado es COMPLETED")
    void testUpdateJobStatusSetsEndTimeOnCompleted() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateJobStatus(jobId, ImportStatus.COMPLETED);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("Debe asignar endTime cuando estado es FAILED")
    void testUpdateJobStatusSetsEndTimeOnFailed() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("No debe asignar endTime cuando estado es PROCESSING")
    void testUpdateJobStatusDoesNotSetEndTimeOnProcessing() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getEndTime());
    }

    @Test
    @DisplayName("Debe actualizar progreso correctamente")
    void testUpdateProgressChangesProgress() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateProgress(jobId, 50);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(50, result.get().getProgress());
    }

    @Test
    @DisplayName("Debe actualizar progreso a 100 correctamente")
    void testUpdateProgressTo100() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateProgress(jobId, 100);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getProgress());
    }

    @Test
    @DisplayName("Debe actualizar totalRecords correctamente")
    void testUpdateTotalRecordsChangesTotalRecords() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        exportJobTracker.updateTotalRecords(jobId, 500);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(500, result.get().getTotalRecords());
    }

    @Test
    @DisplayName("Debe almacenar fileData correctamente")
    void testSetFileDataStoresFileData() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);
        byte[] fileData = new byte[]{1, 2, 3, 4, 5};

        // Act
        exportJobTracker.setFileData(jobId, fileData);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertArrayEquals(fileData, result.get().getFileData());
    }

    @Test
    @DisplayName("Debe almacenar mensaje de error correctamente")
    void testSetErrorMessageStoresErrorMessage() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);
        String errorMessage = "Error durante la exportación";

        // Act
        exportJobTracker.setErrorMessage(jobId, errorMessage);
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(errorMessage, result.get().getErrorMessage());
    }

    @Test
    @DisplayName("Debe generar jobIds únicos para cada trabajo")
    void testCreateJobGeneratesUniqueJobIds() {
        // Arrange & Act
        String jobId1 = exportJobTracker.createJob(entId, fileName);
        String jobId2 = exportJobTracker.createJob(entId, fileName);
        String jobId3 = exportJobTracker.createJob(entId, fileName);

        // Assert
        assertNotEquals(jobId1, jobId2);
        assertNotEquals(jobId2, jobId3);
        assertNotEquals(jobId1, jobId3);
    }

    @Test
    @DisplayName("Debe mantener trabajos independientes")
    void testMultipleJobsAreIndependent() {
        // Arrange
        String jobId1 = exportJobTracker.createJob(entId, "archivo1.xlsx");
        String jobId2 = exportJobTracker.createJob(entId, "archivo2.xlsx");

        // Act
        exportJobTracker.updateProgress(jobId1, 75);
        exportJobTracker.updateProgress(jobId2, 25);

        // Assert
        Optional<ExportJobStatus> result1 = exportJobTracker.getJobStatus(jobId1);
        Optional<ExportJobStatus> result2 = exportJobTracker.getJobStatus(jobId2);
        
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(75, result1.get().getProgress());
        assertEquals(25, result2.get().getProgress());
    }

    @Test
    @DisplayName("No debe fallar al actualizar estado de trabajo inexistente")
    void testUpdateJobStatusDoesNotFailForNonExistentJob() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> exportJobTracker.updateJobStatus("non-existent", ImportStatus.COMPLETED));
    }

    @Test
    @DisplayName("No debe fallar al actualizar progreso de trabajo inexistente")
    void testUpdateProgressDoesNotFailForNonExistentJob() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> exportJobTracker.updateProgress("non-existent", 50));
    }

    @Test
    @DisplayName("No debe fallar al actualizar totalRecords de trabajo inexistente")
    void testUpdateTotalRecordsDoesNotFailForNonExistentJob() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> exportJobTracker.updateTotalRecords("non-existent", 100));
    }

    @Test
    @DisplayName("No debe fallar al setear fileData de trabajo inexistente")
    void testSetFileDataDoesNotFailForNonExistentJob() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> exportJobTracker.setFileData("non-existent", new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("No debe fallar al setear errorMessage de trabajo inexistente")
    void testSetErrorMessageDoesNotFailForNonExistentJob() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> exportJobTracker.setErrorMessage("non-existent", "Error"));
    }

    @Test
    @DisplayName("Debe almacenar jobId en el estado del trabajo")
    void testJobStatusContainsJobId() {
        // Arrange
        String jobId = exportJobTracker.createJob(entId, fileName);

        // Act
        Optional<ExportJobStatus> result = exportJobTracker.getJobStatus(jobId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jobId, result.get().getJobId());
    }
}
