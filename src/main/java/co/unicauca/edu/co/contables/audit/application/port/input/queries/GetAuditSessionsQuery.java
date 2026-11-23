package co.unicauca.edu.co.contables.audit.application.port.input.queries;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.SessionAuditResponse;

/**
 * @brief input port for querying session events
 */
public interface GetAuditSessionsQuery {

    PageResponse<SessionAuditResponse> execute(GetSessionsRequest request);
}
