package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductAsyncImportProcessor;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductBatchProcessor;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductBatchValidationService;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExcelParsingService;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductImportJobTracker;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductAsyncImportProcessorUnitTest {

    @Mock
    private ProductExcelParsingService excelParsingService;

    @Mock
    private ProductBatchValidationService batchValidationService;

    @Mock
    private ProductBatchProcessor batchProcessor;

    @Mock
    private ProductImportJobTracker jobTracker;

    @InjectMocks
    private ProductAsyncImportProcessor asyncImportProcessor;

    private byte[] testFileBytes;
    private String testEntId;
    private String testFileName;
    private String testJobId;

    @BeforeEach
    void setUp() {
        testFileBytes = new byte[]{1, 2, 3, 4, 5};
        testEntId = "ENT123";
        testFileName = "productos.xlsx";
        testJobId = "JOB123";
    }

    @Test
    @DisplayName("Debe procesar importación exitosa con todos los datos válidos")
    void testProcessImportAsyncSuccessful() {
        List<ProductExcelData> products = createTestProducts(5);
        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, new ArrayList<>(), 5, new HashMap<>());

        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(products, new ArrayList<>(), 0, products.size());

        ProductBatchProcessor.BatchProcessingResult batchResult =
                new ProductBatchProcessor.BatchProcessingResult(5, 0, new ArrayList<>());

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);
        when(batchProcessor.processBatch(any(), eq(testEntId))).thenReturn(batchResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker, atLeastOnce()).updateProgress(eq(testJobId), anyInt());
        verify(jobTracker).updateJobMetrics(eq(testJobId), eq(5), eq(5), eq(0), eq(0));
    }

    @Test
    @DisplayName("Debe manejar importación sin datos válidos")
    void testProcessImportAsyncNoDataFound() {
        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(new ArrayList<>(), new ArrayList<>(), 0, new HashMap<>());

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).updateJobMetrics(testJobId, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("Debe manejar todos los registros con errores de parsing")
    void testProcessImportAsyncAllParsingErrors() {
        List<ProductExcelData> products = createTestProducts(3);
        List<ImportErrorDetail> errors = createErrorsForRows(List.of(1, 2, 3));

        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, errors, 3, new HashMap<>());

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).updateJobMetrics(testJobId, 3, 0, 3, 0);
    }

    @Test
    @DisplayName("Debe manejar registros sin errores de validación")
    void testProcessImportAsyncNoValidRecords() {
        List<ProductExcelData> products = createTestProducts(5);
        List<ImportErrorDetail> validationErrors = createErrorsForRows(List.of(1, 2, 3, 4, 5));

        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, new ArrayList<>(), 5, new HashMap<>());

        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(new ArrayList<>(), validationErrors, 0, 0);

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).updateJobMetrics(eq(testJobId), eq(5), eq(0), anyInt(), eq(0));
    }

    @Test
    @DisplayName("Debe manejar importación con errores y éxitos parciales")
    void testProcessImportAsyncPartialSuccess() {
        List<ProductExcelData> products = createTestProducts(10);
        List<ImportErrorDetail> processingErrors = createErrorsForRows(List.of(5, 6));

        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, new ArrayList<>(), 10, new HashMap<>());

        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(products, new ArrayList<>(), 0, products.size());

        ProductBatchProcessor.BatchProcessingResult batchResult =
                new ProductBatchProcessor.BatchProcessingResult(8, 2, processingErrors);

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);
        when(batchProcessor.processBatch(any(), eq(testEntId))).thenReturn(batchResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED_WITH_ERRORS);
        verify(jobTracker).updateJobMetrics(eq(testJobId), eq(10), eq(8), eq(2), eq(0));
    }

    @Test
    @DisplayName("Debe manejar solo duplicados sin errores reales")
    void testProcessImportAsyncOnlyDuplicates() {
        List<ProductExcelData> products = createTestProducts(5);

        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, new ArrayList<>(), 5, new HashMap<>());

        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(new ArrayList<>(), new ArrayList<>(), 5, 0);

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateJobMetrics(testJobId, 5, 0, 0, 5);
    }

    @Test
    @DisplayName("Debe manejar excepción crítica durante el procesamiento")
    void testProcessImportAsyncCriticalError() {
        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId))
                .thenThrow(new RuntimeException("Error crítico"));

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.FAILED);
        verify(jobTracker).addErrors(eq(testJobId), anyList());
    }

    @Test
    @DisplayName("Debe procesar múltiples lotes correctamente")
    void testProcessImportAsyncMultipleBatches() {
        List<ProductExcelData> products = createTestProducts(150);

        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, new ArrayList<>(), 150, new HashMap<>());

        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(products, new ArrayList<>(), 0, products.size());

        ProductBatchProcessor.BatchProcessingResult batchResult =
                new ProductBatchProcessor.BatchProcessingResult(50, 0, new ArrayList<>());

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);
        when(batchProcessor.processBatch(any(), eq(testEntId))).thenReturn(batchResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(batchProcessor, atLeast(1)).processBatch(any(), eq(testEntId));
        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe filtrar productos con errores de parsing correctamente")
    void testProcessImportAsyncFilterParsingErrors() {
        List<ProductExcelData> allProducts = createTestProducts(10);
        List<ImportErrorDetail> parsingErrors = createErrorsForRows(List.of(3, 5, 7));

        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(allProducts, parsingErrors, 10, new HashMap<>());

        List<ProductExcelData> validProducts = createTestProducts(7);
        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(validProducts, new ArrayList<>(), 0, validProducts.size());

        ProductBatchProcessor.BatchProcessingResult batchResult =
                new ProductBatchProcessor.BatchProcessingResult(7, 0, new ArrayList<>());

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);
        when(batchProcessor.processBatch(any(), eq(testEntId))).thenReturn(batchResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateJobStatus(testJobId, ImportStatus.COMPLETED_WITH_ERRORS);
        verify(jobTracker).updateJobMetrics(eq(testJobId), eq(10), eq(7), eq(3), eq(0));
    }

    @Test
    @DisplayName("Debe actualizar progreso durante todo el proceso")
    void testProcessImportAsyncProgressUpdates() {
        List<ProductExcelData> products = createTestProducts(5);
        ProductExcelParsingService.ExcelParsingResult parsingResult =
                new ProductExcelParsingService.ExcelParsingResult(products, new ArrayList<>(), 5, new HashMap<>());

        ProductBatchValidationService.BatchValidationResult validationResult =
                new ProductBatchValidationService.BatchValidationResult(products, new ArrayList<>(), 0, products.size());

        ProductBatchProcessor.BatchProcessingResult batchResult =
                new ProductBatchProcessor.BatchProcessingResult(5, 0, new ArrayList<>());

        when(excelParsingService.parseExcelFileFromBytes(testFileBytes, testEntId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(any(), eq(testEntId), any())).thenReturn(validationResult);
        when(batchProcessor.processBatch(any(), eq(testEntId))).thenReturn(batchResult);

        asyncImportProcessor.processImportAsync(testFileBytes, testEntId, testFileName, testJobId);

        verify(jobTracker).updateProgress(testJobId, 10);
        verify(jobTracker).updateProgress(testJobId, 25);
        verify(jobTracker).updateProgress(testJobId, 50);
        verify(jobTracker, atLeastOnce()).updateProgress(testJobId, 90);
        verify(jobTracker).updateProgress(testJobId, 100);
    }

    private List<ProductExcelData> createTestProducts(int count) {
        List<ProductExcelData> products = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            products.add(ProductExcelData.builder()
                    .rowNumber(i)
                    .name("Product " + i)
                    .description("Description " + i)
                    .quantity(10)
                    .cost(100.0)
                    .enterpriseId(testEntId)
                    .build());
        }
        return products;
    }

    private List<ImportErrorDetail> createErrorsForRows(List<Integer> rows) {
        List<ImportErrorDetail> errors = new ArrayList<>();
        for (Integer row : rows) {
            errors.add(ImportErrorDetail.builder()
                    .rowNumber(row)
                    .errorCode("ERR001")
                    .errorMessage("Error en fila " + row)
                    .errorType(ImportErrorType.VALIDATION_ERROR)
                    .build());
        }
        return errors;
    }
}
