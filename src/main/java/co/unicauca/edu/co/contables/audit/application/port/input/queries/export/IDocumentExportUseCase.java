package co.unicauca.edu.co.contables.audit.application.port.input.queries.export;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportDocumentsEventsRequest;

public interface IDocumentExportUseCase {

    String execute(ExportDocumentsEventsRequest request);
}
