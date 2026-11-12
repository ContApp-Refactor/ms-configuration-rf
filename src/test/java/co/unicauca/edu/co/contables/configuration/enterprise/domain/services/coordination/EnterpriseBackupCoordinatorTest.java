package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.UnsupportedBackupModuleException;
import jakarta.persistence.EntityManager;

/**
 * Coordinador de la copia completa con participantes falsos: grafo validado al
 * arrancar, propagación de targetEnterpriseId, actor de la petición (no del
 * ZIP), verificación ANTES de la transacción y rollback por conteos/huérfanos.
 */
class EnterpriseBackupCoordinatorTest {

    private static final RestoreActor ACTOR = new RestoreActor("tenant-B", "user-B");

    @TempDir
    Path tmp;

    private ZipBackupFormat format;
    private EnterpriseRepository repo;
    private EntityManager em;
    private PlatformTransactionManager tm;
    private final UUID sourceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("tenant-A");
        format = new ZipBackupFormat(new ObjectMapper().registerModule(new JavaTimeModule()), 50_000_000L, 1000, 5);
        repo = mock(EnterpriseRepository.class);
        when(repo.findById(sourceId)).thenReturn(Optional.of(EnterpriseEntity.builder().id(sourceId).name("Emp")
                .build()));
        em = mock(EntityManager.class);
        tm = mock(PlatformTransactionManager.class);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private EnterpriseBackupCoordinatorImpl coordinator(BackupParticipant... participants) {
        return new EnterpriseBackupCoordinatorImpl(List.of(participants), format, repo, em, tm, "test",
                tmp.toString());
    }

    /** Participante configurable: enterprise crea la empresa; el resto escribe/lee N filas NDJSON. */
    static class FakeParticipant implements BackupParticipant {
        final String key;
        final int order;
        final Set<String> requires;
        final int schema;
        long rows = 3;
        Long countOverride;
        List<String> problems = List.of();
        RestoreContext seenCtx;
        UUID createdId;
        long rowsRead;
        final List<String> calls;

        FakeParticipant(String key, int order, Set<String> requires, List<String> calls) {
            this(key, order, requires, 1, calls);
        }

        FakeParticipant(String key, int order, Set<String> requires, int schema, List<String> calls) {
            this.key = key;
            this.order = order;
            this.requires = requires;
            this.schema = schema;
            this.calls = calls;
        }

        String path() {
            return key + "/rows.ndjson";
        }

        boolean isEnterprise() {
            return BackupKeys.MODULE_ENTERPRISE.equals(key);
        }

        @Override
        public String moduleKey() {
            return key;
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public int schemaVersion() {
            return schema;
        }

        @Override
        public Set<String> requires() {
            return requires;
        }

        @Override
        public void export(UUID enterpriseId, BackupWriter writer) {
            calls.add("export:" + key);
            if (isEnterprise()) {
                writer.json("enterprise/enterprise.json", Map.of("name", "Emp", "id", enterpriseId.toString()));
            } else {
                writer.writeNdjson(path(), LongStream.range(0, rows).mapToObj(i -> Map.of("id", i)).iterator());
            }
        }

        @Override
        public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
            calls.add("restore:" + key);
            seenCtx = ctx;
            if (isEnterprise()) {
                createdId = ctx.mode() == RestoreMode.IN_PLACE ? ctx.targetEnterpriseId() : UUID.randomUUID();
                idMap.put(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId(), createdId);
            } else {
                try (Stream<Map> s = reader.stream(path(), Map.class)) {
                    rowsRead = s.count();
                }
            }
        }

        @Override
        public void purge(UUID enterpriseId) {
            calls.add("purge:" + key);
        }

        @Override
        public Map<String, Long> count(UUID enterpriseId) {
            return isEnterprise() ? Map.of() : Map.of(path(), countOverride != null ? countOverride : rowsRead);
        }

        @Override
        public List<String> verifyIntegrity(UUID enterpriseId) {
            return problems;
        }
    }

    private FakeParticipant enterprise(List<String> calls) {
        return new FakeParticipant(BackupKeys.MODULE_ENTERPRISE, 0, Set.of(), calls);
    }

    private Path export(EnterpriseBackupCoordinatorImpl o) {
        Path zip = tmp.resolve("b-" + UUID.randomUUID() + ".zip");
        o.exportTo(sourceId, zip, "user-a", BackupProgressListener.NONE);
        // el export usa su propia transacción de solo lectura; los asserts miran solo la del restore
        org.mockito.Mockito.clearInvocations(tm, em);
        return zip;
    }

    /* ---------------- grafo ---------------- */

