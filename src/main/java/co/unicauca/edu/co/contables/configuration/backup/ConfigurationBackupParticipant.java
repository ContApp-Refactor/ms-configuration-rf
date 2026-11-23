package co.unicauca.edu.co.contables.configuration.backup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.NdjsonEntryWriter;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository.AccountingCalendarRepository;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.entity.TagEntity;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.repository.ITagRepository;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code configuration} (order 10) de la copia completa.
 *
 * Entradas (todas NDJSON, un registro por línea):
 * <ul>
 * <li>{@code configuration/document_classes.ndjson}: clases de documento.</li>
 * <li>{@code configuration/document_types.ndjson}: tipos de documento (FK a la clase).</li>
 * <li>{@code configuration/cost_centers.ndjson}: centros de costo (auto-referencia por {@code parentId}).</li>
 * <li>{@code configuration/accounting_calendar.ndjson}: fechas del calendario contable.</li>
 * <li>{@code configuration/tags.ndjson}: tags no comerciales.</li>
 * </ul>
 *
 * Restore: PKs nuevas (IDENTITY), columna de empresa = empresa destino,
 * {@code @TenantId} lo pone Hibernate. Registra {@code DOC_CLASS},
 * {@code DOC_TYPE} y {@code COST_CENTER} en el {@link IdMap}. Los centros de
 * costo se insertan en dos pasadas (sin padre y luego se enlaza el padre) para
 * no depender del orden ni del {@code cascade = ALL} sobre {@code children}.
 * {@code usage_count} se resetea a 0 (D5): cuenta usos por documentos que
 * todavía no viajan. {@code help_center} es un catálogo global y no se copia.
 */
