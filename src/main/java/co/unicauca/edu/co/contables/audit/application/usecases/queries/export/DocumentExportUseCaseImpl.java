package co.unicauca.edu.co.contables.audit.application.usecases.queries.export;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportDocumentsEventsRequest;
import co.unicauca.edu.co.contables.audit.application.internal.ExportJobTracker;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.export.IDocumentExportUseCase;
import co.unicauca.edu.co.contables.audit.application.port.output.IDocumentAsyncExportPort;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentExportUseCaseImpl implements IDocumentExportUseCase {

    private final ExportJobTracker jobTracker;
    private final IDocumentAsyncExportPort asyncExportPort;

    @Override
    public String execute(ExportDocumentsEventsRequest request) {

        String fileName = buildFileName("documents");

        String jobId = jobTracker.createJob(
                request.getRequestedBy(),
                fileName,
                request.getExportFormat(),
                ExportType.DOCUMENT);

        asyncExportPort.process(request, jobId);

        return jobId;
    }

    private String buildFileName(String type) {
        return type + "_audit_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xlsx";
    }

}
