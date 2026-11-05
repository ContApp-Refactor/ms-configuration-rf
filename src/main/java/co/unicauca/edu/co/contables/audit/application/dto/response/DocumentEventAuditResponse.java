package co.unicauca.edu.co.contables.audit.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentEventAuditResponse {

    private String documentCode;
    private String documentType;
    private String thirdPartyName;
    private LocalDate documentDate;
    private String createdBy;
    private String lastModifiedBy;
    private Instant lastModifiedAt;
}
