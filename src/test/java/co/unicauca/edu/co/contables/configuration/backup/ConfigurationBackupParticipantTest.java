package co.unicauca.edu.co.contables.configuration.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository.AccountingCalendarRepository;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.entity.TagEntity;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.repository.ITagRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;

/**
 * Ida y vuelta del participante {@code configuration}: tenant A -> ZIP v2 -> tenant B
 * con ids nuevos, FKs remapeadas, contadores en 0 y el origen intacto.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class ConfigurationBackupParticipantTest {

    private static final String TENANT_A = "cfg-tenant-A";
    private static final String TENANT_B = "cfg-tenant-B";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private ConfigurationBackupParticipant participant;
    @Autowired
    private ZipBackupFormat zipFormat;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DocumentClassRepository classRepo;
    @Autowired
    private DocumentTypeRepository typeRepo;
    @Autowired
    private CostCenterRepository costRepo;
    @Autowired
    private AccountingCalendarRepository calendarRepo;
    @Autowired
    private ITagRepository tagRepo;

    @TempDir
    Path tmp;

    @AfterEach
    void cleanTenant() {
        TenantContext.clear();
    }

    @Test
    void idaYVueltaConservaDatosRemapeaFksYReseteaContadores() {
        UUID entA = UUID.randomUUID();
        UUID entB = UUID.randomUUID();
        UUID noise = UUID.randomUUID();

        TenantContext.setTenantId(TENANT_A);
        long[] sourceIds = seed(entA, noise);
        Map<String, Long> countsA = inTx(() -> participant.count(entA));
        assertThat(countsA).containsEntry(ConfigurationBackupParticipant.ENTRY_DOCUMENT_CLASSES, 2L)
                .containsEntry(ConfigurationBackupParticipant.ENTRY_DOCUMENT_TYPES, 3L)
                .containsEntry(ConfigurationBackupParticipant.ENTRY_COST_CENTERS, 3L)
                .containsEntry(ConfigurationBackupParticipant.ENTRY_ACCOUNTING_CALENDAR, 2L)
                .containsEntry(ConfigurationBackupParticipant.ENTRY_TAGS, 1L);

        // ---- export ----
        Path zip = tmp.resolve("cfg.zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(entA, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "user-a"), List.of(), null);
        try (ZipBackupWriter writer = zipFormat.newWriter(zip, header)) {
            tx.executeWithoutResult(s -> participant.export(entA,
                    writer.module(participant.moduleKey(), participant.schemaVersion())));
            writer.finish();
        }

        // ---- restore como tenant B ----
        TenantContext.setTenantId(TENANT_B);
        InMemoryIdMap idMap = new InMemoryIdMap();
        RestoreContext ctx = RestoreContext
                .of(new RestoreActor(TENANT_B, "user-b"), RestoreMode.NEW_ENTERPRISE, entA, 2, LocalDateTime.now())
                .withTargetEnterpriseId(entB);
        try (ZipBackupReader reader = zipFormat.open(zip)) {
            reader.verifyAll();
            tx.executeWithoutResult(s -> participant.restore(reader.module(participant.moduleKey()), idMap, ctx));
            assertThat(reader.manifest().module(participant.moduleKey()).orElseThrow().entry(
                    ConfigurationBackupParticipant.ENTRY_COST_CENTERS).orElseThrow().count()).isEqualTo(3);
        }

        // ---- conteos y namespaces ----
        assertThat(inTx(() -> participant.count(entB))).isEqualTo(countsA);
        assertThat(inTx(() -> participant.verifyIntegrity(entB))).isEmpty();
        assertThat(idMap.size(BackupKeys.DOC_CLASS)).isEqualTo(2);
        assertThat(idMap.size(BackupKeys.DOC_TYPE)).isEqualTo(3);
        assertThat(idMap.size(BackupKeys.COST_CENTER)).isEqualTo(3);

        // ---- nada apunta a ids del origen; PKs nuevas ----
        Long newIdOfSourceClass = idMap.requireLong(BackupKeys.DOC_CLASS, sourceIds[0]);
        assertThat(newIdOfSourceClass).isNotEqualTo(sourceIds[0]);
        List<Map<String, Object>> typesB = jdbc.queryForList(
                "select t.id, t.name, t.usage_count, t.id_enterprise, t.tenant_id, t.document_class_id, "
                        + "c.id_enterprise as class_ent, c.tenant_id as class_tenant "
                        + "from types_of_documents t join classes_of_documents c on c.id = t.document_class_id "
                        + "where t.tenant_id = ?",
                TENANT_B);
        assertThat(typesB).hasSize(3).allSatisfy(row -> {
            assertThat(row.get("id_enterprise")).isEqualTo(entB.toString());
            assertThat(row.get("class_ent")).isEqualTo(entB.toString());
            assertThat(row.get("class_tenant")).isEqualTo(TENANT_B);
            assertThat(((Number) row.get("usage_count")).intValue()).isZero();
        });
        assertThat(typesB).extracting(r -> ((Number) r.get("document_class_id")).longValue())
                .doesNotContain(sourceIds[0], sourceIds[1]);

        // jerarquía de centros de costo conservada (por código) y contadores en 0
        List<Map<String, Object>> centersB = jdbc.queryForList(
                "select c.code, c.usage_count, c.id_enterprise, p.code as parent_code, p.id_enterprise as parent_ent "
                        + "from cost_centers c left join cost_centers p on p.id = c.parent_id "
                        + "where c.tenant_id = ? order by c.code",
                TENANT_B);
        assertThat(centersB).hasSize(3);
        assertThat(centersB.get(0).get("code")).isEqualTo("1");
        assertThat(centersB.get(0).get("parent_code")).isNull();
        assertThat(centersB.get(1).get("code")).isEqualTo("11");
        assertThat(centersB.get(1).get("parent_code")).isEqualTo("1");
        assertThat(centersB.get(1).get("parent_ent")).isEqualTo(entB.toString());
        assertThat(centersB.get(2).get("code")).isEqualTo("1101");
        assertThat(centersB.get(2).get("parent_code")).isEqualTo("11");
        assertThat(centersB).allSatisfy(r -> {
            assertThat(r.get("id_enterprise")).isEqualTo(entB.toString());
            assertThat(((Number) r.get("usage_count")).intValue()).isZero();
        });

        // tags y calendario
        List<TagEntity> tagsB = tagRepo.findByEnterpriseId(entB.toString());
        assertThat(tagsB).singleElement().satisfies(t -> {
            assertThat(t.getTitle()).isEqualTo("urgente");
            assertThat(t.getDescription()).isEqualTo("desc tag");
        });
        assertThat(calendarRepo.findAllByIdEnterpriseAndDateBetweenOrderByDateAsc(entB.toString(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .extracting(AccountingCalendarEntity::getDate)
                .containsExactly(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28));

        // ---- el origen sigue intacto y el ruido de otra empresa no viajó ----
        TenantContext.setTenantId(TENANT_A);
        assertThat(inTx(() -> participant.count(entA))).isEqualTo(countsA);
        assertThat(inTx(() -> participant.count(noise)))
                .containsEntry(ConfigurationBackupParticipant.ENTRY_DOCUMENT_CLASSES, 1L);
    }

    private <T> T inTx(java.util.function.Supplier<T> work) {
        return tx.execute(s -> work.get());
    }

    /** Siembra A (2 clases, 3 tipos, árbol de 3 centros, 2 fechas, 1 tag) y 1 clase de otra empresa. */
    private long[] seed(UUID ent, UUID noise) {
        String e = ent.toString();
        return tx.execute(s -> {
            DocumentClassEntity c1 = classRepo.save(
                    DocumentClassEntity.builder().name("Ingreso").idEnterprise(e).status(true).build());
            DocumentClassEntity c2 = classRepo.save(
                    DocumentClassEntity.builder().name("Egreso").idEnterprise(e).status(false).build());
            classRepo.save(DocumentClassEntity.builder().name("Ruido").idEnterprise(noise.toString()).build());
            typeRepo.save(type("FV", "Factura", c1, "SALES", e, 5));
            typeRepo.save(type("NC", "Nota credito", c1, "SALES", e, 0));
            typeRepo.save(type("CE", "Comprobante egreso", c2, "TREASURY", e, 2));

            CostCenterEntity root = costRepo.save(center("1", "Raiz", null, e, 7));
            CostCenterEntity mid = costRepo.save(center("11", "Medio", root, e, 3));
            costRepo.save(center("1101", "Hoja", mid, e, 1));

            calendarRepo.save(AccountingCalendarEntity.builder().idEnterprise(e)
                    .date(LocalDate.of(2026, 1, 31)).build());
            calendarRepo.save(AccountingCalendarEntity.builder().idEnterprise(e)
                    .date(LocalDate.of(2026, 2, 28)).build());

            TagEntity tag = new TagEntity();
            tag.setEnterpriseId(e);
            tag.setTitle("urgente");
            tag.setDescription("desc tag");
            tagRepo.save(tag);
            return new long[] { c1.getId(), c2.getId() };
        });
    }

    private static DocumentTypeEntity type(String prefix, String name, DocumentClassEntity cls, String module,
            String ent, int usage) {
        return DocumentTypeEntity.builder().prefix(prefix).name(name).documentClass(cls).module(module)
                .idEnterprise(ent).status(true).usageCount(usage).build();
    }

    private static CostCenterEntity center(String code, String name, CostCenterEntity parent, String ent,
            int usage) {
        return CostCenterEntity.builder().code(code).name(name).parent(parent).idEnterprise(ent).status(true)
                .usageCount(usage).build();
    }
}
