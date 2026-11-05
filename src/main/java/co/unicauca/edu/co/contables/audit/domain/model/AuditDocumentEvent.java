package co.unicauca.edu.co.contables.audit.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;

import lombok.Getter;

@Getter
public class AuditDocumentEvent {

    // Attributes
    private final Long id;
    private final String enterpriseId;
    private final String documentId;
    private final String documentCode;
    private final String documentType;
    private final LocalDate documentDate; // accounting date
    private final String userId;
    private final String userName;
    private final List<String> userRoles;
    private final DocumentOperationType operationType;
    private final String thirdPartyId;
    private final String thirdPartyName;
    private final String moduleName;
    private final Instant operationAt; // operation date
    private final DocumentData documentData;
    private final Instant createdAt;

    // Constructor
    private AuditDocumentEvent(Long id, String enterpriseId, String documentCode, String documentType,
            String documentId,
            String userId, String userName, List<String> userRoles, DocumentOperationType operationType,
            String thirdPartyId, String thirdPartyName, String moduleName, Instant operationAt,
            LocalDate documentDate, DocumentData documentData, Instant createdAt) {
        this.id = id;
        this.enterpriseId = enterpriseId;
        this.documentCode = documentCode;
        this.documentType = documentType;
        this.documentId = documentId;
        this.userId = userId;
        this.userName = userName;
        this.userRoles = userRoles != null ? List.copyOf(userRoles) : Collections.emptyList();
        this.operationType = operationType;
        this.thirdPartyId = thirdPartyId;
        this.thirdPartyName = thirdPartyName;
        this.moduleName = moduleName;
        this.operationAt = operationAt;
        this.documentDate = documentDate;
        this.documentData = documentData;
        this.createdAt = createdAt;
    }

    // Factory method
    public static AuditDocumentEvent create(String enterpriseId, String documentCode, String documentType,
            String documentId,
            String userId, String userName, List<String> userRoles, DocumentOperationType operationType,
            String thirdPartyId, String thirdPartyName, String moduleName, Instant operationAt,
            LocalDate documentDate, DocumentData documentData) {
        validateEnterpriseId(enterpriseId);
        validateOperationType(operationType);
        validateModuleName(moduleName);
        validateUserData(userId, userName, userRoles);
        validateDocumentData(documentId, documentType, documentCode, documentDate, thirdPartyId,
                thirdPartyName, moduleName, documentData);
        validateOperationAt(operationAt);
        Instant now = Instant.now();
        return new AuditDocumentEvent(null, enterpriseId, documentCode, documentType,
                documentId, userId, userName,
                userRoles, operationType, thirdPartyId, thirdPartyName, moduleName, operationAt,
                documentDate, documentData, now);
    }

    // Reconstruct
    public static AuditDocumentEvent reconstruct(Long id, String enterpriseId, String documentCode,
            String documentType,
            String documentId,
            String userId, String userName, List<String> userRoles, DocumentOperationType operationType,
            String thirdPartyId, String thirdPartyName, String moduleName, Instant operationAt,
            LocalDate documentDate, DocumentData documentData, Instant createdAt) {
        return new AuditDocumentEvent(id, enterpriseId, documentCode, documentType, documentId, userId,
                userName,
                userRoles, operationType, thirdPartyId, thirdPartyName, moduleName, operationAt,
                documentDate, documentData, createdAt);
    }

    private static void validateEnterpriseId(String enterpriseId) {
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new InvalidAuditEventException("Enterprise ID cannot be null or empty");
        }
    }

    private static void validateModuleName(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            throw new InvalidAuditEventException("Module name cannot be null or empty");
        }
    }

    private static void validateOperationType(DocumentOperationType operationType) {
        if (operationType == null) {
            throw new InvalidAuditEventException("Operation type cannot be null");
        }
    }

    private static void validateUserData(String userId, String userName, List<String> userRoles) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidAuditEventException("User ID cannot be null or empty");
        }
        if (userName == null || userName.isBlank()) {
            throw new InvalidAuditEventException("User name cannot be null or empty");
        }
        if (userRoles == null || userRoles.isEmpty()) {
            throw new InvalidAuditEventException("User roles cannot be null or empty");
        }
        validateRoles(userRoles);
    }

    private static void validateRoles(List<String> userRoles) {
        for (String role : userRoles) {

            if (role == null || role.isBlank()) {
                throw new InvalidAuditEventException(
                        "User role cannot be null or empty");
            }
        }
    }

    private static void validateDocumentData(String documentId, String documentType, String documentCode,
            LocalDate documentDate, String thirdPartyId, String thirdPartyName, String moduleName,
            DocumentData documentData) {
        if (documentId == null || documentId.isBlank()) {
            throw new InvalidAuditEventException("Document ID cannot be null or empty");
        }
        if (documentType == null || documentType.isBlank()) {
            throw new InvalidAuditEventException("Document type cannot be null or empty");
        }
        if (documentCode == null || documentCode.isBlank()) {
            throw new InvalidAuditEventException("Document code cannot be null or empty");
        }
        if (documentDate == null) {
            throw new InvalidAuditEventException("Document date cannot be null");
        }
        if (thirdPartyId != null && thirdPartyId.isBlank()) {
            throw new InvalidAuditEventException("Third party ID cannot be empty if provided");
        }
        if (thirdPartyName != null && thirdPartyName.isBlank()) {
            throw new InvalidAuditEventException("Third party name cannot be empty if provided");
        }
        if (moduleName != null && moduleName.isBlank()) {
            throw new InvalidAuditEventException("Module name cannot be empty if provided");
        }
        if (documentData == null) {
            throw new InvalidAuditEventException("Document data cannot be null");
        }
    }

    private static void validateOperationAt(Instant operationAt) {
        if (operationAt == null) {
            throw new InvalidAuditEventException("Operation timestamp cannot be null");
        }
        if (operationAt.isAfter(Instant.now().plusSeconds(300))) {
            throw new InvalidAuditEventException("Operation timestamp cannot be in the future");
        }
    }
}
