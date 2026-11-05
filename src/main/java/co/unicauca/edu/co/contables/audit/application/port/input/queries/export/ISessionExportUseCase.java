package co.unicauca.edu.co.contables.audit.application.port.input.queries.export;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportSessionsRequest;

public interface ISessionExportUseCase {

    String execute(ExportSessionsRequest request);
}
