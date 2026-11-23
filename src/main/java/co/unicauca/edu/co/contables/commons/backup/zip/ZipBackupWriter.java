package co.unicauca.edu.co.contables.commons.backup.zip;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import co.unicauca.edu.co.contables.commons.backup.BackupSizeExceededException;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.NdjsonEntryWriter;

/**
 * @brief Escritor en streaming del ZIP v2.
 *
 * Escribe a {@code <target>.part} y, en {@link #finish()}, agrega
 * {@code manifest.json} AL FINAL (con conteos, tamaños y SHA-256 de cada
 * entrada) y mueve el archivo a {@code target} (ATOMIC_MOVE si el FS lo
 * permite). Nunca arma el ZIP en un {@code byte[]}.
 *
 * Si los bytes comprimidos escritos superan {@code maxSizeBytes} se lanza
 * {@link BackupSizeExceededException}; cerrar el escritor sin
 * {@link #finish()} (p. ej. por una excepción en un participante) borra el
 * temporal. Uso típico:
 *
 * <pre>
 * try (ZipBackupWriter zip = format.newWriter(target, header)) {
 *     for (BackupParticipant p : participants) {
 *         p.export(enterpriseId, zip.module(p.moduleKey(), p.schemaVersion()));
 *     }
 *     ManifestV2 manifest = zip.finish();
 * }
 * </pre>
 *
 * No es thread-safe.
 */
public final class ZipBackupWriter implements AutoCloseable {

    public static final String MANIFEST_ENTRY = "manifest.json";

    /** Datos de cabecera del manifest que conoce el coordinador. */
    public record Header(UUID sourceEnterpriseId, String sourceEnterpriseName, String appVersion,
            ManifestV2.CreatedBy createdBy, List<String> excluded, LocalDateTime exportedAt) {
    }

    private final Path target;
    private final Path partFile;
    private final long maxSizeBytes;
    private final Header header;
    private final ObjectMapper manifestMapper;
    private final ObjectWriter lineWriter;
    private final ObjectMapper dataMapper;
    private final OutputStream rawOut;
    private final SizeLimitedOutputStream fileOut;
    private final ZipOutputStream zip;

    private final Map<String, ModuleState> modules = new LinkedHashMap<>();
    private final Set<String> paths = new HashSet<>();
    private final List<ManifestV2.AssetEntry> assets = new ArrayList<>();

    private EntryOutputStream current;
    private boolean finished;
    private boolean closed;

    private ZipBackupWriter(Path target, ObjectMapper baseMapper, long maxSizeBytes, Header header)
            throws IOException {
        if (maxSizeBytes <= 0) {
            throw new IllegalArgumentException("maxSizeBytes debe ser > 0");
        }
        this.target = target.toAbsolutePath().normalize();
        this.partFile = this.target.resolveSibling(this.target.getFileName() + ".part");
        this.maxSizeBytes = maxSizeBytes;
        this.header = header;
        this.dataMapper = NdjsonSupport.dataMapper(baseMapper);
        this.manifestMapper = this.dataMapper;
        this.lineWriter = NdjsonSupport.lineWriter(dataMapper);
        if (this.target.getParent() != null) {
            Files.createDirectories(this.target.getParent());
        }
        this.rawOut = new BufferedOutputStream(Files.newOutputStream(partFile));
        this.fileOut = new SizeLimitedOutputStream(rawOut, maxSizeBytes);
        this.zip = new ZipOutputStream(fileOut);
    }

    /** Crea el escritor; el archivo final aparece en {@code target} solo tras {@link #finish()}. */
    public static ZipBackupWriter create(Path target, ObjectMapper baseMapper, long maxSizeBytes, Header header) {
        try {
            return new ZipBackupWriter(target, baseMapper, maxSizeBytes, header);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el archivo de backup " + target, e);
        }
    }

