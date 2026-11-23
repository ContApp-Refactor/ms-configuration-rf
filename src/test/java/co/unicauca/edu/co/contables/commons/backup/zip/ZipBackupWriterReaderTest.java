package co.unicauca.edu.co.contables.commons.backup.zip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.unicauca.edu.co.contables.commons.backup.BackupChecksumMismatchException;
import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupSizeExceededException;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.NdjsonEntryWriter;
import co.unicauca.edu.co.contables.commons.backup.UnsupportedBackupVersionException;

/**
 * Pruebas del formato ZIP v2 (escritor en streaming + lector validado) y de la
 * compatibilidad con el ZIP v1 (solo {@code enterprise.json}).
 */
class ZipBackupWriterReaderTest {

    record TaxRow(Long id, String code, BigDecimal rate, LocalDate validFrom, Long salesAccountId) {
    }

    record EnterpriseRow(UUID id, String name, LocalDateTime createdAt) {
    }

    private static final long MAX = 10L * 1024 * 1024;
    private static final UUID SOURCE = UUID.fromString("0b6f7c1e-1111-4222-8333-944445555666");

    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    Path tmp;

    private ZipBackupReader.Limits limits() {
        return ZipBackupReader.Limits.defaults(MAX);
    }

    private static List<TaxRow> taxes(int n) {
        return IntStream.rangeClosed(1, n)
                .mapToObj(i -> new TaxRow((long) i, "IVA" + i, new BigDecimal("0.19"), LocalDate.of(2026, 1, i % 28 + 1),
                        i % 2 == 0 ? null : 1000L + i))
                .toList();
    }

    private Path writeSample(String name) {
        Path target = tmp.resolve(name);
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(SOURCE, "Panadería X", "test@sha",
                ManifestV2.CreatedBy.of("tenant-A", "jperez"), List.of("audit"), null);
        try (ZipBackupWriter zip = ZipBackupWriter.create(target, mapper, MAX, header)) {
            BackupWriter ent = zip.module("enterprise", 1);
            ent.json("enterprise/enterprise.json",
                    new EnterpriseRow(SOURCE, "Panadería X", LocalDateTime.of(2026, 11, 11, 10, 15)));
            ent.asset("assets/logo", "image/png", new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 }));

