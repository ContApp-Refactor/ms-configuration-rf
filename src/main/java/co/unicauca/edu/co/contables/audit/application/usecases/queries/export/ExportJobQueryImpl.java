package co.unicauca.edu.co.contables.audit.application.usecases.queries.export;

import java.util.Optional;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.application.dto.response.ExportFileResult;
import co.unicauca.edu.co.contables.audit.application.dto.response.ExportJobResponse;
import co.unicauca.edu.co.contables.audit.application.internal.ExportJobTracker;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.export.IExportJobQuery;
import co.unicauca.edu.co.contables.audit.domain.exceptions.ExportAuditException;
import co.unicauca.edu.co.contables.audit.domain.model.ExportJob;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExportJobQueryImpl implements IExportJobQuery {

    private final ExportJobTracker jobTracker;

    @Override
    public Optional<ExportJobResponse> getJobStatus(String jobId) {
        return jobTracker.getJob(jobId).map(this::toResponse);
    }

    @Override
    public ExportFileResult getJobFileData(String jobId) {
        ExportJob job = jobTracker.getJob(jobId)
                .orElseThrow(() -> new ExportAuditException(jobId));

        if (job.isFailed()) {
            throw new ExportAuditException(job.getErrorMessage() + " - " + job.getStatus().name());
        }

        if (!job.isCompleted()) {
            throw new ExportAuditException(job.getStatus().name() + " - Job is not completed yet.");
        }

        return ExportFileResult.builder()
                .data(job.getFileData())
                .fileName(job.getFileName())
                .format(job.getFormat())
                .build();
    }

    @Override
    public void removeJob(String jobId) {
        jobTracker.removeJob(jobId);
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return ExportJobResponse.builder()
                .jobId(job.getJobId())
                .status(job.getStatus().name())
                .progress(job.getProgress())
                .totalRecords(job.getTotalRecords())
                .fileName(job.getFileName())
                .exportType(job.getExportType().name())
                .exportFormat(job.getFormat().name())
                .errorMessage(job.getErrorMessage())
                .startTime(job.getStartTime())
                .endTime(job.getEndTime())
                .build();
    }
}
