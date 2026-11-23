package co.unicauca.edu.co.contables.commons.backup;

import java.util.Optional;
import java.util.UUID;

/**
 * @brief Mapa de equivalencias id viejo (del ZIP) → id nuevo (de la BD destino),
 * separado por namespace ({@link BackupKeys}).
 *
 * Reemplaza a {@code RestoreEquivalenceMap} (solo Long→Long). Las claves
 * aceptan Long/Integer/Short/Byte (se normalizan a Long), UUID y String (un
 * String con forma de UUID equivale al UUID). El valor nuevo se guarda tal cual.
 *
 * Regla de oro: una FK que no se puede remapear es un ERROR. {@link #require}
 * lanza {@link BackupIntegrityException}; nunca devuelve {@code null}.
 */
public interface IdMap {

    /**
     * Registra viejo→nuevo. Volver a registrar el mismo par es idempotente;
     * registrar otro {@code newId} para el mismo {@code oldId} lanza
     * {@link BackupIntegrityException}. {@code oldId}/{@code newId} nulos se rechazan.
     */
    void put(String namespace, Object oldId, Object newId);

    /**
     * Id nuevo para {@code oldId}. Lanza {@link BackupIntegrityException} si no
     * está registrado o si {@code oldId} es {@code null}.
     */
    <T> T require(String namespace, Object oldId);

    /** Como {@link #require} pero para FKs opcionales: {@code null} entra → {@code null} sale. */
    <T> T requireNullable(String namespace, Object oldId);

    /** {@link #require} convertido a Long (falla si el id nuevo no es numérico entero). */
    Long requireLong(String namespace, Object oldId);

    /** {@link #require} convertido a UUID (acepta UUID o String con forma de UUID). */
    UUID requireUuid(String namespace, Object oldId);

    /** {@link #require} como texto (p. ej. columnas de empresa guardadas como String). */
    String requireString(String namespace, Object oldId);

    /** Id nuevo si existe; vacío si no o si {@code oldId} es {@code null}. */
    <T> Optional<T> find(String namespace, Object oldId);

    boolean contains(String namespace, Object oldId);

    /** Cantidad de pares registrados en el namespace. */
    int size(String namespace);
}
