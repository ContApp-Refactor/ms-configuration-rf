package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.mapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;
import co.unicauca.edu.co.contables.audit.domain.model.DocumentData;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditDocumentEventEntity;

@Component
public class AuditDocumentEventJpaMapper {

    public AuditDocumentEvent toDomain(AuditDocumentEventEntity entity) {
        return AuditDocumentEvent.reconstruct(
                entity.getId(),
                entity.getEnterpriseId(),
                entity.getDocumentCode(),
                entity.getDocumentType(),
                entity.getDocumentId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserRoles(),
                entity.getOperationType(),
                entity.getThirdPartyId(),
                entity.getThirdPartyName(),
                entity.getModuleName(),
                entity.getOperationAt(),
                entity.getDocumentDate(),
                parseDocumentData(entity.getDocumentData()),
                entity.getCreatedAt());
    }

    public AuditDocumentEventEntity toEntity(AuditDocumentEvent domain) {
        return AuditDocumentEventEntity.builder()
                .id(domain.getId())
                .enterpriseId(domain.getEnterpriseId())
                .documentId(domain.getDocumentId())
                .documentCode(domain.getDocumentCode())
                .documentType(domain.getDocumentType())
                .documentDate(domain.getDocumentDate())
                .userId(domain.getUserId())
                .userName(domain.getUserName())
                .userRoles(domain.getUserRoles())
                .operationType(domain.getOperationType())
                .thirdPartyId(domain.getThirdPartyId())
                .thirdPartyName(domain.getThirdPartyName())
                .moduleName(domain.getModuleName())
                .operationAt(domain.getOperationAt())
                .documentData(serializeDocumentData(domain.getDocumentData()))
                .createdAt(domain.getCreatedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private DocumentData parseDocumentData(Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            throw new AuditMappingException("Stored document data cannot be null or empty");
        }

        Map<String, Object> header = (Map<String, Object>) dataMap.get("header");

        List<Map<String, Object>> details = (List<Map<String, Object>>) dataMap.getOrDefault("details",
                Collections.emptyList());

        Map<String, Object> totals = (Map<String, Object>) dataMap.get("totals");

        Map<String, Object> metadata = (Map<String, Object>) dataMap.getOrDefault("metadata", Collections.emptyMap());

        return DocumentData.of(header, details, totals, metadata);
    }

    private Map<String, Object> serializeDocumentData(DocumentData data) {
        if (data == null) {
            throw new AuditMappingException("DocumentData cannot be null");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        if (!data.getHeader().isEmpty()) {
            result.put("header", data.getHeader());
        }

        if (!data.getDetails().isEmpty()) {
            result.put("details", data.getDetails());
        }

        if (!data.getTotals().isEmpty()) {
            result.put("totals", data.getTotals());
        }

        if (!data.getMetadata().isEmpty()) {
            result.put("metadata", data.getMetadata());
        }

        if (result.isEmpty()) {
            throw new AuditMappingException("DocumentData cannot be empty");
        }

        return result;
    }
}
