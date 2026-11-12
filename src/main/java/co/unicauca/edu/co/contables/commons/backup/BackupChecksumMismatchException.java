package co.unicauca.edu.co.contables.commons.backup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Una entrada del ZIP no coincide con el SHA-256 declarado en el manifest.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BackupChecksumMismatchException extends BackupCorruptedException {

    private final String path;

    public BackupChecksumMismatchException(String path, String expected, String actual) {
        super("El archivo no es una copia válida: '" + path + "' no coincide con su checksum (esperado "
                + expected + ", obtenido " + actual + ")");
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
