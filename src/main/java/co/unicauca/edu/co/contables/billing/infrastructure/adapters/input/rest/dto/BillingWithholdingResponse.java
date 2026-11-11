package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.math.BigDecimal;

/**
 * Retencion de una factura. {@code code}: RETE_IVA, RETE_ICA, RETE_FUENTE o AUTO_RENTA; {@code taxId}
 * (string, como los demas ids) es el impuesto de la empresa elegido; {@code ratePct} su tarifa en
 * porcentaje y {@code amountCents} lo que resta del total (tarifa x subtotal completo). En borrador
 * salen con la tarifa vigente; en una factura emitida es el snapshot congelado.
 */
public record BillingWithholdingResponse(
        String code,
        String taxId,
        String taxName,
        BigDecimal ratePct,
        long amountCents) {
}
