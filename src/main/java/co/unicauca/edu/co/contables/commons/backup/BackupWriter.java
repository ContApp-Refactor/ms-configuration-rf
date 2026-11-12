package co.unicauca.edu.co.contables.commons.backup;

import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @brief Escritor de entradas de UN módulo dentro del ZIP v2.
 *
 * Lo entrega el coordinador a {@link BackupParticipant#export}. Cada entrada se
 * escribe en streaming directo al ZIP (sin acumular en memoria) y queda
 * registrada en el manifest con su conteo, tamaño y SHA-256.
 *
 * Solo puede haber UNA entrada abierta a la vez (restricción de
 * {@code ZipOutputStream}). Los paths son relativos, con {@code /} como
 * separador, sin {@code ..} (p. ej. {@code "taxes/taxes.ndjson"}).
 */
public interface BackupWriter {

    /** Clave del módulo dueño de las entradas que se escriben con este escritor. */
    String moduleKey();

    /**
     * Abre una entrada NDJSON (un registro JSON por línea). Hay que cerrarla
     * (try-with-resources) antes de abrir otra.
     */
    NdjsonEntryWriter ndjson(String path);

    /** Escribe una entrada NDJSON completa desde un iterador (lo consume sin acumular). */
    default long writeNdjson(String path, Iterator<?> records) {
        try (NdjsonEntryWriter out = ndjson(path)) {
            while (records.hasNext()) {
                out.write(records.next());
            }
            return out.count();
        }
    }

    /** Escribe una entrada NDJSON completa desde un stream (lo consume sin acumular). */
    default long writeNdjson(String path, Stream<?> records) {
        return writeNdjson(path, records.iterator());
    }

    /** Escribe una entrada JSON con un único objeto (conteo = 1). */
    void json(String path, Object value);

    /**
     * Copia un binario (p. ej. el logo) como asset; queda en
     * {@code manifest.assets} con su tipo, tamaño y SHA-256. No cierra {@code content}.
     */
    void asset(String path, String contentType, InputStream content);
}
