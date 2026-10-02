package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.AccountCodeUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @brief Servicio para validación por lotes de cuentas contables
 *
 * Realiza validaciones masivas en lotes de datos de cuentas durante
 * el proceso de importación desde archivos Excel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueBatchValidationService {

    private final AccountCatalogueValidationService validationService;

    /**
     * @brief Coordina validación masiva de lote con manejo de errores por registro
     *
     * Procesa cada registro del lote aplicando validaciones secuenciales,
     * acumula errores por registro sin detener el proceso, y separa registros
     * válidos de inválidos. Maneja excepciones del sistema por registro.
     * @param accountsData lote de registros Excel a validar
     * @param entId ID de empresa para contexto de validación
     * @param columnMap mapeo dinámico de columnas para ubicación de errores
     * @return estadísticas completas de validación con registros válidos y errores
     */
    public BatchValidationResult validateBatch(List<AccountCatalogueExcelData> accountsData, String entId,
                                               Map<String, Integer> columnMap) {
        List<ImportErrorDetail> errors = new ArrayList<>();
        List<AccountCatalogueExcelData> validRecords = new ArrayList<>();

        for (AccountCatalogueExcelData excelData : accountsData) {
            try {
                List<ImportErrorDetail> recordErrors = validateSingleRecord(excelData, columnMap);
                errors.addAll(recordErrors);

                if (recordErrors.isEmpty()) {
                    validRecords.add(excelData);
                }

            } catch (Exception e) {
                errors.add(ImportErrorDetail.builder()
                        .rowNumber(excelData.getRowNumber())
                        .errorCode("VALIDATION_SYSTEM_ERROR")
                        .errorMessage("Error del sistema validando registro: " + e.getMessage())
                        .errorType(ImportErrorType.SYSTEM_ERROR)
                        .build());
            }
        }

        return BatchValidationResult.builder()
                .validRecords(validRecords)
                .errors(errors)
                .totalProcessed(accountsData.size())
                .validCount(validRecords.size())
                .errorCount(errors.size())
                .build();
    }

    /**
     * @brief Valida registro Excel aplicando todas las reglas de negocio
     *
     * Ejecuta validaciones secuenciales en orden específico: campos requeridos primero,
     * luego formato de código, formato de descripción, restricciones de cruce
     * y finalmente centro de costo. Todas las validaciones acumulan errores sin detenerse.
     * @param excelData registro individual a validar
     * @param columnMap mapeo de columnas para ubicación precisa de errores
     * @return lista completa de errores encontrados (vacía si válido)
     */
    private List<ImportErrorDetail> validateSingleRecord(AccountCatalogueExcelData excelData,
                                                         Map<String, Integer> columnMap) {
        List<ImportErrorDetail> errors = new ArrayList<>();

        validateRequiredFields(excelData, errors, columnMap);

        validateCodeFormat(excelData, errors, columnMap);

        validateDescriptionFormat(excelData, errors, columnMap);

        validateCrossingField(excelData, errors, columnMap);

        validateCostCenterField(excelData, errors, columnMap);

        return errors;
    }

    /**
     * @brief Valida presencia de campos obligatorios en registro Excel
     *
     * Verifica que todos los campos requeridos estén presentes y no vacíos:
     * código, descripción, naturaleza, estado financiero y clasificación.
     * Campos opcionales (crossing, costCenter) no se validan aquí.
     * @param excelData registro Excel a validar
     * @param errors lista acumulativa donde agregar errores encontrados
     * @param columnMap mapeo de columnas para ubicación de errores
     */
    private void validateRequiredFields(AccountCatalogueExcelData excelData, List<ImportErrorDetail> errors,
                                       Map<String, Integer> columnMap) {
        if (excelData.getCode() == null || excelData.getCode().trim().isEmpty()) {
            errors.add(createRequiredFieldError(excelData.getRowNumber(), ImportConstants.CODE_COLUMN, columnMap));
        }

        if (excelData.getDescription() == null || excelData.getDescription().trim().isEmpty()) {
            errors.add(createRequiredFieldError(excelData.getRowNumber(), ImportConstants.NAME_COLUMN, columnMap));
        }

        if (excelData.getNature() == null) {
            errors.add(createRequiredFieldError(excelData.getRowNumber(), ImportConstants.NATURE_COLUMN, columnMap));
        }

        if (excelData.getFinancialStatus() == null) {
            errors.add(createRequiredFieldError(excelData.getRowNumber(), 
                    ImportConstants.FINANCIAL_STATUS_COLUMN, columnMap));
        }

        if (excelData.getClassification() == null) {
            errors.add(createRequiredFieldError(excelData.getRowNumber(), 
                    ImportConstants.CLASSIFICATION_COLUMN, columnMap));
        }
    }

    /**
     * @brief Valida formato y restricciones del código de cuenta
     *
     * Realiza validaciones en cascada: formato numérico con regex, longitud específica
     * (1,2,4,6,8 dígitos), valor positivo, y conversión segura a Long.
     * Cada validación genera errores específicos con códigos y mensajes detallados.
     * @param excelData registro Excel que contiene el código a validar
     * @param errors lista acumulativa donde agregar errores de validación encontrados
     * @param columnMap mapeo de columnas para calcular ubicación precisa de errores
     */
    private void validateCodeFormat(AccountCatalogueExcelData excelData, List<ImportErrorDetail> errors,
                                   Map<String, Integer> columnMap) {
        String code = excelData.getCode();
        if (code == null || code.trim().isEmpty()) {
            return; // Ya validado en campos requeridos
        }

        try {
            // Validar que solo contenga dígitos
            if (!code.trim().matches("^\\d+$")) {
                errors.add(createError(excelData.getRowNumber(), ImportConstants.CODE_COLUMN, code,
                        ImportConstants.ErrorCodes.INVALID_CODE_FORMAT,
                        "El código debe contener solo dígitos",
                        ImportErrorType.FORMAT_ERROR, columnMap));
                return;
            }

            // Validar longitud (1, 2, 4, 6 u 8)
            if (!AccountCodeUtils.isValidCodeLength(code)) {
                errors.add(createError(excelData.getRowNumber(), ImportConstants.CODE_COLUMN, code,
                        ImportConstants.ErrorCodes.INVALID_CODE_LENGTH,
                        "El código debe tener exactamente 1, 2, 4, 6 u 8 dígitos. Actual: " + code.trim().length(),
                        ImportErrorType.VALIDATION_ERROR, columnMap));
            }

            // Validar que sea positivo
            long codeValue = Long.parseLong(code.trim());
            if (codeValue <= 0) {
                errors.add(createError(excelData.getRowNumber(), ImportConstants.CODE_COLUMN, code,
                        ImportConstants.ErrorCodes.INVALID_CODE_FORMAT,
                        "El código debe ser un número positivo",
                        ImportErrorType.VALIDATION_ERROR, columnMap));
            }

        } catch (NumberFormatException e) {
            errors.add(createError(excelData.getRowNumber(), ImportConstants.CODE_COLUMN, code,
                    ImportConstants.ErrorCodes.INVALID_CODE_FORMAT,
                    "El código no es un número válido",
                    ImportErrorType.FORMAT_ERROR, columnMap));
        }
    }

    /**
     * @brief Valida formato y contenido de descripción de cuenta
     *
     * Delega validación a AccountCatalogueValidationService.validateAccountDescription()
     * que verifica longitud, caracteres especiales y reglas de negocio específicas.
     * @param excelData registro Excel con descripción a validar
     * @param errors lista acumulativa donde agregar errores encontrados
     * @param columnMap mapeo de columnas para ubicación de errores
     */
    private void validateDescriptionFormat(AccountCatalogueExcelData excelData, List<ImportErrorDetail> errors,
                                          Map<String, Integer> columnMap) {
        String description = excelData.getDescription();
        if (description == null || description.trim().isEmpty()) {
            return; // Ya validado en campos requeridos
        }

        try {
            validationService.validateAccountDescription(description);
        } catch (Exception e) {
            errors.add(createError(excelData.getRowNumber(), ImportConstants.NAME_COLUMN, description,
                    ImportConstants.ErrorCodes.BUSINESS_RULE_VIOLATION,
                    e.getMessage(),
                    ImportErrorType.BUSINESS_RULE_ERROR, columnMap));
        }
    }

    
    /**
     * @brief Valida restricciones de campo crossing (solo cuentas auxiliares)
     *
     * Si crossing=true, verifica que el código tenga exactamente 8 dígitos
     * (cuenta auxiliar). Null/false son valores siempre válidos.
     * Genera error detallado con longitud actual del código.
     * @param excelData registro Excel con campo crossing a validar
     * @param errors lista acumulativa donde agregar errores de validación
     * @param columnMap mapeo de columnas para ubicación precisa de errores
     */
    private void validateCrossingField(AccountCatalogueExcelData excelData, List<ImportErrorDetail> errors,
                                      Map<String, Integer> columnMap) {
        Boolean crossing = excelData.getCrossing();
        if (crossing == null || !crossing) {
            return; // null o false son valores válidos siempre
        }

        // Si crossing es true, debe ser cuenta auxiliar
        if (!excelData.isAuxiliaryAccount()) {
            String code = excelData.getCode();
            errors.add(createError(excelData.getRowNumber(), ImportConstants.CROSSING_COLUMN, "SI",
                    ImportConstants.ErrorCodes.BUSINESS_RULE_VIOLATION,
                    String.format("El cruce solo puede establecerse en cuentas auxiliares (8 dígitos). " +
                            "Código actual: '%s' tiene %d dígitos", code, code != null ? code.trim().length() : 0),
                    ImportErrorType.BUSINESS_RULE_ERROR, columnMap));
        }
    }

    /**
     * @brief Valida restricciones dobles de centro de costo (auxiliar + Estado de Resultados)
     *
     * Si costCenter=true, requiere dos condiciones simultáneas:
     * 1) Código debe tener exactamente 8 dígitos (cuenta auxiliar)
     * 2) Estado financiero debe ser "Estado de Resultados"
     * Null/false son valores siempre válidos. Valida ambas restricciones con mensajes específicos.
     * @param excelData registro Excel con campo costCenter a validar
     * @param errors lista acumulativa donde agregar errores de validación encontrados
     * @param columnMap mapeo de columnas para ubicación precisa de errores
     */
    private void validateCostCenterField(AccountCatalogueExcelData excelData, List<ImportErrorDetail> errors,
                                        Map<String, Integer> columnMap) {
        Boolean costCenter = excelData.getCostCenter();
        if (costCenter == null || !costCenter) {
            return; // null o false son valores válidos siempre
        }

        // Si costCenter es true, debe ser cuenta auxiliar
        if (!excelData.isAuxiliaryAccount()) {
            String code = excelData.getCode();
            errors.add(createError(excelData.getRowNumber(), ImportConstants.COST_CENTER_COLUMN, "SI",
                    ImportConstants.ErrorCodes.BUSINESS_RULE_VIOLATION,
                    String.format("El centro de costo solo puede establecerse en cuentas auxiliares (8 dígitos). " +
                            "Código actual: '%s' tiene %d dígitos", code, code != null ? code.trim().length() : 0),
                    ImportErrorType.BUSINESS_RULE_ERROR, columnMap));
            return;
        }

        // Si costCenter es true, el estado financiero debe ser Estado de Resultados
        if (excelData.getFinancialStatus() != FinancialStatusEnum.INCOMESTATEMENT) {
            errors.add(createError(excelData.getRowNumber(), ImportConstants.COST_CENTER_COLUMN, "SI",
                    ImportConstants.ErrorCodes.BUSINESS_RULE_VIOLATION,
                    String.format("El centro de costo solo puede establecerse cuando el estado financiero " +
                            "sea 'Estado de Resultados'. Estado actual: %s",
                            excelData.getFinancialStatus() != null ? 
                                    excelData.getFinancialStatus().getState() : "No definido"),
                    ImportErrorType.BUSINESS_RULE_ERROR, columnMap));
        }
    }

    /**
     * @brief Crea error estandarizado para campos requeridos faltantes
     *
     * @param rowNumber número de fila donde ocurrió el error
     * @param columnName nombre de columna que contiene el campo requerido faltante
     * @param columnMap mapeo de columnas para calcular número de columna
     * @return objeto ImportErrorDetail con error de campo requerido
     */
    private ImportErrorDetail createRequiredFieldError(int rowNumber, String columnName,
                                                      Map<String, Integer> columnMap) {
        return createError(rowNumber, columnName, null,
                ImportConstants.ErrorCodes.REQUIRED_FIELD_MISSING,
                "El campo " + columnName + " es obligatorio",
                ImportErrorType.VALIDATION_ERROR,
                columnMap);
    }

    /**
     * @brief Crea objeto de error con información completa de ubicación y contexto
     *
     * @param rowNumber número de fila donde se encontró el error (1-based)
     * @param columnName nombre lógico de la columna afectada
     * @param fieldValue valor problemático que causó el error (puede ser null)
     * @param errorCode código específico del tipo de error
     * @param errorMessage mensaje descriptivo del error para el usuario
     * @param errorType categorización del tipo de error (VALIDATION_ERROR, BUSINESS_RULE_ERROR, etc.)
     * @param columnMap mapeo dinámico de nombres a índices de columna
     * @return objeto ImportErrorDetail completamente construido con ubicación precisa
     */
    private ImportErrorDetail createError(int rowNumber, String columnName, String fieldValue,
                                         String errorCode, String errorMessage,
                                         ImportErrorType errorType, Map<String, Integer> columnMap) {
        Integer columnNumber = null;
        if (columnMap != null && columnName != null) {
            columnNumber = columnMap.get(columnName);
            if (columnNumber != null) {
                columnNumber = columnNumber + ImportConstants.Defaults.COLUMN_START_INDEX;
            }
        }

        return ImportErrorDetail.builder()
                .rowNumber(rowNumber)
                .columnNumber(columnNumber)
                .columnName(columnName)
                .fieldValue(fieldValue)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .errorType(errorType)
                .build();
    }

    /**
     * @brief Resultado completo de validación por lotes
     *
     * Contiene registros válidos listos para procesamiento posterior,
     * lista completa de errores encontrados, y estadísticas de procesamiento
     * (total procesado, válidos, errores) para reporting y control de flujo.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchValidationResult {
        private List<AccountCatalogueExcelData> validRecords;
        private List<ImportErrorDetail> errors;
        private int totalProcessed;
        private int validCount;
        private int errorCount;
    }
}

