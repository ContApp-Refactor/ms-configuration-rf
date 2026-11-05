package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.projection;

import java.time.Instant;

public interface SessionProjection {
    String getSessionId();
    String getUserName();
    String getUserRole();
    Instant getLoginTime();
    Instant getLogoutTime();
}
