package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExportJobTracker;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ExportJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductExportJobTrackerUnitTest {

    @InjectMocks
    private ProductExportJobTracker productExportJobTracker;

    private String testJobId;
    private String testEntId;
    private String testFileName;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        testFileName = "productos_export.xlsx";
    }

    @Test
    @DisplayName("Debe crear un nuevo job de exportación exitosamente")
    void testCreateJobSuccess() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        assertNotNull(testJobId);
        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(testEntId, jobStatus.get().getEntId());
        assertEquals(testFileName, jobStatus.get().getFileName());
        assertEquals(ImportStatus.PENDING, jobStatus.get().getStatus());
        assertEquals(0, jobStatus.get().getProgress());
    }

    @Test
    @DisplayName("Debe obtener el estado de un job existente")
    void testGetJobStatusExists() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);

        assertTrue(jobStatus.isPresent());
        assertEquals(testJobId, jobStatus.get().getJobId());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío para job inexistente")
    void testGetJobStatusNotExists() {
        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus("invalid-job-id");

        assertFalse(jobStatus.isPresent());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a PROCESSING")
    void testUpdateJobStatusToProcessing() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        productExportJobTracker.updateJobStatus(testJobId, ImportStatus.PROCESSING);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.PROCESSING, jobStatus.get().getStatus());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a COMPLETED y establecer endTime")
    void testUpdateJobStatusToCompleted() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        productExportJobTracker.updateJobStatus(testJobId, ImportStatus.COMPLETED);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.COMPLETED, jobStatus.get().getStatus());
        assertNotNull(jobStatus.get().getEndTime());
    }

    @Test
    @DisplayName("Debe actualizar el estado del job a FAILED y establecer endTime")
    void testUpdateJobStatusToFailed() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        productExportJobTracker.updateJobStatus(testJobId, ImportStatus.FAILED);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(ImportStatus.FAILED, jobStatus.get().getStatus());
        assertNotNull(jobStatus.get().getEndTime());
    }

    @Test
    @DisplayName("Debe actualizar el progreso del job")
    void testUpdateProgress() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        productExportJobTracker.updateProgress(testJobId, 50);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(50, jobStatus.get().getProgress());
    }

    @Test
    @DisplayName("Debe actualizar el total de registros")
    void testUpdateTotalRecords() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        productExportJobTracker.updateTotalRecords(testJobId, 1000);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(1000, jobStatus.get().getTotalRecords());
    }

    @Test
    @DisplayName("Debe almacenar los datos del archivo generado")
    void testSetFileData() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);
        byte[] testFileData = "test data".getBytes();

        productExportJobTracker.setFileData(testJobId, testFileData);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertArrayEquals(testFileData, jobStatus.get().getFileData());
    }

    @Test
    @DisplayName("Debe almacenar un mensaje de error")
    void testSetErrorMessage() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);
        String errorMessage = "Error durante la exportación";

        productExportJobTracker.setErrorMessage(testJobId, errorMessage);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertTrue(jobStatus.isPresent());
        assertEquals(errorMessage, jobStatus.get().getErrorMessage());
    }

    @Test
    @DisplayName("Debe eliminar un job del tracker")
    void testRemoveJob() {
        testJobId = productExportJobTracker.createJob(testEntId, testFileName);

        productExportJobTracker.removeJob(testJobId);

        Optional<ExportJobStatus> jobStatus = productExportJobTracker.getJobStatus(testJobId);
        assertFalse(jobStatus.isPresent());
    }


}
