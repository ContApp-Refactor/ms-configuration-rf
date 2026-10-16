package co.unicauca.edu.co.contables.audit.application.port.output;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportOperationsRequest;

public interface IOperationAsyncExportPort {

    void process(ExportOperationsRequest request, String jobId);
}
