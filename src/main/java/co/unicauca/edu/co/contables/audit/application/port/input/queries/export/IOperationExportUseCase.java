package co.unicauca.edu.co.contables.audit.application.port.input.queries.export;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportOperationsRequest;

public interface IOperationExportUseCase {

    String execute(ExportOperationsRequest request);
}
