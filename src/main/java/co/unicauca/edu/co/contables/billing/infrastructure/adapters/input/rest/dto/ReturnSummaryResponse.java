package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.time.Instant;
import java.time.LocalDate;

/** Resumen de una devolucion para listados (sin lineas); {@code ordinal} es nulo si no se conoce o es borrador. */
public record ReturnSummaryResponse(
        String id,
        String number,
        String status,
        String kind,
        String documentType,
        String invoiceId,
        String originalNumber,
        Integer ordinal,
        String clientId,
        String clientName,
        LocalDate date,
        BillingTotalsResponse totals,
        Instant createdAt,
        Instant updatedAt,
        Instant emittedAt,
        Instant annulledAt,
        Long version) {
}
