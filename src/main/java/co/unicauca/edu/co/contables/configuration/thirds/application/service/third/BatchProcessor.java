package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport.BatchValidationService;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport.DataConverter;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdExcelData;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.ValidationUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @brief Procesador de lotes optimizado para importación masiva
 *
 * Maneja transacciones por lote (no por registro) para mejor performance.
 * Utiliza saveAll() para insertar múltiples registros en una sola operación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessor {

    private final CreateThirdService createThirdService;
    private final DataConverter dataConverter;
    private final ThirdOutputPort thirdOutputPort;

    /**
     * @brief Procesa un lote completo de registros en una sola transacción usando saveAll()
     * @param batch lista de registros Excel a procesar
     * @param cache datos de referencia pre-cargados
     * @param continueOnError si es true, continúa procesando aunque haya errores
     * @return resultado del procesamiento del lote
     */
    @Transactional
    public BatchProcessingResult processBatch(List<ThirdExcelData> batch, 
                                             BatchValidationService.ReferenceDataCache cache,
                                             boolean continueOnError) {
        long batchStartTime = System.currentTimeMillis();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);
        List<ImportErrorDetail> errors = new ArrayList<>();
        List<Third> thirdsToSave = new ArrayList<>();

        // Fase 4.1: Preparar todos los registros (validar y normalizar)
        long prepareStartTime = System.currentTimeMillis();
        for (ThirdExcelData excelData : batch) {
            try {
                ProcessingResult result = prepareRecord(excelData, cache);
                
                switch (result.getStatus()) {
                    case SUCCESS:
                        thirdsToSave.add(result.getPreparedThird());
                        break;
                    case DUPLICATE_SKIPPED:
                        skippedCount.incrementAndGet();
                        break;
                    case FAILED:
                        if (continueOnError) {
                            failureCount.incrementAndGet();
                            errors.add(createProcessingError(excelData, result.getErrorMessage()));
                        } else {
                            // En modo "parar en error", lanzar excepción para rollback
                            throw new RuntimeException("Error en registro fila " + excelData.getRowNumber() + ": " + result.getErrorMessage());
                        }
                        break;
                    case SKIPPED:
                        skippedCount.incrementAndGet();
                        if (result.getErrorMessage() != null) {
                            errors.add(createProcessingError(excelData, result.getErrorMessage()));
                        }
                        break;
                }

            } catch (Exception e) {
                if (continueOnError) {
                    failureCount.incrementAndGet();
                    errors.add(createProcessingError(excelData, e.getMessage()));
                } else {
                    // Re-lanzar para rollback del lote completo
                    throw e;
                }
            }
        }
        long prepareTime = System.currentTimeMillis() - prepareStartTime;

        // Fase 4.2: Guardar todos los terceros preparados en una sola operación (saveAll)
        long saveTime = 0;
        if (!thirdsToSave.isEmpty()) {
            long saveStartTime = System.currentTimeMillis();
            try {
                List<Third> savedThirds = thirdOutputPort.saveAllThirds(thirdsToSave);
                successCount.addAndGet(savedThirds.size());
                saveTime = System.currentTimeMillis() - saveStartTime;
                
                double recordsPerSec = (savedThirds.size() * 1000.0) / saveTime;
                log.info("💾 saveAll() - {} registros en {} ms ({} reg/seg)", 
                        savedThirds.size(), saveTime, String.format("%.2f", recordsPerSec));
            } catch (Exception e) {
                saveTime = System.currentTimeMillis() - saveStartTime;
                if (continueOnError) {
                    failureCount.addAndGet(thirdsToSave.size());
                    for (int i = 0; i < thirdsToSave.size(); i++) {
                        errors.add(ImportErrorDetail.builder()
                                .rowNumber(batch.get(i).getRowNumber())
                                .errorCode("BATCH_SAVE_ERROR")
                                .errorMessage("Error al guardar lote: " + e.getMessage())
                                .errorType(ImportErrorType.SYSTEM_ERROR)
                                .build());
                    }
                } else {
                    throw e;
                }
            }
        }

        long batchTotalTime = System.currentTimeMillis() - batchStartTime;
        
        // Log detallado del desglose del lote
        double preparePercent = (prepareTime * 100.0) / batchTotalTime;
        double savePercent = (saveTime * 100.0) / batchTotalTime;
        log.info("🔍 Lote - Total: {} ms | Preparación: {} ms ({}%) | Guardado: {} ms ({}%)",
                batchTotalTime, prepareTime, String.format("%.1f", preparePercent), 
                saveTime, String.format("%.1f", savePercent));

        return BatchProcessingResult.builder()
                .successCount(successCount.get())
                .failureCount(failureCount.get())
                .skippedCount(skippedCount.get())
                .errors(errors)
                .build();
    }

    /**
     * @brief Prepara un registro individual sin guardarlo (validación y normalización)
     * @param excelData registro Excel a preparar
     * @param cache datos de referencia pre-cargados
     * @return resultado de la preparación con el Third listo para persistir
     */
    private ProcessingResult prepareRecord(ThirdExcelData excelData, BatchValidationService.ReferenceDataCache cache) {
        try {
            // Convertir usando cache (evita consultas N+1)
            Third third = dataConverter.convertWithCache(excelData, cache);
            
            if (third == null) {
                return ProcessingResult.skipped("Registro omitido por datos incompletos");
            }

            // Obtener códigos geográficos resueltos
            DataConverter.GeographyData geography = dataConverter.getGeographyData(excelData, cache);

            // Preparar tercero (validar y normalizar SIN guardarlo)
            Third preparedThird = createThirdService.prepareThirdForBatchSave(third, 
                    geography.getCountryCode(), geography.getStateCode(), geography.getCityCode());
            
            return ProcessingResult.success(preparedThird);

        } catch (Exception e) {
            // Clasificar el tipo de error
            if (ValidationUtils.isDuplicateError(e.getMessage())) {
                return ProcessingResult.duplicateSkipped();
            }
            if (ValidationUtils.isGeographyError(e.getMessage())) {
                return ProcessingResult.skipped("Error geográfico: " + e.getMessage());
            }
            
            return ProcessingResult.failed(e.getMessage());
        }
    }

    /**
     * @brief Crea un error de procesamiento estructurado
     * @param excelData registro Excel que causó el error
     * @param message mensaje descriptivo del error
     * @return objeto ImportErrorDetail con la información del error
     */
    private ImportErrorDetail createProcessingError(ThirdExcelData excelData, String message) {
        return ImportErrorDetail.builder()
                .rowNumber(excelData.getRowNumber())
                .errorCode("PROCESSING_ERROR")
                .errorMessage("Error al procesar: " + message)
                .errorType(ImportErrorType.SYSTEM_ERROR)
                .build();
    }

    /**
     * @brief Resultado del procesamiento de un lote
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
    }

    /**
     * @brief Resultado del procesamiento de un registro individual
     */
    @Data
    @AllArgsConstructor
    private static class ProcessingResult {
        private ProcessingStatus status;
        private String errorMessage;
        private Third preparedThird;

        public static ProcessingResult success(Third preparedThird) {
            return new ProcessingResult(ProcessingStatus.SUCCESS, null, preparedThird);
        }

        public static ProcessingResult duplicateSkipped() {
            return new ProcessingResult(ProcessingStatus.DUPLICATE_SKIPPED, null, null);
        }

        public static ProcessingResult failed(String errorMessage) {
            return new ProcessingResult(ProcessingStatus.FAILED, errorMessage, null);
        }

        public static ProcessingResult skipped(String reason) {
            return new ProcessingResult(ProcessingStatus.SKIPPED, reason, null);
        }
    }

    /**
     * @brief Estados de procesamiento de un registro
     */
    private enum ProcessingStatus {
        SUCCESS, DUPLICATE_SKIPPED, FAILED, SKIPPED
    }
}
