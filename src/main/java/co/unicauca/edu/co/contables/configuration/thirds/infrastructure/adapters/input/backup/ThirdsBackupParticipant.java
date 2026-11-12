package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.NdjsonEntryWriter;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PersonClassification;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdTypeRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdsAndTypesRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.TypeIdRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code thirds} (order 20) de la copia completa de una empresa.
 *
 * Entradas (todas de LA empresa, nunca del tenant completo):
 * <ol>
 * <li>{@code thirds/type_ids.ndjson}: TYPE_ID ({@code ti_entid}).</li>
 * <li>{@code thirds/third_types.ndjson}: THIRD_TYPE ({@code tt_entid}).</li>
 * <li>{@code thirds/thirds.ndjson}: THIRDS ({@code ent_id}), con la FK al tipo de identificación.</li>
 * <li>{@code thirds/thirds_and_types.ndjson}: relación N:M {@code thirds_and_types} (sin columna de empresa: se
 * resuelve por join con los terceros y tipos de la empresa).</li>
 * </ol>
 *
 * Restore: ids nuevos (IDENTITY), columna de empresa = empresa destino, {@code th_usage_count} = 0 y
 * {@code @TenantId} lo pone Hibernate. Registra en el {@link IdMap}: {@link BackupKeys#TYPE_ID},
 * {@link BackupKeys#THIRD_TYPE} y {@link BackupKeys#THIRD} (Long→Long). Escribe por repositorios JPA, sin pasar
 * por los servicios que publican {@code ThirdEvent} a Rabbit.
 *
 * No se copian países/departamentos/ciudades (seed de {@code GeographyDataInitializer}; el tercero guarda
 * ubicación como texto). La entidad {@code ThirdAndTypeEntity} mapea la misma tabla que
 * {@code ThirdsAndTypesEntity} y se ignora.
 */
@Component
@RequiredArgsConstructor
public class ThirdsBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;

    public static final String ENTRY_TYPE_IDS = "thirds/type_ids.ndjson";
    public static final String ENTRY_THIRD_TYPES = "thirds/third_types.ndjson";
    public static final String ENTRY_THIRDS = "thirds/thirds.ndjson";
    public static final String ENTRY_LINKS = "thirds/thirds_and_types.ndjson";

    private static final int PAGE_SIZE = 500;
    private static final int MAX_PROBLEMS = 50;

    private final TypeIdRepository typeIdRepository;
    private final ThirdTypeRepository thirdTypeRepository;
    private final ThirdRepository thirdRepository;
    private final ThirdsAndTypesRepository linkRepository;
    private final EntityManager entityManager;

    /* ---------------------------- DTOs del ZIP ---------------------------- */

    public record TypeIdRow(Long id, String code, String name, Boolean status, PersonClassification classification) {
    }

    public record ThirdTypeRow(Long id, String name, Boolean status) {
    }

    public record ThirdRow(Long id, Long typeIdRef, ePersonType personType, String names, String lastNames,
            String socialReason, String gender, Long idNumber, Long verificationNumber, Boolean state, String country,
            String province, String city, String address, String phoneNumber, String email) {
    }

    public record ThirdLinkRow(Long thirdId, Long thirdTypeId) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_THIRDS;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_THIRDS;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String entId = enterpriseId.toString();

        try (NdjsonEntryWriter out = writer.ndjson(ENTRY_TYPE_IDS)) {
            Page<TypeIdEntity> page;
            int n = 0;
            do {
                page = typeIdRepository.findAllByTientIdPageable(entId, PageRequest.of(n++, PAGE_SIZE, Sort.by("id")));
                page.forEach(e -> out.write(new TypeIdRow(e.getId(), e.getTiId(), e.getTiName(), e.getStatus(),
                        e.getClassification())));
            } while (page.hasNext());
        }

        try (NdjsonEntryWriter out = writer.ndjson(ENTRY_THIRD_TYPES)) {
            Page<ThirdTypeEntity> page;
            int n = 0;
            do {
                page = thirdTypeRepository.findAllByTtentIdPageable(entId,
                        PageRequest.of(n++, PAGE_SIZE, Sort.by("ttId")));
                page.forEach(e -> out.write(new ThirdTypeRow(e.getTtId(), e.getTtName(), e.getStatus())));
            } while (page.hasNext());
        }

        try (NdjsonEntryWriter out = writer.ndjson(ENTRY_THIRDS)) {
            Page<ThirdEntity> page;
            int n = 0;
            do {
                page = thirdRepository.findAllForExport(entId, PageRequest.of(n++, PAGE_SIZE, Sort.by("thId")));
                page.forEach(e -> out.write(toRow(e)));
            } while (page.hasNext());
        }

        try (NdjsonEntryWriter out = writer.ndjson(ENTRY_LINKS)) {
            Slice<ThirdsAndTypesRepository.LinkRow> slice;
            int n = 0;
            do {
                slice = linkRepository.findBackupLinksByEntId(entId, PageRequest.of(n++, PAGE_SIZE));
                slice.forEach(l -> out.write(new ThirdLinkRow(l.getThId(), l.getTtId())));
            } while (slice.hasNext());
        }
    }

    private static ThirdRow toRow(ThirdEntity e) {
        Long typeId = e.getTypeId() == null ? null : e.getTypeId().getId();
        return new ThirdRow(e.getThId(), typeId, e.getPersonType(), e.getNames(), e.getLastNames(),
                e.getSocialReason(), e.getGender(), e.getIdNumber(), e.getVerificationNumber(), e.getState(),
                e.getCountry(), e.getProvince(), e.getCity(), e.getAddress(), e.getPhoneNumber(), e.getEmail());
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String target = ctx.targetEnterpriseIdAsString();

        if (reader.has(ENTRY_TYPE_IDS)) {
            reader.forEachBatch(ENTRY_TYPE_IDS, TypeIdRow.class, PAGE_SIZE, batch -> {
                List<TypeIdEntity> entities = new ArrayList<>(batch.size());
                for (TypeIdRow r : batch) {
                    entities.add(TypeIdEntity.builder().tiId(r.code()).tiName(r.name()).tientId(target)
                            .status(r.status()).classification(r.classification()).build());
                }
                List<TypeIdEntity> saved = typeIdRepository.saveAll(entities);
                for (int i = 0; i < batch.size(); i++) {
                    idMap.put(BackupKeys.TYPE_ID, batch.get(i).id(), saved.get(i).getId());
                }
                entityManager.flush();
            });
        }

        if (reader.has(ENTRY_THIRD_TYPES)) {
            reader.forEachBatch(ENTRY_THIRD_TYPES, ThirdTypeRow.class, PAGE_SIZE, batch -> {
                List<ThirdTypeEntity> entities = new ArrayList<>(batch.size());
                for (ThirdTypeRow r : batch) {
                    entities.add(ThirdTypeEntity.builder().ttName(r.name()).ttentId(target).status(r.status())
                            .build());
                }
                List<ThirdTypeEntity> saved = thirdTypeRepository.saveAll(entities);
                for (int i = 0; i < batch.size(); i++) {
                    idMap.put(BackupKeys.THIRD_TYPE, batch.get(i).id(), saved.get(i).getTtId());
                }
                entityManager.flush();
            });
        }

        if (reader.has(ENTRY_THIRDS)) {
            reader.forEachBatch(ENTRY_THIRDS, ThirdRow.class, PAGE_SIZE, batch -> {
                List<ThirdEntity> entities = new ArrayList<>(batch.size());
                for (ThirdRow r : batch) {
                    Long newTypeId = idMap.requireNullable(BackupKeys.TYPE_ID, r.typeIdRef());
                    entities.add(ThirdEntity.builder()
                            .entId(target)
                            .typeId(newTypeId == null ? null : typeIdRepository.getReferenceById(newTypeId))
                            .personType(r.personType()).names(r.names()).lastNames(r.lastNames())
                            .socialReason(r.socialReason()).gender(r.gender()).idNumber(r.idNumber())
                            .verificationNumber(r.verificationNumber()).state(r.state()).country(r.country())
                            .province(r.province()).city(r.city()).address(r.address())
                            .phoneNumber(r.phoneNumber()).email(r.email()).usageCount(0).build());
                }
                List<ThirdEntity> saved = thirdRepository.saveAll(entities);
                for (int i = 0; i < batch.size(); i++) {
                    idMap.put(BackupKeys.THIRD, batch.get(i).id(), saved.get(i).getThId());
                }
                entityManager.flush();
                saved.forEach(entityManager::detach);
            });
        }

        if (reader.has(ENTRY_LINKS)) {
            reader.forEachBatch(ENTRY_LINKS, ThirdLinkRow.class, PAGE_SIZE, batch -> {
                List<ThirdsAndTypesEntity> entities = new ArrayList<>(batch.size());
                for (ThirdLinkRow r : batch) {
                    entities.add(ThirdsAndTypesEntity.builder()
                            .thId(idMap.requireLong(BackupKeys.THIRD, r.thirdId()))
                            .ttId(idMap.requireLong(BackupKeys.THIRD_TYPE, r.thirdTypeId())).build());
                }
                List<ThirdsAndTypesEntity> saved = linkRepository.saveAll(entities);
                entityManager.flush();
                saved.forEach(entityManager::detach);
            });
        }
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore IN_PLACE)                                           */
    /* ------------------------------------------------------------------ */

    /** Borra los datos de thirds de la empresa: relaciones N:M, terceros, tipos de tercero y tipos de identificación. */
    @Override
    public void purge(UUID enterpriseId) {
        String entId = enterpriseId.toString();
        entityManager.flush();
        entityManager.createQuery("DELETE FROM ThirdsAndTypesEntity tat WHERE tat.thId IN "
                + "(SELECT t.thId FROM ThirdEntity t WHERE t.entId = :entId) OR tat.ttId IN "
                + "(SELECT tt.ttId FROM ThirdTypeEntity tt WHERE tt.ttentId = :entId)")
                .setParameter("entId", entId).executeUpdate();
        entityManager.createQuery("DELETE FROM ThirdEntity t WHERE t.entId = :entId")
                .setParameter("entId", entId).executeUpdate();
        entityManager.createQuery("DELETE FROM ThirdTypeEntity tt WHERE tt.ttentId = :entId")
                .setParameter("entId", entId).executeUpdate();
        entityManager.createQuery("DELETE FROM TypeIdEntity ti WHERE ti.tientId = :entId")
                .setParameter("entId", entId).executeUpdate();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        String entId = enterpriseId.toString();
        return Map.of(
                ENTRY_TYPE_IDS, typeIdRepository.countByTientId(entId),
                ENTRY_THIRD_TYPES, thirdTypeRepository.countByTtentId(entId),
                ENTRY_THIRDS, thirdRepository.countByEntId(entId),
                ENTRY_LINKS, linkRepository.countBackupLinksByEntId(entId));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String entId = enterpriseId.toString();
        List<String> problems = new ArrayList<>();
        PageRequest limit = PageRequest.of(0, MAX_PROBLEMS);
        for (Long thirdId : thirdRepository.findIdsWithForeignTypeId(entId, limit)) {
            problems.add("el tercero " + thirdId + " apunta a un tipo de identificación que no es de la empresa");
        }
        for (ThirdsAndTypesRepository.LinkRow l : linkRepository.findOrphanLinksByEntId(entId, limit)) {
            problems.add("la relación tercero " + l.getThId() + " - tipo de tercero " + l.getTtId()
                    + " apunta a un tipo que no es de la empresa");
        }
        return problems;
    }
}
