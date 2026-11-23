package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando el archivo no es de tipo PDF válido
 *
 * Se utiliza para validar que solo se procesen archivos PDF en el servicio de RUT,
 * rechazando otros tipos de archivos que puedan ser enviados por error.
 */
public class PdfRutInvalidFileTypeException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de archivo PDF inválidos.
     */
    public PdfRutInvalidFileTypeException() {
        super(ThirdsErrorCode.PDF_RUT_INVALID_FILE_TYPE);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error de tipo de archivo
     */
    public PdfRutInvalidFileTypeException(String customMessage) {
        super(ThirdsErrorCode.PDF_RUT_INVALID_FILE_TYPE, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error de tipo de archivo
     * @param cause causa original del error que provocó esta excepción
     */
    public PdfRutInvalidFileTypeException(String customMessage, Throwable cause) {
        super(ThirdsErrorCode.PDF_RUT_INVALID_FILE_TYPE, customMessage, cause);
    }
}
