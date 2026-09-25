package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdExcelData;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @brief Servicio especializado en la detección de duplicados durante la importación
 *
 * Maneja tanto duplicados internos del Excel como duplicados con la base de
 * datos existente, optimizando el rendimiento mediante consultas por lotes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateDetectionService {

    private final ThirdOutputPort thirdOutputPort;

    /**
     * @brief Detecta duplicados en un lote de datos de terceros
     * @param thirdsData lista de datos de terceros a analizar
     * @param entId identificador de la entidad
     * @param skipDuplicates indica si se deben omitir duplicados o marcar como error
     * @return resultado de detección con registros únicos y reportes de duplicados
     */
    public DuplicateDetectionResult detectDuplicates(List<ThirdExcelData> thirdsData, String entId,
            boolean skipDuplicates) {

        List<ThirdExcelData> uniqueRecords = new ArrayList<>();
        List<ImportErrorDetail> errors = new ArrayList<>();

        // 1. Detectar duplicados internos del Excel
        DuplicateAnalysisResult internalDuplicates = detectInternalDuplicates(thirdsData, skipDuplicates);
        uniqueRecords.addAll(internalDuplicates.getUniqueRecords());
        errors.addAll(internalDuplicates.getErrors());

        // 2. Detectar duplicados con la base de datos existente
        if (!uniqueRecords.isEmpty()) {
            DuplicateAnalysisResult dbDuplicates = detectDatabaseDuplicates(uniqueRecords, entId, skipDuplicates);
            uniqueRecords = dbDuplicates.getUniqueRecords();
            errors.addAll(dbDuplicates.getErrors());
        }

        return DuplicateDetectionResult.builder()
                .uniqueRecords(uniqueRecords)
                .errors(errors)
                .totalAnalyzed(thirdsData.size())
                .uniqueCount(uniqueRecords.size())
                .duplicateCount(thirdsData.size() - uniqueRecords.size())
                .build();
    }

    /**
     * @brief Detecta duplicados internos dentro del archivo Excel
     * @param thirdsData lista de datos a analizar
     * @param skipDuplicates indica si omitir duplicados o marcar como error
     * @return resultado con registros únicos y errores de duplicados internos
     */
    private DuplicateAnalysisResult detectInternalDuplicates(List<ThirdExcelData> thirdsData, boolean skipDuplicates) {

        List<ThirdExcelData> uniqueRecords = new ArrayList<>();
        List<ImportErrorDetail> errors = new ArrayList<>();

        // Mapa para rastrear registros ya procesados por ID de identificación
        Map<String, ThirdExcelData> processedRecords = new HashMap<>();

        for (ThirdExcelData currentRecord : thirdsData) {
            if (currentRecord.getIdNumber() == null) {
                continue;
            }

            String duplicateKey = createDuplicateKey(currentRecord);
            ThirdExcelData existingRecord = processedRecords.get(duplicateKey);

            if (existingRecord != null) {
                handleInternalDuplicate(currentRecord, existingRecord, skipDuplicates, errors);
            } else {
                processedRecords.put(duplicateKey, currentRecord);
                uniqueRecords.add(currentRecord);
            }
        }

        return DuplicateAnalysisResult.builder()
                .uniqueRecords(uniqueRecords)
                .errors(errors)
                .build();
    }

    /**
     * @brief Detecta duplicados con registros existentes en la base de datos
     * @param thirdsData lista de registros únicos a verificar contra BD
     * @param entId identificador de la entidad
     * @param skipDuplicates indica si omitir duplicados o marcar como error
     * @return resultado con registros únicos y errores de duplicados con BD
     */
    private DuplicateAnalysisResult detectDatabaseDuplicates(List<ThirdExcelData> thirdsData, String entId,
            boolean skipDuplicates) {

        List<ThirdExcelData> uniqueRecords = new ArrayList<>();
        List<ImportErrorDetail> errors = new ArrayList<>();

        // batch: verificar existencia
        Set<Long> idNumbers = thirdsData.stream()
                .map(ThirdExcelData::getIdNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> existingIds = thirdOutputPort.findExistingIdNumbers(idNumbers, entId);

        for (ThirdExcelData currentRecord : thirdsData) {
            if (currentRecord.getIdNumber() == null) {
                continue;
            }

            if (existingIds.contains(currentRecord.getIdNumber())) {
                handleDatabaseDuplicate(currentRecord, skipDuplicates, errors);
            } else {
                uniqueRecords.add(currentRecord);
            }
        }

        return DuplicateAnalysisResult.builder()
                .uniqueRecords(uniqueRecords)
                .errors(errors)
                .build();
    }

    /**
     * @brief Maneja un duplicado interno encontrado
     * @param currentRecord registro duplicado encontrado
     * @param existingRecord primer registro con el mismo ID
     * @param skipDuplicates indica si crear error o solo omitir
     * @param errors lista donde agregar errores si no se omiten
     */
    private void handleInternalDuplicate(ThirdExcelData currentRecord, ThirdExcelData existingRecord,
            boolean skipDuplicates, List<ImportErrorDetail> errors) {
        if (!skipDuplicates) {
            // Solo crear error si no se deben omitir duplicados
            errors.add(ImportErrorDetail.builder()
                    .rowNumber(currentRecord.getRowNumber())
                    .columnName("Número Identificación")
                    .fieldValue(String.valueOf(currentRecord.getIdNumber()))
                    .errorCode("INTERNAL_DUPLICATE_FOUND")
                    .errorMessage(String.format(
                            "Número de identificación duplicado en archivo. Primera ocurrencia en fila %d.",
                            existingRecord.getRowNumber()))
                    .errorType(ImportErrorType.DUPLICATE_ERROR)
                    .build());
        }
    }

    /**
     * @brief Maneja un duplicado con la base de datos encontrado
     * @param currentRecord registro que duplica uno existente en BD
     * @param skipDuplicates indica si crear error o solo omitir
     * @param errors lista donde agregar errores si no se omiten
     */
    private void handleDatabaseDuplicate(ThirdExcelData currentRecord, boolean skipDuplicates,
            List<ImportErrorDetail> errors) {
        if (!skipDuplicates) {
            // Solo crear error si no se deben omitir duplicados
            errors.add(ImportErrorDetail.builder()
                    .rowNumber(currentRecord.getRowNumber())
                    .columnName("Número Identificación")
                    .fieldValue(String.valueOf(currentRecord.getIdNumber()))
                    .errorCode("DATABASE_DUPLICATE_FOUND")
                    .errorMessage("El tercero ya existe en la base de datos.")
                    .errorType(ImportErrorType.DUPLICATE_ERROR)
                    .build());
        }
    }

    /**
     * @brief Crea una clave única para identificar duplicados
     *
     * Considera número de identificación y entidad para generar
     * una clave única que identifica registros duplicados.
     *
     * @param record registro del cual generar la clave
     * @return clave única para identificación de duplicados
     */
    private String createDuplicateKey(ThirdExcelData record) {
        return record.getEntId() + "_" + record.getIdNumber();
    }

    /**
     * @brief Clase que representa el resultado de análisis de duplicados
     *
     * Contiene los registros únicos encontrados y los errores
     * generados durante el análisis de duplicados.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateAnalysisResult {
        private List<ThirdExcelData> uniqueRecords;
        private List<ImportErrorDetail> errors;
    }

    /**
     * @brief Clase que representa el resultado completo de detección de duplicados
     *
     * Contiene métricas completas del proceso de detección incluyendo
     * registros únicos, errores, totales analizados y conteos.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateDetectionResult {
        private List<ThirdExcelData> uniqueRecords;
        private List<ImportErrorDetail> errors;
        private int totalAnalyzed;
        private int uniqueCount;
        private int duplicateCount;
    }

}
