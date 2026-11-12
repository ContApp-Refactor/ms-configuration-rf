package co.unicauca.edu.co.contables.accounting.backup.support;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * @brief Iterador paginado por id ascendente (keyset) para exportar tablas grandes sin cargarlas completas.
 *
 * El cargador recibe {@code (afterId, batchSize)} y devuelve la página de entidades con id mayor a
 * {@code afterId}, ya mapeada a filas por el {@code mapper} (que puede leer colecciones hijas mientras la
 * transacción está abierta). Entre páginas no se retienen entidades.
 */
public final class KeysetPager<T, R> implements Iterator<R> {

    @FunctionalInterface
    public interface PageLoader<T> {
        List<T> load(long afterId, int batchSize);
    }

    private final int batchSize;
    private final PageLoader<T> loader;
    private final ToLongFunction<T> idOf;
    private final Function<List<T>, List<R>> mapper;

    private long lastId = 0L;
    private Iterator<R> current = List.<R>of().iterator();
    private boolean exhausted;

    private KeysetPager(int batchSize, PageLoader<T> loader, ToLongFunction<T> idOf,
            Function<List<T>, List<R>> mapper) {
        this.batchSize = batchSize;
        this.loader = loader;
        this.idOf = idOf;
        this.mapper = mapper;
    }

    public static <T, R> KeysetPager<T, R> of(int batchSize, PageLoader<T> loader, ToLongFunction<T> idOf,
            Function<List<T>, List<R>> mapper) {
        return new KeysetPager<>(batchSize, loader, idOf, mapper);
    }

    @Override
    public boolean hasNext() {
        while (!current.hasNext()) {
            if (exhausted) {
                return false;
            }
            List<T> page = loader.load(lastId, batchSize);
            if (page.size() < batchSize) {
                exhausted = true;
            }
            if (page.isEmpty()) {
                return false;
            }
            lastId = idOf.applyAsLong(page.get(page.size() - 1));
            current = mapper.apply(page).iterator();
        }
        return true;
    }

    @Override
    public R next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current.next();
    }
}
