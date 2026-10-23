package co.unicauca.edu.co.contables.billing.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Estado completo de un BillingDocument, para reconstruirlo desde persistencia ({@code restore})
 * y para comparar estados. Los totales no se guardan aqui: se derivan de las lineas.
 */
public record BillingDocumentState(
        UUID id,
        String enterpriseId,
        DocumentType type,
        DocumentStatus status,
        DocumentNumber number,
        LocalDate issueDate,
        CounterpartySnapshot counterparty,
        String supplierInvoiceNumber,
        IssuerSnapshot issuer,
        OriginalDocumentRef original,
        String returnReason,
        List<DocumentLine> lines,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        Instant emittedAt,
        String emittedBy,
        Instant annulledAt,
        String annulledBy,
        String annulReason,
        Long version) {

    public BillingDocumentState {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /** Copia con otra version (la asigna la persistencia). */
    public BillingDocumentState withVersion(Long newVersion) {
        return new BillingDocumentState(id, enterpriseId, type, status, number, issueDate, counterparty,
                supplierInvoiceNumber, issuer, original, returnReason, lines, createdAt, createdBy, updatedAt,
                updatedBy, emittedAt, emittedBy, annulledAt, annulledBy, annulReason, newVersion);
    }
}
