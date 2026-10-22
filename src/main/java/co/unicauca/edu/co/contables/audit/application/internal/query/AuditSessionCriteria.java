package co.unicauca.edu.co.contables.audit.application.internal.query;

import java.time.Duration;
import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;

import lombok.Getter;

@Getter
public class AuditSessionCriteria {

    private final Instant dateFrom;
    private final Instant dateTo;
    private final String userName;
    private final String userRole;
    private final UserAction action;

    private AuditSessionCriteria(Instant dateFrom, Instant dateTo, String userName,
            String userRole, UserAction action) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.userName = userName;
        this.userRole = userRole;
        this.action = action;
    }

    public static AuditSessionCriteria create(Instant dateFrom, Instant dateTo, String userName,
            String userRole, UserAction action) {
        validateDateRange(dateFrom, dateTo);
        return new AuditSessionCriteria(dateFrom, dateTo, userName, userRole, action);
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

    public boolean hasRoleCriteria() {
        return userRole != null && !userRole.isBlank();
    }

    public boolean hasUserNameCriteria() {
        return userName != null && !userName.isBlank();
    }

    public boolean hasActionCriteria() {
        return action != null;
    }
}
