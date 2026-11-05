package co.unicauca.edu.co.contables.audit.application.port.output;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportDocumentsEventsRequest;

public interface IDocumentAsyncExportPort {

    void process(ExportDocumentsEventsRequest request, String jobId);
}
