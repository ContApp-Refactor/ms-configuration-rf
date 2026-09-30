package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueErrorCode;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueImportException;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @brief Servicio para detección de duplicados en importación de cuentas
 *
 * Identifica y maneja registros duplicados durante el proceso de importación
 * masiva de cuentas contables desde archivos Excel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueDuplicateDetectionService {

    private final IAccountCatalogueRepository accountCatalogueRepository;

    /**
     * @brief Evalúa registro individual contra duplicados internos y de base de datos
     * 
     * @param accountData registro Excel a evaluar
     * @param entId ID de empresa para contexto de consulta
     * @param seenCodes códigos ya vistos en este lote
     * @param seenDescriptions descripciones ya vistas en este lote
     * @param dbDuplicatesByCode mapa de códigos a entidades encontradas en base de datos
     * @param dbDuplicatesByDescription mapa de descripciones normalizadas a entidades encontradas en base de datos
     * @return true si el registro es duplicado, false en caso contrario
     */
    private boolean isDuplicateRecord(AccountCatalogueExcelData accountData, String entId,
            Set<String> seenCodes, Set<String> seenDescriptions,
            Map<String, AccountCatalogueEntity> dbDuplicatesByCode,
            Map<String, AccountCatalogueEntity> dbDuplicatesByDescription) {

        String code = accountData.getCode();
        String description = accountData.getDescription();
        String normalizedDescription = StringNormalizer.normalizeForComparison(description);
        normalizedDescription = normalizedDescription != null ? normalizedDescription : "";
        String codeKey = code + "_" + entId;
        String descKey = normalizedDescription + "_" + entId;

        // Verificar duplicados internos
        if (seenCodes.contains(codeKey)) {
            return true;
        }
        seenCodes.add(codeKey);

        if (seenDescriptions.contains(descKey)) {
            return true;
        }
        seenDescriptions.add(descKey);

        // Verificar duplicados en BD
        return dbDuplicatesByCode.containsKey(code) ||
               dbDuplicatesByDescription.containsKey(normalizedDescription);
    }

    /**
     * @brief Detecta duplicados por código y descripción en Excel y base de datos
     *
     * Implementa algoritmo de detección híbrida: compara registros internos del Excel
     * y consulta base de datos para códigos/descripciones existentes.     * 
     * @param accountsData registros Excel a analizar
     * @param entId ID de empresa para contexto de consulta
     * @return resultado de detección con registros únicos y métricas de duplicados
     */
    public DuplicateDetectionResult detectDuplicates(List<AccountCatalogueExcelData> accountsData, String entId) {
        Set<String> seenCodes = new HashSet<>();
        Set<String> seenDescriptions = new HashSet<>();
        List<AccountCatalogueExcelData> uniqueRecords = new ArrayList<>();
        int duplicateCount = 0;

        // 1. Obtener códigos y descripciones para consultar en BD
        List<String> codes = accountsData.stream()
                .map(AccountCatalogueExcelData::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> descriptions = accountsData.stream()
                .map(AccountCatalogueExcelData::getDescription)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(desc -> !desc.isEmpty())
                .distinct()
                .toList();

        // 2. Detectar duplicados en base de datos
        Map<String, AccountCatalogueEntity> dbDuplicatesByCode = detectDatabaseDuplicatesByCode(codes, entId);
        Map<String, AccountCatalogueEntity> dbDuplicatesByDescription = detectDatabaseDuplicatesByDescription(
                descriptions, entId);

        // 3. Procesar cada registro y filtrar duplicados silenciosamente
        for (AccountCatalogueExcelData accountData : accountsData) {
            boolean isDuplicate = isDuplicateRecord(accountData, entId, seenCodes, seenDescriptions,
                    dbDuplicatesByCode, dbDuplicatesByDescription);

            if (isDuplicate) {
                duplicateCount++;
            } else {
                uniqueRecords.add(accountData);
            }
        }

        return DuplicateDetectionResult.builder()
                .uniqueRecords(uniqueRecords)
                .errors(new ArrayList<>()) // Sin errores, solo omisión silenciosa
                .totalAnalyzed(accountsData.size())
                .duplicateCount(duplicateCount)
                .uniqueCount(uniqueRecords.size())
                .build();
    }

    /**
     * @brief Consulta base de datos para detectar códigos existentes EN BATCH
     * @details Usa findByCodesIn para ejecutar una sola query con IN clause
     * en lugar de N queries individuales. Optimización crítica para importación masiva.
     * @param codes códigos a consultar
     * @param entId ID de empresa para contexto de consulta
     * @return mapa de códigos a entidades encontradas
     */
    private Map<String, AccountCatalogueEntity> detectDatabaseDuplicatesByCode(
            List<String> codes, String entId) {
        if (codes == null || codes.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, AccountCatalogueEntity> duplicates = new HashMap<>();

        try {
           
            List<AccountCatalogueEntity> existingAccounts = accountCatalogueRepository.findByCodesIn(codes, entId);
            
            // Construir mapa de resultados
            for (AccountCatalogueEntity entity : existingAccounts) {
                duplicates.put(entity.getCode(), entity);
            }

        } catch (Exception e) {
            throw new AccountCatalogueImportException(
                AccountCatalogueErrorCode.ACCOUNT_IMPORT_ERROR,
                "Error al consultar duplicados por código en batch",
                e
            );
        }

        return duplicates;
    }

    /**
     * @brief Consulta base de datos para detectar descripciones existentes EN BATCH
     * @details Usa findByDescriptionsInIgnoreCase para ejecutar una sola query con IN clause
     * en lugar de N queries individuales. Normaliza las descripciones para comparación.
     * @param descriptions descripciones a consultar
     * @param entId ID de empresa para contexto de consulta
     * @return mapa de descripciones normalizadas a entidades encontradas
     */
    private Map<String, AccountCatalogueEntity> detectDatabaseDuplicatesByDescription(
            List<String> descriptions, String entId) {
        if (descriptions == null || descriptions.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, AccountCatalogueEntity> duplicates = new HashMap<>();

        try {
            // Convertir a uppercase para la query (case-insensitive)
            List<String> upperDescriptions = descriptions.stream()
                    .map(String::toUpperCase)
                    .distinct()
                    .toList();
            
            List<AccountCatalogueEntity> existingAccounts = accountCatalogueRepository
                    .findByDescriptionsInIgnoreCase(upperDescriptions, entId);
            
            // Construir mapa de resultados con descripciones normalizadas
            for (AccountCatalogueEntity entity : existingAccounts) {
                String normalized = StringNormalizer.normalizeForComparison(entity.getDescription());
                if (normalized == null) {
                    normalized = "";
                }
                duplicates.put(normalized, entity);
            }

        } catch (Exception e) {
            throw new AccountCatalogueImportException(
                AccountCatalogueErrorCode.ACCOUNT_IMPORT_ERROR,
                "Error al consultar duplicados por descripción en batch",
                e
            );
        }

        return duplicates;
    }

    /**
     * @brief Clase que representa el resultado de detección de duplicados
     *
     * Contiene registros únicos encontrados y errores generados durante
     * el análisis de duplicados. Métricas de conteo para tracking.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateDetectionResult {
        private List<AccountCatalogueExcelData> uniqueRecords;
        private List<ImportErrorDetail> errors;
        private int totalAnalyzed;
        private int duplicateCount;
        private int uniqueCount;
    }
}
