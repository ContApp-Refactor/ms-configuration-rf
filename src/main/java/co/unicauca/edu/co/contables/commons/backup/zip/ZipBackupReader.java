package co.unicauca.edu.co.contables.commons.backup.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.commons.backup.BackupChecksumMismatchException;
import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.BackupException;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupSizeExceededException;
import co.unicauca.edu.co.contables.commons.backup.UnsupportedBackupVersionException;

/**
 * @brief Lector validado de un ZIP de backup v2 (y v1 de compatibilidad).
 *
 * Al abrir valida, ANTES de leer datos: tamaño del archivo
 * ({@code app.backup.max-size-bytes}), número de entradas, nombres seguros
 * (anti zip-slip), sin duplicados, {@code format}/{@code version} del manifest,
 * que cada entrada declarada exista y que no haya entradas no declaradas, y que
 * la suma de tamaños descomprimidos declarados no supere el límite (anti
 * zip-bomb). Al leer cada entrada se corta si se descomprime más de lo
 * declarado y al terminar se comparan tamaño y SHA-256.
 *
 * Detección de versión: si existe {@code manifest.json} → v2; si solo existe
 * {@code enterprise.json} → v1, con un manifest sintético de un único módulo
 * {@code enterprise} cuya entrada es {@code enterprise.json} (el módulo
 * enterprise la deserializa con su {@code BackupManifest} de siempre).
 *
 * Usa {@link ZipFile} (directorio central), así que el manifest puede estar al
 * final. No es thread-safe; cerrarlo libera el archivo.
 */
public final class ZipBackupReader implements AutoCloseable {

    public static final String MANIFEST_ENTRY = ZipBackupWriter.MANIFEST_ENTRY;
    /** Única entrada de los ZIP v1. */
    public static final String V1_ENTERPRISE_ENTRY = "enterprise.json";
    /** Módulo del manifest sintético v1. */
    public static final String V1_MODULE_KEY = "enterprise";

    /**
     * @param maxZipBytes          tamaño máximo del archivo ZIP
     * @param maxUncompressedBytes suma máxima de tamaños descomprimidos declarados
     * @param maxEntries           número máximo de entradas del ZIP
     * @param maxJsonBytes         tope para {@code manifest.json} y para la entrada v1 (sin tamaño declarado)
     */
    public record Limits(long maxZipBytes, long maxUncompressedBytes, int maxEntries, long maxJsonBytes) {

        public static final int DEFAULT_MAX_ENTRIES = 1000;
        public static final int DEFAULT_UNCOMPRESSED_RATIO = 5;
        public static final long DEFAULT_MAX_JSON_BYTES = 10L * 1024 * 1024;

        public Limits {
            if (maxZipBytes <= 0 || maxUncompressedBytes <= 0 || maxEntries <= 0 || maxJsonBytes <= 0) {
                throw new IllegalArgumentException("Los límites del backup deben ser > 0");
            }
        }

        /** Descomprimido = 5× el ZIP, 1000 entradas, 10 MB para JSON sueltos. */
        public static Limits defaults(long maxSizeBytes) {
            return new Limits(maxSizeBytes, saturatedMultiply(maxSizeBytes, DEFAULT_UNCOMPRESSED_RATIO),
                    DEFAULT_MAX_ENTRIES, DEFAULT_MAX_JSON_BYTES);
        }

        static long saturatedMultiply(long a, long b) {
            long r = a * b;
            return (a != 0 && r / a != b) ? Long.MAX_VALUE : r;
        }
    }

    private final Path file;
    private final ZipFile zipFile;
    private final ObjectMapper dataMapper;
    private final Limits limits;
    private final int formatVersion;
    private final ManifestV2 manifest;

