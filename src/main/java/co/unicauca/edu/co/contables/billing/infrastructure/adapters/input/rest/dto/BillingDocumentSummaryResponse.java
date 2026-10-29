package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.time.Instant;
import java.time.LocalDate;

/** Resumen para listados (sin lineas). Los totales pueden ser los ultimos guardados. */
public record BillingDocumentSummaryResponse(
        String id,
        String number,
        String status,
        String kind,
        String documentType,
        String clientId,
        String clientName,
        String supplierInvoiceNumber,
        LocalDate date,
        BillingTotalsResponse totals,
        Instant createdAt,
        Instant updatedAt,
        Instant emittedAt,
        Instant annulledAt,
        Long version) {
}
