package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.math.BigDecimal;

/**
 * Un impuesto del desglose de una linea: nombre del maestro ("Iva") y tarifa en porcentaje
 * (escala 2). Solo presentacion: {@code ivaPct} de la linea es la suma y es lo que se calcula.
 */
public record BillingLineTaxResponse(String name, BigDecimal rate) {
}
