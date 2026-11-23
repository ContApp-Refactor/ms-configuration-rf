package co.unicauca.edu.co.contables.financial.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import co.unicauca.edu.co.contables.financial.infrastructure.in.backup.FinancialBackupParticipant;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementAnnotationEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementHistoryEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementLogEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementTemplateEntity;
import jakarta.persistence.EntityManager;

/**
 * Ida y vuelta del participante {@code financial}: empresa A -> ZIP v2 -> empresa B con ids
 * nuevos, {@code reportId} regenerado, hijos anidados recreados y el origen intacto.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class FinancialBackupParticipantTest {

    private static final String TENANT_A = "fin-tenant-A";
    private static final String TENANT_B = "fin-tenant-B";
    private static final String SNAPSHOT = "{\"assets\":1500,\"liabilities\":900}";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private FinancialBackupParticipant participant;
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
    void idaYVueltaRegeneraReportIdYRecreaHijos() {
        UUID entA = UUID.randomUUID();
        UUID entB = UUID.randomUUID();
        UUID noise = UUID.randomUUID();

        TenantContext.setTenantId(TENANT_A);
        UUID sourceReportId = seed(entA, noise);
        Map<String, Long> countsA = inTx(() -> participant.count(entA));
        assertThat(countsA).containsEntry(FinancialBackupParticipant.ENTRY_TEMPLATES, 2L)
                .containsEntry(FinancialBackupParticipant.ENTRY_STATEMENTS, 2L);

        // ---- export ----
        Path zip = tmp.resolve("fin.zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(entA, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "user-a"), List.of(), null);
        try (ZipBackupWriter writer = zipFormat.newWriter(zip, header)) {
            tx.executeWithoutResult(s -> participant.export(entA,
                    writer.module(participant.moduleKey(), participant.schemaVersion())));
            writer.finish();
        }

        // ---- restore como empresa B ----
        TenantContext.setTenantId(TENANT_B);
        RestoreContext ctx = RestoreContext
                .of(new RestoreActor(TENANT_B, "user-b"), RestoreMode.NEW_ENTERPRISE, entA, 2, LocalDateTime.now())
                .withTargetEnterpriseId(entB);
        try (ZipBackupReader reader = zipFormat.open(zip)) {
            reader.verifyAll();
            tx.executeWithoutResult(
                    s -> participant.restore(reader.module(participant.moduleKey()), new InMemoryIdMap(), ctx));
        }

        // ---- conteos ----
        assertThat(inTx(() -> participant.count(entB))).isEqualTo(countsA);
        assertThat(inTx(() -> participant.verifyIntegrity(entB))).isEmpty();

        // ---- el reporte nuevo: reportId distinto, snapshot intacto, hijos recreados ----
        List<Map<String, Object>> statementsB = jdbc.queryForList(
                "select id, report_id, type from financial_statement where ent_id = ? order by id", entB.toString());
        assertThat(statementsB).hasSize(2);
        assertThat(statementsB).extracting(r -> r.get("report_id").toString())
                .doesNotContain(sourceReportId.toString());
        assertThat(inTx(() -> em.createQuery("select s.reportSnapshot from FinancialStatementEntity s"
                + " where s.entId = :e", String.class).setParameter("e", entB.toString()).getResultList()))
                .containsOnly(SNAPSHOT);

        Long withChildren = ((Number) statementsB.get(0).get("id")).longValue();
        assertThat(jdbc.queryForObject(
                "select count(*) from financial_statement_annotation where financial_statement_id = ?",
                Long.class, withChildren)).isEqualTo(2L);
        assertThat(jdbc.queryForObject(
                "select count(*) from financial_statement_history where financial_statement_id = ?",
                Long.class, withChildren)).isEqualTo(1L);
        assertThat(jdbc.queryForObject(
                "select count(*) from financial_statement_log where financial_statement_id = ?",
                Long.class, withChildren)).isEqualTo(3L);
        Long second = ((Number) statementsB.get(1).get("id")).longValue();
        assertThat(jdbc.queryForObject(
                "select count(*) from financial_statement_annotation where financial_statement_id = ?",
                Long.class, second)).isZero();

        // ---- plantillas: una sola por defecto, empresa B ----
        assertThat(jdbc.queryForList("select name, is_default, ent_id from financial_statement_template"
                + " where ent_id = ? order by name", entB.toString()))
                .extracting(r -> r.get("name") + ":" + r.get("is_default"))
                .containsExactly("Clasica:true", "Moderna:false");

        // ---- el origen sigue intacto; el ruido no viajó ----
        assertThat(inTx(() -> participant.count(entA))).isEqualTo(countsA);
        assertThat(inTx(() -> participant.count(noise)))
                .containsEntry(FinancialBackupParticipant.ENTRY_STATEMENTS, 1L);
    }

    private <T> T inTx(java.util.function.Supplier<T> work) {
        return tx.execute(s -> work.get());
    }

    /** Siembra A: 2 plantillas y 2 reportes (el 1.o con 2 anotaciones, 1 historial y 3 logs) + 1 reporte de otra empresa. */
    private UUID seed(UUID ent, UUID noise) {
        String e = ent.toString();
        OffsetDateTime now = OffsetDateTime.now();
        return tx.execute(s -> {
            em.persist(template(e, "Clasica", true, now));
            em.persist(template(e, "Moderna", false, now));

            UUID reportId = UUID.randomUUID();
            FinancialStatementEntity first = statement(reportId, e, EFinancialStatementType.INCOME_STATEMENT, now);
            em.persist(first);
            em.persist(statement(UUID.randomUUID(), e,
                    EFinancialStatementType.STATEMENT_FINANCIAL_POSITION, now));
            em.persist(statement(UUID.randomUUID(), noise.toString(),
                    EFinancialStatementType.INCOME_STATEMENT, now));

            for (String text : List.of("nota 1", "nota 2")) {
                em.persist(FinancialStatementAnnotationEntity.builder().financialStatement(first).text(text)
                        .createdAt(now).updatedAt(now).build());
            }
            em.persist(FinancialStatementHistoryEntity.builder().financialStatement(first).state("SENT")
                    .deliveryWay("EMAIL").createdAt(now).build());
            for (String type : List.of("CREATED", "EXPORTED", "SENT")) {
                em.persist(FinancialStatementLogEntity.builder().financialStatement(first).eventType(type)
                        .message("evento " + type).icon("check").color("green").createdAt(now).build());
            }
            return reportId;
        });
    }

    private static FinancialStatementTemplateEntity template(String ent, String name, boolean isDefault,
            OffsetDateTime at) {
        return FinancialStatementTemplateEntity.builder().entId(ent).name(name).pathLogotype("/logos/" + name)
                .alignment("LEFT").font("Arial").fontSize(12).mainColor("#112233").isDefault(isDefault)
                .createdAt(at).build();
    }

    private static FinancialStatementEntity statement(UUID reportId, String ent, EFinancialStatementType type,
            OffsetDateTime at) {
        return FinancialStatementEntity.builder().reportId(reportId).type(type).entId(ent).createdAt(at)
                .reportSnapshot(SNAPSHOT).build();
    }
}
