package co.unicauca.edu.co.contables.commons.backup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief El ZIP (generado o subido) supera {@code app.backup.max-size-bytes}.
 *
 * Equivalente en el shared kernel de {@code BackupTooLargeException} del
 * módulo enterprise (que {@code commons} no puede importar).
 */
@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class BackupSizeExceededException extends BackupException {

    private final long actualSizeBytes;
    private final long maxSizeBytes;

    public BackupSizeExceededException(long actualSizeBytes, long maxSizeBytes) {
        super("La copia de seguridad (" + actualSizeBytes + " bytes) supera el máximo permitido ("
                + maxSizeBytes + " bytes)");
        this.actualSizeBytes = actualSizeBytes;
        this.maxSizeBytes = maxSizeBytes;
    }

    public long getActualSizeBytes() {
        return actualSizeBytes;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }
}
