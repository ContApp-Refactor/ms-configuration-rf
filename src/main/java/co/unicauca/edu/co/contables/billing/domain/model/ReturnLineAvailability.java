package co.unicauca.edu.co.contables.billing.domain.model;

import java.math.BigDecimal;

/**
 * Disponibilidad de devolucion de una linea de la factura original (RET-3, RET-9). Las cantidades
 * son {@link BigDecimal} de escala 3 porque {@code returned} y {@code available} pueden ser cero
 * (el tipo {@link Quantity} exige estrictamente mayor que cero). Precio e IVA son los efectivos y
 * congelados de la linea original, los que heredara la devolucion.
 */
public record ReturnLineAvailability(
        int lineNo,
        long productId,
        String productName,
        BigDecimal sold,
        BigDecimal returned,
        BigDecimal available,
        Money unitPrice,
        TaxRate taxRate) {
}
