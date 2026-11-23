package co.unicauca.edu.co.contables.commons.backup;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

/**
 * @brief Utilidades JPA compartidas por los participantes de la copia que escriben
 * y leen DIRECTO por {@link EntityManager} (sin servicios de aplicación, sin eventos).
 *
 * No conoce ninguna entidad de negocio: trabaja con {@code Class} y con nombres de
 * entidad JPA en texto, de modo que un participante puede referenciar una FK de
 * otro módulo sin importar sus clases.
 */
public final class BackupJpa {

    /** Tamaño de página/lote de export y restore. */
    public static final int BATCH = 500;

    private BackupJpa() {
    }

    /**
     * Recorre las entidades {@code type} que cumplen {@code where} por páginas de
     * id ascendente (keyset): cada página es una consulta con {@code maxResults},
     * se transforma a registros con {@code pageMapper} (que puede navegar relaciones
     * LAZY, la transacción de export sigue abierta) y las entidades se desacoplan del
     * contexto para no acumular memoria.
     *
     * @param idAttr  atributo JPA de la clave (p. ej. {@code "id"})
     * @param where   condición JPQL sobre el alias {@code e} (p. ej. {@code "e.enterpriseId = :ent"})
     * @param idOf    extrae la clave de la entidad para avanzar el cursor
     */
    public static <E, R> Iterator<R> keyset(EntityManager em, Class<E> type, String idAttr, String where,
            Map<String, Object> params, Function<E, Long> idOf, Function<List<E>, List<R>> pageMapper) {
        String entityName = em.getMetamodel().entity(type).getName();
        String jpql = "select e from " + entityName + " e where (" + where + ") and e." + idAttr
                + " > :lastId order by e." + idAttr;
        return new KeysetIterator<>(em, type, jpql, params, idOf, pageMapper);
    }

    /** Como {@link #keyset} pero con un mapeo 1:1 entidad → registro. */
    public static <E, R> Iterator<R> keysetOf(EntityManager em, Class<E> type, String idAttr, String where,
            Map<String, Object> params, Function<E, Long> idOf, Function<E, R> mapper) {
        return keyset(em, type, idAttr, where, params, idOf,
                page -> page.stream().map(mapper).toList());
    }

    /** {@code select count(e) from X e where (where)}. */
    public static long count(EntityManager em, Class<?> type, String where, Map<String, Object> params) {
        String entityName = em.getMetamodel().entity(type).getName();
        return countJpql(em, "select count(e) from " + entityName + " e where (" + where + ")", params);
    }

    /** Ejecuta un JPQL de conteo (huérfanos, joins). */
    public static long countJpql(EntityManager em, String jpql, Map<String, Object> params) {
        TypedQuery<Long> q = em.createQuery(jpql, Long.class);
        params.forEach(q::setParameter);
        Long n = q.getSingleResult();
        return n == null ? 0L : n;
    }

    /** Conteo SQL nativo (sin filtro de tenant): para uniques globales. */
    public static long countNative(EntityManager em, String sql, Object... positional) {
        Query q = em.createNativeQuery(sql);
        for (int i = 0; i < positional.length; i++) {
            q.setParameter(i + 1, positional[i]);
        }
        Object n = q.getSingleResult();
        return n == null ? 0L : ((Number) n).longValue();
    }

    /**
     * Referencia perezosa a una entidad de OTRO módulo por su nombre JPA
     * ({@code "AccountCatalogueEntity"}), sin importar su clase.
     */
    @SuppressWarnings("unchecked")
    public static <T> T reference(EntityManager em, String entityName, Object id) {
        Class<?> type = em.getMetamodel().getEntities().stream()
                .filter(e -> e.getName().equals(entityName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Entidad JPA desconocida: " + entityName))
                .getJavaType();
        return (T) em.getReference(type, id);
    }

    public static void flushAndClear(EntityManager em) {
        em.flush();
        em.clear();
    }

    private static final class KeysetIterator<E, R> implements Iterator<R> {
        private final EntityManager em;
        private final Class<E> type;
        private final String jpql;
        private final Map<String, Object> params;
        private final Function<E, Long> idOf;
        private final Function<List<E>, List<R>> pageMapper;
        private long lastId = 0L;
        private Iterator<R> page = List.<R>of().iterator();
        private boolean exhausted;

        KeysetIterator(EntityManager em, Class<E> type, String jpql, Map<String, Object> params,
                Function<E, Long> idOf, Function<List<E>, List<R>> pageMapper) {
            this.em = em;
            this.type = type;
            this.jpql = jpql;
            this.params = params;
            this.idOf = idOf;
            this.pageMapper = pageMapper;
        }

        @Override
        public boolean hasNext() {
            while (!page.hasNext()) {
                if (exhausted) {
                    return false;
                }
                TypedQuery<E> q = em.createQuery(jpql, type);
                params.forEach(q::setParameter);
                q.setParameter("lastId", lastId);
                q.setMaxResults(BATCH);
                List<E> next = q.getResultList();
                if (next.size() < BATCH) {
                    exhausted = true;
                }
                if (next.isEmpty()) {
                    return false;
                }
                lastId = idOf.apply(next.get(next.size() - 1));
                List<R> rows = pageMapper.apply(next);
                next.forEach(em::detach);
                page = rows.iterator();
            }
            return true;
        }

        @Override
        public R next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return page.next();
        }
    }
}
