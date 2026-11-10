package co.unicauca.edu.co.contables.billing.domain.model;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.util.List;
import java.util.Objects;

/**
 * Linea inmutable de un documento (D7). Guarda la referencia de catalogo vigente y los overrides
 * manuales opcionales; el valor efectivo es {@code override ?? catalogo}, calculado por separado
 * para precio e IVA (RN-10, RN-12). Los importes derivados se calculan, no se almacenan (RN-14).
 *
 * <p>{@code catalogTaxes} es el desglose (nombre, tarifa) de los impuestos de la categoria que
 * suman {@code catalogTaxRate}; viaja con la referencia de catalogo (se refresca en borrador y queda
 * congelado al emitir). Es solo presentacion: el calculo usa la suma. Vacio en documentos viejos o
 * cuando el IVA salio del valor por defecto.
 */
public record DocumentLine(
        int lineNo,
        Integer sourceLineNo,
        long productId,
        String productCode,
        String productName,
        Long unitOfMeasureId,
        String unitOfMeasureName,
        Quantity quantity,
        Money catalogUnitPrice,
        TaxRate catalogTaxRate,
        Money unitPriceOverride,
        TaxRate taxRateOverride,
        List<TaxComponent> catalogTaxes) {

    public DocumentLine {
        catalogTaxes = TaxComponent.copyOf(catalogTaxes);
        if (lineNo < 1) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                    "El numero de item debe ser mayor o igual a 1");
        }
        Objects.requireNonNull(quantity, "quantity");
        Objects.requireNonNull(catalogUnitPrice, "catalogUnitPrice");
        Objects.requireNonNull(catalogTaxRate, "catalogTaxRate");
        Money effectivePrice = unitPriceOverride != null ? unitPriceOverride : catalogUnitPrice;
        if (effectivePrice.isNegative()) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                    "El precio unitario no puede ser negativo (producto " + productId + ")");
        }
    }

    /** Linea sin desglose de impuestos (documentos previos al desglose o IVA por defecto). */
    public DocumentLine(int lineNo, Integer sourceLineNo, long productId, String productCode, String productName,
            Long unitOfMeasureId, String unitOfMeasureName, Quantity quantity, Money catalogUnitPrice,
            TaxRate catalogTaxRate, Money unitPriceOverride, TaxRate taxRateOverride) {
        this(lineNo, sourceLineNo, productId, productCode, productName, unitOfMeasureId, unitOfMeasureName,
                quantity, catalogUnitPrice, catalogTaxRate, unitPriceOverride, taxRateOverride, List.of());
    }

    /** Linea de factura (venta o compra) a partir del catalogo y overrides opcionales. */
    public static DocumentLine forInvoice(int lineNo, ProductSnapshot product, Quantity quantity,
            Money unitPriceOverride, TaxRate taxRateOverride) {
        Objects.requireNonNull(product, "product");
        return new DocumentLine(lineNo, null, product.productId(), product.code(), product.name(),
                product.unitOfMeasureId(), product.unitOfMeasureName(), quantity,
                product.catalogUnitPrice(), product.catalogTaxRate(), unitPriceOverride, taxRateOverride,
                product.catalogTaxes());
    }

    /**
     * Linea de devolucion heredada de una linea de la factura original (RET-2, RN-19): la
     * referencia de catalogo es el precio e IVA efectivos de la original, sin overrides, de modo
     * que el valor devuelto queda congelado. Tambien hereda el desglose efectivo de la original
     * ({@link #taxes()}: vacio si la original tenia IVA editado a mano). La cantidad no puede
     * superar la vendida en esa linea; el control contra lo ya devuelto lo hace {@code ReturnAvailabilityCalculator}.
     */
    public static DocumentLine forReturn(int lineNo, DocumentLine source, Quantity quantity) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(quantity, "quantity");
        if (quantity.value().compareTo(source.quantity().value()) > 0) {
            throw new BillingException(BillingErrorCode.BILLING_RETURN_QUANTITY_EXCEEDED,
                    "La cantidad a devolver supera la vendida en el item " + source.lineNo());
        }
        return new DocumentLine(lineNo, source.lineNo(), source.productId(), source.productCode(),
                source.productName(), source.unitOfMeasureId(), source.unitOfMeasureName(), quantity,
                source.unitPrice(), source.taxRate(), null, null, source.taxes());
    }

    /** Reconstruccion desde persistencia (sin desglose: documentos previos al desglose). */
    public static DocumentLine restore(int lineNo, Integer sourceLineNo, long productId, String productCode,
            String productName, Long unitOfMeasureId, String unitOfMeasureName, Quantity quantity,
            Money catalogUnitPrice, TaxRate catalogTaxRate, Money unitPriceOverride, TaxRate taxRateOverride) {
        return restore(lineNo, sourceLineNo, productId, productCode, productName, unitOfMeasureId,
                unitOfMeasureName, quantity, catalogUnitPrice, catalogTaxRate, unitPriceOverride, taxRateOverride,
                List.of());
    }

    /** Reconstruccion desde persistencia con el desglose congelado. */
    public static DocumentLine restore(int lineNo, Integer sourceLineNo, long productId, String productCode,
            String productName, Long unitOfMeasureId, String unitOfMeasureName, Quantity quantity,
            Money catalogUnitPrice, TaxRate catalogTaxRate, Money unitPriceOverride, TaxRate taxRateOverride,
            List<TaxComponent> catalogTaxes) {
        return new DocumentLine(lineNo, sourceLineNo, productId, productCode, productName, unitOfMeasureId,
                unitOfMeasureName, quantity, catalogUnitPrice, catalogTaxRate, unitPriceOverride, taxRateOverride,
                catalogTaxes);
    }

    /** Refresca datos de producto y referencia de catalogo conservando cantidad y overrides. */
    public DocumentLine withCatalog(ProductSnapshot product) {
        Objects.requireNonNull(product, "product");
        if (product.productId() != productId) {
            throw new IllegalArgumentException("El snapshot es del producto " + product.productId()
                    + " y la linea es del producto " + productId);
        }
        return new DocumentLine(lineNo, sourceLineNo, productId, product.code(), product.name(),
                product.unitOfMeasureId(), product.unitOfMeasureName(), quantity,
                product.catalogUnitPrice(), product.catalogTaxRate(), unitPriceOverride, taxRateOverride,
                product.catalogTaxes());
    }

    public DocumentLine renumber(int newLineNo) {
        return new DocumentLine(newLineNo, sourceLineNo, productId, productCode, productName, unitOfMeasureId,
                unitOfMeasureName, quantity, catalogUnitPrice, catalogTaxRate, unitPriceOverride, taxRateOverride,
                catalogTaxes);
    }

    /** Precio efectivo: override si existe, si no el de catalogo. */
    public Money unitPrice() {
        return unitPriceOverride != null ? unitPriceOverride : catalogUnitPrice;
    }

    /** IVA efectivo: override si existe, si no el de catalogo. */
    public TaxRate taxRate() {
        return taxRateOverride != null ? taxRateOverride : catalogTaxRate;
    }

    /**
     * Desglose efectivo para mostrar: el de catalogo, salvo que el IVA este editado a mano (el
     * override se muestra como valor unico y el desglose ya no lo explica). Vacio = valor unico.
     */
    public List<TaxComponent> taxes() {
        return taxRateOverride != null ? List.of() : catalogTaxes;
    }

    public boolean priceOverridden() {
        return unitPriceOverride != null;
    }

    public boolean taxOverridden() {
        return taxRateOverride != null;
    }

    public Money subtotal() {
        return unitPrice().times(quantity);
    }

    public Money taxAmount() {
        return taxRate().applyTo(subtotal());
    }

    public Money total() {
        return subtotal().plus(taxAmount());
    }
}
