package co.unicauca.edu.co.contables.audit.application.usecases.queries.export;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportOperationsRequest;
import co.unicauca.edu.co.contables.audit.application.internal.ExportJobTracker;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.export.IOperationExportUseCase;
import co.unicauca.edu.co.contables.audit.application.port.output.IOperationAsyncExportPort;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationExportUseCaseImpl implements IOperationExportUseCase {

    private final ExportJobTracker jobTracker;
    private final IOperationAsyncExportPort asyncExportPort;

    @Override
    public String execute(ExportOperationsRequest request) {

        String fileName = buildFileName("operations");

        String jobId = jobTracker.createJob(
                request.getRequestedBy(),
                fileName,
                request.getExportFormat(),
                ExportType.OPERATION);

        asyncExportPort.process(request, jobId);

        return jobId;
    }

    private String buildFileName(String type) {
        return type + "_audit_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xlsx";
    }

}
