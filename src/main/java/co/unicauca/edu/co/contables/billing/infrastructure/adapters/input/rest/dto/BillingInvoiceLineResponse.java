package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Linea de factura: referencias de catalogo, valores efectivos y derivados, en centavos.
 * {@code taxes} es el desglose congelado de impuestos (vacio = valor unico {@code ivaPct}: documento
 * viejo, IVA por defecto o editado a mano).
 */
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
        long totalCents,
        List<BillingLineTaxResponse> taxes) {
}
