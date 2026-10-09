package co.unicauca.edu.co.contables.audit.application.port.input.queries;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetDocumentsEventsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.DocumentEventAuditResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;

public interface GetAuditDocumentEventQuery {
    PageResponse<DocumentEventAuditResponse> execute(GetDocumentsEventsRequest request);
}
