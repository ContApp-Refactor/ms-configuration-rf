package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.nio.file.Path;
import java.util.UUID;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreOptions;

/**
 * @brief Puerto de salida para el restore (import) de una empresa.
 *
 * <ul>
 * <li>Empresa NUEVA (ZIP v1 o v2): delega en {@code IEnterpriseBackupCoordinator}
 * (todos los módulos en una sola transacción + verificación final).</li>
 * <li>Sobrescribir ({@code inplace}) con ZIP v1 (solo datos de la empresa): lógica histórica
 * de este servicio. Con ZIP v2 (copia completa) el IN_PLACE lo hace el coordinador
 * (purga + restore), no este servicio, que lo rechaza con {@code InvalidRestoreRequestException}.</li>
 * </ul>
 */
public interface IEnterpriseRestoreService {

    /** Versión de formato del ZIP (1 = solo datos de empresa, 2 = copia completa). */
    int formatVersion(Path zip);

    /** Restaura el ZIP como empresa nueva; devuelve su id. */
    UUID restore(Path zip, RestoreOptions options, RestoreActor actor);

    /**
     * Valida un restore in-place SIN tocar la BD: el ZIP es v1 y la empresa
     * destino existe en el tenant actual. Devuelve el {@code enterprise.json}.
     */
    BackupManifest validateInPlace(Path zip, UUID targetEnterpriseId);

    /** Sobrescribe {@code targetEnterpriseId} con los datos de un ZIP v1, en una transacción. */
    UUID restoreInPlace(Path zip, UUID targetEnterpriseId);
}
