package co.unicauca.edu.co.contables.inventory.products.domain.exception.product;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para errores de validación de archivos
 *
 * Se lanza cuando se detectan problemas en archivos durante operaciones
 * de importación, incluyendo validaciones de formato, extensión y contenido.
 */
public class ProductFileValidationException extends BaseBusinessException {

    public ProductFileValidationException(String message) {
        super(ErrorCode.FILE_VALIDATION_ERROR, message);
    }

    public ProductFileValidationException(String message, Throwable cause) {
        super(ErrorCode.FILE_VALIDATION_ERROR, message, cause);
    }

    public static ProductFileValidationException forNullFile() {
        return new ProductFileValidationException("El archivo no puede ser null");
    }

    public static ProductFileValidationException forEmptyFile(String fileName) {
        return new ProductFileValidationException(
            String.format("El archivo '%s' está vacío", fileName)
        );
    }

    public static ProductFileValidationException forInvalidExtension(String fileName, String[] supportedExtensions) {
        return new ProductFileValidationException(
            String.format("El archivo '%s' tiene una extensión no válida. Extensiones soportadas: %s",
                fileName, String.join(", ", supportedExtensions))
        );
    }

    public static ProductFileValidationException forInvalidMimeType(String fileName, String actualMimeType, String[] supportedMimeTypes) {
        return new ProductFileValidationException(
            String.format("El archivo '%s' tiene un tipo MIME no válido (%s). Tipos soportados: %s",
                fileName, actualMimeType, String.join(", ", supportedMimeTypes))
        );
    }
}