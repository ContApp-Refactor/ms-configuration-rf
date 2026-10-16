package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogDocumentEventRequest;
import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;
import co.unicauca.edu.co.contables.audit.domain.model.DocumentData;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.DocumentEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuditMappingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocumentEventMapper {

    public LogDocumentEventRequest toRequest(DocumentEventDto eventDto) {
        return LogDocumentEventRequest.builder()
                .enterpriseId(eventDto.getEnterpriseId())
                .documentId(eventDto.getDocumentId())
                .documentCode(eventDto.getDocumentCode())
                .documentType(eventDto.getDocumentType())
                .documentDate(eventDto.getDocumentDate())
                .userId(eventDto.getUserId())
                .userName(eventDto.getUserName())
                .userRoles(eventDto.getUserRoles())
                .operationType(parseOperationType(eventDto.getOperationType()))
                .thirdPartyId(eventDto.getThirdPartyId())
                .thirdPartyName(eventDto.getThirdPartyName())
                .moduleName(eventDto.getModuleName())
                .operationAt(eventDto.getOperationAt())
                .documentData(parseDocumentData(eventDto.getDocumentData()))
                .build();
    }

    private DocumentOperationType parseOperationType(String type) {
        if (type == null) {
            throw new AuditMappingException("Operation type cannot be null");
        }
        try {
            return DocumentOperationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AuditMappingException("Invalid operation type received: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    private DocumentData parseDocumentData(Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            throw new AuditMappingException("Document data cannot be null or empty");
        }
        Map<String, Object> header = (Map<String, Object>) dataMap.get("header");
        List<Map<String, Object>> details = (List<Map<String, Object>>) dataMap.getOrDefault("details",
                Collections.emptyList());
        Map<String, Object> totals = (Map<String, Object>) dataMap.get("totals");
        Map<String, Object> metadata = (Map<String, Object>) dataMap.getOrDefault("metadata", Collections.emptyMap());
        return DocumentData.of(header, details, totals, metadata);
    }
}
