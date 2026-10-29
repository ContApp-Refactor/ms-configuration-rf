package co.unicauca.edu.co.contables.billing.domain.event;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Un documento paso a EMITIDA. */
public record BillingDocumentEmitted(UUID eventId, Instant occurredAt, BillingDocument document,
        ReturnEventContext returnContext) implements BillingDomainEvent {

    public BillingDocumentEmitted {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(document, "document");
    }
}
