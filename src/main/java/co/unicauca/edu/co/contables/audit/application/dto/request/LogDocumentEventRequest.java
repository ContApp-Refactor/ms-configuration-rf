package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;
import co.unicauca.edu.co.contables.audit.domain.model.DocumentData;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogDocumentEventRequest {

    private String enterpriseId;
    private String documentId;
    private String documentCode;
    private String documentType;
    private LocalDate documentDate;
    private String userId;
    private String userName;
    private List<String> userRoles;
    private DocumentOperationType operationType;
    private String thirdPartyId;
    private String thirdPartyName;
    private String moduleName;
    private Instant operationAt;
    private DocumentData documentData;
}
