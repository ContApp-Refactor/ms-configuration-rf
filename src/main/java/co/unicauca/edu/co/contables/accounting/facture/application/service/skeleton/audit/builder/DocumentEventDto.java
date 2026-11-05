package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEventDto {

    @JsonProperty("enterprise_id")
    private String enterpriseId;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("document_code")
    private String documentCode;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("document_date")
    private LocalDate documentDate;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_roles")
    private List<String> userRoles;

    @JsonProperty("operation_type")
    private String operationType;

    @JsonProperty("third_party_id")
    private String thirdPartyId;

    @JsonProperty("third_party_name")
    private String thirdPartyName;

    @JsonProperty("module_name")
    private String moduleName;

    @JsonProperty("operation_at")
    private Instant operationAt;

    @JsonProperty("document_data")
    private Map<String, Object> documentData;
}
