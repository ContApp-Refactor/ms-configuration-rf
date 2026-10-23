package co.unicauca.edu.co.contables.audit.application.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 
 * @brief DTO for session audit event response
 *        Represents the view that is sent to the primary adapters
 */
@Getter
@Builder
public class SessionAuditResponse {
    // Obligatorio: la coleccion newman localiza la sesion recien publicada con
    // json.data.find(s => s.sessionId === testSessionId). Sin este campo la
    // sesion no puede encontrarse aunque exista en base.
    private String sessionId;
    private String userName;
    private List<String> userRole;
    private Instant loginTime;
    private Instant logoutTime;
}
