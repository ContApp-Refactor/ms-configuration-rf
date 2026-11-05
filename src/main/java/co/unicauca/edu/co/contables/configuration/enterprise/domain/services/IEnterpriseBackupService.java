package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.UUID;

import org.springframework.core.io.Resource;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;

/**
 * @brief Puerto de salida para la copia de seguridad (export) de una empresa.
 */
public interface IEnterpriseBackupService {

    /**
     * Exporta la empresa (sus datos propios, su ubicación, su tipo de persona
     * y las materias visibles en su tenant) a un ZIP bajo
     * {@code app.backup.dir} y devuelve la referencia para descargarlo o
     * para encadenarlo con un restore (duplicate).
     */
    BackupReferenceDto backup(UUID enterpriseId);

    /**
     * Carga el ZIP de un backup existente como {@link Resource} para
     * descarga por streaming. Valida que {@code backupId} no permita
     * path traversal fuera de {@code app.backup.dir}.
     */
    Resource loadBackupFile(String backupId);

    /** Lee y deserializa el {@code enterprise.json} contenido en un backup existente. */
    BackupManifest readManifest(String backupId);

    /** Deserializa el {@code enterprise.json} contenido en un ZIP de backup subido por el usuario. */
    BackupManifest readManifest(byte[] zipBytes);

    /** Elimina el ZIP de un backup (usado por duplicate para limpiar el artefacto intermedio). */
    void deleteBackup(String backupId);
}