    @Test
    void ordenaPorOrderSinListaManual() {
        List<String> calls = new ArrayList<>();
        EnterpriseBackupCoordinatorImpl o = coordinator(
                new FakeParticipant("taxes", 40, Set.of("puc"), calls),
                enterprise(calls),
                new FakeParticipant("puc", 30, Set.of(), calls));
        assertThat(o.supportedModules()).extracting(BackupModuleInfo::key)
                .containsExactly("enterprise", "puc", "taxes");
    }

    @Test
    void grafoInvalidoFallaAlArrancar() {
        List<String> c = new ArrayList<>();
        assertThatThrownBy(() -> coordinator(enterprise(c), new FakeParticipant("taxes", 40, Set.of("puc"), c)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("requiere el módulo 'puc'");
        assertThatThrownBy(() -> coordinator(enterprise(c), new FakeParticipant("a", 10, Set.of("b"), c),
                new FakeParticipant("b", 20, Set.of("a"), c)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Ciclo");
        assertThatThrownBy(() -> coordinator(enterprise(c), new FakeParticipant("taxes", 20, Set.of("puc"), c),
                new FakeParticipant("puc", 30, Set.of(), c)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("order debe ser mayor");
        assertThatThrownBy(() -> coordinator(enterprise(c), new FakeParticipant("puc", 30, Set.of(), c),
                new FakeParticipant("puc", 31, Set.of(), c)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("repetido");
        assertThatThrownBy(() -> coordinator(new FakeParticipant("puc", 30, Set.of(), c)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Falta el participante 'enterprise'");
        assertThatThrownBy(() -> coordinator(enterprise(c), new FakeParticipant("puc", 0, Set.of(), c)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("después de 'enterprise'");
    }

    /* ---------------- round-trip ---------------- */

    @Test
    void roundTripPropagaLaEmpresaDestinoYUsaElActorDeLaPeticion() {
        List<String> calls = new ArrayList<>();
        FakeParticipant ent = enterprise(calls);
        FakeParticipant conf = new FakeParticipant("configuration", 10, Set.of("enterprise"), calls);
        EnterpriseBackupCoordinatorImpl o = coordinator(conf, ent);

        Path zip = export(o);
        RestoreResult result = o.restore(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR,
                BackupProgressListener.NONE);

        assertThat(calls).containsExactly("export:enterprise", "export:configuration", "restore:enterprise",
                "restore:configuration");
        assertThat(ent.seenCtx.targetEnterpriseId()).isNull();
        assertThat(ent.seenCtx.sourceEnterpriseId()).isEqualTo(sourceId);
        assertThat(conf.seenCtx.targetEnterpriseId()).isEqualTo(ent.createdId);
        assertThat(conf.seenCtx.tenantId()).isEqualTo("tenant-B");
        assertThat(conf.seenCtx.userId()).isEqualTo("user-B");
        assertThat(conf.seenCtx.mode()).isEqualTo(RestoreMode.NEW_ENTERPRISE);
        assertThat(conf.rowsRead).isEqualTo(3);
        assertThat(result.enterpriseId()).isEqualTo(ent.createdId);
        assertThat(result.restoredModules()).containsExactly("enterprise", "configuration");
        assertThat(result.counts()).containsEntry("configuration/rows.ndjson", 3L);
        verify(tm, times(1)).getTransaction(any());
        verify(tm).commit(any());
        verify(em, times(2)).flush();
    }

    @Test
    void elManifestDeclaraModulosConteosYExcluidos() {
        List<String> calls = new ArrayList<>();
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(calls),
                new FakeParticipant("configuration", 10, Set.of(), calls));
        ManifestV2 m = o.exportTo(sourceId, tmp.resolve("m.zip"), "user-a", BackupProgressListener.NONE);
        assertThat(m.version()).isEqualTo(2);
        assertThat(m.sourceEnterpriseId()).isEqualTo(sourceId);
        assertThat(m.sourceEnterpriseName()).isEqualTo("Emp");
        assertThat(m.createdBy().username()).isEqualTo("user-a");
        assertThat(m.createdBy().tenantHash()).startsWith("sha256:").doesNotContain("tenant-A");
        assertThat(m.excluded()).isEqualTo(BackupKeys.EXCLUDED);
        assertThat(m.module("configuration").orElseThrow().entries().get(0).count()).isEqualTo(3L);
    }

    @Test
    void exportarEmpresaDeOtroTenantFalla() {
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(new ArrayList<>()));
        UUID other = UUID.randomUUID();
        assertThatThrownBy(() -> o.exportTo(other, tmp.resolve("x.zip"), "u", null))
                .hasMessageContaining(other.toString());
        assertThat(Files.exists(tmp.resolve("x.zip"))).isFalse();
    }

    @Test
    void duplicarUsaModoDuplicateNombreCopiaYBorraElTemporal() throws Exception {
        List<String> calls = new ArrayList<>();
        FakeParticipant ent = enterprise(calls);
        EnterpriseBackupCoordinatorImpl o = coordinator(ent);

        RestoreResult r = o.duplicate(sourceId, null, ACTOR, "user-a", BackupProgressListener.NONE);

        assertThat(r.enterpriseId()).isEqualTo(ent.createdId);
        assertThat(ent.seenCtx.mode()).isEqualTo(RestoreMode.DUPLICATE);
        assertThat(ent.seenCtx.targetName()).isEqualTo("Emp (copia)");
        try (Stream<Path> left = Files.list(tmp.resolve("tmp"))) {
            assertThat(left).isEmpty();
        }
    }

    @Test
    void elProgresoSeInformaPorEtapaYModulo() {
        List<String> calls = new ArrayList<>();
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(calls),
                new FakeParticipant("configuration", 10, Set.of(), calls));
        List<String> events = new ArrayList<>();
        BackupProgressListener l = new BackupProgressListener() {
            @Override
            public void onStage(Stage stage, int total) {
                events.add(stage + ":" + total);
            }

            @Override
            public void onModuleCompleted(Stage stage, String key) {
                events.add(stage + ":" + key);
            }
        };
        o.duplicate(sourceId, "X", ACTOR, "u", l);
        assertThat(events).containsExactly("EXPORT:2", "EXPORT:enterprise", "EXPORT:configuration",
                "RESTORE:2", "RESTORE:enterprise", "RESTORE:configuration",
                "VERIFY:2", "VERIFY:enterprise", "VERIFY:configuration");
    }

    /* ---------------- validaciones previas (sin transacción) ---------------- */

    @Test
    void moduloDesconocidoFallaSalvoSkipUnknownModules() {
        List<String> calls = new ArrayList<>();
        Path zip = export(coordinator(enterprise(calls), new FakeParticipant("billing", 90, Set.of(), calls)));
        FakeParticipant ent = enterprise(calls);
        EnterpriseBackupCoordinatorImpl onlyEnterprise = coordinator(ent);

        assertThatThrownBy(() -> onlyEnterprise.prepare(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR))
                .isInstanceOf(UnsupportedBackupModuleException.class).hasMessageContaining("billing");
        verify(tm, never()).getTransaction(any());

        RestoreResult r = onlyEnterprise.restore(zip, RestoreOptions.newEnterprise(null, null, true), ACTOR, null);
        assertThat(r.skippedModules()).containsExactly("billing");
        assertThat(r.restoredModules()).containsExactly("enterprise");
    }

    @Test
    void schemaMasNuevoQueElParticipanteSeRechaza() {
        List<String> calls = new ArrayList<>();
        Path zip = export(coordinator(enterprise(calls), new FakeParticipant("puc", 30, Set.of(), 2, calls)));
        EnterpriseBackupCoordinatorImpl old = coordinator(enterprise(calls),
                new FakeParticipant("puc", 30, Set.of(), 1, calls));
        assertThatThrownBy(() -> old.prepare(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR))
                .hasMessageContaining("formato 2");
    }

    @Test
    void checksumAlteradoFallaAntesDeAbrirLaTransaccion() throws Exception {
        List<String> calls = new ArrayList<>();
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(calls),
                new FakeParticipant("configuration", 10, Set.of(), calls));
        Path zip = export(o);
        Path tampered = tamper(zip, "configuration/rows.ndjson");

        assertThatThrownBy(() -> o.prepare(tampered, RestoreOptions.newEnterprise(null, null, false), ACTOR))
                .isInstanceOf(BackupCorruptedException.class).hasMessageContaining("checksum");
        verify(tm, never()).getTransaction(any());
        assertThat(calls).doesNotContain("restore:enterprise");
    }

    @Test
    void inPlaceSinEmpresaDestinoEsInvalido() {
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(new ArrayList<>()));
        Path zip = export(o);
        assertThatThrownBy(() -> o.prepare(zip, new RestoreOptions(RestoreMode.IN_PLACE, null, null, false), ACTOR))
                .isInstanceOf(InvalidRestoreRequestException.class);
    }

    @Test
    void inPlaceSobreEmpresaDeOtroTenantEsNotFound() {
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(new ArrayList<>()));
        Path zip = export(o);
        assertThatThrownBy(() -> o.prepare(zip, RestoreOptions.inPlace(UUID.randomUUID(), null, null, false), ACTOR))
                .isInstanceOf(co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception
                        .EnterpriseNotFoundException.class);
    }

    @Test
    void inPlacePurgaEnOrdenInversoYRestauraSobreLaEmpresaExistente() {
        List<String> calls = new ArrayList<>();
        FakeParticipant ent = enterprise(calls);
        FakeParticipant conf = new FakeParticipant("configuration", 10, Set.of("enterprise"), calls);
        EnterpriseBackupCoordinatorImpl o = coordinator(conf, ent);
        Path zip = export(o);
        calls.clear();
        UUID existing = UUID.randomUUID();
        when(repo.existsById(existing)).thenReturn(true);

        RestoreResult result = o.restore(zip, RestoreOptions.inPlace(existing, null, null, false), ACTOR,
                BackupProgressListener.NONE);

        assertThat(calls).containsExactly("purge:configuration", "purge:enterprise", "restore:enterprise",
                "restore:configuration");
        assertThat(conf.seenCtx.targetEnterpriseId()).isEqualTo(existing);
        assertThat(conf.seenCtx.mode()).isEqualTo(RestoreMode.IN_PLACE);
        assertThat(result.enterpriseId()).isEqualTo(existing);
    }

    /* ---------------- verificación dentro de la transacción ---------------- */

    @Test
    void conteoDistintoHaceRollbackConBackupIntegrityException() {
        List<String> calls = new ArrayList<>();
        FakeParticipant conf = new FakeParticipant("configuration", 10, Set.of(), calls);
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(calls), conf);
        Path zip = export(o);
        conf.countOverride = 2L;

        assertThatThrownBy(() -> o.restore(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR, null))
                .isInstanceOf(BackupIntegrityException.class)
                .hasMessageContaining("configuration")
                .hasMessageContaining("3 registros en la copia y 2");
        verify(tm).rollback(any());
        verify(tm, never()).commit(any());
    }

    @Test
    void huerfanosHacenRollbackConElModulo() {
        List<String> calls = new ArrayList<>();
        FakeParticipant conf = new FakeParticipant("configuration", 10, Set.of(), calls);
        conf.problems = List.of("3 tipos de documento sin clase");
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(calls), conf);
        Path zip = export(o);

        assertThatThrownBy(() -> o.restore(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR, null))
                .isInstanceOf(BackupIntegrityException.class)
                .hasMessage("configuration: 3 tipos de documento sin clase");
        verify(tm).rollback(any());
    }

    @Test
    void fkSinEquivalenciaYErrorInesperadoSalenConElModulo() {
        List<String> calls = new ArrayList<>();
        FakeParticipant broken = new FakeParticipant("taxes", 40, Set.of(), calls) {
            @Override
            public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
                idMap.require(BackupKeys.ACCOUNT, 99L);
            }
        };
        EnterpriseBackupCoordinatorImpl o = coordinator(enterprise(calls), broken);
        Path zip = export(o);
        assertThatThrownBy(() -> o.restore(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR, null))
                .isInstanceOf(BackupIntegrityException.class).hasMessageStartingWith("taxes: ");

        FakeParticipant npe = new FakeParticipant("taxes", 40, Set.of(), calls) {
            @Override
            public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
                throw new NullPointerException("boom");
            }
        };
        EnterpriseBackupCoordinatorImpl o2 = coordinator(enterprise(calls), npe);
        assertThatThrownBy(() -> o2.restore(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR, null))
                .isInstanceOf(RestoreFailedException.class)
                .satisfies(e -> assertThat(((RestoreFailedException) e).getUserMessage()).contains("'taxes'"));
    }

    @Test
    void elParticipanteEnterpriseDebeRegistrarLaEmpresaCreada() {
        List<String> calls = new ArrayList<>();
        FakeParticipant lazy = new FakeParticipant(BackupKeys.MODULE_ENTERPRISE, 0, Set.of(), calls) {
            @Override
            public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
                // no registra ENTERPRISE
            }
        };
        EnterpriseBackupCoordinatorImpl o = coordinator(lazy);
        Path zip = export(o);
        assertThatThrownBy(() -> o.restore(zip, RestoreOptions.newEnterprise(null, null, false), ACTOR, null))
                .isInstanceOf(BackupIntegrityException.class).hasMessageContaining("no registró la empresa");
    }

    /** Copia el ZIP cambiando UN byte de la entrada (mismo tamaño: solo falla el SHA-256). */
    private Path tamper(Path zip, String entryName) throws Exception {
        Path out = tmp.resolve("tampered-" + UUID.randomUUID() + ".zip");
        try (ZipFile in = new ZipFile(zip.toFile());
                OutputStream fos = Files.newOutputStream(out);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            Enumeration<? extends ZipEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                byte[] data;
                try (InputStream is = in.getInputStream(e)) {
                    data = is.readAllBytes();
                }
                if (e.getName().equals(entryName)) {
                    String s = new String(data, StandardCharsets.UTF_8).replace("\"id\":1", "\"id\":7");
                    data = s.getBytes(StandardCharsets.UTF_8);
                }
                zos.putNextEntry(new ZipEntry(e.getName()));
                zos.write(data);
                zos.closeEntry();
            }
        }
        return out;
    }
}
