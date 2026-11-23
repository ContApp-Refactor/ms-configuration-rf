package co.unicauca.edu.co.contables.commons.backup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @brief Lector de las entradas de UN módulo de un ZIP ya validado.
 *
 * Lo entrega el coordinador a {@link BackupParticipant#restore}. Todo lo que
 * se lee pasa por verificación: al terminar cada entrada se compara su
 * SHA-256, su tamaño y (NDJSON) su conteo con el manifest; si no coinciden se
 * lanza {@link BackupChecksumMismatchException} / {@link BackupCorruptedException}
 * y la transacción del restore hace rollback.
 */
public interface BackupReader {

    /** Clave del módulo. */
    String moduleKey();

    /** Versión del formato de las entradas del módulo según el manifest. */
    int schemaVersion();

    /** Versión del formato del ZIP: 1 (solo {@code enterprise.json}) o 2. */
    int formatVersion();

    /** Paths declarados para este módulo. */
    Set<String> paths();

    /** {@code true} si el módulo declara la entrada (las opcionales pueden faltar en ZIPs viejos). */
    boolean has(String path);

    /** Conteo declarado en el manifest para la entrada, o -1 si no se declaró (v1). */
    long declaredCount(String path);

    /** Lee una entrada JSON de un solo objeto. */
    <T> T readJson(String path, Class<T> type);

    /**
     * Stream perezoso de una entrada NDJSON. DEBE cerrarse (try-with-resources):
     * al cerrarlo se termina de leer la entrada y se verifica checksum y conteo.
     */
    <T> Stream<T> stream(String path, Class<T> type);

    /** Recorre una entrada NDJSON registro a registro. */
    default <T> void forEach(String path, Class<T> type, Consumer<? super T> consumer) {
        try (Stream<T> s = stream(path, type)) {
            s.forEachOrdered(consumer);
        }
    }

    /** Recorre una entrada NDJSON en lotes de {@code batchSize} (para {@code saveAll}+{@code flush}+{@code clear}). */
    default <T> void forEachBatch(String path, Class<T> type, int batchSize, Consumer<List<T>> consumer) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize debe ser > 0");
        }
        try (Stream<T> s = stream(path, type)) {
            Iterator<T> it = s.iterator();
            List<T> batch = new ArrayList<>(batchSize);
            while (it.hasNext()) {
                batch.add(it.next());
                if (batch.size() == batchSize) {
                    consumer.accept(batch);
                    batch = new ArrayList<>(batchSize);
                }
            }
            if (!batch.isEmpty()) {
                consumer.accept(batch);
            }
        }
    }

    /** Lee una entrada NDJSON completa en memoria. Solo para tablas chicas. */
    default <T> List<T> readAll(String path, Class<T> type) {
        try (Stream<T> s = stream(path, type)) {
            return s.toList();
        }
    }

    /** {@code true} si el ZIP declara el asset. */
    boolean hasAsset(String path);

    /** Tipo de contenido declarado del asset, o {@code null} si no existe. */
    String assetContentType(String path);

    /** Abre un asset binario; al cerrarlo se verifica tamaño y SHA-256. */
    InputStream openAsset(String path);
}
