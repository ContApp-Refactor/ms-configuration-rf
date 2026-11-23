package co.unicauca.edu.co.contables.audit.domain.model;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;

import lombok.Getter;

@Getter
public class AuditOperation {

    // Attributes
    private final Long id;
    private final String userId;
    private final String userName;
    private final List<String> userRole;
    private final OperationType operationType;
    private final Instant operationAt;
    private final String moduleName;
    private final String affectedTable;
    private final String registerId;
    private final String enterpriseId;
    private final OperationData dataObject;
    private final Instant createdAt;

    // Constructor
    private AuditOperation(Long id, String userId, String userName, List<String> userRole, OperationType operationType,
            Instant operationAt, String moduleName, String affectedTable, String registerId, String enterpriseId,
            OperationData dataObject,
            Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.operationType = operationType;
        this.operationAt = operationAt;
        this.moduleName = moduleName;
        this.affectedTable = affectedTable;
        this.registerId = registerId;
        this.enterpriseId = enterpriseId;
        this.dataObject = dataObject;
        this.createdAt = createdAt;
    }

    // Factory method
    public static AuditOperation create(String userId, String userName, List<String> userRole,
            OperationType operationType, Instant operationAt, String moduleName, String affectedTable,
            String registerId, String enterpriseId, OperationData dataObject) {
        validateEnterpriseId(enterpriseId);
        validateUserData(userId, userName, userRole);
        validateOperationData(operationType, affectedTable, registerId, dataObject);
        validateModuleName(moduleName);
        validateOperationAt(operationAt);

        Instant now = Instant.now();

        return new AuditOperation(null, userId, userName, userRole, operationType, operationAt, moduleName,
                affectedTable, registerId, enterpriseId, dataObject, now);
    }

    public static AuditOperation reconstruct(Long id, String userId, String userName, List<String> userRole,
            OperationType operationType, Instant operationAt, String moduleName, String affectedTable,
            String registerId, String enterpriseId,
            OperationData dataObject, Instant createdAt) {
        return new AuditOperation(id, userId, userName, userRole, operationType,
                operationAt, moduleName, affectedTable, registerId, enterpriseId, dataObject, createdAt);
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

    private static void validateUserData(String userId, String userName, List<String> userRole) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidAuditEventException("User ID cannot be null or empty");
        }
        if (userName == null || userName.isBlank()) {
            throw new InvalidAuditEventException("User name cannot be null or empty");
        }
        if (userRole == null || userRole.isEmpty()) {
            throw new InvalidAuditEventException("User role cannot be null or empty");
        }
    }

    private static void validateOperationData(OperationType operationType, String affectedTable,
            String registerId, OperationData dataObject) {
        if (operationType == null) {
            throw new InvalidAuditEventException("Operation type cannot be null");
        }
        if (affectedTable == null || affectedTable.isBlank()) {
            throw new InvalidAuditEventException("Affected table cannot be null or empty");
        }
        if (registerId == null || registerId.isBlank()) {
            throw new InvalidAuditEventException("Register ID cannot be null or empty");
        }
        if (dataObject == null) {
            throw new InvalidAuditEventException("Data object cannot be null");
        }
        switch (operationType) {
            case CREATE, DELETE -> {
                if (dataObject.getEntity().isEmpty())
                    throw new InvalidAuditEventException(operationType + " requires entity snapshot data");
                if (!dataObject.getChanges().isEmpty())
                    throw new InvalidAuditEventException(operationType + " cannot contain change data");
            }
            case UPDATE, ACTIVATE, INACTIVATE -> {
                if (dataObject.getChanges().isEmpty())
                    throw new InvalidAuditEventException(operationType + " requires change data");
                if (!dataObject.getEntity().isEmpty()) {
                    throw new InvalidAuditEventException(operationType + " cannot contain entity snapshot data");
                }
            }
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
