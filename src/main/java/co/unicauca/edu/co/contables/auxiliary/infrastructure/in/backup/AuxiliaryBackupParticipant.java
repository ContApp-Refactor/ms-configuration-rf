package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.backup;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookLogEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookTemplateEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportEntity;
import co.unicauca.edu.co.contables.commons.backup.BackupJpa;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code auxiliary} (order 131): libros auxiliares ya
 * generados (con plantilla, criterios, bitácora e historial) y reportes
 * programados de la empresa.
 *
 * Estas tablas no tienen {@code @TenantId}: se filtran solo por {@code entId}.
 * Restore por {@link EntityManager}, sin servicios. PKs nuevas y todos los
 * {@code publicId} (UNIQUE global, expuestos por la API) se REGENERAN.
 *
 * Los criterios guardan, como texto, el id de un tercero y de un centro de costo:
 * se remapean con {@code THIRD}/{@code COST_CENTER}; si el original ya no existe
 * (tercero borrado) la referencia queda en null en vez de apuntar a un id ajeno
 * (los ids son globales entre empresas). Los rangos ({@code fromRange/toRange}) son
 * códigos PUC y se copian.
 *
 * SEGURIDAD OPERATIVA: los reportes programados se restauran PAUSADOS
 * ({@code PAUSED}). Un job {@code ACTIVE} copiado volvería a enviar el correo
 * ({@code emailTo}) en paralelo al de la empresa original. {@code ownerSub} pasa a
 * ser el usuario que restaura. Las ejecuciones ({@code SCHEDULED_REPORT_EXECUTION}) y
 * las notificaciones de usuario ({@code NOTIFICATION}) NO viajan: son registro
 * operativo, sin columna de empresa.
 */
