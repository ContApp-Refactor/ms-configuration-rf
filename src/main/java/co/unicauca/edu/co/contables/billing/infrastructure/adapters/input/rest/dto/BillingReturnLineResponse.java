package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.math.BigDecimal;

/**
 * Linea de devolucion: referencia a la linea de la factura original, cantidad devuelta y valores
 * heredados congelados, en centavos. La cantidad vendida de la original se consulta en
 * {@code /return-availability} de la factura.
 */
public record BillingReturnLineResponse(
        int lineNo,
        Integer sourceLineNo,
        String productId,
        String productCode,
        String productName,
        String unitOfMeasure,
        BigDecimal cantidadDevuelta,
        long precioUnitarioCents,
        BigDecimal ivaPct,
        long subtotalCents,
        long ivaCents,
        long totalCents) {
}