            BackupWriter tx = zip.module("taxes", 1);
            assertThat(tx.writeNdjson("taxes/taxes.ndjson", taxes(1200).stream())).isEqualTo(1200);
            try (NdjsonEntryWriter empty = tx.ndjson("taxes/empty.ndjson")) {
                assertThat(empty.count()).isZero();
            }
            ManifestV2 manifest = zip.finish();
            assertThat(manifest.version()).isEqualTo(2);
        }
        return target;
    }

    @Test
    void roundTripV2ConConteosChecksumsYManifestAlFinal() throws IOException {
        Path zipPath = writeSample("ok.zip");

        assertThat(zipPath).exists();
        assertThat(tmp.resolve("ok.zip.part")).doesNotExist();
        try (ZipFile zf = new ZipFile(zipPath.toFile())) {
            List<String> names = Collections.list(zf.entries()).stream().map(ZipEntry::getName).toList();
            assertThat(names).last().isEqualTo("manifest.json");
            String manifestJson = new String(zf.getInputStream(zf.getEntry("manifest.json")).readAllBytes(),
                    StandardCharsets.UTF_8);
            assertThat(manifestJson).contains("\"exportedAt\" : \"").doesNotContain("tenant-A");
        }

        try (ZipBackupReader reader = ZipBackupReader.open(zipPath, mapper, limits())) {
            assertThat(reader.formatVersion()).isEqualTo(2);
            ManifestV2 m = reader.manifest();
            assertThat(m.format()).isEqualTo(ManifestV2.FORMAT);
            assertThat(m.sourceEnterpriseId()).isEqualTo(SOURCE);
            assertThat(m.sourceEnterpriseName()).isEqualTo("Panadería X");
            assertThat(m.exportedAt()).isNotNull();
            assertThat(m.createdBy().tenantHash()).startsWith("sha256:");
            assertThat(m.excluded()).containsExactly("audit");
            assertThat(reader.moduleKeys()).containsExactly("enterprise", "taxes");

            ManifestV2.EntryInfo taxesInfo = m.module("taxes").orElseThrow().entry("taxes/taxes.ndjson").orElseThrow();
            assertThat(taxesInfo.count()).isEqualTo(1200);
            assertThat(taxesInfo.sha256()).hasSize(64);
            assertThat(m.assets()).singleElement().satisfies(a -> {
                assertThat(a.contentType()).isEqualTo("image/png");
                assertThat(a.sizeBytes()).isEqualTo(5);
            });

            reader.verifyAll();

            BackupReader tx = reader.module("taxes");
            assertThat(tx.schemaVersion()).isEqualTo(1);
            assertThat(tx.formatVersion()).isEqualTo(2);
            assertThat(tx.has("taxes/taxes.ndjson")).isTrue();
            assertThat(tx.has("taxes/otra.ndjson")).isFalse();
            assertThat(tx.declaredCount("taxes/taxes.ndjson")).isEqualTo(1200);
            assertThat(tx.readAll("taxes/taxes.ndjson", TaxRow.class)).isEqualTo(taxes(1200));
            assertThat(tx.readAll("taxes/empty.ndjson", TaxRow.class)).isEmpty();

            List<Integer> batchSizes = new ArrayList<>();
            tx.forEachBatch("taxes/taxes.ndjson", TaxRow.class, 500, b -> batchSizes.add(b.size()));
            assertThat(batchSizes).containsExactly(500, 500, 200);

            BackupReader ent = reader.module("enterprise");
            EnterpriseRow row = ent.readJson("enterprise/enterprise.json", EnterpriseRow.class);
            assertThat(row.id()).isEqualTo(SOURCE);
            assertThat(row.createdAt()).isEqualTo(LocalDateTime.of(2026, 11, 11, 10, 15));
            assertThat(ent.hasAsset("assets/logo")).isTrue();
            assertThat(ent.assetContentType("assets/logo")).isEqualTo("image/png");
            try (InputStream logo = ent.openAsset("assets/logo")) {
                assertThat(logo.readAllBytes()).containsExactly(1, 2, 3, 4, 5);
            }
        }
    }

    @Test
    void streamCerradoAMitadIgualVerificaElChecksum() throws IOException {
        Path zipPath = writeSample("partial.zip");
        Path tampered = rewrite(zipPath, "tampered.zip", Map.of("taxes/taxes.ndjson", flipLastDigit()), null);
        try (ZipBackupReader reader = ZipBackupReader.open(tampered, mapper, limits());
                Stream<TaxRow> s = reader.module("taxes").stream("taxes/taxes.ndjson", TaxRow.class)) {
            assertThat(s.limit(3).toList()).hasSize(3);
            assertThatThrownBy(s::close).isInstanceOf(BackupChecksumMismatchException.class);
        }
    }

    @Test
    void checksumAlteradoSeRechazaAlLeerYEnVerifyAll() throws IOException {
        Path zipPath = writeSample("base.zip");
        Path tampered = rewrite(zipPath, "tampered.zip", Map.of("taxes/taxes.ndjson", flipLastDigit()), null);

        try (ZipBackupReader reader = ZipBackupReader.open(tampered, mapper, limits())) {
            assertThatThrownBy(reader::verifyAll)
                    .isInstanceOf(BackupChecksumMismatchException.class)
                    .hasMessageContaining("taxes/taxes.ndjson");
            assertThatThrownBy(() -> reader.module("taxes").readAll("taxes/taxes.ndjson", TaxRow.class))
                    .isInstanceOf(BackupChecksumMismatchException.class);
        }
    }

    @Test
    void conteoDistintoAlManifestEsCorrupto() throws IOException {
        Path zipPath = writeSample("base.zip");
        Path tampered = rewrite(zipPath, "count.zip",
                Map.of("manifest.json", editManifest(m -> {
                    entryNode(m, "taxes/taxes.ndjson").put("count", 1199);
                    return m;
                })), null);
        try (ZipBackupReader reader = ZipBackupReader.open(tampered, mapper, limits())) {
            assertThatThrownBy(() -> reader.module("taxes").readAll("taxes/taxes.ndjson", TaxRow.class))
                    .isInstanceOf(BackupCorruptedException.class)
                    .hasMessageContaining("1200 registros");
        }
    }

    @Test
    void entradaNoDeclaradaSeRechaza() throws IOException {
        Path zipPath = writeSample("base.zip");
        Path tampered = rewrite(zipPath, "extra.zip", Map.of(), Map.of("billing/invoices.ndjson", "{}\n"));
        assertThatThrownBy(() -> ZipBackupReader.open(tampered, mapper, limits()))
                .isInstanceOf(BackupCorruptedException.class)
                .hasMessageContaining("no está declarada");
    }

    @Test
    void zipSlipSeRechaza() throws IOException {
        Path zipPath = writeSample("base.zip");
        for (String evil : List.of("../evil.txt", "/etc/passwd", "a/../../b", "C:/win.ini", "a\\b.txt")) {
            Path tampered = rewrite(zipPath, "slip.zip", Map.of(), Map.of(evil, "x"));
            assertThatThrownBy(() -> ZipBackupReader.open(tampered, mapper, limits()))
                    .as(evil)
                    .isInstanceOf(BackupCorruptedException.class)
                    .hasMessageContaining("nombre no permitido");
        }
    }

    @Test
    void versionOFormatoNoSoportadoSeRechaza() throws IOException {
        Path zipPath = writeSample("base.zip");
        Path v3 = rewrite(zipPath, "v3.zip", Map.of("manifest.json", editManifest(m -> m.put("version", 3))), null);
        assertThatThrownBy(() -> ZipBackupReader.open(v3, mapper, limits()))
                .isInstanceOf(UnsupportedBackupVersionException.class);

        Path otherFormat = rewrite(zipPath, "fmt.zip",
                Map.of("manifest.json", editManifest(m -> m.put("format", "otra-cosa"))), null);
        assertThatThrownBy(() -> ZipBackupReader.open(otherFormat, mapper, limits()))
                .isInstanceOf(UnsupportedBackupVersionException.class);
    }

    @Test
    void tamanoDescomprimidoDistintoAlDeclaradoSeRechaza() throws IOException {
        Path zipPath = writeSample("base.zip");
        Path tampered = rewrite(zipPath, "size.zip",
                Map.of("manifest.json", editManifest(m -> {
                    entryNode(m, "taxes/taxes.ndjson").put("sizeBytes", 10);
                    return m;
                })), null);
        assertThatThrownBy(() -> ZipBackupReader.open(tampered, mapper, limits()))
                .isInstanceOf(BackupCorruptedException.class)
                .hasMessageContaining("tamaño declarado");
    }

    @Test
    void limitesAntiZipBomb() throws IOException {
        Path zipPath = writeSample("base.zip");
        long zipSize = Files.size(zipPath);

        // descomprimido declarado por encima del límite
        ZipBackupReader.Limits tinyUncompressed = new ZipBackupReader.Limits(MAX, 1000, 1000, 1_000_000);
        assertThatThrownBy(() -> ZipBackupReader.open(zipPath, mapper, tinyUncompressed))
                .isInstanceOf(BackupSizeExceededException.class);

        // demasiadas entradas
        ZipBackupReader.Limits fewEntries = new ZipBackupReader.Limits(MAX, MAX * 5, 2, 1_000_000);
        assertThatThrownBy(() -> ZipBackupReader.open(zipPath, mapper, fewEntries))
                .isInstanceOf(BackupCorruptedException.class)
                .hasMessageContaining("entradas");

        // archivo más grande que app.backup.max-size-bytes
        ZipBackupReader.Limits smallZip = new ZipBackupReader.Limits(zipSize - 1, MAX * 5, 1000, 1_000_000);
        assertThatThrownBy(() -> ZipBackupReader.open(zipPath, mapper, smallZip))
                .isInstanceOf(BackupSizeExceededException.class);
    }

    @Test
    void escritorQueSuperaElMaximoAbortaYNoDejaArchivos() {
        Path target = tmp.resolve("big.zip");
        assertThatThrownBy(() -> {
            try (ZipBackupWriter zip = ZipBackupWriter.create(target, mapper, 2048, null)) {
                zip.module("taxes", 1).writeNdjson("taxes/taxes.ndjson",
                        IntStream.range(0, 50_000).mapToObj(i -> new TaxRow((long) i, UUID.randomUUID().toString(),
                                BigDecimal.valueOf(i), LocalDate.now(), (long) i)).iterator());
                zip.finish();
            }
        }).isInstanceOf(BackupSizeExceededException.class);
        assertThat(target).doesNotExist();
        assertThat(tmp.resolve("big.zip.part")).doesNotExist();
    }

    @Test
    void escritorValidaNombresYUnaEntradaALaVez() {
        try (ZipBackupWriter zip = ZipBackupWriter.create(tmp.resolve("w.zip"), mapper, MAX, null)) {
            BackupWriter w = zip.module("taxes", 1);
            assertThatThrownBy(() -> w.json("../x.json", Map.of())).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> w.json("manifest.json", Map.of())).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> zip.module("taxes", 1)).isInstanceOf(IllegalStateException.class);
            try (NdjsonEntryWriter open = w.ndjson("taxes/a.ndjson")) {
                open.write(Map.of("multi", "linea\ncon salto"));
                assertThatThrownBy(() -> w.ndjson("taxes/b.ndjson")).isInstanceOf(IllegalStateException.class);
            }
            w.json("taxes/c.json", Map.of("k", 1));
            assertThatThrownBy(() -> w.json("taxes/c.json", Map.of())).isInstanceOf(IllegalStateException.class);
        }
        assertThat(tmp.resolve("w.zip")).doesNotExist();
    }

    @Test
    void zipV1ConSoloEnterpriseJsonSigueLeyendose() throws IOException {
        Path v1 = tmp.resolve("v1.zip");
        String json = "{\"version\":1,\"exportedAt\":\"2025-03-01T08:30:00\",\"sourceEnterpriseId\":\"" + SOURCE
                + "\",\"enterprise\":{\"name\":\"Vieja\",\"nit\":\"900\"},"
                + "\"subjects\":[{\"code\":\"M1\",\"name\":\"Mat\"}]}";
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(v1))) {
            out.putNextEntry(new ZipEntry("enterprise.json"));
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }

        try (ZipBackupReader reader = ZipBackupReader.open(v1, mapper, limits())) {
            assertThat(reader.formatVersion()).isEqualTo(1);
            assertThat(reader.manifest().version()).isEqualTo(1);
            assertThat(reader.manifest().sourceEnterpriseId()).isEqualTo(SOURCE);
            assertThat(reader.manifest().exportedAt()).isEqualTo(LocalDateTime.of(2025, 3, 1, 8, 30));
            assertThat(reader.moduleKeys()).containsExactly(ZipBackupReader.V1_MODULE_KEY);
            reader.verifyAll();

            BackupReader ent = reader.module(ZipBackupReader.V1_MODULE_KEY);
            assertThat(ent.formatVersion()).isEqualTo(1);
            assertThat(ent.has(ZipBackupReader.V1_ENTERPRISE_ENTRY)).isTrue();
            JsonNode node = ent.readJson(ZipBackupReader.V1_ENTERPRISE_ENTRY, JsonNode.class);
            assertThat(node.path("enterprise").path("name").asText()).isEqualTo("Vieja");
            assertThat(node.path("subjects")).hasSize(1);
        }
    }

    @Test
    void archivoQueNoEsZipOSinManifestEsCorrupto() throws IOException {
        Path notZip = tmp.resolve("x.zip");
        Files.writeString(notZip, "esto no es un zip");
        assertThatThrownBy(() -> ZipBackupReader.open(notZip, mapper, limits()))
                .isInstanceOf(BackupCorruptedException.class);

        Path noManifest = tmp.resolve("y.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(noManifest))) {
            out.putNextEntry(new ZipEntry("otra.json"));
            out.write("{}".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        assertThatThrownBy(() -> ZipBackupReader.open(noManifest, mapper, limits()))
                .isInstanceOf(BackupCorruptedException.class)
                .hasMessageContaining("manifest.json");
    }

    /* ---------------- helpers ---------------- */

    /** Cambia el último dígito de la entrada conservando su tamaño (así solo falla el SHA-256). */
    private static UnaryOperator<byte[]> flipLastDigit() {
        return bytes -> {
            byte[] copy = bytes.clone();
            for (int i = copy.length - 1; i >= 0; i--) {
                if (copy[i] >= '0' && copy[i] <= '9') {
                    copy[i] = (byte) (copy[i] == '9' ? '8' : copy[i] + 1);
                    return copy;
                }
            }
            throw new IllegalStateException("sin dígitos");
        };
    }

    private UnaryOperator<byte[]> editManifest(UnaryOperator<ObjectNode> edit) {
        return bytes -> {
            try {
                ObjectNode node = (ObjectNode) mapper.readTree(bytes);
                return mapper.writeValueAsBytes(edit.apply(node));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private static ObjectNode entryNode(ObjectNode manifest, String path) {
        for (JsonNode module : (ArrayNode) manifest.get("modules")) {
            for (JsonNode entry : module.get("entries")) {
                if (path.equals(entry.get("path").asText())) {
                    return (ObjectNode) entry;
                }
            }
        }
        throw new IllegalArgumentException(path);
    }

    /** Copia el ZIP aplicando transformaciones por entrada y agregando entradas extra. */
    private Path rewrite(Path source, String name, Map<String, UnaryOperator<byte[]>> transforms,
            Map<String, String> extra) throws IOException {
        Path target = tmp.resolve(name);
        Map<String, byte[]> contents = new LinkedHashMap<>();
        try (ZipFile zf = new ZipFile(source.toFile())) {
            for (ZipEntry e : Collections.list(zf.entries())) {
                byte[] data = zf.getInputStream(e).readAllBytes();
                UnaryOperator<byte[]> t = transforms.get(e.getName());
                contents.put(e.getName(), t == null ? data : t.apply(data));
            }
        }
        if (extra != null) {
            extra.forEach((k, v) -> contents.put(k, v.getBytes(StandardCharsets.UTF_8)));
        }
        try (OutputStream os = Files.newOutputStream(target); ZipOutputStream out = new ZipOutputStream(os)) {
            for (Map.Entry<String, byte[]> e : contents.entrySet()) {
                out.putNextEntry(new ZipEntry(e.getKey()));
                out.write(e.getValue());
                out.closeEntry();
            }
        }
        return target;
    }
}