@Component
@RequiredArgsConstructor
public class AuxiliaryBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_BOOKS = "auxiliary/books.ndjson";
    public static final String ENTRY_SCHEDULED_REPORTS = "auxiliary/scheduled_reports.ndjson";

    private static final String WHERE = "e.entId = :ent";

    private final EntityManager em;

    public record CriteriaRow(ECriteriaType criteriaType, Long fromRange, Long toRange, String thirdPartyId,
            String costCenterId, LocalDate startDate, LocalDate endDate) {
    }

    public record TemplateRow(String name, String pathLogotype, EAlignment aligment, String font,
            String mainColor) {
    }

    public record BookLogRow(ETypeEvent eventType, String message) {
    }

    public record BookHistoryRow(EState state, EDeliveryWay deliveryWay) {
    }

    public record BookRow(Long id, EAuxiliaryBookType type, Long userId, EAuxiliaryBookFormat format,
            TemplateRow template, CriteriaRow criteria, List<BookLogRow> logs, BookHistoryRow history) {
    }

    public record ScheduledReportRow(Long id, EAuxiliaryBookType bookType, CriteriaRow criteria,
            EFrequency frequency, Instant startAt, Instant endAt, Instant nextRunAt, EJobStatus status, Long userId,
            String createdBy, EDeliveryWay deliveryWay, String emailTo, String emailSubject, String emailBody,
            String templateName, String templatePathLogotype, String templateAlignment, String templateFont,
            Integer templateFontSize, String templateMainColor) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_AUXILIARY;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_AUXILIARY;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_THIRDS, BackupKeys.MODULE_CONFIGURATION);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        Map<String, Object> p = Map.of("ent", enterpriseId.toString());

        writer.writeNdjson(ENTRY_BOOKS, BackupJpa.keysetOf(em, AuxiliaryBookEntity.class, "id", WHERE, p,
                AuxiliaryBookEntity::getId, this::toRow));

        writer.writeNdjson(ENTRY_SCHEDULED_REPORTS, BackupJpa.keysetOf(em, ScheduledReportEntity.class, "id", WHERE,
                p, ScheduledReportEntity::getId,
                j -> new ScheduledReportRow(j.getId(), j.getBookType(), toRow(j.getCriteria()), j.getFrequency(),
                        j.getStartAt(), j.getEndAt(), j.getNextRunAt(), j.getStatus(), j.getUserId(),
                        j.getCreatedBy(), j.getDeliveryWay(), j.getEmailTo(), j.getEmailSubject(), j.getEmailBody(),
                        j.getTemplateName(), j.getTemplatePathLogotype(), j.getTemplateAlignment(),
                        j.getTemplateFont(), j.getTemplateFontSize(), j.getTemplateMainColor())));
    }

    private BookRow toRow(AuxiliaryBookEntity b) {
        AuxiliaryBookTemplateEntity t = b.getTemplate();
        AuxiliaryBookHistoryEntity h = b.getHistory();
        List<BookLogRow> logs = b.getLog() == null ? List.of()
                : b.getLog().stream().map(l -> new BookLogRow(l.getETypeEvent(), l.getMessage())).toList();
        return new BookRow(b.getId(), b.getType(), b.getUserId(), b.getFormat(),
                t == null ? null : new TemplateRow(t.getName(), t.getPathLogotype(), t.getAligment(), t.getFont(),
                        t.getMainColor()),
                toRow(b.getCriteria()), logs, h == null ? null : new BookHistoryRow(h.getState(), h.getDeliveryWay()));
    }

    private static CriteriaRow toRow(AuxiliaryBookCriteriaEntity c) {
        return c == null ? null
                : new CriteriaRow(c.getCriteriaType(), c.getFromRange(), c.getToRange(), c.getThirdPartyId(),
                        c.getCostCenterId(), c.getStartDate(), c.getEndDate());
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.targetEnterpriseIdAsString();

        if (reader.has(ENTRY_BOOKS)) {
            reader.forEachBatch(ENTRY_BOOKS, BookRow.class, BackupJpa.BATCH, rows -> {
                for (BookRow r : rows) {
                    AuxiliaryBookEntity b = new AuxiliaryBookEntity();
                    b.setPublicId(UUID.randomUUID().toString());
                    b.setType(r.type());
                    b.setEntId(ent);
                    b.setUserId(r.userId());
                    b.setFormat(r.format());
                    if (r.template() != null) {
                        AuxiliaryBookTemplateEntity t = new AuxiliaryBookTemplateEntity();
                        t.setName(r.template().name());
                        t.setPathLogotype(r.template().pathLogotype());
                        t.setAligment(r.template().aligment());
                        t.setFont(r.template().font());
                        t.setMainColor(r.template().mainColor());
                        b.setTemplate(t);
                    }
                    b.setCriteria(toEntity(r.criteria(), idMap));
                    List<AuxiliaryBookLogEntity> logs = new ArrayList<>();
                    if (r.logs() != null) {
                        for (BookLogRow l : r.logs()) {
                            AuxiliaryBookLogEntity le = new AuxiliaryBookLogEntity();
                            le.setPublicId(UUID.randomUUID().toString());
                            le.setETypeEvent(l.eventType());
                            le.setMessage(l.message());
                            le.setAuxiliaryBook(b);
                            logs.add(le);
                        }
                    }
                    b.setLog(logs);
                    if (r.history() != null) {
                        AuxiliaryBookHistoryEntity h = new AuxiliaryBookHistoryEntity();
                        h.setPublicId(UUID.randomUUID().toString());
                        h.setState(r.history().state());
                        h.setDeliveryWay(r.history().deliveryWay());
                        h.setAuxiliaryBook(b);
                        b.setHistory(h);
                    }
                    em.persist(b);
                }
                BackupJpa.flushAndClear(em);
            });
        }

        if (reader.has(ENTRY_SCHEDULED_REPORTS)) {
            reader.forEachBatch(ENTRY_SCHEDULED_REPORTS, ScheduledReportRow.class, BackupJpa.BATCH, rows -> {
                for (ScheduledReportRow r : rows) {
                    ScheduledReportEntity j = new ScheduledReportEntity();
                    j.setPublicId(UUID.randomUUID().toString());
                    j.setBookType(r.bookType());
                    j.setCriteria(toEntity(r.criteria(), idMap));
                    j.setFrequency(r.frequency());
                    j.setStartAt(r.startAt());
                    j.setEndAt(r.endAt());
                    j.setNextRunAt(r.nextRunAt());
                    // PAUSADO: un job ACTIVE copiado enviaría el correo en paralelo al original.
                    j.setStatus(r.status() == EJobStatus.CANCELLED ? EJobStatus.CANCELLED : EJobStatus.PAUSED);
                    j.setEntId(ent);
                    j.setUserId(r.userId());
                    j.setOwnerSub(ctx.userId());
                    j.setCreatedBy(r.createdBy());
                    j.setDeliveryWay(r.deliveryWay());
                    j.setEmailTo(r.emailTo());
                    j.setEmailSubject(r.emailSubject());
                    j.setEmailBody(r.emailBody());
                    j.setTemplateName(r.templateName());
                    j.setTemplatePathLogotype(r.templatePathLogotype());
                    j.setTemplateAlignment(r.templateAlignment());
                    j.setTemplateFont(r.templateFont());
                    j.setTemplateFontSize(r.templateFontSize());
                    j.setTemplateMainColor(r.templateMainColor());
                    em.persist(j);
                }
                BackupJpa.flushAndClear(em);
            });
        }
    }

    private static AuxiliaryBookCriteriaEntity toEntity(CriteriaRow c, IdMap idMap) {
        if (c == null) {
            return null;
        }
        AuxiliaryBookCriteriaEntity e = new AuxiliaryBookCriteriaEntity();
        e.setCriteriaType(c.criteriaType());
        e.setFromRange(c.fromRange());
        e.setToRange(c.toRange());
        e.setThirdPartyId(softRef(idMap, BackupKeys.THIRD, c.thirdPartyId()));
        e.setCostCenterId(softRef(idMap, BackupKeys.COST_CENTER, c.costCenterId()));
        e.setStartDate(c.startDate());
        e.setEndDate(c.endDate());
        return e;
    }

    /** Referencia en texto a un id de otra tabla: remapeada, o null si el original ya no existe. */
    private static String softRef(IdMap idMap, String namespace, String oldId) {
        if (oldId == null || oldId.isBlank()) {
            return null;
        }
        try {
            return idMap.<Long>find(namespace, Long.valueOf(oldId.trim())).map(String::valueOf).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore IN_PLACE)                                           */
    /* ------------------------------------------------------------------ */

    /**
     * Borra bitácora e historial, libros, reportes programados y luego las plantillas y criterios que eran
     * suyos (esas filas son las referenciadas por FK, por eso van después de sus dueños).
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        em.flush();
        List<Long> templateIds = em.createQuery("select b.template.id from AuxiliaryBookEntity b "
                + "where b.entId = :ent and b.template is not null", Long.class).setParameter("ent", ent)
                .getResultList();
        List<Long> criteriaIds = new ArrayList<>(em.createQuery("select b.criteria.id from AuxiliaryBookEntity b "
                + "where b.entId = :ent and b.criteria is not null", Long.class).setParameter("ent", ent)
                .getResultList());
        criteriaIds.addAll(em.createQuery("select j.criteria.id from ScheduledReportEntity j "
                + "where j.entId = :ent and j.criteria is not null", Long.class).setParameter("ent", ent)
                .getResultList());

        String ofBooks = " x where x.auxiliaryBook.id in (select b.id from AuxiliaryBookEntity b where b.entId = :ent)";
        em.createQuery("delete from AuxiliaryBookLogEntity" + ofBooks).setParameter("ent", ent).executeUpdate();
        em.createQuery("delete from AuxiliaryBookHistoryEntity" + ofBooks).setParameter("ent", ent).executeUpdate();
        em.createQuery("delete from AuxiliaryBookEntity e where " + WHERE).setParameter("ent", ent).executeUpdate();
        em.createQuery("delete from ScheduledReportEntity e where " + WHERE).setParameter("ent", ent)
                .executeUpdate();

        deleteByIds("AuxiliaryBookTemplateEntity", templateIds);
        deleteByIds("AuxiliaryBookCriteriaEntity", criteriaIds);
        em.clear();
    }

    private void deleteByIds(String entity, List<Long> ids) {
        for (int i = 0; i < ids.size(); i += 1000) {
            em.createQuery("delete from " + entity + " x where x.id in :ids")
                    .setParameter("ids", ids.subList(i, Math.min(i + 1000, ids.size()))).executeUpdate();
        }
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        Map<String, Object> p = Map.of("ent", enterpriseId.toString());
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put(ENTRY_BOOKS, BackupJpa.count(em, AuxiliaryBookEntity.class, WHERE, p));
        counts.put(ENTRY_SCHEDULED_REPORTS, BackupJpa.count(em, ScheduledReportEntity.class, WHERE, p));
        return counts;
    }

    /**
     * Sin FKs duras a otros módulos: plantilla, criterios, bitácora e historial se crean con su
     * libro, y las referencias de los criterios son blandas (remapeadas o null).
     */
    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        return List.of();
    }
}
