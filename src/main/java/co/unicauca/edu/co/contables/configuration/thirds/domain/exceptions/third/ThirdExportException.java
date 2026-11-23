package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción específica para errores durante la exportación de terceros
 *
 * Se utiliza cuando fallan operaciones de exportación de datos de terceros
 * a diferentes formatos (Excel, PDF, etc.) por problemas técnicos o de validación.
 */
public class ThirdExportException extends BaseBusinessException {

    /**
     * @brief Constructor con código de error
     * @param errorCode código específico del error de exportación
     */
    public ThirdExportException(ThirdsErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * @brief Constructor con código de error y mensaje personalizado
     * @param errorCode código específico del error de exportación
     * @param customMessage mensaje personalizado que describe el error de exportación
     */
    public ThirdExportException(ThirdsErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    /**
     * @brief Constructor con código de error, mensaje personalizado y causa
     * @param errorCode código específico del error de exportación
     * @param customMessage mensaje personalizado que describe el error de exportación
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdExportException(ThirdsErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }
}
