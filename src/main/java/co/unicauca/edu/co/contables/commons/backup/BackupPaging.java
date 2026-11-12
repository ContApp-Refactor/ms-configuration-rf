package co.unicauca.edu.co.contables.commons.backup;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

/**
 * @brief Lectura por páginas para {@code export}: convierte "una consulta paginada"
 * en un {@link Iterator} perezoso que {@link BackupWriter#writeNdjson} consume sin
 * acumular toda la tabla en memoria.
 *
 * Cada página se convierte COMPLETA a registros del ZIP antes de ejecutar
 * {@code afterPage} (típicamente {@code entityManager::clear}), así las entidades
 * leídas no se acumulan en el contexto de persistencia.
 */
public final class BackupPaging {

    /** Tamaño de página/lote de la copia (igual al de {@code saveAll} en el restore). */
    public static final int PAGE_SIZE = 500;

    private BackupPaging() {
    }

    /** Página {@code n} ordenada por {@code id}: orden estable para recorrer sin repetir ni saltar filas. */
    public static Pageable page(int n) {
        return PageRequest.of(n, PAGE_SIZE, Sort.by("id"));
    }

    /**
     * @param fetch     devuelve la página {@code n} (empezando en 0)
     * @param mapper    entidad → registro del ZIP
     * @param afterPage se ejecuta tras mapear cada página (puede ser {@code null})
     */
    public static <T, R> Iterator<R> iterate(IntFunction<Slice<T>> fetch, Function<T, R> mapper,
            Runnable afterPage) {
        return new Iterator<>() {
            private int page = 0;
            private boolean more = true;
            private Iterator<R> buffer = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!buffer.hasNext() && more) {
                    Slice<T> slice = fetch.apply(page++);
                    List<R> rows = slice.getContent().stream().map(mapper).toList();
                    more = slice.hasNext();
                    buffer = rows.iterator();
                    if (afterPage != null) {
                        afterPage.run();
                    }
                }
                return buffer.hasNext();
            }

            @Override
            public R next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return buffer.next();
            }
        };
    }
}
