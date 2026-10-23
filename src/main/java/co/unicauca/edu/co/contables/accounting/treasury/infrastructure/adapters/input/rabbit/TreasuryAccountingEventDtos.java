package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contrato de entrada del consumidor contable de tesoreria.
 *
 * <p>{@code OutboxEventAdapter} serializa cada evento con este envelope exacto
 * ({@code eventId, eventType, eventVersion, occurredAt, tenantId, enterpriseId,
 * correlationId, sourceDocument{type,id}, payload}). El {@code payload} es el JSON
 * crudo del dominio (comprobante o baja), por eso se recibe como {@link JsonNode}
 * y se convierte recien en el listener.</p>
 */
public final class TreasuryAccountingEventDtos {

    private TreasuryAccountingEventDtos() {}

    public record SourceDocument(String type, Long id) {}

    public record TreasuryAccountingEvent(
            String eventId,
            String eventType,
            int eventVersion,
            Instant occurredAt,
            String tenantId,
            String enterpriseId,
            String correlationId,
            SourceDocument sourceDocument,
            JsonNode payload) {}
}
