package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;

import lombok.Builder;
import lombok.Getter;

/**
 * @brief DTO for session event registration requests
 *        Used for communication between primary adapters and the application
 */

@Getter
@Builder
public class LogSessionRequest {
    private String sessionId;
    private String userId;
    private String userName;
    private List<String> userRole;
    private UserAction action;
    private Instant actionAt;
    private String ipAddress;
}
