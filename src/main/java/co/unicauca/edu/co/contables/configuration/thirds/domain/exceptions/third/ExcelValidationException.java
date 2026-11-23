package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción específica para errores durante la aplicación de validaciones en Excel
 *
 * Se utiliza cuando fallan las validaciones aplicadas a archivos Excel durante
 * procesos de importación o exportación de terceros.
 */
public class ExcelValidationException extends BaseBusinessException {

    /**
     * @brief Constructor con código de error
     * @param errorCode código específico del error de validación Excel
     */
    public ExcelValidationException(ThirdsErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * @brief Constructor con código de error y mensaje personalizado
     * @param errorCode código específico del error de validación Excel
     * @param customMessage mensaje personalizado que describe el error de validación
     */
    public ExcelValidationException(ThirdsErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    /**
     * @brief Constructor con código de error, mensaje personalizado y causa
     * @param errorCode código específico del error de validación Excel
     * @param customMessage mensaje personalizado que describe el error de validación
     * @param cause causa original del error que provocó esta excepción
     */
    public ExcelValidationException(ThirdsErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }
}
