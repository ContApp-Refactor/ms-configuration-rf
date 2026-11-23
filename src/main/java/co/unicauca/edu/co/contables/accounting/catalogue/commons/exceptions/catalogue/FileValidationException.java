package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción unificada para errores de validación de archivos.
 * Centraliza el manejo de errores relacionados con carga de archivos.
 */
public class FileValidationException extends BaseBusinessException {

    public FileValidationException(AccountCatalogueErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FileValidationException(AccountCatalogueErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static FileValidationException forNullFile() {
        return new FileValidationException(
            AccountCatalogueErrorCode.EXCEL_VALIDATION_ERROR,
            "El archivo no puede ser null"
        );
    }

    public static FileValidationException forEmptyFile(String fileName) {
        return new FileValidationException(
            AccountCatalogueErrorCode.ACCOUNT_IMPORT_NO_DATA,
            String.format("El archivo '%s' está vacío", fileName)
        );
    }

    public static FileValidationException forInvalidExtension(String fileName, String[] supportedExtensions) {
        return new FileValidationException(
            AccountCatalogueErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo '%s' tiene una extensión no válida. Extensiones soportadas: %s", 
                fileName, String.join(", ", supportedExtensions))
        );
    }
}

