package co.unicauca.edu.co.contables.audit.application.internal.query;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.AuditDateType;
import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;

import lombok.Getter;

// application/internal/AuditDocumentExportCriteria.java
@Getter
public class AuditDocumentExportCriteria {

    private final String enterpriseId;
    private final Instant dateFrom;
    private final Instant dateTo;
    private final AuditDateType dateType;
    private final String documentCode;
    private final String documentType;
    private final String thirdPartyName;
    private final DocumentOperationType operationType;
    private final String userName;

    private AuditDocumentExportCriteria(String enterpriseId, Instant dateFrom, Instant dateTo,
            AuditDateType dateType, String documentCode, String documentType, String thirdPartyName,
            DocumentOperationType operationType, String userName) {
        this.enterpriseId = enterpriseId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.dateType = dateType != null ? dateType : AuditDateType.OPERATION_DATE;
        this.documentCode = documentCode;
        this.documentType = documentType;
        this.thirdPartyName = thirdPartyName;
        this.operationType = operationType;
        this.userName = userName;
    }

    public static AuditDocumentExportCriteria create(String enterpriseId, Instant dateFrom,
            Instant dateTo, AuditDateType dateType, String documentCode, String documentType, String thirdPartyName,
            DocumentOperationType operationType, String userName) {
        if (enterpriseId == null || enterpriseId.isBlank())
            throw new IllegalArgumentException("enterpriseId requerido");
        return new AuditDocumentExportCriteria(enterpriseId, dateFrom, dateTo,
                dateType, documentCode, documentType, thirdPartyName, operationType, userName);
    }

    public boolean hasDateRange() {
        return dateFrom != null || dateTo != null;
    }

    public boolean hasDocumentCode() {
        return documentCode != null && !documentCode.isBlank();
    }

    public boolean hasDocumentType() {
        return documentType != null && !documentType.isBlank();
    }

    public boolean hasThirdPartyName() {
        return thirdPartyName != null && !thirdPartyName.isBlank();
    }

    public boolean hasOperationType() {
        return operationType != null;
    }

    public boolean hasUserName() {
        return userName != null && !userName.isBlank();
    }
}