package co.unicauca.edu.co.contables.audit.application.port.input.queries;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetOperationsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.OperationAuditResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;

public interface GetAuditOperationsQuery {

    PageResponse<OperationAuditResponse> execute(GetOperationsRequest request);
}
