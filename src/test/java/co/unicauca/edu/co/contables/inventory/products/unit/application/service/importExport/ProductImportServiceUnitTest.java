package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductAsyncImportProcessor;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductImportJobTracker;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductImportService;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductImportRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.validation.ExcelFileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductImportServiceUnitTest {

    @Mock
    private ExcelFileValidator excelFileValidator;

    @Mock
    private ProductAsyncImportProcessor asyncImportProcessor;

    @Mock
    private ProductImportJobTracker jobTracker;

    @InjectMocks
    private ProductImportService productImportService;

    private ProductImportRequest importRequest;
    private String testEntId;
    private String testFileName;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        testFileName = "productos.xlsx";
        testFile = new MockMultipartFile(
            "file",
            testFileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test content".getBytes()
        );
        
        importRequest = ProductImportRequest.builder()
            .entId(testEntId)
            .excelFile(testFile)
            .build();
    }

    @Test
    @DisplayName("Debe importar productos asíncronamente de forma exitosa")
    void testImportProductsAsyncSuccess() throws Exception {
        String expectedJobId = "job-123";
        
        doNothing().when(excelFileValidator).validate(testFile);
        when(jobTracker.createJob(testEntId, testFileName)).thenReturn(expectedJobId);
        doNothing().when(asyncImportProcessor).processImportAsync(any(byte[].class), eq(testEntId), eq(testFileName), eq(expectedJobId));

        String actualJobId = productImportService.importProductsAsync(importRequest);

        assertNotNull(actualJobId);
        assertEquals(expectedJobId, actualJobId);
        verify(excelFileValidator, times(1)).validate(testFile);
        verify(jobTracker, times(1)).createJob(testEntId, testFileName);
        verify(asyncImportProcessor, times(1)).processImportAsync(any(byte[].class), eq(testEntId), eq(testFileName), eq(expectedJobId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la validación del archivo falla")
    void testImportProductsAsyncValidationFailure() {
        doThrow(new IllegalArgumentException("Archivo inválido")).when(excelFileValidator).validate(testFile);

        assertThrows(RuntimeException.class, () -> 
            productImportService.importProductsAsync(importRequest)
        );

        verify(excelFileValidator, times(1)).validate(testFile);
        verify(jobTracker, never()).createJob(anyString(), anyString());
        verify(asyncImportProcessor, never()).processImportAsync(any(byte[].class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando ocurre IOException al leer el archivo")
    void testImportProductsAsyncIOException() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(testFileName);
        when(mockFile.getBytes()).thenThrow(new IOException("Error al leer archivo"));
        
        importRequest = ProductImportRequest.builder()
            .entId(testEntId)
            .excelFile(mockFile)
            .build();
        
        doNothing().when(excelFileValidator).validate(mockFile);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            productImportService.importProductsAsync(importRequest)
        );

        assertTrue(exception.getMessage().contains("Error al procesar el archivo"));
        verify(excelFileValidator, times(1)).validate(mockFile);
        verify(jobTracker, never()).createJob(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando falla la creación del job")
    void testImportProductsAsyncJobCreationFailure() {
        doNothing().when(excelFileValidator).validate(testFile);
        when(jobTracker.createJob(testEntId, testFileName)).thenThrow(new RuntimeException("Error al crear job"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            productImportService.importProductsAsync(importRequest)
        );

        assertTrue(exception.getMessage().contains("Error al iniciar la importación"));
        verify(excelFileValidator, times(1)).validate(testFile);
        verify(jobTracker, times(1)).createJob(testEntId, testFileName);
        verify(asyncImportProcessor, never()).processImportAsync(any(byte[].class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe obtener el estado de importación exitosamente")
    void testGetImportStatusSuccess() {
        String testJobId = "job-123";
        ImportJobStatus expectedStatus = ImportJobStatus.builder()
            .jobId(testJobId)
            .enterpriseId(testEntId)
            .fileName(testFileName)
            .build();
        
        when(jobTracker.getJobStatus(testJobId)).thenReturn(Optional.of(expectedStatus));

        Optional<ImportJobStatus> actualStatus = productImportService.getImportStatus(testJobId);

        assertTrue(actualStatus.isPresent());
        assertEquals(testJobId, actualStatus.get().getJobId());
        verify(jobTracker, times(1)).getJobStatus(testJobId);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el job no existe")
    void testGetImportStatusNotFound() {
        String testJobId = "non-existent-job";
        
        when(jobTracker.getJobStatus(testJobId)).thenReturn(Optional.empty());

        Optional<ImportJobStatus> actualStatus = productImportService.getImportStatus(testJobId);

        assertFalse(actualStatus.isPresent());
        verify(jobTracker, times(1)).getJobStatus(testJobId);
    }


}
