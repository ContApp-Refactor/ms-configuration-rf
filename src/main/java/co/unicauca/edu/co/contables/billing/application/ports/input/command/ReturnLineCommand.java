package co.unicauca.edu.co.contables.billing.application.ports.input.command;

import java.math.BigDecimal;

/**
 * Linea pedida de una devolucion: solo la referencia a la linea de la factura original y la
 * cantidad. El precio y el IVA se heredan congelados de la original (RET-2); cualquier otro dato
 * enviado por el cliente se ignora.
 */
public record ReturnLineCommand(int sourceLineNo, BigDecimal quantity) {
}