    private ZipBackupReader(Path file, ZipFile zipFile, ObjectMapper dataMapper, Limits limits) {
        this.file = file;
        this.zipFile = zipFile;
        this.dataMapper = dataMapper;
        this.limits = limits;
        Map<String, ZipEntry> entries = scanEntries();
        if (entries.containsKey(MANIFEST_ENTRY)) {
            this.formatVersion = ManifestV2.VERSION;
            this.manifest = readAndValidateManifest(entries);
        } else if (entries.containsKey(V1_ENTERPRISE_ENTRY)) {
            this.formatVersion = 1;
            this.manifest = syntheticV1Manifest(entries.get(V1_ENTERPRISE_ENTRY));
        } else {
            throw new BackupCorruptedException("El archivo no es una copia válida: no contiene '"
                    + MANIFEST_ENTRY + "' ni '" + V1_ENTERPRISE_ENTRY + "'");
        }
    }

    /**
     * Abre y valida el ZIP.
     * @throws BackupSizeExceededException si el archivo o lo declarado descomprimido supera los límites
     * @throws BackupCorruptedException    si no es un ZIP válido o su estructura es inválida/peligrosa
     * @throws UnsupportedBackupVersionException si {@code format}/{@code version} no se soportan
     */
    public static ZipBackupReader open(Path file, ObjectMapper baseMapper, Limits limits) {
        long size;
        try {
            size = Files.size(file);
        } catch (IOException e) {
            throw new BackupCorruptedException("No se pudo leer el archivo de copia", e);
        }
        if (size > limits.maxZipBytes()) {
            throw new BackupSizeExceededException(size, limits.maxZipBytes());
        }
        ZipFile zf;
        try {
            zf = new ZipFile(file.toFile());
        } catch (ZipException e) {
            throw new BackupCorruptedException("El archivo de copia está corrupto o no es un ZIP válido", e);
        } catch (IOException e) {
            throw new BackupCorruptedException("No se pudo abrir el archivo de copia", e);
        }
        try {
            return new ZipBackupReader(file, zf, NdjsonSupport.dataMapper(baseMapper), limits);
        } catch (RuntimeException e) {
            closeQuietly(zf);
            throw e;
        }
    }

    /** 1 (solo {@code enterprise.json}) o 2. */
    public int formatVersion() {
        return formatVersion;
    }

    /** Manifest validado (sintético para v1). */
    public ManifestV2 manifest() {
        return manifest;
    }

    /** Claves de módulo en el orden del manifest. */
    public Set<String> moduleKeys() {
        Set<String> keys = new LinkedHashSet<>();
        manifest.modules().forEach(m -> keys.add(m.key()));
        return keys;
    }

    public boolean hasModule(String moduleKey) {
        return manifest.module(moduleKey).isPresent();
    }

    /** Lector de un módulo; falla si el ZIP no lo trae (consultar antes {@link #hasModule}). */
    public BackupReader module(String moduleKey) {
        ManifestV2.ModuleEntry module = manifest.module(moduleKey)
                .orElseThrow(() -> new IllegalArgumentException("La copia no incluye el módulo '" + moduleKey + "'"));
        return new ModuleReader(module);
    }

    /**
     * Lee TODAS las entradas y assets verificando tamaño y SHA-256 sin
     * deserializar. Pensado para llamarse antes de abrir la transacción del
     * restore, así un ZIP manipulado se rechaza sin tocar la BD.
     */
    public void verifyAll() {
        for (ManifestV2.ModuleEntry module : manifest.modules()) {
            for (ManifestV2.EntryInfo entry : module.entries()) {
                try (VerifyingInputStream in = openVerifying(entry.path(), entry.sizeBytes(), entry.sha256())) {
                    in.drainAndVerify();
                }
            }
        }
        for (ManifestV2.AssetEntry asset : manifest.assets()) {
            try (VerifyingInputStream in = openVerifying(asset.path(), asset.sizeBytes(), asset.sha256())) {
                in.drainAndVerify();
            }
        }
    }

    public Path file() {
        return file;
    }

    @Override
    public void close() {
        closeQuietly(zipFile);
    }

    /* ------------------------------------------------------------------ */
    /* Validación estructural                                             */
    /* ------------------------------------------------------------------ */

