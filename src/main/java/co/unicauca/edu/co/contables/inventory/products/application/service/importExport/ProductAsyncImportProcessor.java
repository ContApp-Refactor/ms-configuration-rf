package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;
import co.unicauca.edu.co.contables.inventory.products.domain.utils.ImportConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @brief Servicio dedicado exclusivamente al procesamiento asíncrono de importaciones de productos
 *
 * Esta clase está separada de ProductImportService para evitar problemas de self-invocation
 * con @Async. Spring requiere que los métodos @Async se llamen desde otra clase para
 * que el proxy AOP funcione correctamente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAsyncImportProcessor {

    private final ProductExcelParsingService excelParsingService;
    private final ProductBatchValidationService batchValidationService;
    private final ProductBatchProcessor batchProcessor;
    private final ProductImportJobTracker jobTracker;

    /**
     * @brief Procesa la importación de productos de forma asíncrona con tracking de estado
     *
     * Este método se ejecuta en un thread separado del pool de @Async configurado en AsyncConfig.
     *
     * @param fileBytes contenido del archivo Excel en bytes
     * @param entId identificador de la empresa
     * @param fileName nombre del archivo
     * @param jobId identificador único del job
     */
    @Async
    public void processImportAsync(byte[] fileBytes, String entId, String fileName, String jobId) {
        try {
            jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
            jobTracker.updateProgress(jobId, 10);

            // FASE 1: Parseo del archivo Excel desde bytes
            ProductExcelParsingService.ExcelParsingResult parsingResult =
                    excelParsingService.parseExcelFileFromBytes(fileBytes, entId);
            jobTracker.updateProgress(jobId, 25);

            if (parsingResult.getProductsData().isEmpty()) {
                handleAsyncError(jobId, "No se encontraron datos válidos para importar",
                        parsingResult.getErrors(), 0);
                return;
            }

            // Filtrar productos sin errores de parsing
            List<ProductExcelData> productsWithoutParsingErrors = filterProductsWithoutParsingErrors(
                    parsingResult.getProductsData(), parsingResult.getErrors());

            int parsingErrorCount = calculateUniqueFailedRecords(parsingResult.getErrors());

            if (productsWithoutParsingErrors.isEmpty()) {
                handleAsyncError(jobId, "Todos los registros tienen errores de parsing",
                        parsingResult.getErrors(), parsingResult.getTotalRows());
                return;
            }

            // FASE 2: Validación de datos en lote
            ProductBatchValidationService.BatchValidationResult validationResult =
                    batchValidationService.validateBatch(productsWithoutParsingErrors, entId,
                            parsingResult.getColumnMap());
            jobTracker.updateProgress(jobId, 50);

            if (validationResult.getValidRecords().isEmpty()) {
                handleValidationOnlyErrors(jobId, parsingResult, validationResult, parsingErrorCount);
                return;
            }

            // FASE 3: Procesamiento por lotes con seguimiento de progreso
            ProcessingResult processingResult = processInBatches(
                    validationResult.getValidRecords(), entId, jobId);
            jobTracker.updateProgress(jobId, 90);

            // FASE 4: Consolidación de resultados finales
            buildFinalAsyncResponse(jobId, entId, fileName, parsingResult, validationResult,
                    processingResult, parsingErrorCount);

            jobTracker.updateProgress(jobId, 100);

        } catch (Exception e) {
            handleAsyncCriticalError(jobId, e);
        }
    }

    /**
     * @brief Procesa registros válidos en lotes optimizados con tracking de progreso
     */
    private ProcessingResult processInBatches(List<ProductExcelData> validRecords, String entId, String jobId) {
        if (validRecords.isEmpty()) {
            return new ProcessingResult(0, 0, new ArrayList<>());
        }

        List<List<ProductExcelData>> batches = createBatches(validRecords, ImportConstants.Defaults.BATCH_SIZE);

        int totalSuccess = 0;
        int totalFailure = 0;
        List<ImportErrorDetail> allErrors = new ArrayList<>();

        int totalBatches = batches.size();
        for (int i = 0; i < totalBatches; i++) {
            List<ProductExcelData> batch = batches.get(i);

            try {
                ProductBatchProcessor.BatchProcessingResult batchResult =
                        batchProcessor.processBatch(batch, entId);

                totalSuccess += batchResult.getSuccessCount();
                totalFailure += batchResult.getFailureCount();
                allErrors.addAll(batchResult.getErrors());

                int batchProgress = 50 + (40 * (i + 1) / totalBatches);
                jobTracker.updateProgress(jobId, batchProgress);

            } catch (Exception e) {
                if (!ImportConstants.Defaults.CONTINUE_ON_ERROR) {
                    throw e;
                }
            }
        }

        return new ProcessingResult(totalSuccess, totalFailure, allErrors);
    }

    /**
     * @brief Filtra productos sin errores de parsing
     */
    private List<ProductExcelData> filterProductsWithoutParsingErrors(
            List<ProductExcelData> allProducts, List<ImportErrorDetail> parsingErrors) {

        if (parsingErrors == null || parsingErrors.isEmpty()) {
            return allProducts;
        }

        Set<Integer> errorRows = parsingErrors.stream()
                .map(ImportErrorDetail::getRowNumber)
                .collect(Collectors.toSet());

        return allProducts.stream()
                .filter(product -> !errorRows.contains(product.getRowNumber()))
                .toList();
    }

    /**
     * @brief Consolida y almacena resultados finales en el job tracker
     */
    private void buildFinalAsyncResponse(String jobId, String entId, String fileName,
                                          ProductExcelParsingService.ExcelParsingResult parsingResult,
                                          ProductBatchValidationService.BatchValidationResult validationResult,
                                          ProcessingResult processingResult,
                                          int parsingErrorCount) {

        List<ImportErrorDetail> allErrors = new ArrayList<>();
        allErrors.addAll(parsingResult.getErrors());
        allErrors.addAll(validationResult.getErrors());
        allErrors.addAll(processingResult.getErrors());

        int validationFailures = calculateUniqueFailedRecords(validationResult.getErrors());
        int processingFailures = processingResult.getFailureCount();
        int totalFailures = parsingErrorCount + validationFailures + processingFailures;

        jobTracker.updateJobMetrics(jobId,
                parsingResult.getTotalRows(),
                processingResult.getSuccessCount(),
                totalFailures,
                validationResult.getDuplicateCount());

        jobTracker.addErrors(jobId, allErrors);

        ImportStatus finalStatus;
        if (totalFailures > 0 && processingResult.getSuccessCount() > 0) {
            finalStatus = ImportStatus.COMPLETED_WITH_ERRORS;
        } else if (totalFailures > 0) {
            // Solo es FAILED si hay errores reales (sin importaciones exitosas)
            finalStatus = ImportStatus.FAILED;
        } else {
            // Si no hay errores, es COMPLETED (incluso si solo hay duplicados omitidos)
            finalStatus = ImportStatus.COMPLETED;
        }

        jobTracker.updateJobStatus(jobId, finalStatus);
    }

    /**
     * @brief Maneja el caso cuando solo hay errores de validación sin procesamiento
     */
    private void handleValidationOnlyErrors(String jobId,
                                             ProductExcelParsingService.ExcelParsingResult parsingResult,
                                             ProductBatchValidationService.BatchValidationResult validationResult,
                                             int parsingErrorCount) {

        List<ImportErrorDetail> allErrors = new ArrayList<>();
        allErrors.addAll(parsingResult.getErrors());
        allErrors.addAll(validationResult.getErrors());

        int validationFailures = calculateUniqueFailedRecords(validationResult.getErrors());
        int totalFailures = parsingErrorCount + validationFailures;

        jobTracker.updateJobMetrics(jobId,
                parsingResult.getTotalRows(),
                0,
                totalFailures,
                validationResult.getDuplicateCount());

        jobTracker.addErrors(jobId, allErrors);
        
        // Si solo hay duplicados omitidos sin errores reales, marcar como COMPLETED
        ImportStatus finalStatus = (totalFailures > 0) ? ImportStatus.FAILED : ImportStatus.COMPLETED;
        jobTracker.updateJobStatus(jobId, finalStatus);
        jobTracker.updateProgress(jobId, 100);
    }

    /**
     * @brief Maneja errores durante el procesamiento asíncrono
     */
    private void handleAsyncError(String jobId, String errorMessage, List<ImportErrorDetail> errors, int totalRows) {
        jobTracker.updateJobMetrics(jobId, totalRows, 0, totalRows, 0);
        jobTracker.addErrors(jobId, errors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.updateProgress(jobId, 100);
    }

    /**
     * @brief Maneja errores críticos durante el procesamiento
     */
    private void handleAsyncCriticalError(String jobId, Exception e) {
        List<ImportErrorDetail> systemErrors = List.of(
                ImportErrorDetail.builder()
                        .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                        .errorMessage("Error del sistema durante la importación: " + e.getMessage())
                        .errorType(ImportErrorType.SYSTEM_ERROR)
                        .build());

        jobTracker.addErrors(jobId, systemErrors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.updateProgress(jobId, 100);
    }

    /**
     * @brief Divide una lista en sub-listas (batches)
     */
    private <T> List<List<T>> createBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    /**
     * @brief Calcula registros únicos con errores
     */
    private int calculateUniqueFailedRecords(List<ImportErrorDetail> errors) {
        if (errors == null || errors.isEmpty()) {
            return 0;
        }
        return (int) errors.stream()
                .mapToInt(ImportErrorDetail::getRowNumber)
                .distinct()
                .count();
    }

    /**
     * @brief Resultado del procesamiento por lotes
     */
    @Getter
    @AllArgsConstructor
    private static class ProcessingResult {
        private final int successCount;
        private final int failureCount;
        private final List<ImportErrorDetail> errors;
    }
}

