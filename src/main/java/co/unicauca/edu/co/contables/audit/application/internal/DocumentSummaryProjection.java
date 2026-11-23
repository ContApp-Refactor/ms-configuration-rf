package co.unicauca.edu.co.contables.audit.application.internal;

import java.time.Instant;
import java.time.LocalDate;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;
import lombok.Getter;

@Getter
public class DocumentSummaryProjection {

    private final String documentCode;
    private final String documentType;
    private final String thirdPartyName;
    private final LocalDate documentDate;
    private final String createdBy;
    private final String lastModifiedBy;
    private final Instant lastModifiedAt;

    private DocumentSummaryProjection(String documentCode, String documentType, String thirdPartyName,
            LocalDate documentDate, String createdBy, String lastModifiedBy,
            Instant lastModifiedAt) {
        this.documentCode = documentCode;
        this.documentType = documentType;
        this.thirdPartyName = thirdPartyName;
        this.documentDate = documentDate;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedAt = lastModifiedAt;
    }

    public static DocumentSummaryProjection of(String documentCode, String documentType, String thirdPartyName,
            LocalDate documentDate, String createdBy, String lastModifiedBy,
            Instant lastModifiedAt) {
        if (documentCode == null || documentCode.isBlank())
            throw new InvalidAuditEventException("Document code required");
        if (documentType == null || documentType.isBlank())
            throw new InvalidAuditEventException("Document type required");
        if (documentDate == null)
            throw new InvalidAuditEventException("Document date required");
        if (createdBy == null || createdBy.isBlank())
            throw new InvalidAuditEventException("Created by required");
        if (lastModifiedBy == null || lastModifiedBy.isBlank())
            throw new InvalidAuditEventException("Last modified by required");
        if (lastModifiedAt == null)
            throw new InvalidAuditEventException("Last modified at required");

        return new DocumentSummaryProjection(documentCode, documentType, thirdPartyName,
                documentDate, createdBy, lastModifiedBy, lastModifiedAt);
    }
}
