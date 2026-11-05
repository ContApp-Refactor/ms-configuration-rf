package co.unicauca.edu.co.contables.audit.application.dto.request;

import java.time.Instant;

import co.unicauca.edu.co.contables.audit.domain.enums.AuditDateType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetDocumentsEventsRequest {

    private Instant dateFrom;
    private Instant dateTo;
    private AuditDateType dateType;
    private String enterpriseId;
    private String documentCode;
    private String documentType;
    private String createdBy;
    private String thirdPartyName;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortField = "lastModifiedAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
