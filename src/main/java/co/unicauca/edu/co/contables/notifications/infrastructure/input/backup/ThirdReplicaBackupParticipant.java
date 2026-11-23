package co.unicauca.edu.co.contables.notifications.infrastructure.input.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.NdjsonEntryWriter;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code notifications.thirdReplicas} (order 25): réplicas de terceros de notificaciones.
 *
 * Sin estas filas la empresa nueva no recibe notificaciones de sus terceros (el listener solo las crea al
 * publicarse un {@code THIRD_UPDATED}, que el restore NO dispara). Entrada:
 * {@code notifications/third_replicas.ndjson}. Se exportan solo las réplicas con tercero vivo en la empresa.
 *
 * Restore: {@code third_id} se remapea con {@code IdMap[THIRD]} (por eso depende de {@code thirds}),
 * {@code enterprise_id} = empresa destino; id propio nuevo e {@code @TenantId} automático. El {@code third_id}
 * es único global: como los terceros son nuevos nunca choca con el origen.
 */
@Component
@RequiredArgsConstructor
public class ThirdReplicaBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_REPLICAS = "notifications/third_replicas.ndjson";

    private static final int PAGE_SIZE = 500;

    private final IThirdReplicaRepository replicaRepository;
    private final EntityManager entityManager;

    public record ThirdReplicaRow(Long thirdId, String fullName, String email, Boolean active) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_THIRD_REPLICAS;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_THIRD_REPLICAS;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_THIRDS);
    }

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String entId = enterpriseId.toString();
        try (NdjsonEntryWriter out = writer.ndjson(ENTRY_REPLICAS)) {
            Slice<ThirdReplicaEntity> slice;
            int n = 0;
            do {
                slice = replicaRepository.findBackupReplicas(entId, PageRequest.of(n++, PAGE_SIZE));
                slice.forEach(r -> out
                        .write(new ThirdReplicaRow(r.getThirdId(), r.getFullName(), r.getEmail(), r.getActive())));
            } while (slice.hasNext());
        }
    }

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        if (!reader.has(ENTRY_REPLICAS)) {
            return;
        }
        String target = ctx.targetEnterpriseIdAsString();
        reader.forEachBatch(ENTRY_REPLICAS, ThirdReplicaRow.class, PAGE_SIZE, batch -> {
            List<ThirdReplicaEntity> entities = new ArrayList<>(batch.size());
            for (ThirdReplicaRow r : batch) {
                ThirdReplicaEntity e = new ThirdReplicaEntity();
                e.setThirdId(idMap.requireLong(BackupKeys.THIRD, r.thirdId()));
                e.setEnterpriseId(target);
                e.setFullName(r.fullName());
                e.setEmail(r.email());
                e.setActive(r.active());
                entities.add(e);
            }
            List<ThirdReplicaEntity> saved = replicaRepository.saveAll(entities);
            entityManager.flush();
            saved.forEach(entityManager::detach);
        });
    }

    /** Borra TODAS las réplicas de terceros de la empresa (también las huérfanas que el export omite). */
    @Override
    public void purge(UUID enterpriseId) {
        entityManager.flush();
        entityManager.createQuery("DELETE FROM ThirdReplicaEntity r WHERE r.enterpriseId = :entId")
                .setParameter("entId", enterpriseId.toString()).executeUpdate();
        entityManager.clear();
    }

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(ENTRY_REPLICAS, replicaRepository.countBackupReplicas(enterpriseId.toString()));
    }

    /**
     * Sin chequeo cruzado: confirmar que {@code third_id} apunta a un tercero de la empresa exigiría leer el
     * módulo thirds. El remapeo ya es estricto ({@code IdMap.requireLong}) y el export filtra réplicas sin tercero.
     */
    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        return List.of();
    }
}
