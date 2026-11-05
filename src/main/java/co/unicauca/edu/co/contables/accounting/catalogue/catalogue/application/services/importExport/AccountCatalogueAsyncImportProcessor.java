package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueHierarchyProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueBatchValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueImportRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @brief Procesador asíncrono para importación de catálogo de cuentas desde Excel
 *
 * Maneja el flujo completo de importación en un hilo separado, actualizando
 * el estado del trabajo en tiempo real a través del JobTracker.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueAsyncImportProcessor {

    private final AccountCatalogueImportJobTracker jobTracker;
    private final AccountCatalogueExcelParsingService excelParsingService;
    private final AccountCatalogueBatchValidationService batchValidationService;
    private final AccountCatalogueDuplicateDetectionService duplicateDetectionService;
    private final AccountCatalogueHierarchyProcessor hierarchyProcessor;
    private final AccountCatalogueBatchProcessor batchProcessor;

    /**
     * @brief Procesa la importación de forma asíncrona
     * @param request solicitud de importación con archivo y configuración
     * @param jobId identificador del trabajo
     * @param fileBytes contenido del archivo Excel en bytes
     */
    @Async
    public void processImportAsync(AccountCatalogueImportRequest request, String jobId, byte[] fileBytes) {
        String entId = request.getEntId();
        String fileName = request.getFileName();

        List<ImportErrorDetail> allErrors = new ArrayList<>();

        try {
            jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
            jobTracker.updateProgress(jobId, 5);

            // ==================== FASE 1: PARSING ====================
            AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult =
                    excelParsingService.parseExcelFileFromBytes(fileBytes, entId);

            allErrors.addAll(parsingResult.getErrors());

            if (parsingResult.getAccountsData().isEmpty()) {
                handleEmptyFile(jobId, entId, fileName, allErrors);
                return;
            }

            jobTracker.updateProgress(jobId, 20);
            jobTracker.updateJobMetrics(jobId, parsingResult.getTotalRows(), 0, 0, 0);

            // ==================== FASE 2: VALIDACIÓN ====================
            AccountCatalogueBatchValidationService.BatchValidationResult validationResult =
                    batchValidationService.validateBatch(
                            parsingResult.getAccountsData(), entId, parsingResult.getColumnMap());

            allErrors.addAll(validationResult.getErrors());

            if (validationResult.getValidRecords().isEmpty()) {
                handleAllValidationsFailed(jobId, entId, fileName, parsingResult.getTotalRows(), allErrors);
                return;
            }

            jobTracker.updateProgress(jobId, 40);

            // ==================== FASE 3: DETECCIÓN DE DUPLICADOS ====================
            AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult =
                    duplicateDetectionService.detectDuplicates(validationResult.getValidRecords(), entId);

            allErrors.addAll(duplicateResult.getErrors());

            if (duplicateResult.getUniqueRecords().isEmpty()) {
                handleNoDuplicates(jobId, entId, fileName, parsingResult, duplicateResult, allErrors);
                return;
            }

            jobTracker.updateProgress(jobId, 60);

            // ==================== FASE 4: ORDENAMIENTO JERÁRQUICO ====================
            List<AccountCatalogueExcelData> sortedAccounts =
                    hierarchyProcessor.sortByHierarchy(duplicateResult.getUniqueRecords());

            jobTracker.updateProgress(jobId, 70);

            // ==================== FASE 5: VALIDACIÓN DE JERARQUÍA ====================
            List<ImportErrorDetail> hierarchyErrors =
                    hierarchyProcessor.validateHierarchyWithDetails(sortedAccounts, entId);

            allErrors.addAll(hierarchyErrors);

            if (!hierarchyErrors.isEmpty()) {
                Set<Integer> errorRows = hierarchyErrors.stream()
                        .map(ImportErrorDetail::getRowNumber)
                        .collect(Collectors.toSet());

                sortedAccounts = sortedAccounts.stream()
                        .filter(account -> !errorRows.contains(account.getRowNumber()))
                        .toList();
            }

            if (sortedAccounts.isEmpty()) {
                handleEmptyAfterHierarchy(jobId, entId, fileName, parsingResult, duplicateResult, allErrors);
                return;
            }

            jobTracker.updateProgress(jobId, 80);

            // ==================== FASE 6: PROCESAMIENTO BATCH ====================
            AccountCatalogueBatchProcessor.BatchProcessingResult processingResult =
                    batchProcessor.processBatch(sortedAccounts, entId);

            allErrors.addAll(processingResult.getErrors());

            // ==================== FINALIZACIÓN ====================
            jobTracker.updateJobMetrics(
                    jobId,
                    parsingResult.getTotalRows(),
                    processingResult.getSuccessCount(),
                    processingResult.getFailureCount(),
                    duplicateResult.getDuplicateCount()
            );
            jobTracker.addErrors(jobId, allErrors);
            jobTracker.updateProgress(jobId, 100);

            ImportStatus finalStatus = determineFinalStatus(
                    processingResult.getSuccessCount(),
                    processingResult.getFailureCount(),
                    allErrors.size()
            );
            jobTracker.updateJobStatus(jobId, finalStatus);

        } catch (AccountCatalogueImportException e) {
            handleCriticalError(jobId, entId, fileName, e.getMessage(), allErrors);
        } catch (Exception e) {
            handleCriticalError(jobId, entId, fileName, "Error del sistema: " + e.getMessage(), allErrors);
        }
    }

    private void handleEmptyFile(String jobId, String entId, String fileName,
                                  List<ImportErrorDetail> allErrors) {
        jobTracker.updateJobMetrics(jobId, 0, 0, 0, 0);
        jobTracker.addErrors(jobId, allErrors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.updateProgress(jobId, 100);
    }

    private void handleAllValidationsFailed(String jobId, String entId, String fileName,
                                            int totalRows, List<ImportErrorDetail> allErrors) {
        long failedRecords = allErrors.stream()
                .map(ImportErrorDetail::getRowNumber)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        
        jobTracker.updateJobMetrics(jobId, totalRows, 0, (int) failedRecords, 0);
        jobTracker.addErrors(jobId, allErrors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.updateProgress(jobId, 100);
    }

    private void handleNoDuplicates(String jobId, String entId, String fileName,
                                    AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult,
                                    AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult,
                                    List<ImportErrorDetail> allErrors) {
        
        long failedRecords = allErrors.stream()
                .map(ImportErrorDetail::getRowNumber)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        jobTracker.updateJobMetrics(
                jobId,
                parsingResult.getTotalRows(),
                0,
                (int) failedRecords,
                duplicateResult.getDuplicateCount()
        );
        jobTracker.addErrors(jobId, allErrors);
        
        ImportStatus status = duplicateResult.getDuplicateCount() > 0 && allErrors.isEmpty()
                ? ImportStatus.COMPLETED
                : ImportStatus.COMPLETED_WITH_ERRORS;
        
        jobTracker.updateJobStatus(jobId, status);
        jobTracker.updateProgress(jobId, 100);
    }

    private void handleEmptyAfterHierarchy(String jobId, String entId, String fileName,
                                           AccountCatalogueExcelParsingService.ExcelParsingResult parsingResult,
                                           AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult duplicateResult,
                                           List<ImportErrorDetail> allErrors) {
        long failedRecords = allErrors.stream()
                .map(ImportErrorDetail::getRowNumber)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        jobTracker.updateJobMetrics(
                jobId,
                parsingResult.getTotalRows(),
                0,
                (int) failedRecords,
                duplicateResult.getDuplicateCount()
        );
        jobTracker.addErrors(jobId, allErrors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.updateProgress(jobId, 100);
    }

    private void handleCriticalError(String jobId, String entId, String fileName,
                                     String errorMessage, List<ImportErrorDetail> allErrors) {
        jobTracker.updateJobMetrics(jobId, 0, 0, 0, 0);
        jobTracker.addErrors(jobId, allErrors);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.updateProgress(jobId, 100);
    }

    private ImportStatus determineFinalStatus(int successCount, int failureCount, int errorCount) {
        if (successCount > 0 && (failureCount > 0 || errorCount > 0)) {
            return ImportStatus.COMPLETED_WITH_ERRORS;
        } else if (successCount > 0) {
            return ImportStatus.COMPLETED;
        } else {
            return ImportStatus.FAILED;
        }
    }

}

