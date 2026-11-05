package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportOperationsRequest {

    private Instant dateFrom;
    private Instant dateTo;
    private String moduleName;
    private String affectedTable;
    private String userName;
    private String userRole;
    private OperationType operationType;
    private String registerId;
    private String enterpriseId;
    private String enterpriseName;
    private String requestedBy;
    @Builder.Default
    private ExportFormat exportFormat = ExportFormat.EXCEL;
}
