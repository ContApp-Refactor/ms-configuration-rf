package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.math.BigDecimal;

/** Vendido / devuelto / disponible de una linea de la factura y el precio e IVA que heredara la devolucion. */
public record ReturnAvailabilityLineResponse(
        int lineNo,
        String productId,
        String productName,
        BigDecimal sold,
        BigDecimal returned,
        BigDecimal available,
        long precioUnitarioCents,
        BigDecimal ivaPct) {
}
