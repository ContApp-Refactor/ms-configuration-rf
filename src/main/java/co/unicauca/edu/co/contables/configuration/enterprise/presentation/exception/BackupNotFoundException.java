package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando no existe (o no es válido) el backup con el
 * id dado.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BackupNotFoundException extends RuntimeException {
    public BackupNotFoundException(String backupId) {
        super("No existe el backup con id '" + backupId + "'");
    }
}
