package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Acceso al catalogo de productos (precio de referencia e IVA vigentes, D8). */
public interface ProductPort {

    /** Snapshot del producto, o vacio si no existe en la empresa. Incluye productos inactivos. */
    Optional<ProductSnapshot> findProduct(String enterpriseId, long productId, DocumentKind kind);

    /**
     * Snapshots de varios productos en una sola operacion (los inexistentes se omiten; se conserva
     * el orden de {@code productIds}). Los adaptadores pueden sobrescribirlo para compartir consultas
     * auxiliares (p. ej. impuestos) entre los productos mientras dura la operacion; por defecto
     * consulta uno a uno con {@link #findProduct}.
     */
    default Map<Long, ProductSnapshot> findProducts(String enterpriseId, Collection<Long> productIds,
            DocumentKind kind) {
        Map<Long, ProductSnapshot> found = new LinkedHashMap<>();
        for (Long productId : productIds) {
            findProduct(enterpriseId, productId, kind).ifPresent(snapshot -> found.put(productId, snapshot));
        }
        return found;
    }
}
