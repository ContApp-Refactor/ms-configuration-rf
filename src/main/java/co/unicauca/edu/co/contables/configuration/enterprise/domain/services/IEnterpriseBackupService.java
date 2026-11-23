package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.nio.file.Path;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupProgressListener;

/**
 * @brief Puerto de salida para los archivos de copia de seguridad de empresas.
 *
 * El contenido lo arma el coordinador ({@code IEnterpriseBackupCoordinator},
 * ZIP v2 con todos los módulos); este servicio decide DÓNDE vive cada ZIP:
 * {@code app.backup.dir/<sha256(tenant)>/<backupId>.zip}. Los ZIP viejos
 * (directo en {@code app.backup.dir}) se siguen leyendo. Ningún método
 * verifica que el {@code backupId} sea del tenant: eso lo hace
 * {@code ICopyProcessService} con el registro {@code CopyProcess}.
 */
public interface IEnterpriseBackupService {

    /** Exporta la empresa (del tenant actual) a un ZIP v2 nuevo. */
    default BackupReferenceDto backup(UUID enterpriseId) {
        return backup(enterpriseId, null, BackupProgressListener.NONE);
    }

    /** Exporta la empresa a un ZIP v2 nuevo, informando el avance por módulo. */
    BackupReferenceDto backup(UUID enterpriseId, String username, BackupProgressListener listener);

    /** Guarda (en streaming, sin cargarlo en memoria) un ZIP subido bajo el tenant actual. */
    BackupReferenceDto storeUpload(MultipartFile file);

    /** Ruta del ZIP de un backup existente; {@code BackupNotFoundException} si no existe o el id no es válido. */
    Path resolveBackupFile(String backupId);

    /** Carga el ZIP de un backup existente como {@link Resource} para descarga por streaming. */
    Resource loadBackupFile(String backupId);

    /** Lee el {@code enterprise.json} (v1 o v2) de un backup existente. */
    BackupManifest readManifest(String backupId);

    /** Lee el {@code enterprise.json} (v1 o v2) de un ZIP en disco, validando el ZIP. */
    BackupManifest readManifest(Path zip);

    /** Elimina el ZIP de un backup (si existe). */
    void deleteBackup(String backupId);
}
