package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n;

/**
 * Constants for internationalized message keys.
 * Centralizes all message keys used in the application.
 */
public final class MessageKeys {
    
    private MessageKeys() {
       // Constants class, should not be instantiated
    }
    
    // Generic Error Messages
    public static final String ERROR_GENERIC = "kardex.error.generic";
    public static final String ERROR_NOT_FOUND = "kardex.error.not.found";
    public static final String ERROR_INVALID_VALUE = "kardex.error.invalid.value";
    public static final String ERROR_QUANTITY_EXCEEDED = "kardex.error.quantity.exceeded";
    public static final String ERROR_DUPLICATE_RECORD = "kardex.error.duplicate.record";
    public static final String ERROR_MISSING_RECORD = "kardex.error.missing.record";
    public static final String ERROR_OPERATION_NOT_ALLOWED = "kardex.error.operation.not.allowed";
    public static final String ERROR_INVALID_TYPE = "kardex.error.invalid.type";
    public static final String INVALID_ACCOUNTING_DATE = "kardex.error.invalid.accounting.date";
    public static final String DATE_CANNOT_BE_FUTURE = "kardex.error.date.cannot.be.future";
    public static final String DATE_CANNOT_BE_BEFORE_LAST_RECORD = "kardex.error.date.cannot.be.before.last.record";

    // Generic Validation Messages
    public static final String VALIDATION_FIELD_REQUIRED = "kardex.validation.field.required";
    public static final String VALIDATION_FIELD_POSITIVE = "kardex.validation.field.positive";
    public static final String VALIDATION_DATE_RANGE_INVALID = "kardex.validation.date.range.invalid";
    public static final String VALIDATION_DATE_RANGE_INCOMPLETE = "kardex.validation.date.range.incomplete";
}