@Component
@RequiredArgsConstructor
public class ConfigurationBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;

    public static final String ENTRY_DOCUMENT_CLASSES = "configuration/document_classes.ndjson";
    public static final String ENTRY_DOCUMENT_TYPES = "configuration/document_types.ndjson";
    public static final String ENTRY_COST_CENTERS = "configuration/cost_centers.ndjson";
    public static final String ENTRY_ACCOUNTING_CALENDAR = "configuration/accounting_calendar.ndjson";
    public static final String ENTRY_TAGS = "configuration/tags.ndjson";

    private static final int PAGE_SIZE = 500;

    /* ---------- DTOs de snapshot (no exponen entidades JPA) ---------- */

    public record DocumentClassSnapshot(Long id, String name, Boolean status) {
    }

    public record DocumentTypeSnapshot(Long id, String prefix, String name, Long documentClassId, String module,
            Boolean status) {
    }

    public record CostCenterSnapshot(Long id, String code, String name, Long parentId, Boolean status) {
    }

    public record AccountingCalendarSnapshot(Long id, LocalDate date) {
    }

    public record TagSnapshot(Long id, String title, String description) {
    }

    private final DocumentClassRepository documentClassRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final CostCenterRepository costCenterRepository;
    private final AccountingCalendarRepository calendarRepository;
    private final ITagRepository tagRepository;
    private final EntityManager entityManager;

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_CONFIGURATION;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_CONFIGURATION;
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
        String ent = enterpriseId.toString();
        exportPaged(writer, ENTRY_DOCUMENT_CLASSES, p -> documentClassRepository.findAllByIdEnterprise(ent, p),
                c -> new DocumentClassSnapshot(c.getId(), c.getName(), c.getStatus()));
        exportPaged(writer, ENTRY_DOCUMENT_TYPES, p -> documentTypeRepository.findAllByIdEnterprise(ent, p),
                t -> new DocumentTypeSnapshot(t.getId(), t.getPrefix(), t.getName(),
                        t.getDocumentClass().getId(), t.getModule(), t.getStatus()));
        exportPaged(writer, ENTRY_COST_CENTERS, p -> costCenterRepository.findAllByIdEnterprise(ent, p),
                c -> new CostCenterSnapshot(c.getId(), c.getCode(), c.getName(),
                        c.getParent() == null ? null : c.getParent().getId(), c.getStatus()));
        exportPaged(writer, ENTRY_ACCOUNTING_CALENDAR, p -> calendarRepository.findAllByIdEnterprise(ent, p),
                a -> new AccountingCalendarSnapshot(a.getId(), a.getDate()));
        // Tag no tiene @TenantId: se lee por enterpriseId (el coordinador ya validó la empresa).
        exportPaged(writer, ENTRY_TAGS, p -> tagRepository.findAllByEnterpriseId(ent, p),
                t -> new TagSnapshot(t.getId(), t.getTitle(), t.getDescription()));
    }

    /** Lee por páginas ordenadas por id y escribe cada registro en streaming; desacopla cada entidad leída. */
    private <E, S> void exportPaged(BackupWriter writer, String path, Function<Pageable, Page<E>> pageFn,
            Function<E, S> mapper) {
        try (NdjsonEntryWriter out = writer.ndjson(path)) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id"));
            Page<E> page;
            do {
                page = pageFn.apply(pageable);
                for (E entity : page.getContent()) {
                    out.write(mapper.apply(entity));
                    entityManager.detach(entity);
                }
                pageable = page.nextPageable();
            } while (page.hasNext());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.targetEnterpriseIdAsString();
        restoreDocumentClasses(reader, idMap, ent);
        restoreDocumentTypes(reader, idMap, ent);
        restoreCostCenters(reader, idMap, ent);
        restoreCalendar(reader, ent);
        restoreTags(reader, ent);
        entityManager.flush();
    }

    private void restoreDocumentClasses(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ENTRY_DOCUMENT_CLASSES)) {
            return;
        }
        reader.forEachBatch(ENTRY_DOCUMENT_CLASSES, DocumentClassSnapshot.class, PAGE_SIZE, batch -> {
            List<DocumentClassEntity> entities = new ArrayList<>(batch.size());
            for (DocumentClassSnapshot s : batch) {
                entities.add(DocumentClassEntity.builder()
                        .name(s.name())
                        .idEnterprise(ent)
                        .status(orTrue(s.status()))
                        .build());
            }
            documentClassRepository.saveAll(entities);
            for (int i = 0; i < batch.size(); i++) {
                idMap.put(BackupKeys.DOC_CLASS, batch.get(i).id(), entities.get(i).getId());
            }
            flushAndDetach(entities);
        });
    }

    private void restoreDocumentTypes(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ENTRY_DOCUMENT_TYPES)) {
            return;
        }
        reader.forEachBatch(ENTRY_DOCUMENT_TYPES, DocumentTypeSnapshot.class, PAGE_SIZE, batch -> {
            List<DocumentTypeEntity> entities = new ArrayList<>(batch.size());
            for (DocumentTypeSnapshot s : batch) {
                Long newClassId = idMap.requireLong(BackupKeys.DOC_CLASS, s.documentClassId());
                entities.add(DocumentTypeEntity.builder()
                        .prefix(s.prefix())
                        .name(s.name())
                        .documentClass(documentClassRepository.getReferenceById(newClassId))
                        .module(s.module())
                        .idEnterprise(ent)
                        .status(orTrue(s.status()))
                        .usageCount(0)
                        .build());
            }
            documentTypeRepository.saveAll(entities);
            for (int i = 0; i < batch.size(); i++) {
                idMap.put(BackupKeys.DOC_TYPE, batch.get(i).id(), entities.get(i).getId());
            }
            flushAndDetach(entities);
        });
    }

    /** Pasada 1: inserta todo sin padre. Pasada 2: enlaza {@code parent} con los ids nuevos. */
    private void restoreCostCenters(BackupReader reader, IdMap idMap, String ent) {
        if (!reader.has(ENTRY_COST_CENTERS)) {
            return;
        }
        Map<Long, Long> oldParentByOldId = new HashMap<>();
        reader.forEachBatch(ENTRY_COST_CENTERS, CostCenterSnapshot.class, PAGE_SIZE, batch -> {
            List<CostCenterEntity> entities = new ArrayList<>(batch.size());
            for (CostCenterSnapshot s : batch) {
                entities.add(CostCenterEntity.builder()
                        .code(s.code())
                        .name(s.name())
                        .idEnterprise(ent)
                        .status(orTrue(s.status()))
                        .usageCount(0)
                        .build());
                if (s.parentId() != null) {
                    oldParentByOldId.put(s.id(), s.parentId());
                }
            }
            costCenterRepository.saveAll(entities);
            for (int i = 0; i < batch.size(); i++) {
                idMap.put(BackupKeys.COST_CENTER, batch.get(i).id(), entities.get(i).getId());
            }
            flushAndDetach(entities);
        });

        int pending = 0;
        List<CostCenterEntity> touched = new ArrayList<>();
        for (Map.Entry<Long, Long> link : oldParentByOldId.entrySet()) {
            Long childId = idMap.requireLong(BackupKeys.COST_CENTER, link.getKey());
            Long parentId = idMap.requireLong(BackupKeys.COST_CENTER, link.getValue());
            CostCenterEntity child = costCenterRepository.findById(childId).orElseThrow(
                    () -> new IllegalStateException("El centro de costo restaurado " + childId + " no existe"));
            child.setParent(costCenterRepository.getReferenceById(parentId));
            touched.add(child);
            if (++pending == PAGE_SIZE) {
                flushAndDetach(touched);
                touched.clear();
                pending = 0;
            }
        }
        flushAndDetach(touched);
    }

    private void restoreCalendar(BackupReader reader, String ent) {
        if (!reader.has(ENTRY_ACCOUNTING_CALENDAR)) {
            return;
        }
        reader.forEachBatch(ENTRY_ACCOUNTING_CALENDAR, AccountingCalendarSnapshot.class, PAGE_SIZE, batch -> {
            List<AccountingCalendarEntity> entities = new ArrayList<>(batch.size());
            for (AccountingCalendarSnapshot s : batch) {
                entities.add(AccountingCalendarEntity.builder().idEnterprise(ent).date(s.date()).build());
            }
            calendarRepository.saveAll(entities);
            flushAndDetach(entities);
        });
    }

    private void restoreTags(BackupReader reader, String ent) {
        if (!reader.has(ENTRY_TAGS)) {
            return;
        }
        reader.forEachBatch(ENTRY_TAGS, TagSnapshot.class, PAGE_SIZE, batch -> {
            List<TagEntity> entities = new ArrayList<>(batch.size());
            for (TagSnapshot s : batch) {
                TagEntity tag = new TagEntity();
                tag.setEnterpriseId(ent);
                tag.setTitle(s.title());
                tag.setDescription(s.description());
                entities.add(tag);
            }
            tagRepository.saveAll(entities);
            flushAndDetach(entities);
        });
    }

    private void flushAndDetach(List<?> entities) {
        entityManager.flush();
        for (Object entity : entities) {
            entityManager.detach(entity);
        }
    }

    private static boolean orTrue(Boolean value) {
        return value == null || value;
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore IN_PLACE)                                           */
    /* ------------------------------------------------------------------ */

    /** Borra los datos de configuration de la empresa: tipos antes que clases; centros de costo en un solo DELETE. */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        entityManager.createQuery("DELETE FROM DocumentTypeEntity t WHERE t.idEnterprise = :ent")
                .setParameter("ent", ent).executeUpdate();
        entityManager.createQuery("DELETE FROM DocumentClassEntity c WHERE c.idEnterprise = :ent")
                .setParameter("ent", ent).executeUpdate();
        // Se anula el padre antes: un DELETE masivo con auto-referencia podría chocar con la FK según el orden de filas.
        entityManager.createQuery("UPDATE CostCenterEntity c SET c.parent = NULL WHERE c.idEnterprise = :ent")
                .setParameter("ent", ent).executeUpdate();
        entityManager.createQuery("DELETE FROM CostCenterEntity c WHERE c.idEnterprise = :ent")
                .setParameter("ent", ent).executeUpdate();
        entityManager.createQuery("DELETE FROM AccountingCalendarEntity a WHERE a.idEnterprise = :ent")
                .setParameter("ent", ent).executeUpdate();
        entityManager.createQuery("DELETE FROM TagEntity t WHERE t.enterpriseId = :ent")
                .setParameter("ent", ent).executeUpdate();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put(ENTRY_DOCUMENT_CLASSES, documentClassRepository.countByIdEnterprise(ent));
        counts.put(ENTRY_DOCUMENT_TYPES, documentTypeRepository.countByIdEnterprise(ent));
        counts.put(ENTRY_COST_CENTERS, costCenterRepository.countByIdEnterprise(ent));
        counts.put(ENTRY_ACCOUNTING_CALENDAR, calendarRepository.countByIdEnterprise(ent));
        counts.put(ENTRY_TAGS, tagRepository.countByEnterpriseId(ent));
        return counts;
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();
        List<Long> types = documentTypeRepository.findIdsWithForeignDocumentClass(ent);
        if (!types.isEmpty()) {
            problems.add("tipos de documento con clase inexistente o de otra empresa (ids " + types + ")");
        }
        List<Long> centers = costCenterRepository.findIdsWithForeignParent(ent);
        if (!centers.isEmpty()) {
            problems.add("centros de costo con padre inexistente o de otra empresa (ids " + centers + ")");
        }
        return problems;
    }
}
