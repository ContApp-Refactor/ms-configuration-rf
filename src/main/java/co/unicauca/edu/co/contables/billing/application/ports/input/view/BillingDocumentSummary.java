package co.unicauca.edu.co.contables.billing.application.ports.input.view;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentTotals;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Resumen de un documento para listados (sin lineas). Los totales pueden ser los ultimos guardados. */
public record BillingDocumentSummary(
        UUID id,
        DocumentType type,
        DocumentStatus status,
        DocumentNumber number,
        LocalDate issueDate,
        Long counterpartyId,
        String counterpartyName,
        String supplierInvoiceNumber,
        UUID originalDocumentId,
        DocumentNumber originalNumber,
        DocumentTotals totals,
        Instant createdAt,
        Instant updatedAt,
        Instant emittedAt,
        Instant annulledAt,
        Long version) {

    public static BillingDocumentSummary from(BillingDocument doc) {
        return new BillingDocumentSummary(
                doc.id(),
                doc.type(),
                doc.status(),
                doc.number(),
                doc.issueDate(),
                doc.counterparty() == null ? null : doc.counterparty().thirdId(),
                doc.counterparty() == null ? null : doc.counterparty().name(),
                doc.supplierInvoiceNumber(),
                doc.original() == null ? null : doc.original().id(),
                doc.original() == null ? null : doc.original().number(),
                doc.totals(),
                doc.createdAt(),
                doc.updatedAt(),
                doc.emittedAt(),
                doc.annulledAt(),
                doc.version());
    }
}
