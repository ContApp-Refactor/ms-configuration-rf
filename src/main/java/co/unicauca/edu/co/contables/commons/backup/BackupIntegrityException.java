package co.unicauca.edu.co.contables.commons.backup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Falla de integridad durante el restore: FK que no se puede remapear,
 * huérfanos o conteos distintos a los del manifest. Provoca rollback total.
 *
 * {@code moduleKey} puede ser {@code null} cuando la lanza {@link IdMap}
 * (no conoce el módulo); el coordinador la reenvuelve con
 * {@link #inModule(String)} para que el mensaje diga qué módulo falló.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BackupIntegrityException extends BackupException {

    private final String moduleKey;
    private final String detail;

    public BackupIntegrityException(String moduleKey, String detail) {
        super(format(moduleKey, detail));
        this.moduleKey = moduleKey;
        this.detail = detail;
    }

    public BackupIntegrityException(String moduleKey, String detail, Throwable cause) {
        super(format(moduleKey, detail), cause);
        this.moduleKey = moduleKey;
        this.detail = detail;
    }

    /** FK sin equivalencia en el {@link IdMap}. */
    public static BackupIntegrityException missingId(String namespace, Object oldId) {
        return new BackupIntegrityException(null,
                "la copia referencia " + namespace + " id=" + oldId + ", que no está en la copia");
    }

    /** Copia de esta excepción con el módulo indicado (si no tenía uno). */
    public BackupIntegrityException inModule(String module) {
        if (moduleKey != null) {
            return this;
        }
        return new BackupIntegrityException(module, detail, this);
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public String getDetail() {
        return detail;
    }

    private static String format(String moduleKey, String detail) {
        return moduleKey == null ? detail : moduleKey + ": " + detail;
    }
}