    /** Escritor para las entradas de un módulo. Cada {@code moduleKey} se registra una sola vez. */
    public BackupWriter module(String moduleKey, int schemaVersion) {
        ensureOpen();
        if (moduleKey == null || moduleKey.isBlank()) {
            throw new IllegalArgumentException("moduleKey es obligatorio");
        }
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion debe ser >= 1 en " + moduleKey);
        }
        if (modules.containsKey(moduleKey)) {
            throw new IllegalStateException("El módulo '" + moduleKey + "' ya fue escrito en este backup");
        }
        ModuleState state = new ModuleState(moduleKey, schemaVersion);
        modules.put(moduleKey, state);
        return new ModuleWriter(state);
    }

    /** Bytes comprimidos escritos al disco hasta ahora. */
    public long bytesWritten() {
        return fileOut.count;
    }

    /**
     * Escribe {@code manifest.json}, cierra el ZIP y lo mueve a {@code target}.
     * @return el manifest escrito
     */
    public ManifestV2 finish() {
        ensureOpen();
        if (current != null) {
            throw new IllegalStateException("La entrada '" + current.path + "' sigue abierta");
        }
        ManifestV2 manifest = buildManifest();
        try {
            zip.putNextEntry(new ZipEntry(MANIFEST_ENTRY));
            zip.write(manifestMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest));
            zip.closeEntry();
            zip.finish();
            zip.close();
            closed = true;
            if (fileOut.count > maxSizeBytes) {
                throw new BackupSizeExceededException(fileOut.count, maxSizeBytes);
            }
            move();
            finished = true;
            return manifest;
        } catch (IOException e) {
            throw new UncheckedIOException("Error al cerrar el backup " + target, e);
        } finally {
            if (!finished) {
                abort();
            }
        }
    }

    /** Descarta el backup: cierra el ZIP y borra el temporal. Idempotente. */
    public void abort() {
        if (!closed) {
            closed = true;
            try {
                zip.close();
            } catch (IOException | RuntimeException ignored) {
                // el temporal se borra igual
            }
        }
        try {
            // Si zip.close() falló a mitad (p. ej. límite de tamaño) no cierra el archivo:
            // sin esto, en Windows el temporal queda bloqueado y no se puede borrar.
            rawOut.close();
        } catch (IOException | RuntimeException ignored) {
            // mejor esfuerzo
        }
        try {
            Files.deleteIfExists(partFile);
        } catch (IOException ignored) {
            // mejor esfuerzo
        }
    }

    /** Sin {@link #finish()} previo equivale a {@link #abort()}. */
    @Override
    public void close() {
        if (!finished) {
            abort();
        }
    }

    public Path target() {
        return target;
    }

    private void move() throws IOException {
        try {
            Files.move(partFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(partFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private ManifestV2 buildManifest() {
        List<ManifestV2.ModuleEntry> moduleEntries = modules.values().stream()
                .map(m -> new ManifestV2.ModuleEntry(m.key, m.schema, List.copyOf(m.entries)))
                .toList();
        Header h = header == null ? new Header(null, null, null, null, null, null) : header;
        return new ManifestV2(
                ManifestV2.FORMAT,
                ManifestV2.VERSION,
                h.exportedAt() == null ? LocalDateTime.now() : h.exportedAt(),
                h.appVersion(),
                h.sourceEnterpriseId(),
                h.sourceEnterpriseName(),
                h.createdBy(),
                moduleEntries,
                assets,
                h.excluded());
    }

    private void ensureOpen() {
        if (closed || finished) {
            throw new IllegalStateException("El backup ya fue cerrado");
        }
        if (fileOut.exceeded) {
            throw new IllegalStateException("El backup superó el tamaño máximo y debe descartarse");
        }
    }

    private EntryOutputStream openEntry(String path, EntryKind kind, ModuleState module, String contentType) {
        ensureOpen();
        if (!NdjsonSupport.isSafePath(path)) {
            throw new IllegalArgumentException("Nombre de entrada inválido: '" + path + "'");
        }
        if (MANIFEST_ENTRY.equals(path)) {
            throw new IllegalArgumentException("'" + MANIFEST_ENTRY + "' está reservado");
        }
        if (current != null) {
            throw new IllegalStateException("No se puede abrir '" + path + "': la entrada '" + current.path
                    + "' sigue abierta");
        }
        if (!paths.add(path)) {
            throw new IllegalStateException("La entrada '" + path + "' ya existe en el backup");
        }
        try {
            zip.putNextEntry(new ZipEntry(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Error al abrir la entrada " + path, e);
        }
        current = new EntryOutputStream(path, kind, module, contentType);
        return current;
    }

    private enum EntryKind { NDJSON, JSON, ASSET }

    private static final class ModuleState {
        final String key;
        final int schema;
        final List<ManifestV2.EntryInfo> entries = new ArrayList<>();

        ModuleState(String key, int schema) {
            this.key = key;
            this.schema = schema;
        }
    }

    /** Stream de UNA entrada: calcula SHA-256 y tamaño; close() cierra la entrada, no el ZIP. */
    private final class EntryOutputStream extends OutputStream {
        final String path;
        final EntryKind kind;
        final ModuleState module;
        final String contentType;
        final MessageDigest digest = NdjsonSupport.newSha256();
        long size;
        long count;
        boolean entryClosed;

        EntryOutputStream(String path, EntryKind kind, ModuleState module, String contentType) {
            this.path = path;
            this.kind = kind;
            this.module = module;
            this.contentType = contentType;
        }

        @Override
        public void write(int b) throws IOException {
            ensureWritable();
            zip.write(b);
            digest.update((byte) b);
            size++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            ensureWritable();
            zip.write(b, off, len);
            digest.update(b, off, len);
            size += len;
        }

        private void ensureWritable() {
            if (entryClosed) {
                throw new IllegalStateException("La entrada '" + path + "' ya está cerrada");
            }
        }

        @Override
        public void close() {
            if (entryClosed) {
                return;
            }
            entryClosed = true;
            current = null;
            try {
                zip.closeEntry();
            } catch (IOException e) {
                throw new UncheckedIOException("Error al cerrar la entrada " + path, e);
            }
            String sha = NdjsonSupport.hex(digest.digest());
            if (kind == EntryKind.ASSET) {
                assets.add(new ManifestV2.AssetEntry(path, contentType, size, sha));
            } else {
                module.entries.add(new ManifestV2.EntryInfo(path, count, size, sha));
            }
        }
    }

    private final class ModuleWriter implements BackupWriter {
        private final ModuleState state;

        ModuleWriter(ModuleState state) {
            this.state = state;
        }

        @Override
        public String moduleKey() {
            return state.key;
        }

        @Override
        public NdjsonEntryWriter ndjson(String path) {
            EntryOutputStream out = openEntry(path, EntryKind.NDJSON, state, null);
            return new NdjsonEntryWriter() {
                @Override
                public void write(Object record) {
                    if (record == null) {
                        throw new IllegalArgumentException("Registro nulo en " + path);
                    }
                    try {
                        out.write(lineWriter.writeValueAsBytes(record));
                        out.write('\n');
                        out.count++;
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("No se pudo serializar un registro de " + path, e);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Error al escribir " + path, e);
                    }
                }

                @Override
                public long count() {
                    return out.count;
                }

                @Override
                public void close() {
                    out.close();
                }
            };
        }

        @Override
        public void json(String path, Object value) {
            if (value == null) {
                throw new IllegalArgumentException("Valor nulo en " + path);
            }
            try (EntryOutputStream out = openEntry(path, EntryKind.JSON, state, null)) {
                out.write(lineWriter.writeValueAsBytes(value));
                out.count = 1;
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("No se pudo serializar " + path, e);
            } catch (IOException e) {
                throw new UncheckedIOException("Error al escribir " + path, e);
            }
        }

        @Override
        public void asset(String path, String contentType, InputStream content) {
            try (EntryOutputStream out = openEntry(path, EntryKind.ASSET, state, contentType)) {
                content.transferTo(out);
                out.count = 1;
            } catch (IOException e) {
                throw new UncheckedIOException("Error al escribir el asset " + path, e);
            }
        }
    }

    /** Cuenta bytes comprimidos y corta al pasar el máximo. */
    private static final class SizeLimitedOutputStream extends FilterOutputStream {
        final long max;
        long count;
        boolean exceeded;

        SizeLimitedOutputStream(OutputStream out, long max) {
            super(out);
            this.max = max;
        }

        @Override
        public void write(int b) throws IOException {
            check(1);
            out.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            check(len);
            out.write(b, off, len);
            count += len;
        }

        private void check(int len) {
            if (count + len > max) {
                exceeded = true;
                throw new BackupSizeExceededException(count + len, max);
            }
        }
    }
}
