package co.unicauca.edu.co.contables.billing.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/** Referencia congelada a la factura original de una devolucion. */
public record OriginalDocumentRef(UUID id, DocumentType type, DocumentNumber number, LocalDate issueDate) {
}
