package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada en los endpoints de proceso de copia
 * ({@code /copy/restore}, {@code /copy/restore/upload}) cuando el backup
 * referenciado (por {@code backupRef} o por archivo subido) está corrupto o
 * no es válido.
 *
 * Es un envoltorio 422 de {@link InvalidBackupFileException} (que es 400):
 * el contrato que consume el frontend para estos dos endpoints pide 422
 * específicamente para este caso, sin afectar el 400 que
 * {@code InvalidBackupFileException} sigue teniendo en el resto del módulo.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CorruptBackupException extends RuntimeException {
    public CorruptBackupException(String message, Throwable cause) {
        super(message, cause);
    }
}
