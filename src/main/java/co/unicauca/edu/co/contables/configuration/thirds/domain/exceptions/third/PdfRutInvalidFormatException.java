package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando el archivo PDF no tiene el formato válido de RUT de la DIAN
 *
 * Se utiliza cuando un archivo PDF, aunque sea válido como tipo de archivo,
 * no contiene la estructura esperada de un certificado RUT emitido por la DIAN.
 */
public class PdfRutInvalidFormatException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para formatos de RUT PDF inválidos.
     */
    public PdfRutInvalidFormatException() {
        super(ThirdsErrorCode.PDF_RUT_INVALID_FORMAT);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el problema de formato
     */
    public PdfRutInvalidFormatException(String customMessage) {
        super(ThirdsErrorCode.PDF_RUT_INVALID_FORMAT, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el problema de formato
     * @param cause causa original del error que provocó esta excepción
     */
    public PdfRutInvalidFormatException(String customMessage, Throwable cause) {
        super(ThirdsErrorCode.PDF_RUT_INVALID_FORMAT, customMessage, cause);
    }
}
