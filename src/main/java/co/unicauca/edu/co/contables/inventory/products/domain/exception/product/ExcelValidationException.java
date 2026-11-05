package co.unicauca.edu.co.contables.inventory.products.domain.exception.product;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para errores de validación en archivos Excel
 *
 * Se lanza cuando se detectan problemas durante el procesamiento y validación
 * de archivos Excel durante operaciones de importación/exportación.
 */
public class ExcelValidationException extends BaseBusinessException {

    public ExcelValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ExcelValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}