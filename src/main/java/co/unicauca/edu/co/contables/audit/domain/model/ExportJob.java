package co.unicauca.edu.co.contables.audit.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportJobStatus;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportType;
import co.unicauca.edu.co.contables.audit.domain.exceptions.ExportAuditException;

import lombok.Getter;

@Getter
public class ExportJob {
    private String jobId;
    private String enterpriseId;
    private String fileName;
    private ExportJobStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalRecords;
    private Integer progress;
    private byte[] fileData;
    private ExportFormat format;
    private ExportType exportType;
    private String errorMessage;

    private ExportJob(String jobId, String enterpriseId, String fileName, ExportJobStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime, Integer totalRecords, Integer progress, byte[] fileData, ExportFormat format,
            ExportType exportType, String errorMessage) {
        this.jobId = jobId;
        this.enterpriseId = enterpriseId;
        this.fileName = fileName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalRecords = totalRecords;
        this.progress = progress;
        this.fileData = fileData;
        this.format = format;
        this.exportType = exportType;
        this.errorMessage = errorMessage;
    }

    public static ExportJob create(String enterpriseId, String fileName,
            ExportFormat format, ExportType exportType) {
        String jobId = UUID.randomUUID().toString();
        return new ExportJob(jobId, enterpriseId, fileName, ExportJobStatus.PENDING,
                LocalDateTime.now(), null, 0, 0, null, format, exportType, null);
    }

    public void markAsProcessing() {
        if (status != ExportJobStatus.PENDING) {
            throw new ExportAuditException(
                    "Only pending jobs can start processing");
        }

        this.status = ExportJobStatus.PROCESSING;
        this.progress = 10;
    }

    public void updateProgress(int progress) {
        if (progress < 0 || progress > 100)
            throw new ExportAuditException("Progress must be 0-100");
        this.progress = progress;
    }

    public void complete(byte[] fileData, int totalRecords) {
        if (status != ExportJobStatus.PROCESSING) {
            throw new ExportAuditException(
                    "Only processing jobs can be completed");
        }

        this.fileData = fileData;
        this.totalRecords = totalRecords;
        this.progress = 100;
        this.status = ExportJobStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        if (status != ExportJobStatus.PROCESSING) {
            throw new ExportAuditException(
                    "Only processing jobs can fail");
        }

        this.errorMessage = errorMessage;
        this.status = ExportJobStatus.FAILED;
        this.endTime = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == ExportJobStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == ExportJobStatus.FAILED;
    }
}
