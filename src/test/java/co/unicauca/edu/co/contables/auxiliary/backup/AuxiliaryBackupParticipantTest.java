package co.unicauca.edu.co.contables.auxiliary.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.backup.AuxiliaryBackupParticipant;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookLogEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookTemplateEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportEntity;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.InMemoryIdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupWriter;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import jakarta.persistence.EntityManager;

/**
 * Ida y vuelta del participante {@code auxiliary}: empresa A -> ZIP v2 -> empresa B con ids y
 * publicId nuevos, referencias blandas de criterios remapeadas (o null), reportes programados
 * PAUSADOS y el origen intacto.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class AuxiliaryBackupParticipantTest {

    private static final String TENANT_A = "aux-tenant-A";
    private static final String TENANT_B = "aux-tenant-B";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private AuxiliaryBackupParticipant participant;
    @Autowired
    private ZipBackupFormat zipFormat;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private EntityManager em;
    @Autowired
    private JdbcTemplate jdbc;

    @TempDir
    Path tmp;

    @AfterEach
    void cleanTenant() {
        TenantContext.clear();
    }

    @Test
    void idaYVueltaRemapeaReferenciasPausaJobsYRegeneraPublicIds() {
        UUID entA = UUID.randomUUID();
        UUID entB = UUID.randomUUID();
        UUID noise = UUID.randomUUID();

        // H2 (modo PostgreSQL) no tiene TINYINT: Hibernate no puede crear esta tabla (EState es ordinal).
        jdbc.execute("create table if not exists auxiliary_book_history (state smallint not null,"
                + " auxiliary_book_id bigint not null unique, id bigint not null primary key,"
                + " public_id varchar(255) not null unique, delivery_way varchar(20))");

        TenantContext.setTenantId(TENANT_A);
        List<String> sourcePublicIds = seed(entA, noise);
        Map<String, Long> countsA = inTx(() -> participant.count(entA));
        assertThat(countsA).containsEntry(AuxiliaryBackupParticipant.ENTRY_BOOKS, 2L)
                .containsEntry(AuxiliaryBackupParticipant.ENTRY_SCHEDULED_REPORTS, 1L);

        // ---- export ----
        Path zip = tmp.resolve("aux.zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(entA, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "user-a"), List.of(), null);
        try (ZipBackupWriter writer = zipFormat.newWriter(zip, header)) {
            tx.executeWithoutResult(s -> participant.export(entA,
                    writer.module(participant.moduleKey(), participant.schemaVersion())));
            writer.finish();
        }

        // ---- restore como empresa B (tercero 7 -> 70, centro de costo 3 -> 30; el tercero 999 no existe) ----
        TenantContext.setTenantId(TENANT_B);
        InMemoryIdMap idMap = new InMemoryIdMap();
        idMap.put(BackupKeys.THIRD, 7L, 70L);
        idMap.put(BackupKeys.COST_CENTER, 3L, 30L);
        RestoreContext ctx = RestoreContext
                .of(new RestoreActor(TENANT_B, "user-b"), RestoreMode.NEW_ENTERPRISE, entA, 2, LocalDateTime.now())
                .withTargetEnterpriseId(entB);
        try (ZipBackupReader reader = zipFormat.open(zip)) {
            reader.verifyAll();
            tx.executeWithoutResult(s -> participant.restore(reader.module(participant.moduleKey()), idMap, ctx));
        }

        // ---- conteos ----
        assertThat(inTx(() -> participant.count(entB))).isEqualTo(countsA);
        assertThat(inTx(() -> participant.verifyIntegrity(entB))).isEmpty();

        // ---- libros: publicId nuevo, hijos recreados, criterios remapeados ----
        List<Map<String, Object>> booksB = jdbc.queryForList(
                "select b.id, b.public_id, b.type, b.ent_id, c.third_party_id, c.cost_center_id, c.from_range,"
                        + " t.name as template_name"
                        + " from auxiliary_book b join auxiliary_book_criteria c on c.id = b.ab_criteria_id"
                        + " left join auxiliary_book_template t on t.id = b.ab_template_id"
                        + " where b.ent_id = ? order by b.id", entB.toString());
        assertThat(booksB).hasSize(2);
        assertThat(booksB).extracting(r -> r.get("public_id").toString()).doesNotContainAnyElementsOf(sourcePublicIds);
        Map<String, Object> first = booksB.get(0);
        assertThat(first.get("third_party_id")).isEqualTo("70");
        assertThat(first.get("cost_center_id")).isEqualTo("30");
        assertThat(((Number) first.get("from_range")).longValue()).isEqualTo(1105L);
        assertThat(first.get("template_name")).isEqualTo("Plantilla A");
        Map<String, Object> second = booksB.get(1);
        assertThat(second.get("third_party_id")).as("tercero inexistente en la copia: no se conserva un id ajeno").isNull();
        assertThat(second.get("cost_center_id")).isNull();

        Long firstId = ((Number) first.get("id")).longValue();
        assertThat(jdbc.queryForObject("select count(*) from auxiliary_book_log where auxiliary_book_id = ?",
                Long.class, firstId)).isEqualTo(2L);
        assertThat(jdbc.queryForObject("select count(*) from auxiliary_book_history where auxiliary_book_id = ?",
                Long.class, firstId)).isEqualTo(1L);

        // ---- reporte programado: PAUSADO, dueño = usuario que restaura, publicId nuevo ----
        List<Map<String, Object>> jobsB = jdbc.queryForList(
                "select j.public_id, j.status, j.owner_sub, j.email_to, c.third_party_id"
                        + " from scheduled_report j join auxiliary_book_criteria c on c.id = j.criteria_id"
                        + " where j.ent_id = ?", entB.toString());
        assertThat(jobsB).singleElement().satisfies(j -> {
            assertThat(j.get("status")).isEqualTo("PAUSED");
            assertThat(j.get("owner_sub")).isEqualTo("user-b");
            assertThat(j.get("email_to")).isEqualTo("contador@example.com");
            assertThat(j.get("third_party_id")).isEqualTo("70");
            assertThat(sourcePublicIds).doesNotContain(j.get("public_id").toString());
        });

        // ---- el origen sigue intacto (job ACTIVO) y el ruido no viajó ----
        assertThat(inTx(() -> participant.count(entA))).isEqualTo(countsA);
        assertThat(jdbc.queryForObject("select status from scheduled_report where ent_id = ?", String.class,
                entA.toString())).isEqualTo("ACTIVE");
        assertThat(inTx(() -> participant.count(noise)))
                .containsEntry(AuxiliaryBackupParticipant.ENTRY_BOOKS, 1L);
    }

    private <T> T inTx(java.util.function.Supplier<T> work) {
        return tx.execute(s -> work.get());
    }

    /** Siembra A (2 libros, 1 job ACTIVO) y 1 libro de otra empresa. Devuelve los publicId del origen. */
    private List<String> seed(UUID ent, UUID noise) {
        String e = ent.toString();
        return tx.execute(s -> {
            List<String> publicIds = new ArrayList<>();

            AuxiliaryBookEntity first = book(e, "1105", "7", "3");
            AuxiliaryBookTemplateEntity template = new AuxiliaryBookTemplateEntity();
            template.setName("Plantilla A");
            template.setPathLogotype("/logos/a.png");
            template.setAligment(EAlignment.CENTER);
            template.setFont("Arial");
            template.setMainColor("#123456");
            first.setTemplate(template);
            List<AuxiliaryBookLogEntity> logs = new ArrayList<>();
            for (ETypeEvent type : List.of(ETypeEvent.REGISTERED, ETypeEvent.SUCCESS_GENERATION)) {
                AuxiliaryBookLogEntity log = new AuxiliaryBookLogEntity();
                log.setPublicId(UUID.randomUUID().toString());
                log.setETypeEvent(type);
                log.setMessage("evento " + type);
                log.setAuxiliaryBook(first);
                logs.add(log);
            }
            first.setLog(logs);
            AuxiliaryBookHistoryEntity history = new AuxiliaryBookHistoryEntity();
            history.setPublicId(UUID.randomUUID().toString());
            history.setState(EState.GENERATED);
            history.setDeliveryWay(EDeliveryWay.DOWNLOAD);
            history.setAuxiliaryBook(first);
            first.setHistory(history);
            em.persist(first);

            AuxiliaryBookEntity second = book(e, "1110", "999", null);
            em.persist(second);
            em.persist(book(noise.toString(), "1105", "7", "3"));
            publicIds.add(first.getPublicId());
            publicIds.add(second.getPublicId());

            ScheduledReportEntity job = new ScheduledReportEntity();
            job.setPublicId(UUID.randomUUID().toString());
            job.setBookType(EAuxiliaryBookType.THIRD_PARTY);
            job.setCriteria(criteria("1105", "7", "3"));
            job.setFrequency(EFrequency.MONTHLY);
            job.setStartAt(Instant.parse("2026-01-01T00:00:00Z"));
            job.setNextRunAt(Instant.parse("2026-12-01T00:00:00Z"));
            job.setStatus(EJobStatus.ACTIVE);
            job.setEntId(e);
            job.setUserId(1L);
            job.setOwnerSub("user-a");
            job.setCreatedBy("user-a");
            job.setDeliveryWay(EDeliveryWay.EMAIL);
            job.setEmailTo("contador@example.com");
            job.setEmailSubject("Libro mensual");
            em.persist(job);
            publicIds.add(job.getPublicId());
            return publicIds;
        });
    }

    private static AuxiliaryBookEntity book(String ent, String range, String thirdId, String costCenterId) {
        AuxiliaryBookEntity b = new AuxiliaryBookEntity();
        b.setPublicId(UUID.randomUUID().toString());
        b.setType(EAuxiliaryBookType.THIRD_PARTY);
        b.setEntId(ent);
        b.setUserId(1L);
        b.setFormat(EAuxiliaryBookFormat.PDF);
        b.setCriteria(criteria(range, thirdId, costCenterId));
        return b;
    }

    private static AuxiliaryBookCriteriaEntity criteria(String range, String thirdId, String costCenterId) {
        AuxiliaryBookCriteriaEntity c = new AuxiliaryBookCriteriaEntity();
        c.setCriteriaType(ECriteriaType.ACCOUNT);
        c.setFromRange(Long.valueOf(range));
        c.setToRange(Long.valueOf(range));
        c.setThirdPartyId(thirdId);
        c.setCostCenterId(costCenterId);
        c.setStartDate(LocalDate.of(2026, 1, 1));
        c.setEndDate(LocalDate.of(2026, 12, 31));
        return c;
    }
}
