package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Devolucion completa (NC / DC). {@code invoiceId}, {@code originalNumber} y {@code originalDate}
 * referencian a la factura original; {@code ordinal} (1, 2, 3...) es derivado y solo se informa en
 * la consulta de una devolucion emitida o anulada (nulo en borrador).
 */
public record BillingReturnResponse(
        String id,
        String number,
        String status,
        String kind,
        String documentType,
        String invoiceId,
        String originalNumber,
        LocalDate originalDate,
        String clientId,
        String clientName,
        String clientDocument,
        LocalDate date,
        String reason,
        Integer ordinal,
        List<BillingReturnLineResponse> lines,
        BillingTotalsResponse totals,
        Instant createdAt,
        Instant updatedAt,
        Instant emittedAt,
        Instant annulledAt,
        String annulReason,
        Long version) {
}
