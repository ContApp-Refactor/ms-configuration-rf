package co.unicauca.edu.co.contables.audit.application.dto.response;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.audit.domain.model.DocumentData;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentEventDetailResponse {

    private String operationType;
    private String userName;
    private List<String> userRoles;
    private Instant operationAt;
    private DocumentData documentData;
}
