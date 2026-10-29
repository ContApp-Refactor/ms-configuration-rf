package co.unicauca.edu.co.contables.billing.domain.event;

import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;

/**
 * Contexto adicional de eventos de devolucion: ordinal de la devolucion dentro de su factura
 * original y estado de devolucion de la factura despues del hecho. Nulo para facturas.
 */
public record ReturnEventContext(Integer ordinal, ReturnStatus originalStatusAfter) {
}
