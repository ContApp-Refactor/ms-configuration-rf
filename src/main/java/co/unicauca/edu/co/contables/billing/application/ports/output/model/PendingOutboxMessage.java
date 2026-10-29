package co.unicauca.edu.co.contables.billing.application.ports.output.model;

import java.time.Instant;

/**
 * Mensaje del outbox ya bloqueado para publicar. {@code attempts} es el contador ANTES de este
 * intento; {@code payloadJson} es el sobre serializado tal como se guardo al emitir/anular.
 */
public record PendingOutboxMessage(long id, String eventId, String eventType, String tenantId, String exchange,
        String routingKey, String payloadJson, int attempts, Instant nextAttemptAt) {
}
