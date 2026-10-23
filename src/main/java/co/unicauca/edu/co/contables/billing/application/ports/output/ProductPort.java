package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import java.util.Optional;

/** Acceso al catalogo de productos (precio de referencia e IVA vigentes, D8). */
public interface ProductPort {

    /** Snapshot del producto, o vacio si no existe en la empresa. Incluye productos inactivos. */
    Optional<ProductSnapshot> findProduct(String enterpriseId, long productId, DocumentKind kind);
}
