package co.unicauca.edu.co.contables.commons.backup.zip;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @brief Fábrica (bean) de lectores/escritores ZIP v2 configurada con las
 * propiedades {@code app.backup.*} y el ObjectMapper de la aplicación.
 *
 * <ul>
 * <li>{@code app.backup.max-size-bytes} (default 500 MB): tope del ZIP generado o subido.</li>
 * <li>{@code app.backup.max-entries} (default 1000): tope de entradas del ZIP.</li>
 * <li>{@code app.backup.max-uncompressed-ratio} (default 5): descomprimido máximo = ratio × max-size-bytes.</li>
 * </ul>
 */
@Component
public class ZipBackupFormat {

    private final ObjectMapper objectMapper;
    private final long maxSizeBytes;
    private final ZipBackupReader.Limits limits;

    public ZipBackupFormat(ObjectMapper objectMapper,
            @Value("${app.backup.max-size-bytes:524288000}") long maxSizeBytes,
            @Value("${app.backup.max-entries:1000}") int maxEntries,
            @Value("${app.backup.max-uncompressed-ratio:5}") int uncompressedRatio) {
        this.objectMapper = objectMapper;
        this.maxSizeBytes = maxSizeBytes;
        this.limits = new ZipBackupReader.Limits(maxSizeBytes,
                ZipBackupReader.Limits.saturatedMultiply(maxSizeBytes, uncompressedRatio), maxEntries,
                ZipBackupReader.Limits.DEFAULT_MAX_JSON_BYTES);
    }

    /** Escritor en streaming hacia {@code target} (el archivo aparece al llamar {@code finish()}). */
    public ZipBackupWriter newWriter(Path target, ZipBackupWriter.Header header) {
        return ZipBackupWriter.create(target, objectMapper, maxSizeBytes, header);
    }

    /** Abre y valida un ZIP (v1 o v2). */
    public ZipBackupReader open(Path zip) {
        return ZipBackupReader.open(zip, objectMapper, limits);
    }

    public long maxSizeBytes() {
        return maxSizeBytes;
    }

    public ZipBackupReader.Limits limits() {
        return limits;
    }
}