    private Map<String, ZipEntry> scanEntries() {
        Map<String, ZipEntry> result = new LinkedHashMap<>();
        int n = 0;
        try {
            Enumeration<? extends ZipEntry> en = zipFile.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                if (++n > limits.maxEntries()) {
                    throw new BackupCorruptedException("La copia tiene más de " + limits.maxEntries() + " entradas");
                }
                String name = e.getName();
                if (e.isDirectory()) {
                    String dir = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
                    if (!NdjsonSupport.isSafePath(dir)) {
                        throw unsafeName(name);
                    }
                    continue;
                }
                if (!NdjsonSupport.isSafePath(name)) {
                    throw unsafeName(name);
                }
                if (result.putIfAbsent(name, e) != null) {
                    throw new BackupCorruptedException("La entrada '" + name + "' está repetida en la copia");
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // nombres con codificación inválida o ZipFile cerrado
            throw new BackupCorruptedException("El archivo de copia tiene entradas ilegibles", e);
        }
        return result;
    }

    private static BackupCorruptedException unsafeName(String name) {
        return new BackupCorruptedException("La copia contiene una entrada con nombre no permitido: '" + name + "'");
    }

    private ManifestV2 readAndValidateManifest(Map<String, ZipEntry> entries) {
        ManifestV2 m;
        try (VerifyingInputStream in = openVerifying(MANIFEST_ENTRY, null, null)) {
            m = dataMapper.readValue(in, ManifestV2.class);
        } catch (BackupException e) {
            throw e;
        } catch (IOException e) {
            throw new BackupCorruptedException("El archivo no es una copia válida: '" + MANIFEST_ENTRY
                    + "' no es un JSON válido", e);
        }
        if (m == null) {
            throw new BackupCorruptedException("El archivo no es una copia válida: '" + MANIFEST_ENTRY + "' vacío");
        }
        if (!ManifestV2.FORMAT.equals(m.format())) {
            throw new UnsupportedBackupVersionException("Formato de copia no soportado: '" + m.format() + "'");
        }
        if (m.version() == null) {
            throw new BackupCorruptedException("El manifest de la copia no declara 'version'");
        }
        if (m.version() != ManifestV2.VERSION) {
            throw new UnsupportedBackupVersionException("Versión de copia no soportada: " + m.version()
                    + " (esta aplicación lee las versiones 1 y " + ManifestV2.VERSION + ")");
        }

        Set<String> declared = new HashSet<>();
        Set<String> moduleKeys = new HashSet<>();
        long totalDeclared = 0;
        for (ManifestV2.ModuleEntry module : m.modules()) {
            if (module == null || module.key() == null || module.key().isBlank()) {
                throw new BackupCorruptedException("El manifest tiene un módulo sin 'key'");
            }
            if (!moduleKeys.add(module.key())) {
                throw new BackupCorruptedException("El módulo '" + module.key() + "' está repetido en el manifest");
            }
            if (module.schema() == null || module.schema() < 1) {
                throw new BackupCorruptedException("El módulo '" + module.key() + "' no declara un 'schema' válido");
            }
            for (ManifestV2.EntryInfo e : module.entries()) {
                if (e == null) {
                    throw new BackupCorruptedException("El módulo '" + module.key() + "' tiene una entrada vacía");
                }
                validateDeclared(e.path(), e.sizeBytes(), e.sha256(), declared, entries);
                if (e.count() == null || e.count() < 0) {
                    throw new BackupCorruptedException("La entrada '" + e.path() + "' no declara un 'count' válido");
                }
                totalDeclared = addCapped(totalDeclared, e.sizeBytes());
            }
        }
        for (ManifestV2.AssetEntry a : m.assets()) {
            if (a == null) {
                throw new BackupCorruptedException("El manifest tiene un asset vacío");
            }
            validateDeclared(a.path(), a.sizeBytes(), a.sha256(), declared, entries);
            totalDeclared = addCapped(totalDeclared, a.sizeBytes());
        }
        if (totalDeclared > limits.maxUncompressedBytes()) {
            throw new BackupSizeExceededException(totalDeclared, limits.maxUncompressedBytes());
        }
        for (String name : entries.keySet()) {
            if (!MANIFEST_ENTRY.equals(name) && !declared.contains(name)) {
                throw new BackupCorruptedException("La copia contiene la entrada '" + name
                        + "', que no está declarada en el manifest");
            }
        }
        return m;
    }

    private static void validateDeclared(String path, Long sizeBytes, String sha256, Set<String> declared,
            Map<String, ZipEntry> entries) {
        if (!NdjsonSupport.isSafePath(path) || MANIFEST_ENTRY.equals(path)) {
            throw unsafeName(String.valueOf(path));
        }
        if (!declared.add(path)) {
            throw new BackupCorruptedException("La entrada '" + path + "' está declarada dos veces en el manifest");
        }
        if (sizeBytes == null || sizeBytes < 0) {
            throw new BackupCorruptedException("La entrada '" + path + "' no declara un 'sizeBytes' válido");
        }
        if (!NdjsonSupport.isSha256Hex(sha256)) {
            throw new BackupCorruptedException("La entrada '" + path + "' no declara un 'sha256' válido");
        }
        ZipEntry ze = entries.get(path);
        if (ze == null) {
            throw new BackupCorruptedException("Falta la entrada '" + path + "' declarada en el manifest");
        }
        if (ze.getSize() >= 0 && ze.getSize() != sizeBytes) {
            throw new BackupCorruptedException("La entrada '" + path + "' no coincide con el tamaño declarado");
        }
    }

    private static long addCapped(long total, long add) {
        long r = total + add;
        return r < 0 ? Long.MAX_VALUE : r;
    }

    private ManifestV2 syntheticV1Manifest(ZipEntry v1Entry) {
        UUID sourceId = null;
        LocalDateTime exportedAt = null;
        try (VerifyingInputStream in = openVerifying(V1_ENTERPRISE_ENTRY, null, null)) {
            JsonNode root = dataMapper.readTree(in);
            if (root == null || !root.isObject()) {
                throw new BackupCorruptedException("El archivo no es una copia válida: '" + V1_ENTERPRISE_ENTRY
                        + "' no es un objeto JSON");
            }
            JsonNode version = root.get("version");
            if (version != null && version.isInt() && version.intValue() > 1) {
                throw new UnsupportedBackupVersionException("Versión de copia no soportada: " + version.intValue());
            }
            sourceId = parseUuid(root.get("sourceEnterpriseId"));
            exportedAt = parseDate(root.get("exportedAt"));
        } catch (BackupException e) {
            throw e;
        } catch (IOException e) {
            throw new BackupCorruptedException("El archivo no es una copia válida: '" + V1_ENTERPRISE_ENTRY
                    + "' no es un JSON válido", e);
        }
        ManifestV2.EntryInfo info = new ManifestV2.EntryInfo(V1_ENTERPRISE_ENTRY, 1L,
                v1Entry.getSize() >= 0 ? v1Entry.getSize() : null, null);
        return new ManifestV2(ManifestV2.FORMAT, 1, exportedAt, null, sourceId, null, null,
                List.of(new ManifestV2.ModuleEntry(V1_MODULE_KEY, 1, List.of(info))), List.of(), List.of());
    }

    private UUID parseUuid(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        try {
            return UUID.fromString(node.asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDateTime parseDate(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return dataMapper.treeToValue(node, LocalDateTime.class);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    /* ------------------------------------------------------------------ */
    /* Lectura verificada                                                 */
    /* ------------------------------------------------------------------ */

    /**
     * @param declaredSize tamaño exacto esperado (tope de lectura); {@code null} → tope {@code maxJsonBytes} sin igualdad
     * @param sha256       hash esperado; {@code null} → sin verificación (solo v1 y el propio manifest)
     */
    private VerifyingInputStream openVerifying(String path, Long declaredSize, String sha256) {
        ZipEntry ze = zipFile.getEntry(path);
        if (ze == null || ze.isDirectory()) {
            throw new BackupCorruptedException("Falta la entrada '" + path + "' en la copia");
        }
        try {
            long cap = declaredSize != null ? declaredSize : limits.maxJsonBytes();
            return new VerifyingInputStream(zipFile.getInputStream(ze), path, cap, declaredSize, sha256);
        } catch (ZipException e) {
            throw new BackupCorruptedException("La entrada '" + path + "' está corrupta", e);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la entrada " + path, e);
        }
    }

    private static void closeQuietly(ZipFile zf) {
        try {
            zf.close();
        } catch (IOException ignored) {
            // nada que hacer
        }
    }

    /**
     * Stream de una entrada que: corta al superar {@code cap} bytes
     * descomprimidos (zip-bomb), y al llegar a EOF compara tamaño y SHA-256.
     * {@link #close()} termina de leer la entrada para poder verificarla aunque
     * el consumidor no la haya leído completa.
     */
    private static final class VerifyingInputStream extends InputStream {
        private final InputStream raw;
        private final String path;
        private final long cap;
        private final Long expectedSize;
        private final String expectedSha;
        private final MessageDigest digest = NdjsonSupport.newSha256();
        private long size;
        private boolean verified;
        private boolean failed;
        private boolean closed;

        VerifyingInputStream(InputStream raw, String path, long cap, Long expectedSize, String expectedSha) {
            this.raw = raw;
            this.path = path;
            this.cap = cap;
            this.expectedSize = expectedSize;
            this.expectedSha = expectedSha;
        }

        @Override
        public int read() throws IOException {
            byte[] one = new byte[1];
            int n = read(one, 0, 1);
            return n == -1 ? -1 : one[0] & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed) {
                throw new IOException("La entrada '" + path + "' ya está cerrada");
            }
            if (len == 0) {
                return 0;
            }
            int n;
            try {
                n = raw.read(b, off, len);
            } catch (ZipException e) {
                failed = true;
                throw new BackupCorruptedException("La entrada '" + path + "' está corrupta", e);
            }
            if (n == -1) {
                verify();
                return -1;
            }
            size += n;
            if (size > cap) {
                failed = true;
                throw new BackupCorruptedException("La entrada '" + path
                        + "' se descomprime a más bytes de los declarados (posible zip-bomb)");
            }
            digest.update(b, off, n);
            return n;
        }

        /** Lee hasta EOF (acotado por {@code cap}) y verifica. */
        void drainAndVerify() {
            byte[] buf = new byte[8192];
            try {
                while (!verified && read(buf, 0, buf.length) != -1) {
                    // descartar
                }
            } catch (IOException e) {
                failed = true;
                throw new UncheckedIOException("No se pudo leer la entrada " + path, e);
            }
        }

        private void verify() {
            if (verified) {
                return;
            }
            verified = true;
            if (expectedSize != null && size != expectedSize) {
                failed = true;
                throw new BackupCorruptedException("La entrada '" + path + "' tiene " + size
                        + " bytes y el manifest declara " + expectedSize);
            }
            if (expectedSha != null) {
                String actual = NdjsonSupport.hex(digest.digest());
                if (!actual.equalsIgnoreCase(expectedSha)) {
                    failed = true;
                    throw new BackupChecksumMismatchException(path, expectedSha, actual);
                }
            }
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            try {
                if (!failed && !verified) {
                    drainAndVerify();
                }
            } finally {
                closed = true;
                try {
                    raw.close();
                } catch (IOException ignored) {
                    // nada que hacer
                }
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* Lector por módulo                                                  */
    /* ------------------------------------------------------------------ */

    private final class ModuleReader implements BackupReader {
        private final ManifestV2.ModuleEntry module;
        private final Map<String, ManifestV2.EntryInfo> byPath = new HashMap<>();

        ModuleReader(ManifestV2.ModuleEntry module) {
            this.module = module;
            module.entries().forEach(e -> byPath.put(e.path(), e));
        }

        @Override
        public String moduleKey() {
            return module.key();
        }

        @Override
        public int schemaVersion() {
            return module.schema();
        }

        @Override
        public int formatVersion() {
            return formatVersion;
        }

        @Override
        public Set<String> paths() {
            Set<String> paths = new LinkedHashSet<>();
            module.entries().forEach(e -> paths.add(e.path()));
            return paths;
        }

        @Override
        public boolean has(String path) {
            return byPath.containsKey(path);
        }

        @Override
        public long declaredCount(String path) {
            ManifestV2.EntryInfo e = byPath.get(path);
            return e == null || e.count() == null ? -1 : e.count();
        }

        private ManifestV2.EntryInfo entry(String path) {
            ManifestV2.EntryInfo e = byPath.get(path);
            if (e == null) {
                throw new BackupCorruptedException("La copia no incluye la entrada '" + path + "' del módulo '"
                        + module.key() + "'");
            }
            return e;
        }

        @Override
        public <T> T readJson(String path, Class<T> type) {
            ManifestV2.EntryInfo e = entry(path);
            try (VerifyingInputStream in = openVerifying(path, e.sizeBytes(), e.sha256())) {
                T value = dataMapper.readValue(in, type);
                in.drainAndVerify();
                return value;
            } catch (BackupException ex) {
                throw ex;
            } catch (IOException ex) {
                throw new BackupCorruptedException("La entrada '" + path + "' no es un JSON válido: "
                        + ex.getMessage(), ex);
            }
        }

        @Override
        public <T> Stream<T> stream(String path, Class<T> type) {
            ManifestV2.EntryInfo e = entry(path);
            VerifyingInputStream in = openVerifying(path, e.sizeBytes(), e.sha256());
            MappingIterator<T> it;
            try {
                it = dataMapper.readerFor(type).readValues(in);
            } catch (IOException ex) {
                in.close();
                throw new BackupCorruptedException("La entrada '" + path + "' no es un NDJSON válido", ex);
            }
            NdjsonIterator<T> iterator = new NdjsonIterator<>(it, in, path, e.count());
            return StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL),
                            false)
                    .onClose(iterator::close);
        }

        @Override
        public boolean hasAsset(String path) {
            return manifest.asset(path).isPresent();
        }

        @Override
        public String assetContentType(String path) {
            return manifest.asset(path).map(ManifestV2.AssetEntry::contentType).orElse(null);
        }

        @Override
        public InputStream openAsset(String path) {
            ManifestV2.AssetEntry a = manifest.asset(path)
                    .orElseThrow(() -> new BackupCorruptedException("La copia no incluye el asset '" + path + "'"));
            return openVerifying(path, a.sizeBytes(), a.sha256());
        }
    }

    /** Iterador NDJSON que al terminar verifica checksum (vía stream) y conteo declarado. */
    private static final class NdjsonIterator<T> implements Iterator<T> {
        private final MappingIterator<T> it;
        private final VerifyingInputStream in;
        private final String path;
        private final Long expectedCount;
        private long read;
        private boolean done;

        NdjsonIterator(MappingIterator<T> it, VerifyingInputStream in, String path, Long expectedCount) {
            this.it = it;
            this.in = in;
            this.path = path;
            this.expectedCount = expectedCount;
        }

        @Override
        public boolean hasNext() {
            if (done) {
                return false;
            }
            boolean has;
            try {
                has = it.hasNextValue();
            } catch (BackupException e) {
                done = true;
                throw e;
            } catch (IOException | RuntimeException e) {
                done = true;
                throw new BackupCorruptedException("La entrada '" + path + "' tiene una línea inválida (registro "
                        + (read + 1) + ")", e);
            }
            if (!has) {
                done = true;
                in.drainAndVerify();
                if (expectedCount != null && read != expectedCount) {
                    throw new BackupCorruptedException("La entrada '" + path + "' tiene " + read
                            + " registros y el manifest declara " + expectedCount);
                }
            }
            return has;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            try {
                T value = it.nextValue();
                read++;
                return value;
            } catch (BackupException e) {
                done = true;
                throw e;
            } catch (IOException | RuntimeException e) {
                done = true;
                throw new BackupCorruptedException("La entrada '" + path + "' tiene una línea inválida (registro "
                        + (read + 1) + ")", e);
            }
        }

        void close() {
            try {
                it.close();
            } catch (IOException ignored) {
                // el stream subyacente se cierra (y verifica) abajo
            } finally {
                in.close();
            }
        }
    }
}
