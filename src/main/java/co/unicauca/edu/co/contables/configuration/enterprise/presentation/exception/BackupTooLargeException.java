package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando el backup generado supera
 * {@code app.backup.max-size-bytes}.
 */
@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class BackupTooLargeException extends RuntimeException {
    public BackupTooLargeException(long actualSizeBytes, long maxSizeBytes) {
        super("El backup generado (" + actualSizeBytes + " bytes) supera el máximo permitido ("
                + maxSizeBytes + " bytes)");
    }
}
