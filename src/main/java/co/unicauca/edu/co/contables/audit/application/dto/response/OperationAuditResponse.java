package co.unicauca.edu.co.contables.audit.application.dto.response;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.audit.domain.model.OperationData;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperationAuditResponse {

    private String userName;
    private List<String> userRole;
    private Instant operationAt;
    private String moduleName;
    private String affectedTable;
    private String registerId;
    private String operationType;
    private OperationData dataObject;
}
