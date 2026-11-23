package co.unicauca.edu.co.contables.configuration.thirds.domain.utils;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdExcelData;

import java.util.Map;

/**
 * @brief Utilidades para mapeo y creación consistente de errores de importación
 *
 * Clase de utilidad que centraliza toda la lógica de creación de errores
 * para procesos de importación masiva de terceros, asegurando consistencia
 * en los mensajes y códigos de error en toda la aplicación.
 */
public final class ErrorMappingUtils {

    private ErrorMappingUtils() {
        throw new UnsupportedOperationException("ErrorMappingUtils es una clase de utilidad y no debe ser instanciada");
    }

    // ===== CREACIÓN DE ERRORES =====

    /**
     * @brief Crea un error de importación con toda la información disponible
     *
     * Método principal para crear errores de importación con toda la información
     * necesaria para debugging y reporting al usuario.
     * @param rowNumber número de fila donde ocurrió el error (base 1)
     * @param columnName nombre de la columna donde ocurrió el error
     * @param fieldValue valor del campo que causó el error
     * @param errorCode código del error para categorización y procesamiento automatizado
     * @param errorMessage mensaje descriptivo del error para el usuario
     * @param errorType tipo de error para clasificación (VALIDATION_ERROR, FORMAT_ERROR, etc.)
     * @param columnMap mapa de columnas para obtener el número de columna automáticamente
     * @return error de importación completamente estructurado
     */
    public static ImportErrorDetail createError(int rowNumber, String columnName, String fieldValue,
            String errorCode, String errorMessage,
            ImportErrorType errorType, Map<String, Integer> columnMap) {
        Integer columnNumber = null;
        if (columnMap != null && columnName != null) {
            columnNumber = columnMap.get(columnName);
            if (columnNumber != null) {
                columnNumber = columnNumber + ImportConstants.Defaults.COLUMN_START_INDEX; // Empezar en 1, no en 0
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
     * @brief Crea un error de validación de campo requerido
     *
     * Genera un error cuando un campo obligatorio no está presente en los datos de importación.
     * @param rowNumber número de fila donde falta el campo
     * @param columnName nombre de la columna que es obligatoria
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando que el campo es obligatorio
     */
    public static ImportErrorDetail createRequiredFieldError(int rowNumber, String columnName,
            Map<String, Integer> columnMap) {
        return createError(rowNumber, columnName, null,
                ImportConstants.ErrorCodes.REQUIRED_FIELD_MISSING,
                "El campo " + columnName + " es obligatorio",
                ImportErrorType.VALIDATION_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de formato de email inválido
     *
     * Genera un error cuando el formato del correo electrónico no cumple con el patrón estándar.
     * @param rowNumber número de fila donde se encuentra el email inválido
     * @param emailValue valor del email que no cumple con el formato requerido
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando problema de formato de email
     */
    public static ImportErrorDetail createInvalidEmailError(int rowNumber, String emailValue,
            Map<String, Integer> columnMap) {
        return createError(rowNumber, ImportConstants.EMAIL_COLUMN, emailValue,
                ImportConstants.ErrorCodes.INVALID_EMAIL_FORMAT,
                "El formato del correo electrónico no es válido",
                ImportErrorType.FORMAT_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de formato de teléfono inválido
     *
     * Genera un error cuando el número de teléfono no cumple con el formato esperado.
     * @param rowNumber número de fila donde se encuentra el teléfono inválido
     * @param phoneValue valor del teléfono que no cumple con el formato requerido
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando problema de formato de teléfono
     */
    public static ImportErrorDetail createInvalidPhoneError(int rowNumber, String phoneValue,
            Map<String, Integer> columnMap) {
        return createError(rowNumber, ImportConstants.PHONE_COLUMN, phoneValue,
                ImportConstants.ErrorCodes.INVALID_PHONE_FORMAT,
                "El formato del número de teléfono no es válido",
                ImportErrorType.FORMAT_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de formato de dígito de verificación inválido
     *
     * Genera un error cuando el dígito de verificación no es un solo dígito entre 0-9.
     * @param rowNumber número de fila donde se encuentra el dígito inválido
     * @param verificationValue valor del dígito de verificación que no es válido
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando problema de formato del dígito de verificación
     */
    public static ImportErrorDetail createInvalidVerificationDigitError(int rowNumber, String verificationValue,
            Map<String, Integer> columnMap) {
        return createError(rowNumber, "Dígito Verificación", verificationValue,
                "INVALID_VERIFICATION_DIGIT_FORMAT",
                "El dígito de verificación debe ser un solo dígito (0-9)",
                ImportErrorType.FORMAT_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de formato de NIT inválido (debe tener exactamente 9 dígitos)
     *
     * Genera un error cuando el NIT no tiene exactamente 9 dígitos como requiere la normatividad colombiana.
     * @param rowNumber número de fila donde se encuentra el NIT inválido
     * @param nitValue valor del NIT que no tiene la longitud correcta
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando problema de longitud del NIT
     */
    public static ImportErrorDetail createInvalidNitLengthError(int rowNumber, String nitValue,
            Map<String, Integer> columnMap) {
        return createError(rowNumber, "Número Identificación", nitValue,
                "INVALID_NIT_LENGTH",
                "El NIT debe tener exactamente 9 dígitos",
                ImportErrorType.FORMAT_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de referencia inválida para datos maestros
     *
     * Genera un error cuando un valor hace referencia a datos maestros que no existen
     * o están inactivos (ej: tipo de identificación inexistente).
     * @param rowNumber número de fila donde se encuentra la referencia inválida
     * @param columnName nombre de la columna que contiene la referencia
     * @param fieldValue valor que se intentó referenciar pero no existe
     * @param referenceType tipo de referencia que se validó (ej: "tipo de identificación")
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando referencia inválida
     */
    public static ImportErrorDetail createInvalidReferenceError(int rowNumber, String columnName,
            String fieldValue, String referenceType,
            Map<String, Integer> columnMap) {
        String errorCode = "INVALID_" + referenceType.toUpperCase().replace(" ", "_") + "_REFERENCE";
        String errorMessage = String.format("%s '%s' no existe o está inactivo", referenceType, fieldValue);

        return createError(rowNumber, columnName, fieldValue,
                errorCode, errorMessage,
                ImportErrorType.REFERENCE_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de regla de negocio con detección automática de campo
     *
     * Genera un error cuando se viola una regla de negocio específica del dominio,
     * identificando automáticamente qué campo causó la violación basándose en el mensaje de error.
     * @param excelData datos del registro Excel que causó la violación de regla de negocio
     * @param exception excepción que contiene el mensaje detallado de la regla violada
     * @param columnMap mapa de columnas para determinar el número de columna del campo identificado
     * @return error estructurado con el campo específico que violó la regla de negocio
     */
    public static ImportErrorDetail createBusinessRuleError(ThirdExcelData excelData, Exception exception,
            Map<String, Integer> columnMap) {
        String fieldName = ValidationUtils.detectFieldFromBusinessRuleError(exception.getMessage());
        String fieldValue = getFieldValueFromExcelData(excelData, fieldName);

        return createError(excelData.getRowNumber(), fieldName, fieldValue,
                ImportConstants.ErrorCodes.BUSINESS_RULE_VIOLATION, exception.getMessage(),
                ImportErrorType.BUSINESS_RULE_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error de duplicado
     *
     * Genera un error cuando se intenta crear un registro que ya existe en el sistema,
     * indicando específicamente cuál campo contiene el valor duplicado.
     * @param rowNumber número de fila donde se encuentra el registro duplicado
     * @param fieldValue valor del campo que está duplicado
     * @param columnName nombre del campo que contiene el valor duplicado
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando duplicación de registro
     */
    public static ImportErrorDetail createDuplicateError(int rowNumber, String fieldValue,
            String columnName, Map<String, Integer> columnMap) {
        return createError(rowNumber, columnName, fieldValue,
                ImportConstants.ErrorCodes.DUPLICATE_RECORD,
                "El registro con " + columnName + " '" + fieldValue + "' ya existe",
                ImportErrorType.DUPLICATE_ERROR,
                columnMap);
    }

    /**
     * @brief Crea un error del sistema
     *
     * Genera un error genérico del sistema cuando ocurre un problema técnico
     * no clasificable de otra manera durante la importación.
     * @param errorMessage mensaje detallado del error técnico ocurrido
     * @return error estructurado indicando problema del sistema
     */
    public static ImportErrorDetail createSystemError(String errorMessage) {
        return ImportErrorDetail.builder()
                .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                .errorMessage(ImportConstants.ErrorMessages.SYSTEM_ERROR + ": " + errorMessage)
                .errorType(ImportErrorType.SYSTEM_ERROR)
                .build();
    }

    /**
     * @brief Crea un error de procesamiento durante la creación del registro
     *
     * Genera un error cuando ocurre un problema durante el procesamiento y creación
     * del registro en la base de datos, incluyendo el número de fila para trazabilidad.
     * @param excelData datos del registro Excel que no pudo ser procesado
     * @param errorMessage mensaje detallado del error de procesamiento
     * @return error estructurado con información del registro problemático
     */
    public static ImportErrorDetail createProcessingError(ThirdExcelData excelData, String errorMessage) {
        return ImportErrorDetail.builder()
                .rowNumber(excelData.getRowNumber())
                .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                .errorMessage("Error al procesar registro: " + errorMessage)
                .errorType(ImportErrorType.SYSTEM_ERROR)
                .build();
    }

    /**
     * @brief Crea un error de ciudad faltante cuando se especifica departamento
     *
     * Genera un error cuando se proporciona información de departamento pero no se especifica
     * la ciudad correspondiente, lo cual es requerido para una dirección completa.
     * @param rowNumber número de fila donde falta la información de ciudad
     * @param columnMap mapa de columnas para determinar el número de columna
     * @return error estructurado indicando que la ciudad es obligatoria
     */
    public static ImportErrorDetail createMissingCityError(int rowNumber, Map<String, Integer> columnMap) {
        return createError(rowNumber, "Ciudad", null,
                ImportConstants.ErrorCodes.MISSING_CITY_FOR_COMPLETE_ADDRESS,
                "La ciudad es obligatoria cuando se especifica departamento",
                ImportErrorType.VALIDATION_ERROR,
                columnMap);
    }

    // ===== GENERACIÓN DE CÓDIGOS DE ERROR =====

    /**
     * @brief Genera un código de error consistente basado en el nombre del campo
     *
     * Centraliza la lógica de generación de códigos de error para mantener consistencia
     * en toda la aplicación, normalizando caracteres especiales y formato.
     * @param prefix prefijo del código de error (ej: "INVALID", "MISSING")
     * @param fieldName nombre del campo que causó el error
     * @return código de error normalizado en formato MAYÚSCULAS_CON_GUIONES (ej: "INVALID_TIPO_PERSONA")
     */
    public static String generateErrorCode(String prefix, String fieldName) {
        if (prefix == null || fieldName == null) {
            return "UNKNOWN_ERROR";
        }

        return prefix.toUpperCase() + "_" +
               fieldName.toUpperCase()
                       .replace(" ", "_")
                       .replace("Ó", "O")
                       .replace("É", "E")
                       .replace("Í", "I")
                       .replace("Á", "A")
                       .replace("Ú", "U")
                       .replaceAll("[^A-Z0-9_]", "_");
    }

    /**
     * @brief Genera código de error para valores inválidos de campo
     *
     * Utiliza el prefijo "INVALID" para indicar que el valor del campo no cumple
     * con los criterios de validación esperados.
     * @param fieldName nombre del campo que tiene un valor inválido
     * @return código de error con prefijo "INVALID_"
     */
    public static String generateInvalidFieldErrorCode(String fieldName) {
        return generateErrorCode("INVALID", fieldName);
    }

    /**
     * @brief Genera código de error para errores de parseo de campo
     *
     * Utiliza el sufijo "PARSING_ERROR" para indicar que el valor del campo
     * no pudo ser convertido al tipo de datos esperado.
     * @param fieldName nombre del campo que no pudo ser parseado
     * @return código de error con sufijo "_PARSING_ERROR"
     */
    public static String generateParsingErrorCode(String fieldName) {
        return generateErrorCode("PARSING_ERROR", fieldName);
    }

    // ===== UTILIDADES DE MAPEO =====

    /**
     * @brief Obtiene el valor de un campo específico desde los datos de Excel
     *
     * Mapea el nombre lógico de un campo (utilizado en errores) al valor real
     * contenido en los datos parseados desde Excel, facilitando la presentación
     * de información contextual en los errores.
     * @param excelData datos de Excel del registro que contiene los valores
     * @param fieldName nombre lógico del campo a obtener (ej: "Tipo Identificación")
     * @return valor del campo como String o null si no se encuentra o es null
     */
    public static String getFieldValueFromExcelData(ThirdExcelData excelData, String fieldName) {
        if (excelData == null || fieldName == null) {
            return null;
        }

        return switch (fieldName) {
            case "Número Identificación" ->
                excelData.getIdNumber() != null ? excelData.getIdNumber().toString() : null;
            case "Dígito Verificación" ->
                excelData.getVerificationNumber() != null ? excelData.getVerificationNumber().toString() : null;
            case "Tipo Persona" ->
                excelData.getPersonType() != null ? excelData.getPersonType().toString() : null;
            case "Tipo Identificación" -> excelData.getTypeIdName();
            case "Nombres" -> excelData.getNames();
            case "Apellidos" -> excelData.getLastNames();
            case "Razón Social" -> excelData.getSocialReason();
            case "Género" ->
                excelData.getGender() != null ? excelData.getGender().toString() : null;
            case "Email" -> excelData.getEmail();
            case "Teléfono" -> excelData.getPhoneNumber();
            case "País" -> excelData.getCountryName();
            case "Departamento" -> excelData.getStateName();
            case "Ciudad" -> excelData.getCityName();
            case "Dirección" -> excelData.getAddress();
            default -> null;
        };
    }

}
