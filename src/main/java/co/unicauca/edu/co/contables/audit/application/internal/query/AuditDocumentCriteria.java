package co.unicauca.edu.co.contables.audit.application.internal.query;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.AuditDateType;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditCriteriaException;
import lombok.Getter;

@Getter
public class AuditDocumentCriteria {

    private final Instant dateFrom;
    private final Instant dateTo;
    private final AuditDateType dateType;
    private final String documentType;
    private final String documentCode;
    private final String enterpriseId;
    private final String thirdPartyName;
    private final String createdBy;

    private AuditDocumentCriteria(
            Instant dateFrom,
            Instant dateTo,
            AuditDateType dateType,
            String documentType,
            String documentCode,
            String enterpriseId,
            String thirdPartyName,
            String createdBy) {

        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.dateType = dateType != null ? dateType : AuditDateType.OPERATION_DATE;
        this.documentType = documentType;
        this.documentCode = documentCode;
        this.enterpriseId = enterpriseId;
        this.thirdPartyName = thirdPartyName;
        this.createdBy = createdBy;
    }

    public static AuditDocumentCriteria create(
            Instant dateFrom,
            Instant dateTo,
            AuditDateType dateType,
            String documentType,
            String documentCode,
            String enterpriseId,
            String thirdPartyName,
            String createdBy) {

        validateDateRange(dateFrom, dateTo);

        return new AuditDocumentCriteria(
                dateFrom,
                dateTo,
                dateType,
                documentType,
                documentCode,
                enterpriseId,
                thirdPartyName,
                createdBy);
    }

    private static void validateDateRange(Instant dateFrom, Instant dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new InvalidAuditCriteriaException(" 'dateFrom' cannot be after 'dateTo'.");
        }
    }

    public boolean hasDateRange() {
        return dateFrom != null || dateTo != null;
    }

    public boolean hasDocumentType() {
        return documentType != null && !documentType.isBlank();
    }

    public boolean hasThirdPartyName() {
        return thirdPartyName != null && !thirdPartyName.isBlank();
    }

    public boolean hasCreatedBy() {
        return createdBy != null && !createdBy.isBlank();
    }

    public boolean hasDocumentCode() {
        return documentCode != null && !documentCode.isBlank();
    }

    public boolean hasEnterpriseId() {
        return enterpriseId != null && !enterpriseId.isBlank();
    }
}
