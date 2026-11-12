package co.unicauca.edu.co.contables.commons.backup;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @brief Implementación en memoria de {@link IdMap}. Una instancia por restore
 * (no es un bean ni es thread-safe: el restore corre en un solo hilo).
 */
public class InMemoryIdMap implements IdMap {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final Map<String, Map<Object, Object>> namespaces = new HashMap<>();

    @Override
    public void put(String namespace, Object oldId, Object newId) {
        requireNamespace(namespace);
        if (oldId == null || newId == null) {
            throw new BackupIntegrityException(null,
                    "No se puede registrar una equivalencia nula en " + namespace
                            + " (viejo=" + oldId + ", nuevo=" + newId + ")");
        }
        Object key = normalize(namespace, oldId);
        Map<Object, Object> map = namespaces.computeIfAbsent(namespace, k -> new HashMap<>());
        Object previous = map.putIfAbsent(key, newId);
        if (previous != null && !previous.equals(newId)) {
            throw new BackupIntegrityException(null,
                    "El id " + oldId + " de " + namespace + " aparece dos veces en la copia (ya mapeado a "
                            + previous + ", ahora a " + newId + ")");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T require(String namespace, Object oldId) {
        requireNamespace(namespace);
        if (oldId == null) {
            throw new BackupIntegrityException(null,
                    "Referencia nula a " + namespace + " donde se esperaba un id");
        }
        Object value = lookup(namespace, oldId);
        if (value == null) {
            throw BackupIntegrityException.missingId(namespace, oldId);
        }
        return (T) value;
    }

    @Override
    public <T> T requireNullable(String namespace, Object oldId) {
        return oldId == null ? null : require(namespace, oldId);
    }

    @Override
    public Long requireLong(String namespace, Object oldId) {
        Object value = require(namespace, oldId);
        Long asLong = toLongOrNull(value);
        if (asLong == null) {
            throw new BackupIntegrityException(null,
                    "El id nuevo de " + namespace + " para " + oldId + " no es numérico: " + value);
        }
        return asLong;
    }

    @Override
    public UUID requireUuid(String namespace, Object oldId) {
        Object value = require(namespace, oldId);
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String s && UUID_PATTERN.matcher(s.trim()).matches()) {
            return UUID.fromString(s.trim());
        }
        throw new BackupIntegrityException(null,
                "El id nuevo de " + namespace + " para " + oldId + " no es un UUID: " + value);
    }

    @Override
    public String requireString(String namespace, Object oldId) {
        return require(namespace, oldId).toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(String namespace, Object oldId) {
        requireNamespace(namespace);
        if (oldId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) lookup(namespace, oldId));
    }

    @Override
    public boolean contains(String namespace, Object oldId) {
        return find(namespace, oldId).isPresent();
    }

    @Override
    public int size(String namespace) {
        Map<Object, Object> map = namespaces.get(namespace);
        return map == null ? 0 : map.size();
    }

    private Object lookup(String namespace, Object oldId) {
        Map<Object, Object> map = namespaces.get(namespace);
        return map == null ? null : map.get(normalize(namespace, oldId));
    }

    private static void requireNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("El namespace del IdMap es obligatorio");
        }
    }

    /**
     * Normaliza la clave: enteros → Long (Jackson deserializa ids chicos como
     * Integer si el DTO usa Object), UUID → texto en minúsculas, String con
     * forma de UUID → minúsculas, otros String → recortados.
     */
    private static Object normalize(String namespace, Object id) {
        Long asLong = toLongOrNull(id);
        if (asLong != null) {
            return asLong;
        }
        if (id instanceof UUID uuid) {
            return uuid.toString();
        }
        if (id instanceof String s) {
            String trimmed = s.trim();
            return UUID_PATTERN.matcher(trimmed).matches() ? trimmed.toLowerCase() : trimmed;
        }
        throw new IllegalArgumentException("Tipo de id no soportado en " + namespace + ": "
                + Objects.requireNonNull(id).getClass().getName());
    }

    private static Long toLongOrNull(Object id) {
        if (id instanceof Long l) {
            return l;
        }
        if (id instanceof Integer || id instanceof Short || id instanceof Byte) {
            return ((Number) id).longValue();
        }
        if (id instanceof BigInteger bi) {
            return bi.longValueExact();
        }
        if (id instanceof BigDecimal bd) {
            return bd.longValueExact();
        }
        return null;
    }
}
