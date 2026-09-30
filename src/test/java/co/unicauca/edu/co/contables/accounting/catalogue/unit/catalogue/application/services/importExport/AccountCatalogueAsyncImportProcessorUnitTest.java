package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueHierarchyProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueAsyncImportProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueBatchProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueDuplicateDetectionService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueExcelParsingService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueImportJobTracker;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueBatchValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueAsyncImportProcessorUnitTest {

    @Mock
    private AccountCatalogueImportJobTracker jobTracker;

    @Mock
    private AccountCatalogueExcelParsingService excelParsingService;

    @Mock
    private AccountCatalogueBatchValidationService batchValidationService;

    @Mock
    private AccountCatalogueDuplicateDetectionService duplicateDetectionService;

    @Mock
    private AccountCatalogueHierarchyProcessor hierarchyProcessor;

    @Mock
    private AccountCatalogueBatchProcessor batchProcessor;

    @InjectMocks
    private AccountCatalogueAsyncImportProcessor asyncImportProcessor;

    private String entId;
    private String jobId;
    private String fileName;
    private byte[] fileBytes;
    private AccountCatalogueImportRequest request;
    private AccountCatalogueExcelData excelData;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        jobId = "JOB-001";
        fileName = "catalogo.xlsx";
        fileBytes = new byte[]{1, 2, 3, 4, 5};

        request = AccountCatalogueImportRequest.builder()
                .entId(entId)
                .fileName(fileName)
                .build();

        excelData = AccountCatalogueExcelData.builder()
                .rowNumber(2)
                .idEnterprise(entId)
                .code("11050501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .build();
    }

    @Test
    @DisplayName("Debe procesar importación exitosamente sin errores")
    void testProcessImportAsyncSuccess() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(1)
                        .failureCount(0)
                        .skippedCount(0)
                        .errors(new ArrayList<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(new ArrayList<>());
        when(batchProcessor.processBatch(accountsData, entId)).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe manejar archivo vacío y marcar como fallido")
    void testProcessImportAsyncHandlesEmptyFile() {
        // Arrange
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(Collections.emptyList())
                        .errors(new ArrayList<>())
                        .totalRows(0)
                        .columnMap(new HashMap<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobMetrics(jobId, 0, 0, 0, 0);
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe manejar cuando todas las validaciones fallan")
    void testProcessImportAsyncHandlesAllValidationsFailed() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        List<ImportErrorDetail> validationErrors = List.of(
                ImportErrorDetail.builder()
                        .rowNumber(2)
                        .errorCode("VALIDATION_ERROR")
                        .errorMessage("Error de validación")
                        .build()
        );
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(Collections.emptyList())
                        .errors(validationErrors)
                        .totalProcessed(1)
                        .validCount(0)
                        .errorCount(1)
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
        verify(jobTracker).addErrors(eq(jobId), anyList());
        verify(duplicateDetectionService, never()).detectDuplicates(anyList(), anyString());
    }

    @Test
    @DisplayName("Debe manejar cuando todos los registros son duplicados")
    void testProcessImportAsyncHandlesAllDuplicates() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(Collections.emptyList())
                        .errors(new ArrayList<>())
                        .duplicateCount(1)
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
        verify(hierarchyProcessor, never()).sortByHierarchy(anyList());
    }

    @Test
    @DisplayName("Debe manejar duplicados con errores marcando como completado con errores")
    void testProcessImportAsyncHandlesDuplicatesWithErrors() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        List<ImportErrorDetail> errors = List.of(
                ImportErrorDetail.builder()
                        .rowNumber(2)
                        .errorCode("DUPLICATE_ERROR")
                        .errorMessage("Registro duplicado")
                        .build()
        );
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(errors)
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(Collections.emptyList())
                        .errors(new ArrayList<>())
                        .duplicateCount(1)
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED_WITH_ERRORS);
    }

    @Test
    @DisplayName("Debe manejar errores de jerarquía y filtrar registros afectados")
    void testProcessImportAsyncHandlesHierarchyErrors() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        List<ImportErrorDetail> hierarchyErrors = List.of(
                ImportErrorDetail.builder()
                        .rowNumber(2)
                        .errorCode("HIERARCHY_ERROR")
                        .errorMessage("Padre no encontrado")
                        .build()
        );
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(hierarchyErrors);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
        verify(batchProcessor, never()).processBatch(anyList(), anyString());
    }

    @Test
    @DisplayName("Debe procesar con errores parciales y marcar como completado con errores")
    void testProcessImportAsyncHandlesPartialErrors() {
        // Arrange
        AccountCatalogueExcelData excelData2 = AccountCatalogueExcelData.builder()
                .rowNumber(3)
                .idEnterprise(entId)
                .code("11050502")
                .description("Caja Menor")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData, excelData2);
        Map<String, Integer> columnMap = new HashMap<>();
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(2)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(2)
                        .validCount(2)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        List<ImportErrorDetail> processingErrors = List.of(
                ImportErrorDetail.builder()
                        .rowNumber(3)
                        .errorCode("PROCESSING_ERROR")
                        .errorMessage("Error al procesar")
                        .build()
        );
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(1)
                        .failureCount(1)
                        .skippedCount(0)
                        .errors(processingErrors)
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(new ArrayList<>());
        when(batchProcessor.processBatch(accountsData, entId)).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED_WITH_ERRORS);
    }

    @Test
    @DisplayName("Debe manejar excepción genérica y marcar como fallido")
    void testProcessImportAsyncHandlesGenericException() {
        // Arrange
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId))
                .thenThrow(new RuntimeException("Error de conexión"));

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe actualizar progreso correctamente en cada fase")
    void testProcessImportAsyncUpdatesProgress() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(1)
                        .failureCount(0)
                        .skippedCount(0)
                        .errors(new ArrayList<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(new ArrayList<>());
        when(batchProcessor.processBatch(accountsData, entId)).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateProgress(jobId, 5);
        verify(jobTracker).updateProgress(jobId, 20);
        verify(jobTracker).updateProgress(jobId, 40);
        verify(jobTracker).updateProgress(jobId, 60);
        verify(jobTracker).updateProgress(jobId, 70);
        verify(jobTracker).updateProgress(jobId, 80);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe manejar errores de parsing acumulados")
    void testProcessImportAsyncAccumulatesParsingErrors() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        List<ImportErrorDetail> parsingErrors = List.of(
                ImportErrorDetail.builder()
                        .rowNumber(3)
                        .errorCode("PARSING_ERROR")
                        .errorMessage("Error parseando fila")
                        .build()
        );
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(parsingErrors)
                        .totalRows(2)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(1)
                        .failureCount(0)
                        .skippedCount(0)
                        .errors(new ArrayList<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(new ArrayList<>());
        when(batchProcessor.processBatch(accountsData, entId)).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).addErrors(eq(jobId), argThat(errors -> errors.size() >= 1));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED_WITH_ERRORS);
    }

    @Test
    @DisplayName("Debe marcar como fallido cuando successCount es cero")
    void testProcessImportAsyncFailsWhenNoSuccess() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(1)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(0)
                        .failureCount(1)
                        .skippedCount(0)
                        .errors(new ArrayList<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(new ArrayList<>());
        when(batchProcessor.processBatch(accountsData, entId)).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
    }

    @Test
    @DisplayName("Debe procesar registros válidos después de filtrar errores de jerarquía")
    void testProcessImportAsyncProcessesValidAfterHierarchyFilter() {
        // Arrange
        AccountCatalogueExcelData validData = AccountCatalogueExcelData.builder()
                .rowNumber(2)
                .idEnterprise(entId)
                .code("1")
                .description("Activo")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        AccountCatalogueExcelData invalidData = AccountCatalogueExcelData.builder()
                .rowNumber(3)
                .idEnterprise(entId)
                .code("11050501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(validData, invalidData);
        Map<String, Integer> columnMap = new HashMap<>();
        List<ImportErrorDetail> hierarchyErrors = List.of(
                ImportErrorDetail.builder()
                        .rowNumber(3)
                        .errorCode("HIERARCHY_ERROR")
                        .errorMessage("Padre no encontrado")
                        .build()
        );
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(new ArrayList<>())
                        .totalRows(2)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(new ArrayList<>())
                        .totalProcessed(2)
                        .validCount(2)
                        .errorCount(0)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(new ArrayList<>())
                        .duplicateCount(0)
                        .build();
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(1)
                        .failureCount(0)
                        .skippedCount(0)
                        .errors(new ArrayList<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(hierarchyErrors);
        when(batchProcessor.processBatch(anyList(), eq(entId))).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(batchProcessor).processBatch(argThat(list -> list.size() == 1), eq(entId));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED_WITH_ERRORS);
    }

    @Test
    @DisplayName("Debe acumular errores de todas las fases")
    void testProcessImportAsyncAccumulatesAllErrors() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData);
        Map<String, Integer> columnMap = new HashMap<>();
        
        List<ImportErrorDetail> parsingErrors = List.of(
                ImportErrorDetail.builder().rowNumber(5).errorCode("PARSE").errorMessage("Parse error").build()
        );
        List<ImportErrorDetail> validationErrors = List.of(
                ImportErrorDetail.builder().rowNumber(6).errorCode("VALID").errorMessage("Validation error").build()
        );
        List<ImportErrorDetail> duplicateErrors = List.of(
                ImportErrorDetail.builder().rowNumber(7).errorCode("DUP").errorMessage("Duplicate error").build()
        );
        
        AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                AccountCatalogueExcelParsingService.ExcelParsingResult.builder()
                        .accountsData(accountsData)
                        .errors(parsingErrors)
                        .totalRows(4)
                        .columnMap(columnMap)
                        .build();
        
        AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                AccountCatalogueBatchValidationService.BatchValidationResult.builder()
                        .validRecords(accountsData)
                        .errors(validationErrors)
                        .totalProcessed(1)
                        .validCount(1)
                        .errorCount(1)
                        .build();
        
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult.builder()
                        .uniqueRecords(accountsData)
                        .errors(duplicateErrors)
                        .duplicateCount(0)
                        .build();
        
        AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                AccountCatalogueBatchProcessor.BatchProcessingResult.builder()
                        .successCount(1)
                        .failureCount(0)
                        .skippedCount(0)
                        .errors(new ArrayList<>())
                        .build();
        
        when(excelParsingService.parseExcelFileFromBytes(fileBytes, entId)).thenReturn(parsingResult);
        when(batchValidationService.validateBatch(eq(accountsData), eq(entId), eq(columnMap))).thenReturn(validationResult);
        when(duplicateDetectionService.detectDuplicates(accountsData, entId)).thenReturn(duplicateResult);
        when(hierarchyProcessor.sortByHierarchy(accountsData)).thenReturn(accountsData);
        when(hierarchyProcessor.validateHierarchyWithDetails(accountsData, entId)).thenReturn(new ArrayList<>());
        when(batchProcessor.processBatch(accountsData, entId)).thenReturn(processingResult);

        // Act
        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

        // Assert
        verify(jobTracker).addErrors(eq(jobId), argThat(errors -> errors.size() >= 3));
    }
}
