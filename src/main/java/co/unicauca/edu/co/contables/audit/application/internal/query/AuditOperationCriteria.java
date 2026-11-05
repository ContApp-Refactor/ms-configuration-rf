package co.unicauca.edu.co.contables.audit.application.internal.query;

import java.time.Duration;
import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;

import lombok.Getter;

@Getter
public class AuditOperationCriteria {

    private final Instant dateFrom;
    private final Instant dateTo;
    private final String moduleName;
    private final String affectedTable;
    private final String userName;
    private final String userRole;
    private final OperationType operationType;
    private final String registerId;
    private final String enterpriseId;

    private AuditOperationCriteria(
            Instant dateFrom, Instant dateTo, String moduleName,
            String affectedTable, String userName,
            String userRole,
            OperationType operationType, String registerId, String enterpriseId) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.moduleName = moduleName;
        this.affectedTable = affectedTable;
        this.userName = userName;
        this.userRole = userRole;
        this.operationType = operationType;
        this.registerId = registerId;
        this.enterpriseId = enterpriseId;
    }

    public static AuditOperationCriteria create(Instant dateFrom, Instant dateTo, String moduleName,
            String affectedTable, String userName,
            String userRole,
            OperationType operationType, String registerId, String enterpriseId) {

        validateDateRange(dateFrom, dateTo);
        return new AuditOperationCriteria(dateFrom, dateTo, moduleName, affectedTable, userName, userRole,
                operationType, registerId, enterpriseId);
    }

    private static void validateDateRange(Instant dateFrom, Instant dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new InvalidAuditEventException("Date range is required");
        }

        if (dateFrom.isAfter(dateTo)) {
            throw new InvalidAuditEventException("dateFrom cannot be after dateTo");
        }

        Instant now = Instant.now();

        if (dateFrom.isAfter(now) || dateTo.isAfter(now)) {
            throw new InvalidAuditEventException("Dates cannot be in the future");
        }

        long daysBetween = Duration.between(dateFrom, dateTo).toDays();
        if (daysBetween > 365 * 2) {
            throw new InvalidAuditEventException("Date range cannot exceed 2 years");
        }
    }

    public boolean hasModuleNameCriteria() {
        return moduleName != null && !moduleName.isBlank();
    }

    public boolean hasAffectedTableCriteria() {
        return affectedTable != null && !affectedTable.isBlank();
    }

    public boolean hasUserNameCriteria() {
        return userName != null && !userName.isBlank();
    }

    public boolean hasUserRoleCriteria() {
        return userRole != null && !userRole.isBlank();
    }

    public boolean hasOperationTypeCriteria() {
        return operationType != null;
    }

    public boolean hasRegisterIdCriteria() {
        return registerId != null && !registerId.isBlank();
    }

    public boolean hasEnterpriseIdCriteria() {
        return enterpriseId != null && !enterpriseId.isBlank();
    }
}
