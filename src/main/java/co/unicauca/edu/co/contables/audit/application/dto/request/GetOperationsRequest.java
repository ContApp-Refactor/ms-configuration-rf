package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetOperationsRequest {

    private Instant dateFrom;
    private Instant dateTo;
    private String moduleName;
    private String affectedTable;
    private String userName;
    private String userRole;
    private OperationType operationType;
    private String registerId;
    private String enterpriseId;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortField = "operationAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
