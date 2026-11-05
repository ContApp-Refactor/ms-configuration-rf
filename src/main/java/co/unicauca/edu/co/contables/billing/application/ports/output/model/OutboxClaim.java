package co.unicauca.edu.co.contables.billing.application.ports.output.model;

import java.util.Objects;

/** Fila vencida del outbox reclamada por el relay: id y tenant dueno (consulta entre tenants). */
public record OutboxClaim(long id, String tenantId) {

    public OutboxClaim {
        Objects.requireNonNull(tenantId, "tenantId");
    }
}
