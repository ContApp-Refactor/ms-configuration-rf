package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando ocurre un error durante la validación de datos en archivos Excel.
 */
public class ExcelValidationException extends BaseBusinessException {

    public ExcelValidationException() {
        super(AccountCatalogueErrorCode.EXCEL_VALIDATION_ERROR);
    }

    public ExcelValidationException(String customMessage) {
        super(AccountCatalogueErrorCode.EXCEL_VALIDATION_ERROR, customMessage);
    }

    public ExcelValidationException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.EXCEL_VALIDATION_ERROR, customMessage, cause);
    }

    /**
     * Excepción específica para errores de validación de código.
     */
    public static class ExcelCodeValidationException extends ExcelValidationException {
        public ExcelCodeValidationException(int columnIndex) {
            super("Error aplicando validación de código en columna " + columnIndex);
        }

        public ExcelCodeValidationException(int columnIndex, Throwable cause) {
            super("Error aplicando validación de código en columna " + columnIndex, cause);
        }
    }

    /**
     * Excepción específica para errores de validación de lista desplegable.
     */
    public static class ExcelDropdownValidationException extends ExcelValidationException {
        public ExcelDropdownValidationException(int columnIndex) {
            super("Error aplicando validación de lista desplegable en columna " + columnIndex);
        }

        public ExcelDropdownValidationException(int columnIndex, Throwable cause) {
            super("Error aplicando validación de lista desplegable en columna " + columnIndex, cause);
        }
    }

    /**
     * Excepción específica para errores de validación condicional.
     */
    public static class ExcelConditionalValidationException extends ExcelValidationException {
        public ExcelConditionalValidationException(int columnIndex) {
            super("Error aplicando validación condicional en columna " + columnIndex);
        }

        public ExcelConditionalValidationException(int columnIndex, Throwable cause) {
            super("Error aplicando validación condicional en columna " + columnIndex, cause);
        }
    }
}