package co.unicauca.edu.co.contables.billing.application.ports.output;

import co.unicauca.edu.co.contables.billing.application.ports.output.model.OutboxClaim;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Lado "relay" del outbox. {@link #findDueClaims} se ejecuta SIN tenant y ve todos los tenants;
 * el resto se ejecuta dentro de una transaccion nueva con el tenant de la fila ya fijado.
 */
public interface BillingOutboxRelayPort {

    /** Filas PENDING vencidas de cualquier tenant, mas antiguas primero, hasta {@code limit}. */
    List<OutboxClaim> findDueClaims(Instant now, int limit);

    /**
     * Bloquea la fila (escritura) y la devuelve solo si sigue PENDING y vencida; vacio si otra
     * instancia ya la publico o si pertenece a otro tenant.
     */
    Optional<PendingOutboxMessage> lockPending(long id);

    void markPublished(long id, Instant at);

    /**
     * Registra un intento fallido: guarda el contador, la proxima fecha y el error; si
     * {@code exhausted} la fila queda FAILED (definitivo), si no sigue PENDING.
     */
    void markAttemptFailed(long id, int attempts, Instant nextAttemptAt, boolean exhausted, String error);
}
