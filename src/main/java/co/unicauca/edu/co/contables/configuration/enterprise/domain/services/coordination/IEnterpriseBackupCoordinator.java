package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;

/**
 * @brief Puerto de entrada (servicio de aplicación) de la copia de seguridad
 * completa de empresas (ZIP v2): coordina TODOS los beans
 * {@code BackupParticipant} descubiertos por Spring, ordenados por
 * {@code order()} y validados contra {@code requires()} al arrancar.
 *
 * Propagación de la empresa destino: el participante {@code enterprise}
 * (order mínimo) crea la empresa y registra
 * {@code idMap.put(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId(), nuevoId)};
 * el coordinador lee esa equivalencia y pasa a los participantes siguientes
 * {@code ctx.withTargetEnterpriseId(nuevoId)} (el contexto es inmutable).
 * {@code ctx.sourceEnterpriseId()} nunca es null: si un ZIP v1 viejo no lo
 * declara, el coordinador genera uno.
 */
public interface IEnterpriseBackupCoordinator {

    /**
     * Exporta la empresa (debe ser del tenant actual) a {@code target} en
     * streaming, dentro de una transacción de solo lectura. El archivo aparece
     * recién al terminar bien; si algo falla no queda nada en {@code target}.
     *
     * @param username autor informativo del manifest ({@code createdBy.username})
     */
    ManifestV2 exportTo(UUID enterpriseId, Path target, String username, BackupProgressListener listener);

    /**
     * Abre y valida el ZIP SIN tocar la BD (ver {@link PreparedRestore}).
     * Errores: {@code BackupCorruptedException}/{@code BackupChecksumMismatchException} (422),
     * {@code UnsupportedBackupVersionException} (422), {@code UnsupportedBackupModuleException} (422),
     * {@code BackupSizeExceededException} (413), {@code EnterpriseNitAlreadyExistsException} (409),
     * {@code InvalidRestoreRequestException} (400, p. ej. IN_PLACE sin empresa destino),
     * {@code EnterpriseNotFoundException} (404, IN_PLACE sobre una empresa que no es del tenant) y
     * {@code PurgeNotSupportedException} (422, IN_PLACE con un módulo sin {@code purge}).
     *
     * IN_PLACE (D4 actualizado): el destino es {@link RestoreOptions#targetEnterpriseId()}; en
     * {@link #execute} se purgan los módulos de la copia en orden inverso de {@code order()}
     * y luego se restauran en orden normal, todo en la misma transacción.
     *
     * @param actor tenant y usuario de la petición actual ({@link RestoreActor#fromCurrentContext()}),
     *              nunca los del ZIP
     */
    PreparedRestore prepare(Path zip, RestoreOptions options, RestoreActor actor);

    /**
     * Ejecuta TODOS los {@code restore} en UNA transacción, con un IdMap nuevo,
     * y dentro de la misma transacción verifica {@code verifyIntegrity} y
     * {@code count} contra el manifest. Si algo falla: rollback completo y
     * {@code BackupIntegrityException} / {@code RestoreFailedException} (con el módulo).
     */
    RestoreResult execute(PreparedRestore prepared, BackupProgressListener listener);

    /** {@link #prepare} + {@link #execute} (cerrando el ZIP). */
    default RestoreResult restore(Path zip, RestoreOptions options, RestoreActor actor,
            BackupProgressListener listener) {
        try (PreparedRestore prepared = prepare(zip, options, actor)) {
            return execute(prepared, listener);
        }
    }

    /**
     * Duplica una empresa: export a un ZIP temporal + restore en modo
     * DUPLICATE (NIT repetido permitido, nombre {@code targetName} o
     * {@code "<nombre> (copia)"}). La empresa origen no se modifica. El
     * temporal se borra siempre.
     */
    RestoreResult duplicate(UUID enterpriseId, String targetName, RestoreActor actor, String username,
            BackupProgressListener listener);

    /** Módulos soportados, en orden de ejecución. */
    List<BackupModuleInfo> supportedModules();
}
