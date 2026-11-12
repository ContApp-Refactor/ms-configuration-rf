package co.unicauca.edu.co.contables.commons.backup;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @brief Datos inmutables de un restore que reciben todos los participantes.
 *
 * {@code tenantId}/{@code userId} vienen de {@link RestoreActor} (capturado en
 * el controlador). {@code targetEnterpriseId} es {@code null} mientras corre el
 * participante {@code enterprise} (order 0), que es quien crea la empresa; el
 * coordinador lo completa con {@link #withTargetEnterpriseId(UUID)} antes de
 * llamar al resto.
 *
 * @param tenantId            tenant destino (los inserts lo toman de TenantContext vía {@code @TenantId})
 * @param userId              {@code sub} del usuario que restaura (nuevo {@code idUser} de la empresa)
 * @param sourceEnterpriseId  empresa de origen según el manifest
 * @param targetEnterpriseId  empresa destino (null hasta que la crea el participante enterprise)
 * @param formatVersion       versión del ZIP (1 o 2)
 * @param exportedAt          fecha del export según el manifest
 * @param mode                modo de restore
 * @param targetName          nombre destino opcional (p. ej. "(copia)" o el que pidió el usuario)
 * @param targetNit           NIT destino opcional (D3: para evitar el 409 por NIT repetido)
 * @param skipUnknownModules  si {@code true}, los módulos del ZIP sin participante se ignoran en vez de fallar
 */
public record RestoreContext(
        String tenantId,
        String userId,
        UUID sourceEnterpriseId,
        UUID targetEnterpriseId,
        int formatVersion,
        LocalDateTime exportedAt,
        RestoreMode mode,
        String targetName,
        String targetNit,
        boolean skipUnknownModules) {

    public RestoreContext {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId es obligatorio en RestoreContext");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId es obligatorio en RestoreContext");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode es obligatorio en RestoreContext");
        }
    }

    /** Contexto inicial (sin empresa destino ni overrides). */
    public static RestoreContext of(RestoreActor actor, RestoreMode mode, UUID sourceEnterpriseId,
            int formatVersion, LocalDateTime exportedAt) {
        return new RestoreContext(actor.tenantId(), actor.userId(), sourceEnterpriseId, null,
                formatVersion, exportedAt, mode, null, null, false);
    }

    public RestoreContext withTargetEnterpriseId(UUID id) {
        return new RestoreContext(tenantId, userId, sourceEnterpriseId, id, formatVersion, exportedAt, mode,
                targetName, targetNit, skipUnknownModules);
    }

    public RestoreContext withTargetName(String name) {
        return new RestoreContext(tenantId, userId, sourceEnterpriseId, targetEnterpriseId, formatVersion,
                exportedAt, mode, name, targetNit, skipUnknownModules);
    }

    public RestoreContext withTargetNit(String nit) {
        return new RestoreContext(tenantId, userId, sourceEnterpriseId, targetEnterpriseId, formatVersion,
                exportedAt, mode, targetName, nit, skipUnknownModules);
    }

    public RestoreContext withSkipUnknownModules(boolean skip) {
        return new RestoreContext(tenantId, userId, sourceEnterpriseId, targetEnterpriseId, formatVersion,
                exportedAt, mode, targetName, targetNit, skip);
    }

    /** Empresa destino; falla si todavía no la creó el participante enterprise. */
    public UUID requireTargetEnterpriseId() {
        if (targetEnterpriseId == null) {
            throw new IllegalStateException("La empresa destino todavía no fue creada en este restore");
        }
        return targetEnterpriseId;
    }

    /** Empresa destino como texto, para columnas {@code idEnterprise}/{@code ent_id} de tipo String. */
    public String targetEnterpriseIdAsString() {
        return requireTargetEnterpriseId().toString();
    }
}
