package co.unicauca.edu.co.contables.billing.application.ports.input.command;

import java.math.BigDecimal;

/**
 * Linea solicitada de una factura. {@code unitPriceOverride} y {@code taxRateOverride} nulos
 * significan "seguir el catalogo" (DOC-3, RN-11). Los importes derivados nunca llegan del cliente.
 */
public record InvoiceLineCommand(
        long productId,
        BigDecimal quantity,
        BigDecimal unitPriceOverride,
        BigDecimal taxRateOverride) {
}
