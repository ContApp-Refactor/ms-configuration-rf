package co.unicauca.edu.co.contables.commons.backup.zip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @brief Contenido de {@code manifest.json} del ZIP v2.
 *
 * Se escribe AL FINAL del ZIP (los checksums se conocen al terminar); el
 * lector lo busca por nombre en el directorio central, así que el orden no
 * importa. Para un ZIP v1 el lector arma un manifest sintético con
 * {@code version = 1} y un único módulo {@code enterprise} con la entrada
 * {@code enterprise.json} (sin tamaño ni checksum).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManifestV2(
        String format,
        Integer version,
        LocalDateTime exportedAt,
        String appVersion,
        UUID sourceEnterpriseId,
        String sourceEnterpriseName,
        CreatedBy createdBy,
        List<ModuleEntry> modules,
        List<AssetEntry> assets,
        List<String> excluded) {

    public static final String FORMAT = "contapp-enterprise-backup";
    public static final int VERSION = 2;

    public ManifestV2 {
        modules = modules == null ? List.of() : List.copyOf(modules);
        assets = assets == null ? List.of() : List.copyOf(assets);
        excluded = excluded == null ? List.of() : List.copyOf(excluded);
    }

    public Optional<ModuleEntry> module(String key) {
        return modules.stream().filter(m -> m.key() != null && m.key().equals(key)).findFirst();
    }

    public Optional<AssetEntry> asset(String path) {
        return assets.stream().filter(a -> a.path() != null && a.path().equals(path)).findFirst();
    }

    /** Autor de la copia; solo informativo. El tenant viaja hasheado, nunca en claro. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreatedBy(String tenantHash, String username) {

        /** {@code tenantHash = "sha256:" + hex(sha256(tenantId))}. */
        public static CreatedBy of(String tenantId, String username) {
            return new CreatedBy(tenantId == null ? null : "sha256:" + NdjsonSupport.sha256Hex(tenantId), username);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModuleEntry(String key, Integer schema, List<EntryInfo> entries) {
        public ModuleEntry {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }

        public Optional<EntryInfo> entry(String path) {
            return entries.stream().filter(e -> e.path() != null && e.path().equals(path)).findFirst();
        }
    }

    /**
     * @param count     registros (líneas NDJSON; 1 para JSON)
     * @param sizeBytes tamaño DESCOMPRIMIDO exacto (también es el tope de lectura anti zip-bomb)
     * @param sha256    hex en minúsculas del contenido descomprimido
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntryInfo(String path, Long count, Long sizeBytes, String sha256) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssetEntry(String path, String contentType, Long sizeBytes, String sha256) {
    }
}
