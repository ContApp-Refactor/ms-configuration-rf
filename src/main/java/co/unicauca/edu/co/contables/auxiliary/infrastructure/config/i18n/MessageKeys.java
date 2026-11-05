package co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n;

/**
 * @brief Constantes con las claves de mensajes internacionalizados.
 *
 * Centraliza todas las claves utilizadas por la aplicación para mensajes
 * de error, de log y de validación, evitando strings mágicos dispersos.
 */
public final class MessageKeys {
    private MessageKeys() {
        // Constant class, should not be instantiated
    }

    // Generic Error Messages
    public static final String ERROR_GENERIC = "auxiliary_book.error.generic";
    public static final String ERROR_NOT_FOUND = "auxiliary_book.error.not.found";
    public static final String ERROR_NULL_VALUE = "auxiliary_book.error.null.value";
    public static final String ERROR_INVALID_VALUE = "auxiliary_book.error.invalid.value";
    public static final String ERROR_OPERATION_NOT_ALLOWED = "auxiliary_book.error.operation.not.allowed";
    public static final String ERROR_INVALID_TYPE = "auxiliary_book.error.invalid.type";

    // Generic Log Messages
    public static final String LOG_OPERATION_STARTED = "auxiliary_book.log.operation.started";
    public static final String LOG_OPERATION_COMPLETED = "auxiliary_book.log.operation.completed";
    public static final String LOG_OPERATION_ERROR = "auxiliary_book.log.operation.error";
    public static final String LOG_VALIDATION = "auxiliary_book.log.validation";
    public static final String LOG_QUERY = "auxiliary_book.log.query";
    public static final String LOG_INFO = "auxiliary_book.log.info";

    // Generic Validation Messages
    public static final String VALIDATION_FIELD_REQUIRED = "auxiliary_book.validation.field.required";
    public static final String VALIDATION_FIELD_POSITIVE = "auxiliary_book.validation.field.positive";
    public static final String VALIDATION_DATE_RANGE_INVALID = "auxiliary_book.validation.date.range.invalid";
    public static final String VALIDATION_DATE_RANGE_INCOMPLETE = "auxiliary_book.validation.date.range.incomplete";
}
