package co.unicauca.edu.co.contables.billing.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Disponibilidad de devolucion de una linea de la factura original (RET-3, RET-9). Las cantidades
 * son {@link BigDecimal} de escala 3 porque {@code returned} y {@code available} pueden ser cero
 * (el tipo {@link Quantity} exige estrictamente mayor que cero). Precio e IVA son los efectivos y
 * congelados de la linea original, los que heredara la devolucion; {@code taxes} es su desglose
 * efectivo ({@link DocumentLine#taxes()}), vacio si la original no lo tiene.
 */
public record ReturnLineAvailability(
        int lineNo,
        long productId,
        String productName,
        BigDecimal sold,
        BigDecimal returned,
        BigDecimal available,
        Money unitPrice,
        TaxRate taxRate,
        List<TaxComponent> taxes) {

    public ReturnLineAvailability {
        taxes = TaxComponent.copyOf(taxes);
    }

    /** Disponibilidad sin desglose de impuestos. */
    public ReturnLineAvailability(int lineNo, long productId, String productName, BigDecimal sold,
            BigDecimal returned, BigDecimal available, Money unitPrice, TaxRate taxRate) {
        this(lineNo, productId, productName, sold, returned, available, unitPrice, taxRate, List.of());
    }
}
