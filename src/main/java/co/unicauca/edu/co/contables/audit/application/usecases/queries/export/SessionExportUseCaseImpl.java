package co.unicauca.edu.co.contables.audit.application.usecases.queries.export;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.internal.ExportJobTracker;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditSessionCriteria;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.export.ISessionExportUseCase;
import co.unicauca.edu.co.contables.audit.application.port.output.ISessionAsyncExportPort;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionExportUseCaseImpl implements ISessionExportUseCase {

    private final ExportJobTracker jobTracker;
    private final ISessionAsyncExportPort asyncExportPort;

    @Override
    public String execute(ExportSessionsRequest request) {

        AuditSessionCriteria.create(
                request.getDateFrom(),
                request.getDateTo(),
                request.getUserName(),
                request.getUserRole(),
                request.getAction());

        String fileName = buildFileName("sessions", request.getExportFormat());

        String jobId = jobTracker.createJob(
                request.getRequestedBy(),
                fileName,
                request.getExportFormat(),
                ExportType.SESSION);

        asyncExportPort.process(request, jobId);

        return jobId;
    }

    private String buildFileName(String type, ExportFormat format) {
        String ext = format == ExportFormat.PDF ? ".pdf" : ".xlsx";
        return type + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ext;
    }

}
