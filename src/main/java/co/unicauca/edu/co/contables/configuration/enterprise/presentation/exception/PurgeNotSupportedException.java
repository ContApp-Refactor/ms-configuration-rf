package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import co.unicauca.edu.co.contables.commons.backup.BackupException;

/**
 * @brief Un módulo de la copia no sabe borrar sus datos
 * ({@code BackupParticipant.purge}), por lo que no se puede restaurar sobre
 * una empresa existente. Se lanza antes de tocar la BD o, si surge durante la
 * purga, provoca el rollback total del restore.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PurgeNotSupportedException extends BackupException {

    private final String moduleKey;

    public PurgeNotSupportedException(String moduleKey) {
        super("No se puede restaurar sobre una empresa existente: el módulo '" + moduleKey
                + "' no soporta borrar sus datos actuales. Restaura la copia como empresa nueva.");
        this.moduleKey = moduleKey;
    }

    public String getModuleKey() {
        return moduleKey;
    }
}
