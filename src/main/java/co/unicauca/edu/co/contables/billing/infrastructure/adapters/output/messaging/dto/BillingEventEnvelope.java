package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.dto;

import java.time.Instant;

/**
 * Sobre comun de todos los eventos de facturacion (EVT-3). {@code correlationId} es el id del
 * documento; {@code eventId} es la llave de idempotencia (= messageId AMQP).
 */
public record BillingEventEnvelope(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String tenantId,
        String enterpriseId,
        String correlationId,
        SourceDocument sourceDocument,
        BillingDocumentEventPayload payload) {

    /** Documento origen: tipo, id y numero visible (nulo si aun no tiene). */
    public record SourceDocument(String type, String id, String number) {
    }
}
