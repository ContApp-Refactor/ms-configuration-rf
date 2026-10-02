package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueCreateService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueDataConverter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueHierarchyProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.AccountCodeUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @brief Servicio para procesamiento por lotes de cuentas contables
 *
 * Servicio especializado en procesamiento por lotes de cuentas contables.
 * Procesa registros en lotes transaccionales para optimizar rendimiento.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueBatchProcessor {

    private final AccountCatalogueCreateService createService;
    private final AccountCatalogueDataConverter dataConverter;
    private final AccountCatalogueHierarchyProcessor hierarchyProcessor;

    /**
     * @brief Coordina procesamiento por lotes con particionamiento y transacciones independientes
     *
     * Divide la lista de cuentas en lotes de tamaño BATCH_SIZE, procesa cada lote
     * en una transacción independiente, mantiene mapa de cuentas procesadas para
     * resolución jerárquica, y acumula estadísticas de éxito/fallo/omisión.
     * @param accountsData lista ordenada jerárquicamente de cuentas a procesar
     * @param entId ID de empresa para aislamiento de datos
     * @return estadísticas completas del procesamiento por lotes
     */
    public BatchProcessingResult processBatch(List<AccountCatalogueExcelData> accountsData, String entId) {
        List<ImportErrorDetail> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;

        // Dividir en lotes de tamaño BATCH_SIZE
        List<List<AccountCatalogueExcelData>> batches = partitionList(accountsData, ImportConstants.Defaults.BATCH_SIZE);

        // Mapa para almacenar cuentas ya procesadas y poder usarlas como padres
        Map<String, AccountCatalogue> processedAccountsMap = new HashMap<>();

        // Procesar cada lote
        for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
            List<AccountCatalogueExcelData> batch = batches.get(batchIndex);
            
            try {
                BatchResult batchResult = processSingleBatch(batch, entId, processedAccountsMap, batchIndex + 1);
                
                successCount += batchResult.getSuccessCount();
                failureCount += batchResult.getFailureCount();
                skippedCount += batchResult.getSkippedCount();
                errors.addAll(batchResult.getErrors());

            } catch (Exception e) {
                
                // Registrar error para todos los registros del lote
                for (AccountCatalogueExcelData record : batch) {
                    errors.add(ImportErrorDetail.builder()
                            .rowNumber(record.getRowNumber())
                            .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                            .errorMessage("Error del sistema procesando lote: " + e.getMessage())
                            .errorType(ImportErrorType.SYSTEM_ERROR)
                            .build());
                }
                
                failureCount += batch.size();

                // Si CONTINUE_ON_ERROR es false, detener
                if (!ImportConstants.Defaults.CONTINUE_ON_ERROR) {
                    
                    // Marcar registros restantes como omitidos
                    for (int i = batchIndex + 1; i < batches.size(); i++) {
                        skippedCount += batches.get(i).size();
                    }
                    
                    break;
                }
            }
        }

        return BatchProcessingResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .skippedCount(skippedCount)
                .errors(errors)
                .totalProcessed(successCount + failureCount)
                .build();
    }

    /**
     * @brief Ejecuta procesamiento transaccional de un lote individual con resolución jerárquica
     *
     * Extrae códigos de padres requeridos, construye mapa de padres desde BD,
     * procesa cada registro convirtiendo a dominio y creando en BD.
     * Actualiza mapa de procesados para uso en lotes posteriores.
     * Maneja errores por registro según configuración CONTINUE_ON_ERROR.
     */
    @Transactional
    protected BatchResult processSingleBatch(List<AccountCatalogueExcelData> batch, String entId, 
                                            Map<String, AccountCatalogue> processedAccountsMap, int batchNumber) {
        List<ImportErrorDetail> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            // Obtener códigos únicos de padres necesarios para este lote
            Set<String> requiredParentCodes = extractRequiredParentCodes(batch);
            
            // Construir mapa de padres desde BD (solo los que no están en processedAccountsMap)
            Set<String> missingParentCodes = new HashSet<>(requiredParentCodes);
            missingParentCodes.removeAll(processedAccountsMap.keySet());
            
            Map<String, AccountCatalogueEntity> parentsMap = 
                    hierarchyProcessor.buildParentMapFromDatabase(missingParentCodes, entId);

            // Agrupa cuentas por longitud de código (nivel jerárquico)
            Map<Integer, List<AccountCatalogueExcelData>> accountsByLevel = new TreeMap<>();
            for (AccountCatalogueExcelData excelData : batch) {
                int level = excelData.getCode().length();
                accountsByLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(excelData);
            }
            
            // Procesar nivel por nivel (1→2→4→6→8 dígitos)
            for (Map.Entry<Integer, List<AccountCatalogueExcelData>> levelEntry : accountsByLevel.entrySet()) {
                List<AccountCatalogueExcelData> levelAccounts = levelEntry.getValue();

                // Convertir todas las cuentas del nivel a dominio
                List<AccountCatalogue> accountsToCreate = new ArrayList<>();
                List<AccountCatalogueExcelData> validExcelData = new ArrayList<>();
                
                for (AccountCatalogueExcelData excelData : levelAccounts) {
                    try {
                        // Los padres de este nivel YA están en processedAccountsMap o parentsMap
                        AccountCatalogue accountCatalogue = dataConverter.convertToAccountCatalogue(
                                excelData, parentsMap, processedAccountsMap);
                        accountsToCreate.add(accountCatalogue);
                        validExcelData.add(excelData);
                    } catch (Exception e) {
                        failureCount++;
                        errors.add(ImportErrorDetail.builder()
                                .rowNumber(excelData.getRowNumber())
                                .columnName(ImportConstants.CODE_COLUMN)
                                .fieldValue(excelData.getCode())
                                .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                                .errorMessage("Error convirtiendo cuenta: " + e.getMessage())
                                .errorType(ImportErrorType.SYSTEM_ERROR)
                                .build());
                    }
                }
                
                // Batch insert de todo el nivel (mucho más rápido)
                if (!accountsToCreate.isEmpty()) {
                    List<AccountCatalogue> createdAccounts = createService.createAllAccountCatalogues(accountsToCreate);
                    
                    // Agregar todas al mapa de procesados para el siguiente nivel
                    for (AccountCatalogue created : createdAccounts) {
                        processedAccountsMap.put(created.getCode(), created);
                        successCount++;
                    }
                }
            }

        } catch (Exception e) {
            // Si hay error crítico, marcar todo el lote como fallido
            for (AccountCatalogueExcelData excelData : batch) {
                errors.add(ImportErrorDetail.builder()
                        .rowNumber(excelData.getRowNumber())
                        .columnName(ImportConstants.CODE_COLUMN)
                        .fieldValue(excelData.getCode())
                        .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                        .errorMessage("Error de lote: " + e.getMessage())
                        .errorType(ImportErrorType.SYSTEM_ERROR)
                        .build());
            }
            failureCount = batch.size();
            
            if (!ImportConstants.Defaults.CONTINUE_ON_ERROR) {
                throw e;
            }
        }

        return BatchResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .skippedCount(0)
                .errors(errors)
                .build();
    }

    /**
     * @brief Extrae códigos de padres requeridos para resolución jerárquica
     *
     * Analiza cada cuenta del lote, extrae el código padre usando AccountCodeUtils,
     * y acumula códigos únicos en un Set para evitar duplicados.
     * Los códigos padre serán usados para consultar entidades existentes en BD.
     */
    private Set<String> extractRequiredParentCodes(List<AccountCatalogueExcelData> batch) {
        Set<String> parentCodes = new HashSet<>();
        
        for (AccountCatalogueExcelData data : batch) {
            String parentCode = AccountCodeUtils.extractParentCode(data.getCode());
            if (parentCode != null) {
                parentCodes.add(parentCode);
            }
        }
        
        return parentCodes;
    }

    /**
     * @brief Divide lista en sublistas para procesamiento por lotes
     *
     * Implementa algoritmo de particionamiento que divide una lista grande
     * en sublistas más pequeñas de tamaño batchSize. Usa Math.min para manejar
     * el último lote que puede ser más pequeño que batchSize.
     * @param list lista original a dividir
     * @param batchSize tamaño máximo de cada sublista
     * @return lista de sublistas, cada una con máximo batchSize elementos
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(new ArrayList<>(list.subList(i, end)));
        }
        
        return batches;
    }

    /**
     * @brief Resultado de procesamiento de un lote individual
     *
     * Contiene estadísticas de éxito/fallo/omisión y errores específicos
     * de un lote procesado en una transacción independiente.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BatchResult {
        private int successCount;
        private int failureCount;
        private int skippedCount;
        private List<ImportErrorDetail> errors;
    }

    /**
     * @brief Resultado completo del procesamiento por lotes
     *
     * Agrega estadísticas acumuladas de todos los lotes procesados,
     * incluyendo total procesado y lista completa de errores.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchProcessingResult {
        private int successCount;
        private int failureCount;
        private int skippedCount;
        private List<ImportErrorDetail> errors;
        private int totalProcessed;
    }
}


