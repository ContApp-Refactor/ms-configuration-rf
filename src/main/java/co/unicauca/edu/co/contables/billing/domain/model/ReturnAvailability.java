package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Disponibilidad de devolucion de una factura: una entrada por linea, estado derivado (nunca
 * almacenado, RET-9) y si admite nuevas devoluciones.
 */
public record ReturnAvailability(UUID invoiceId, List<ReturnLineAvailability> lines, ReturnStatus status,
        boolean returnable) {

    public ReturnAvailability {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
