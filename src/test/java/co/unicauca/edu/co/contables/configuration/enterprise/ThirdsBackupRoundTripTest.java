package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.InMemoryIdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupWriter;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PersonClassification;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.backup.ThirdsBackupParticipant;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdTypeRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdsAndTypesRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.TypeIdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.notifications.infrastructure.input.backup.ThirdReplicaBackupParticipant;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;

/**
 * Ida y vuelta de los participantes {@code thirds} y {@code notifications.thirdReplicas}: datos en el tenant A
 * (la empresa a copiar + otra empresa del mismo tenant que NO debe viajar), export con {@link ZipBackupFormat} y
 * restore como tenant B con {@link InMemoryIdMap}.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = "/thirds-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThirdsBackupRoundTripTest {

    private static final String TENANT_A = "tenant-A";
    private static final String TENANT_B = "tenant-B";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private ThirdsBackupParticipant thirds;
    @Autowired
    private ThirdReplicaBackupParticipant replicas;
    @Autowired
    private TypeIdRepository typeIdRepository;
    @Autowired
    private ThirdTypeRepository thirdTypeRepository;
    @Autowired
    private ThirdRepository thirdRepository;
    @Autowired
    private ThirdsAndTypesRepository linkRepository;
    @Autowired
    private IThirdReplicaRepository replicaRepository;
    @Autowired
    private ZipBackupFormat format;
    @Autowired
    private TransactionTemplate tx;

    @TempDir
    Path tmp;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    private record Seed(UUID source, Set<Long> typeIds, Set<Long> thirdTypeIds, Set<Long> thirdIds) {
    }

    private TypeIdEntity typeId(String ent, String code, PersonClassification c) {
        return typeIdRepository.save(TypeIdEntity.builder().tiId(code).tiName("Tipo " + code).tientId(ent)
                .classification(c).status(true).build());
    }

    private ThirdTypeEntity thirdType(String ent, String name) {
        return thirdTypeRepository.save(ThirdTypeEntity.builder().ttName(name).ttentId(ent).status(true).build());
    }

    private ThirdEntity third(String ent, TypeIdEntity type, long idNumber, String name) {
        return thirdRepository.save(ThirdEntity.builder().entId(ent).typeId(type).personType(ePersonType.Natural)
                .names(name).lastNames("Perez").idNumber(idNumber).verificationNumber(7L).state(true)
                .country("CO").province("Cauca").city("Popayan").address("Calle 1").phoneNumber("300")
                .email(name.toLowerCase() + "@x.co").usageCount(5).build());
    }

    private void link(ThirdEntity t, ThirdTypeEntity tt) {
        linkRepository.save(ThirdsAndTypesEntity.builder().thId(t.getThId()).ttId(tt.getTtId()).build());
    }

    private void replica(String ent, Long thirdId, String name) {
        ThirdReplicaEntity r = new ThirdReplicaEntity();
        r.setThirdId(thirdId);
        r.setEnterpriseId(ent);
        r.setFullName(name);
        r.setEmail(name + "@x.co");
        r.setActive(true);
        replicaRepository.save(r);
    }

    /**
     * Tenant A: empresa origen con 2 tipos de id, 2 tipos de tercero, 3 terceros y 3 relaciones; otra empresa
     * con 1 tercero; réplicas de dos terceros, una huérfana (tercero borrado) y una de la otra empresa.
     */
    private Seed seedTenantA() {
        UUID source = UUID.randomUUID();
        String ent = source.toString();
        String other = UUID.randomUUID().toString();
        return tx.execute(s -> {
            TypeIdEntity cc = typeId(ent, "CC", PersonClassification.NATURAL_PERSON);
            TypeIdEntity nit = typeId(ent, "NIT", PersonClassification.LEGAL_ENTITY);
            ThirdTypeEntity cliente = thirdType(ent, "Cliente");
            ThirdTypeEntity proveedor = thirdType(ent, "Proveedor");
            ThirdEntity ana = third(ent, cc, 1001L, "Ana");
            ThirdEntity bar = third(ent, nit, 2002L, "Bar");
            ThirdEntity cle = third(ent, cc, 3003L, "Cle");
            link(ana, cliente);
            link(ana, proveedor);
            link(bar, proveedor);

            TypeIdEntity otherCc = typeId(other, "CC", PersonClassification.NATURAL_PERSON);
            ThirdTypeEntity otherTt = thirdType(other, "Cliente");
            ThirdEntity otherTh = third(other, otherCc, 9009L, "Otro");
            link(otherTh, otherTt);

            replica(ent, ana.getThId(), "ana");
            replica(ent, bar.getThId(), "bar");
            replica(ent, 987654321L, "borrado");
            replica(other, otherTh.getThId(), "otro");
            return new Seed(source, Set.of(cc.getId(), nit.getId()),
                    Set.of(cliente.getTtId(), proveedor.getTtId()),
                    Set.of(ana.getThId(), bar.getThId(), cle.getThId()));
        });
    }

    private Path exportTenantA(UUID source) {
        Path target = tmp.resolve("thirds-" + source + ".zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(source, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "u-a"), List.of(), LocalDateTime.now());
        try (ZipBackupWriter zip = format.newWriter(target, header)) {
            for (BackupParticipant p : List.of(thirds, replicas)) {
                tx.executeWithoutResult(s -> p.export(source, zip.module(p.moduleKey(), p.schemaVersion())));
            }
            zip.finish();
        }
        return target;
    }

    private RestoreContext ctxB(UUID source, UUID target) {
        return new RestoreContext(TENANT_B, "u-b", source, target, 2, LocalDateTime.now(),
                RestoreMode.NEW_ENTERPRISE, null, null, false);
    }

    @Test
    void tercerosTiposRelacionesYReplicasViajanConIdsNuevosYSoloDeLaEmpresa() {
        TenantContext.setTenantId(TENANT_A);
        Seed seed = seedTenantA();
        UUID source = seed.source();
        Path zipPath = exportTenantA(source);

        TenantContext.setTenantId(TENANT_B);
        UUID target = UUID.randomUUID();
        String targetStr = target.toString();
        InMemoryIdMap idMap = new InMemoryIdMap();
        try (ZipBackupReader reader = format.open(zipPath)) {
            reader.verifyAll();
            ManifestV2 manifest = reader.manifest();
            assertThat(manifest.module(BackupKeys.MODULE_THIRDS).orElseThrow()
                    .entry(ThirdsBackupParticipant.ENTRY_THIRDS).orElseThrow().count()).isEqualTo(3);
            assertThat(manifest.module(BackupKeys.MODULE_THIRDS).orElseThrow()
                    .entry(ThirdsBackupParticipant.ENTRY_LINKS).orElseThrow().count()).isEqualTo(3);
            // la réplica del tercero borrado y la de la otra empresa no viajan
            assertThat(manifest.module(BackupKeys.MODULE_THIRD_REPLICAS).orElseThrow()
                    .entry(ThirdReplicaBackupParticipant.ENTRY_REPLICAS).orElseThrow().count()).isEqualTo(2);
            for (BackupParticipant p : List.of(thirds, replicas)) {
                tx.executeWithoutResult(s -> p.restore(reader.module(p.moduleKey()), idMap, ctxB(source, target)));
            }
        }

        // conteos == origen (solo la empresa) e integridad sin huérfanos
        assertThat(thirds.count(target)).isEqualTo(Map.of(
                ThirdsBackupParticipant.ENTRY_TYPE_IDS, 2L,
                ThirdsBackupParticipant.ENTRY_THIRD_TYPES, 2L,
                ThirdsBackupParticipant.ENTRY_THIRDS, 3L,
                ThirdsBackupParticipant.ENTRY_LINKS, 3L));
        assertThat(replicas.count(target)).isEqualTo(Map.of(ThirdReplicaBackupParticipant.ENTRY_REPLICAS, 2L));
        assertThat(thirds.verifyIntegrity(target)).isEmpty();
        assertThat(replicas.verifyIntegrity(target)).isEmpty();
        assertThat(idMap.size(BackupKeys.TYPE_ID)).isEqualTo(2);
        assertThat(idMap.size(BackupKeys.THIRD_TYPE)).isEqualTo(2);
        assertThat(idMap.size(BackupKeys.THIRD)).isEqualTo(3);

        // ids nuevos: ninguno coincide con los del origen
        for (Long old : seed.typeIds()) {
            assertThat(idMap.<Long>require(BackupKeys.TYPE_ID, old)).isNotIn(seed.typeIds());
        }
        for (Long old : seed.thirdTypeIds()) {
            assertThat(idMap.<Long>require(BackupKeys.THIRD_TYPE, old)).isNotIn(seed.thirdTypeIds());
        }
        Set<Long> newThirdIds = new HashSet<>();
        for (Long old : seed.thirdIds()) {
            Long nuevo = idMap.require(BackupKeys.THIRD, old);
            assertThat(nuevo).isNotIn(seed.thirdIds());
            newThirdIds.add(nuevo);
        }

        tx.executeWithoutResult(s -> {
            Map<Long, TypeIdEntity> typesById = new HashMap<>();
            typeIdRepository.findAllByTientId(targetStr).forEach(t -> typesById.put(t.getId(), t));
            assertThat(typesById.values()).extracting(TypeIdEntity::getTiId).containsExactlyInAnyOrder("CC", "NIT");
            assertThat(typesById.values()).allSatisfy(t -> assertThat(t.getTenantId()).isEqualTo(TENANT_B));

            Map<Long, ThirdTypeEntity> thirdTypesById = new HashMap<>();
            thirdTypeRepository.findAllByTtentId(targetStr).forEach(t -> thirdTypesById.put(t.getTtId(), t));
            assertThat(thirdTypesById.values()).extracting(ThirdTypeEntity::getTtName)
                    .containsExactlyInAnyOrder("Cliente", "Proveedor");

            Map<Long, ThirdEntity> byIdNumber = new HashMap<>();
            thirdRepository.findAllForExport(targetStr, PageRequest.of(0, 10)).forEach(t -> {
                byIdNumber.put(t.getIdNumber(), t);
                assertThat(t.getEntId()).isEqualTo(targetStr);
                assertThat(t.getTenantId()).isEqualTo(TENANT_B);
                assertThat(t.getUsageCount()).isZero();
                assertThat(newThirdIds).contains(t.getThId());
                // la FK apunta al tipo de id NUEVO, de la empresa destino
                assertThat(typesById).containsKey(t.getTypeId().getId());
                assertThat(t.getTypeId().getTientId()).isEqualTo(targetStr);
            });
            assertThat(byIdNumber.keySet()).containsExactlyInAnyOrder(1001L, 2002L, 3003L);
            assertThat(byIdNumber.get(1001L).getTypeId().getTiId()).isEqualTo("CC");
            assertThat(byIdNumber.get(2002L).getTypeId().getTiId()).isEqualTo("NIT");
            assertThat(byIdNumber.get(2002L).getTypeId().getClassification())
                    .isEqualTo(PersonClassification.LEGAL_ENTITY);
            assertThat(byIdNumber.get(1001L).getEmail()).isEqualTo("ana@x.co");
            assertThat(byIdNumber.get(1001L).getVerificationNumber()).isEqualTo(7L);

            // relaciones: Ana -> {Cliente, Proveedor}, Bar -> {Proveedor}, Cle -> {}
            Map<Long, Set<String>> typesByThird = new HashMap<>();
            linkRepository.findBackupLinksByEntId(targetStr, PageRequest.of(0, 10)).forEach(l -> {
                assertThat(newThirdIds).contains(l.getThId());
                assertThat(thirdTypesById).containsKey(l.getTtId());
                typesByThird.computeIfAbsent(l.getThId(), k -> new HashSet<>())
                        .add(thirdTypesById.get(l.getTtId()).getTtName());
            });
            assertThat(typesByThird.get(byIdNumber.get(1001L).getThId())).containsExactlyInAnyOrder("Cliente",
                    "Proveedor");
            assertThat(typesByThird.get(byIdNumber.get(2002L).getThId())).containsExactly("Proveedor");
            assertThat(typesByThird).doesNotContainKey(byIdNumber.get(3003L).getThId());

            // réplicas: terceros NUEVOS y empresa destino
            List<ThirdReplicaEntity> restored = replicaRepository
                    .findBackupReplicas(targetStr, PageRequest.of(0, 10)).getContent();
            assertThat(restored).extracting(ThirdReplicaEntity::getFullName).containsExactly("ana", "bar");
            assertThat(restored).allSatisfy(r -> {
                assertThat(r.getEnterpriseId()).isEqualTo(targetStr);
                assertThat(r.getTenantId()).isEqualTo(TENANT_B);
                assertThat(newThirdIds).contains(r.getThirdId());
            });
        });

        // el origen quedó intacto
        TenantContext.setTenantId(TENANT_A);
        assertThat(thirds.count(source).get(ThirdsBackupParticipant.ENTRY_THIRDS)).isEqualTo(3L);
        assertThat(thirdRepository.findAllById(seed.thirdIds())).hasSize(3);
    }

}
