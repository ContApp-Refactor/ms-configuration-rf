package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción específica para errores durante la importación de terceros
 *
 * Centraliza el manejo de todos los errores que pueden ocurrir durante el proceso
 * de importación de terceros desde archivos Excel, incluyendo validaciones,
 * procesamiento por lotes y errores de archivo.
 */
public class ThirdImportException extends BaseBusinessException {

    /**
     * @brief Constructor con código de error y mensaje
     * @param errorCode código específico del error de importación
     * @param message mensaje descriptivo del error ocurrido durante la importación
     */
    public ThirdImportException(ThirdsErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * @brief Constructor con código de error, mensaje y causa
     * @param errorCode código específico del error de importación
     * @param message mensaje descriptivo del error ocurrido durante la importación
     * @param cause causa raíz que originó el error de importación
     */
    public ThirdImportException(ThirdsErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * @brief Crea excepción para archivo Excel con formato inválido
     *
     * Se utiliza cuando un archivo Excel no cumple con la estructura esperada
     * o contiene datos mal formateados que impiden la importación.
     * @param fileName nombre del archivo Excel que tiene formato inválido
     * @param details detalles específicos sobre qué hace inválido el formato
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdImportException forInvalidExcelFile(String fileName, String details) {
        return new ThirdImportException(
            ThirdsErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo Excel '%s' tiene formato inválido: %s", fileName, details)
        );
    }

    /**
     * @brief Crea excepción para errores en el procesamiento por lotes
     *
     * Se utiliza cuando falla el procesamiento de un lote específico durante
     * la importación masiva de terceros.
     * @param batchNumber número identificador del lote que falló
     * @param details detalles específicos del error en el procesamiento del lote
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdImportException forBatchProcessingError(int batchNumber, String details) {
        return new ThirdImportException(
            ThirdsErrorCode.THIRD_EXPORT_ERROR, // Reutilizamos este código para errores de procesamiento
            String.format("Error procesando lote #%d: %s", batchNumber, details)
        );
    }

    /**
     * @brief Crea excepción para archivo vacío o sin datos válidos
     *
     * Se utiliza cuando se intenta importar un archivo que no contiene
     * registros válidos de terceros para procesar.
     * @param fileName nombre del archivo que está vacío
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdImportException forEmptyFile(String fileName) {
        return new ThirdImportException(
            ThirdsErrorCode.THIRD_EXPORT_NO_DATA,
            String.format("El archivo '%s' está vacío o no contiene datos válidos para importar", fileName)
        );
    }

    /**
     * @brief Crea excepción para archivo que excede el tamaño máximo permitido
     *
     * Se utiliza cuando se intenta importar un archivo que supera los límites
     * de tamaño establecidos por el sistema para prevenir problemas de rendimiento.
     * @param fileName nombre del archivo que excede el límite de tamaño
     * @param actualSize tamaño actual del archivo en bytes
     * @param maxSize tamaño máximo permitido en bytes
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdImportException forFileSizeExceeded(String fileName, long actualSize, long maxSize) {
        return new ThirdImportException(
            ThirdsErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo '%s' excede el tamaño máximo permitido. Actual: %d bytes, Máximo: %d bytes",
                fileName, actualSize, maxSize)
        );
    }
}
