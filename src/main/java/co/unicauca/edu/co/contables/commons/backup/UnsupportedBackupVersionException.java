package co.unicauca.edu.co.contables.commons.backup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief El formato o la versión del ZIP (o el schema de un módulo) no lo
 * soporta esta versión de la aplicación.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnsupportedBackupVersionException extends BackupException {

    public UnsupportedBackupVersionException(String message) {
        super(message);
    }
}
