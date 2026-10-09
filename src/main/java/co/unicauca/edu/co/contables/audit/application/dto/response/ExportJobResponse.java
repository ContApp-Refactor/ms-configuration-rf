package co.unicauca.edu.co.contables.audit.application.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportJobResponse {
    private String jobId;
    private String status;
    private Integer progress;
    private Integer totalRecords;
    private String fileName;
    private String exportType;
    private String exportFormat;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
