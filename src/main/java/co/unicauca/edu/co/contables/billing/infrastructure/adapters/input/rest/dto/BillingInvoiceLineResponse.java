package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.math.BigDecimal;

/** Linea de factura: referencias de catalogo, valores efectivos y derivados, en centavos. */
public record BillingInvoiceLineResponse(
        int lineNo,
        String productId,
        String productCode,
        String productName,
        String unitOfMeasure,
        BigDecimal cantidad,
        long precioCatalogoCents,
        BigDecimal ivaCatalogoPct,
        long precioUnitarioCents,
        BigDecimal ivaPct,
        boolean precioEditado,
        boolean ivaEditado,
        long subtotalCents,
        long ivaCents,
        long totalCents) {
}
