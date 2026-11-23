package co.unicauca.edu.co.contables.commons.backup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief El archivo no es una copia válida: ZIP ilegible, manifest inválido,
 * entradas no declaradas o con nombre peligroso (zip-slip), conteos que no
 * coinciden, límites de descompresión superados (zip-bomb), etc.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BackupCorruptedException extends BackupException {

    public BackupCorruptedException(String message) {
        super(message);
    }

    public BackupCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
