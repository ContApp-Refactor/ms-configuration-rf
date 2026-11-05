package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FactureAuditEventBuilder {

    private final IJwtUtils jwtUtils;

    public DocumentEventDto build(
            String moduleName,
            DocumentOperationType operationType,
            String enterpriseId,
            String documentId,
            String documentCode,
            String documentType,
            LocalDate documentDate,
            String thirdPartyId,
            String thirdPartyName,
            Map<String, Object> documentData) {

        return DocumentEventDto.builder()
                .enterpriseId(enterpriseId)
                .documentId(documentId)
                .documentCode(documentCode)
                .documentType(documentType)
                .documentDate(documentDate)
                .userId(jwtUtils.getId())
                .userName(jwtUtils.getUsername())
                .userRoles(jwtUtils.getRealmRoles())
                .operationType(operationType.name())
                .thirdPartyId(thirdPartyId)
                .thirdPartyName(thirdPartyName)
                .moduleName(moduleName)
                .operationAt(Instant.now())
                .documentData(documentData)
                .build();

    }

}
