package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Factura completa. Ids como string; {@code number} es el consecutivo con prefijo (FV-000123) o
 * nulo en borrador; {@code kind} es SALE o PURCHASE; {@code returnStatus} es derivado y puede ser
 * nulo.
 */
public record BillingInvoiceResponse(
        String id,
        String number,
        String status,
        String kind,
        String documentType,
        String clientId,
        String clientName,
        String clientDocument,
        String supplierInvoiceNumber,
        LocalDate date,
        List<BillingInvoiceLineResponse> lines,
        BillingTotalsResponse totals,
        String returnStatus,
        Instant createdAt,
        Instant updatedAt,
        Instant emittedAt,
        Instant annulledAt,
        String annulReason,
        Long version) {
}
