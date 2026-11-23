package co.unicauca.edu.co.contables.audit.application.port.input.queries.export;

import java.util.Optional;

import co.unicauca.edu.co.contables.audit.application.dto.response.ExportFileResult;
import co.unicauca.edu.co.contables.audit.application.dto.response.ExportJobResponse;

public interface IExportJobQuery {

    Optional<ExportJobResponse> getJobStatus(String jobId);

    ExportFileResult getJobFileData(String jobId);

    void removeJob(String jobId);
}
