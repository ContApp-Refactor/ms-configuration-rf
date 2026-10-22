package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción unificada para errores de validación de archivos
 *
 * Centraliza el manejo de errores relacionados con carga de archivos,
 * proporcionando métodos factory específicos para diferentes tipos de validación.
 */
public class FileValidationException extends BaseBusinessException {

    /**
     * @brief Constructor con código de error y mensaje
     * @param errorCode código específico del error de validación
     * @param message mensaje descriptivo del error
     */
    public FileValidationException(ThirdsErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * @brief Constructor con código de error, mensaje y causa
     * @param errorCode código específico del error de validación
     * @param message mensaje descriptivo del error
     * @param cause causa original del error que provocó esta excepción
     */
    public FileValidationException(ThirdsErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * @brief Crea una excepción para archivo null
     * @return excepción configurada para archivo null
     */
    public static FileValidationException forNullFile() {
        return new FileValidationException(
            ThirdsErrorCode.EXCEL_VALIDATION_ERROR,
            "El archivo no puede ser null"
        );
    }

    /**
     * @brief Crea una excepción para archivo vacío
     * @param fileName nombre del archivo que está vacío
     * @return excepción configurada para archivo vacío
     */
    public static FileValidationException forEmptyFile(String fileName) {
        return new FileValidationException(
            ThirdsErrorCode.THIRD_EXPORT_NO_DATA,
            String.format("El archivo '%s' está vacío", fileName)
        );
    }

    /**
     * @brief Crea una excepción para archivo que excede el tamaño máximo
     * @param fileName nombre del archivo que excede el límite
     * @param actualSize tamaño actual del archivo en bytes
     * @param maxSize tamaño máximo permitido en bytes
     * @return excepción configurada para tamaño excedido
     */
    public static FileValidationException forSizeExceeded(String fileName, long actualSize, long maxSize) {
        return new FileValidationException(
            ThirdsErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo '%s' excede el tamaño máximo permitido. Actual: %d bytes, Máximo: %d bytes",
                fileName, actualSize, maxSize)
        );
    }

    /**
     * @brief Crea una excepción para extensión de archivo inválida
     * @param fileName nombre del archivo con extensión inválida
     * @param supportedExtensions array de extensiones soportadas
     * @return excepción configurada para extensión inválida
     */
    public static FileValidationException forInvalidExtension(String fileName, String[] supportedExtensions) {
        return new FileValidationException(
            ThirdsErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo '%s' tiene una extensión no válida. Extensiones soportadas: %s",
                fileName, String.join(", ", supportedExtensions))
        );
    }

    /**
     * @brief Crea una excepción para tipo MIME inválido
     * @param fileName nombre del archivo con tipo MIME inválido
     * @param actualMimeType tipo MIME actual del archivo
     * @param supportedMimeTypes array de tipos MIME soportados
     * @return excepción configurada para tipo MIME inválido
     */
    public static FileValidationException forInvalidMimeType(String fileName, String actualMimeType, String[] supportedMimeTypes) {
        return new FileValidationException(
            ThirdsErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo '%s' tiene un tipo MIME no válido (%s). Tipos soportados: %s",
                fileName, actualMimeType, String.join(", ", supportedMimeTypes))
        );
    }
}
