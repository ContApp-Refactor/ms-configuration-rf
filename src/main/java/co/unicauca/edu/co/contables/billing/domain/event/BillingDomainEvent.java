package co.unicauca.edu.co.contables.billing.domain.event;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import java.time.Instant;
import java.util.UUID;

/** Evento de dominio que se agrega al outbox en la misma transaccion que el documento. */
public sealed interface BillingDomainEvent permits BillingDocumentEmitted, BillingDocumentAnnulled {

    /** Clave de idempotencia del evento (= messageId AMQP). */
    UUID eventId();

    Instant occurredAt();

    BillingDocument document();

    /** Contexto de devolucion; {@code null} para facturas. */
    ReturnEventContext returnContext();
}
