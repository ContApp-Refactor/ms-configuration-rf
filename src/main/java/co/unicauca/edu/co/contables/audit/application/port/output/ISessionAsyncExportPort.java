package co.unicauca.edu.co.contables.audit.application.port.output;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportSessionsRequest;

public interface ISessionAsyncExportPort {

    void process(ExportSessionsRequest request, String jobId);
}
