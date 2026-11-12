package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Datos vigentes del producto en el catalogo: precio de referencia e IVA de catalogo (D7, D8).
 * {@code catalogTaxes} es el desglose (nombre, tarifa) de los impuestos de la categoria cuya suma es
 * {@code catalogTaxRate}; vacio cuando el IVA sale del valor por defecto (sin categoria/impuestos).
 */
public record ProductSnapshot(
        long productId,
        String code,
        String name,
        Long unitOfMeasureId,
        String unitOfMeasureName,
        Money catalogUnitPrice,
        TaxRate catalogTaxRate,
        boolean active,
        List<TaxComponent> catalogTaxes) {

    public ProductSnapshot {
        Objects.requireNonNull(catalogUnitPrice, "catalogUnitPrice");
        Objects.requireNonNull(catalogTaxRate, "catalogTaxRate");
        catalogTaxes = TaxComponent.copyOf(catalogTaxes);
    }

    /** Snapshot sin desglose (IVA por defecto o editado a mano). */
    public ProductSnapshot(long productId, String code, String name, Long unitOfMeasureId,
            String unitOfMeasureName, Money catalogUnitPrice, TaxRate catalogTaxRate, boolean active) {
        this(productId, code, name, unitOfMeasureId, unitOfMeasureName, catalogUnitPrice, catalogTaxRate, active,
                List.of());
    }
}
