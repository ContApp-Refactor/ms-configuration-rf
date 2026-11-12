package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import co.unicauca.edu.co.contables.commons.backup.BackupException;

/**
 * @brief La copia incluye módulos que esta versión de la aplicación no sabe
 * restaurar (no hay {@code BackupParticipant} para ellos).
 *
 * Se lanza antes de abrir la transacción del restore, salvo que el usuario
 * pida {@code skipUnknownModules=true}.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnsupportedBackupModuleException extends BackupException {

    private final List<String> modules;

    public UnsupportedBackupModuleException(List<String> modules) {
        super("La copia incluye " + (modules.size() == 1 ? "el módulo '" : "los módulos '")
                + String.join("', '", modules) + "', que esta versión no puede restaurar. "
                + "Puedes restaurarla omitiéndolos (skipUnknownModules=true).");
        this.modules = List.copyOf(modules);
    }

    public List<String> getModules() {
        return modules;
    }
}
