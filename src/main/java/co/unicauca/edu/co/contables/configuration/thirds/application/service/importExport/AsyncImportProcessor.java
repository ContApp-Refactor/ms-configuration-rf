package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.application.service.third.BatchProcessor;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdImportException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdsErrorCode;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdExcelData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @brief Servicio dedicado exclusivamente al procesamiento asíncrono de importaciones
 * 
 * Esta clase está separada de ImportThirdService para evitar problemas de self-invocation
 * con @Async. Spring requiere que los métodos @Async se llamen desde otra clase para
 * que el proxy AOP funcione correctamente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncImportProcessor {

    private final ExcelParsingService excelParsingService;
    private final BatchValidationService batchValidationService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final BatchProcessor batchProcessor;
    private final ImportJobTracker jobTracker;
    
    private static final boolean SKIP_DUPLICATES = true;
    private static final boolean CONTINUE_ON_ERROR = true;
    private static final int MAX_BATCH_SIZE = 1000;

    /**
     * @brief Procesa la importación de forma asíncrona con tracking de estado
     * 
     * Este método se ejecuta en un thread separado del pool de @Async configurado en AsyncConfig.
     * 
     * @param fileBytes contenido del archivo Excel en bytes
     * @param entId identificador de la entidad
     * @param fileName nombre del archivo
     * @param jobId identificador único del job
     */
    @Async
    public void processImportAsync(byte[] fileBytes, String entId, String fileName, String jobId) {
        try {
            jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
            jobTracker.updateProgress(jobId, 10);

            // 1. PARSEO: Extraer datos del Excel desde bytes
            ExcelParsingService.ExcelParsingResult parsingResult =
                    excelParsingService.parseExcelFileFromBytes(fileBytes, entId);
            jobTracker.updateProgress(jobId, 20);

            if (parsingResult.getThirdsData().isEmpty()) {
                handleAsyncError(jobId, "No se encontraron datos válidos para importar",
                        parsingResult.getErrors());
                return;
            }

            // 2. VALIDACIÓN: Validar datos en lote con cache optimizado
            BatchValidationService.BatchValidationResult validationResult =
                    batchValidationService.validateBatch(parsingResult.getThirdsData(), entId,
                            parsingResult.getColumnMap());
            jobTracker.updateProgress(jobId, 40);

            // 3. DETECCIÓN DE DUPLICADOS: Filtrar duplicados existentes
            DuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                    duplicateDetectionService.detectDuplicates(validationResult.getValidRecords(),
                            entId, SKIP_DUPLICATES);
            jobTracker.updateProgress(jobId, 60);

            // 4. PROCESAMIENTO: Procesar registros únicos en lotes transaccionales
            BatchProcessingResult processingResult = processInBatches(duplicateResult.getUniqueRecords(), jobId);
            jobTracker.updateProgress(jobId, 90);

            // 5. RESPUESTA: Consolidar y almacenar resultados finales
            buildFinalAsyncResponse(jobId, entId, fileName, parsingResult, validationResult,
                    duplicateResult, processingResult);

            jobTracker.updateProgress(jobId, 100);

        } catch (Exception e) {
            handleAsyncCriticalError(jobId, e);
        }
    }
    

    /**
     * @brief Procesar registros únicos en lotes optimizados con tracking de progreso
     */
    private BatchProcessingResult processInBatches(List<ThirdExcelData> uniqueRecords, String jobId) {
        if (uniqueRecords.isEmpty()) {
            return new BatchProcessingResult(0, 0, 0);
        }

        String entId = uniqueRecords.get(0).getEntId();
        BatchValidationService.ReferenceDataCache cache = batchValidationService.preloadReferenceData(entId);

        List<List<ThirdExcelData>> batches = createBatches(uniqueRecords, MAX_BATCH_SIZE);

        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFailure = new AtomicInteger(0);
        AtomicInteger totalSkipped = new AtomicInteger(0);

        int totalBatches = batches.size();
        for (int i = 0; i < totalBatches; i++) {
            List<ThirdExcelData> batch = batches.get(i);

            try {
                BatchProcessor.BatchProcessingResult batchResult = batchProcessor.processBatch(
                        batch, cache, CONTINUE_ON_ERROR);

                totalSuccess.addAndGet(batchResult.getSuccessCount());
                totalFailure.addAndGet(batchResult.getFailureCount());
                totalSkipped.addAndGet(batchResult.getSkippedCount());

                int batchProgress = 60 + (30 * (i + 1) / totalBatches);
                jobTracker.updateProgress(jobId, batchProgress);
                

            } catch (Exception e) {
                throw new ThirdImportException(ThirdsErrorCode.THIRD_EXPORT_ERROR,
                        "Error crítico en lote " + (i + 1) + ": " + e.getMessage(), e);
            }
        }

        // Resumen de rendimiento de la Fase 4

        return new BatchProcessingResult(totalSuccess.get(), totalFailure.get(), totalSkipped.get());
    }

    private void buildFinalAsyncResponse(String jobId, String entId, String fileName,
            ExcelParsingService.ExcelParsingResult parsingResult,
            BatchValidationService.BatchValidationResult validationResult,
            DuplicateDetectionService.DuplicateDetectionResult duplicateResult,
            BatchProcessingResult processingResult) {

        List<ImportErrorDetail> allErrors = new ArrayList<>();
        allErrors.addAll(parsingResult.getErrors());
        allErrors.addAll(validationResult.getErrors());
        allErrors.addAll(duplicateResult.getErrors());

        int duplicatesFromDetection = validationResult.getValidCount() - duplicateResult.getUniqueCount();
        int duplicatesFromProcessing = processingResult.getSkippedCount();
        int totalDuplicatesSkipped = duplicatesFromDetection + duplicatesFromProcessing;

        int validationFailures = calculateUniqueFailedRecords(validationResult.getErrors());
        int processingFailures = processingResult.getFailureCount();
        int totalFailures = validationFailures + processingFailures;

        jobTracker.updateJobMetrics(jobId, 
                parsingResult.getTotalRows(),
                processingResult.getSuccessCount(),
                totalFailures,
                totalDuplicatesSkipped);
        
        jobTracker.addErrors(jobId, allErrors);

        ImportStatus finalStatus;
        if (totalFailures > 0 && processingResult.getSuccessCount() > 0) {
            finalStatus = ImportStatus.COMPLETED_WITH_ERRORS;
        } else if (processingResult.getSuccessCount() == 0) {
            finalStatus = ImportStatus.FAILED;
        } else {
            finalStatus = ImportStatus.COMPLETED;
        }

        jobTracker.updateJobStatus(jobId, finalStatus);
    }

    private void handleAsyncError(String jobId, String errorMessage, List<ImportErrorDetail> errors) {
        jobTracker.updateJobMetrics(jobId, 0, 0, 0, 0);
        jobTracker.addErrors(jobId, errors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
    }

    private void handleAsyncCriticalError(String jobId, Exception e) {
        List<ImportErrorDetail> systemErrors = List.of(
                ImportErrorDetail.builder()
                        .errorCode("SYSTEM_ERROR")
                        .errorMessage("Error del sistema durante la importación: " + e.getMessage())
                        .errorType(ImportErrorType.SYSTEM_ERROR)
                        .build());

        jobTracker.addErrors(jobId, systemErrors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
    }

    private <T> List<List<T>> createBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    private int calculateUniqueFailedRecords(List<ImportErrorDetail> errors) {
        return (int) errors.stream()
                .mapToInt(ImportErrorDetail::getRowNumber)
                .distinct()
                .count();
    }

    @Getter
    @AllArgsConstructor
    private static class BatchProcessingResult {
        private final int successCount;
        private final int failureCount;
        private final int skippedCount;
    }
}

