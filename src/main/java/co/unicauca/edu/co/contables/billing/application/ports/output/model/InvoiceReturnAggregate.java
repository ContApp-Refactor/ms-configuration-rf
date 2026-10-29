package co.unicauca.edu.co.contables.billing.application.ports.output.model;

import co.unicauca.edu.co.contables.billing.domain.model.Money;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Agregado, por factura original, de sus devoluciones EMITIDA: cuantas ({@code count}), suma de
 * totales ({@code total}) y suma de cantidades de linea ({@code quantity}).
 */
public record InvoiceReturnAggregate(UUID originalId, long count, Money total, BigDecimal quantity) {
}
