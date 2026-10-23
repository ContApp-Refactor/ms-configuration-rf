package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.Objects;

/** Datos vigentes del producto en el catalogo: precio de referencia e IVA de catalogo (D7, D8). */
public record ProductSnapshot(
        long productId,
        String code,
        String name,
        Long unitOfMeasureId,
        String unitOfMeasureName,
        Money catalogUnitPrice,
        TaxRate catalogTaxRate,
        boolean active) {

    public ProductSnapshot {
        Objects.requireNonNull(catalogUnitPrice, "catalogUnitPrice");
        Objects.requireNonNull(catalogTaxRate, "catalogTaxRate");
    }
}
