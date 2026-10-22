package co.unicauca.edu.co.contables.audit.application.port.input.queries;

import java.util.List;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetDocumentEventDetailRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.DocumentEventDetailResponse;

public interface GetAuditDocumentEventDetailsQuery {
    List<DocumentEventDetailResponse> execute(GetDocumentEventDetailRequest request);
}
