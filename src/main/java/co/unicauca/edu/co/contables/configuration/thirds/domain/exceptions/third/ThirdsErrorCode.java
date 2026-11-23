package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error específicos del dominio de Terceros.
 * 
 */
@Getter
public enum ThirdsErrorCode implements ErrorCodeDefinition {

    THIRD_NOT_FOUND("THIRD_NOT_FOUND", "Tercero no encontrado"),
    THIRDS_NOT_FOUND("THIRDS_NOT_FOUND", "No se encontraron terceros"),
    THIRD_STATE_NOT_CHANGED("THIRD_STATE_NOT_CHANGED", "No se pudo cambiar el estado del tercero"),
    THIRD_ALREADY_EXISTS("THIRD_ALREADY_EXISTS", "El tercero ya existe"),
    INVALID_THIRD_DATA("INVALID_THIRD_DATA", "Datos del tercero inválidos"),
    THIRD_FOREIGN_KEY_VIOLATION("THIRD_FOREIGN_KEY_VIOLATION", "Referencia a tipo de tercero o identificación inválida"),
    PDF_RUT_INVALID_FORMAT("PDF_RUT_INVALID_FORMAT", "El archivo PDF no tiene el formato válido de RUT de la DIAN"),
    PDF_RUT_INVALID_FILE_TYPE("PDF_RUT_INVALID_FILE_TYPE", "El archivo debe ser de tipo PDF"),
    THIRD_EXPORT_NO_DATA("THIRD_EXPORT_NO_DATA", "No hay terceros registrados para exportar en esta entidad"),
    THIRD_EXPORT_NO_ACTIVE_DATA("THIRD_EXPORT_NO_ACTIVE_DATA", "No hay terceros activos registrados para exportar en esta entidad"),
    THIRD_EXPORT_NO_INACTIVE_DATA("THIRD_EXPORT_NO_INACTIVE_DATA", "No hay terceros inactivos registrados para exportar en esta entidad"),
    THIRD_EXPORT_ERROR("THIRD_EXPORT_ERROR", "Error al generar archivo de exportación"),
    EXCEL_VALIDATION_ERROR("EXCEL_VALIDATION_ERROR", "Error al aplicar validaciones de datos en Excel"),
    EXCEL_REFERENCE_SHEET_ERROR("EXCEL_REFERENCE_SHEET_ERROR", "Error al crear hoja de referencia en Excel"),
    THIRD_PERSON_TYPE_VALIDATION_ERROR("THIRD_PERSON_TYPE_VALIDATION_ERROR", "Error de validación de tipo de persona"),
    THIRD_TYPE_ID_PERSON_TYPE_INCOMPATIBILITY("THIRD_TYPE_ID_PERSON_TYPE_INCOMPATIBILITY", "Incompatibilidad entre tipo de identificación y tipo de persona"),
    THIRD_NIT_INVALID_FORMAT("THIRD_NIT_INVALID_FORMAT", "El NIT debe empezar por 8 o 9 para personas jurídicas"),
    THIRD_VERIFICATION_DIGIT_NOT_ALLOWED("THIRD_VERIFICATION_DIGIT_NOT_ALLOWED", "El dígito de verificación solo se permite para personas jurídicas"),
    THIRD_VERIFICATION_DIGIT_REQUIRED("THIRD_VERIFICATION_DIGIT_REQUIRED", "El dígito de verificación es obligatorio para personas jurídicas"),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "El archivo excede el tamaño máximo permitido"),
    THIRD_IN_USE("THIRD_IN_USE", "El tercero no puede ser modificado o eliminado porque tiene movimientos contables asociados");

    private final String code;
    private final String message;

    ThirdsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
