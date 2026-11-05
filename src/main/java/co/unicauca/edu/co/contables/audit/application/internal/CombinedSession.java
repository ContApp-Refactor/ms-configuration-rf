package co.unicauca.edu.co.contables.audit.application.internal;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;

import lombok.Getter;

@Getter
public class CombinedSession {
    private final String sessionId;
    private final String userName;
    private final List<String> userRole;
    private final Instant loginTime;
    private final Instant logoutTime;

    private CombinedSession(String sessionId, String userName,
            List<String> userRole, Instant loginTime, Instant logoutTime) {
        this.sessionId = sessionId;
        this.userName = userName;
        this.userRole = userRole;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    public static CombinedSession of(String sessionId, String userName,
            List<String> userRole, Instant loginTime, Instant logoutTime) {
        if (sessionId == null || sessionId.isBlank())
            throw new InvalidAuditEventException("Session ID required");
        if (loginTime == null)
            throw new InvalidAuditEventException("Login time is required for session: " + sessionId);
        return new CombinedSession(sessionId, userName, userRole, loginTime, logoutTime);
    }

    public boolean isActive() {
        return logoutTime == null;
    }
}
