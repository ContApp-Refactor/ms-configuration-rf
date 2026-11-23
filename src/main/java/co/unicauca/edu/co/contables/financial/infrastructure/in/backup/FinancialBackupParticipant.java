package co.unicauca.edu.co.contables.financial.infrastructure.in.backup;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupJpa;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementAnnotationEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementHistoryEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementLogEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementTemplateEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code financial} (order 130): plantillas de estados
 * financieros y los estados ya generados (instantánea JSON), con sus
 * anotaciones, historial de entregas y bitácora.
 *
 * Estas tablas no tienen {@code @TenantId}: se filtran solo por {@code entId}
 * (empresa). Restore por {@link EntityManager}, sin servicios. PKs nuevas;
 * {@code reportId} (UUID único global y que viaja en URLs/entregas) se REGENERA.
 * La instantánea es un reporte histórico congelado y se copia tal cual (no se
 * recalcula ni se remapean los ids que pudiera contener). Los hijos
 * (anotaciones, historial, log) viajan anidados en su estado financiero.
 *
 * {@code pathLogotype} de la plantilla es una ruta/URL de archivo: se copia como
 * referencia (no se duplica el binario).
 */
@Component
@RequiredArgsConstructor
public class FinancialBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_TEMPLATES = "financial/templates.ndjson";
    public static final String ENTRY_STATEMENTS = "financial/statements.ndjson";

    private static final String WHERE = "e.entId = :ent";

    private final EntityManager em;

    public record TemplateRow(Long id, String name, String pathLogotype, String alignment, String font,
            Integer fontSize, String mainColor, Boolean isDefault, OffsetDateTime createdAt) {
    }

    public record AnnotationRow(String text, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    public record HistoryRow(String state, String deliveryWay, OffsetDateTime createdAt) {
    }

    public record LogRow(String eventType, String message, String icon, String color, OffsetDateTime createdAt) {
    }

    public record StatementRow(Long id, EFinancialStatementType type, OffsetDateTime createdAt,
            String reportSnapshot, List<AnnotationRow> annotations, List<HistoryRow> history, List<LogRow> logs) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_FINANCIAL;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_FINANCIAL;
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
        Map<String, Object> p = Map.of("ent", enterpriseId.toString());

        writer.writeNdjson(ENTRY_TEMPLATES, BackupJpa.keysetOf(em, FinancialStatementTemplateEntity.class, "id",
                WHERE, p, FinancialStatementTemplateEntity::getId,
                t -> new TemplateRow(t.getId(), t.getName(), t.getPathLogotype(), t.getAlignment(), t.getFont(),
                        t.getFontSize(), t.getMainColor(), t.getIsDefault(), t.getCreatedAt())));

        writer.writeNdjson(ENTRY_STATEMENTS, BackupJpa.keyset(em, FinancialStatementEntity.class, "id", WHERE, p,
                FinancialStatementEntity::getId, this::statementRows));
    }

    private List<StatementRow> statementRows(List<FinancialStatementEntity> page) {
        List<Long> ids = page.stream().map(FinancialStatementEntity::getId).toList();
        Map<Long, List<AnnotationRow>> annotations = em.createQuery(
                "select a from FinancialStatementAnnotationEntity a where a.financialStatement.id in :ids order by a.id",
                FinancialStatementAnnotationEntity.class).setParameter("ids", ids).getResultList().stream()
                .collect(Collectors.groupingBy(a -> a.getFinancialStatement().getId(), LinkedHashMap::new,
                        Collectors.mapping(a -> new AnnotationRow(a.getText(), a.getCreatedAt(), a.getUpdatedAt()),
                                Collectors.toList())));
        Map<Long, List<HistoryRow>> history = em.createQuery(
                "select h from FinancialStatementHistoryEntity h where h.financialStatement.id in :ids order by h.id",
                FinancialStatementHistoryEntity.class).setParameter("ids", ids).getResultList().stream()
                .collect(Collectors.groupingBy(h -> h.getFinancialStatement().getId(), LinkedHashMap::new,
                        Collectors.mapping(h -> new HistoryRow(h.getState(), h.getDeliveryWay(), h.getCreatedAt()),
                                Collectors.toList())));
        Map<Long, List<LogRow>> logs = em.createQuery(
                "select l from FinancialStatementLogEntity l where l.financialStatement.id in :ids order by l.id",
                FinancialStatementLogEntity.class).setParameter("ids", ids).getResultList().stream()
                .collect(Collectors.groupingBy(l -> l.getFinancialStatement().getId(), LinkedHashMap::new,
                        Collectors.mapping(l -> new LogRow(l.getEventType(), l.getMessage(), l.getIcon(),
                                l.getColor(), l.getCreatedAt()), Collectors.toList())));
        return page.stream()
                .map(s -> new StatementRow(s.getId(), s.getType(), s.getCreatedAt(), s.getReportSnapshot(),
                        annotations.getOrDefault(s.getId(), List.of()), history.getOrDefault(s.getId(), List.of()),
                        logs.getOrDefault(s.getId(), List.of())))
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.targetEnterpriseIdAsString();

        if (reader.has(ENTRY_TEMPLATES)) {
            reader.forEachBatch(ENTRY_TEMPLATES, TemplateRow.class, BackupJpa.BATCH, rows -> {
                for (TemplateRow r : rows) {
                    em.persist(FinancialStatementTemplateEntity.builder()
                            .entId(ent)
                            .name(r.name())
                            .pathLogotype(r.pathLogotype())
                            .alignment(r.alignment())
                            .font(r.font())
                            .fontSize(r.fontSize())
                            .mainColor(r.mainColor())
                            .isDefault(r.isDefault() == null ? Boolean.FALSE : r.isDefault())
                            .createdAt(r.createdAt() == null ? OffsetDateTime.now() : r.createdAt())
                            .build());
                }
                BackupJpa.flushAndClear(em);
            });
        }

        if (reader.has(ENTRY_STATEMENTS)) {
            reader.forEachBatch(ENTRY_STATEMENTS, StatementRow.class, BackupJpa.BATCH, rows -> {
                for (StatementRow r : rows) {
                    FinancialStatementEntity s = FinancialStatementEntity.builder()
                            .reportId(UUID.randomUUID())
                            .type(r.type())
                            .entId(ent)
                            .createdAt(r.createdAt() == null ? OffsetDateTime.now() : r.createdAt())
                            .reportSnapshot(r.reportSnapshot())
                            .build();
                    em.persist(s);
                    if (r.annotations() != null) {
                        for (AnnotationRow a : r.annotations()) {
                            em.persist(FinancialStatementAnnotationEntity.builder()
                                    .financialStatement(s)
                                    .text(a.text())
                                    .createdAt(a.createdAt() == null ? OffsetDateTime.now() : a.createdAt())
                                    .updatedAt(a.updatedAt() == null ? OffsetDateTime.now() : a.updatedAt())
                                    .build());
                        }
                    }
                    if (r.history() != null) {
                        for (HistoryRow h : r.history()) {
                            em.persist(FinancialStatementHistoryEntity.builder()
                                    .financialStatement(s)
                                    .state(h.state())
                                    .deliveryWay(h.deliveryWay())
                                    .createdAt(h.createdAt() == null ? OffsetDateTime.now() : h.createdAt())
                                    .build());
                        }
                    }
                    if (r.logs() != null) {
                        for (LogRow l : r.logs()) {
                            em.persist(FinancialStatementLogEntity.builder()
                                    .financialStatement(s)
                                    .eventType(l.eventType())
                                    .message(l.message())
                                    .icon(l.icon())
                                    .color(l.color())
                                    .createdAt(l.createdAt() == null ? OffsetDateTime.now() : l.createdAt())
                                    .build());
                        }
                    }
                }
                BackupJpa.flushAndClear(em);
            });
        }
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore IN_PLACE)                                           */
    /* ------------------------------------------------------------------ */

    /** Borra hijos (anotaciones, historial, bitácora), luego estados y plantillas de la empresa. */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        em.flush();
        for (String child : List.of("FinancialStatementAnnotationEntity", "FinancialStatementHistoryEntity",
                "FinancialStatementLogEntity")) {
            em.createQuery("delete from " + child + " c where c.financialStatement.id in "
                    + "(select s.id from FinancialStatementEntity s where s.entId = :ent)")
                    .setParameter("ent", ent).executeUpdate();
        }
        em.createQuery("delete from FinancialStatementEntity e where " + WHERE)
                .setParameter("ent", ent).executeUpdate();
        em.createQuery("delete from FinancialStatementTemplateEntity e where " + WHERE)
                .setParameter("ent", ent).executeUpdate();
        em.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        Map<String, Object> p = Map.of("ent", enterpriseId.toString());
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put(ENTRY_TEMPLATES, BackupJpa.count(em, FinancialStatementTemplateEntity.class, WHERE, p));
        counts.put(ENTRY_STATEMENTS, BackupJpa.count(em, FinancialStatementEntity.class, WHERE, p));
        return counts;
    }

    /** Sin FKs a otros módulos (los hijos viajan anidados y se crean con su padre): nada que verificar. */
    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        return List.of();
    }
}
