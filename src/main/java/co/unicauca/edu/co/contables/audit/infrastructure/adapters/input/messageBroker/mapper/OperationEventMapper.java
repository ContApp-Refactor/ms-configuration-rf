package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.mapper;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;
import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.domain.model.OperationData;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.messageBroker.dto.OperationEventDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuditMappingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OperationEventMapper {

    public LogOperationRequest toRequest(OperationEventDto eventDto) {
        return LogOperationRequest.builder()
                .enterpriseId(sanitizeText(eventDto.getEnterpriseId()))
                .userId(sanitizeText(eventDto.getUserId()))
                .userName(sanitizeText(eventDto.getUserName()))
                .userRole(eventDto.getUserRole())
                .operationType(parseOperationType(eventDto.getOperationType()))
                .operationAt(eventDto.getOperationAt())
                .moduleName(sanitizeText(eventDto.getModuleName()))
                .affectedTable(sanitizeText(eventDto.getAffectedTable()))
                .registerId(sanitizeText(eventDto.getRegisterId()))
                .dataObject(parseDataObject(eventDto.getDataObject(), eventDto.getOperationType()))
                .build();
    }

    private OperationType parseOperationType(String type) {
        if (type == null) {
            throw new AuditMappingException("Operation type cannot be null");
        }
        try {
            return OperationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AuditMappingException("Invalid operation type received: " + type);
        }
    }

    private OperationData parseDataObject(Map<String, Object> dataMap, String operationType) {
        if (dataMap == null || dataMap.isEmpty()) {
            throw new AuditMappingException("Data object cannot be null or empty");
        }
        OperationType opType = parseOperationType(operationType);

        return switch (opType) {
            case CREATE -> OperationData.forCreate(extractEntity(dataMap));
            case UPDATE -> parseUpdateData(dataMap);
            case DELETE -> OperationData.forDelete(extractEntity(dataMap));
            case ACTIVATE -> parseUpdateData(dataMap);
            case INACTIVATE -> parseUpdateData(dataMap);
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractEntity(Map<String, Object> dataMap) {
        if (dataMap.containsKey("entity")) {
            return (Map<String, Object>) dataMap.get("entity");
        }
        return dataMap;
    }

    @SuppressWarnings("unchecked")
    private OperationData parseUpdateData(Map<String, Object> dataMap) {
        Map<String, Map<String, Object>> changesMap = (Map<String, Map<String, Object>>) dataMap.get("changes");

        if (changesMap == null || changesMap.isEmpty()) {
            throw new AuditMappingException("Update operation requires 'changes' field");
        }

        Map<String, OperationData.FieldChange> fieldChanges = changesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Map<String, Object> changeData = entry.getValue();
                            return OperationData.FieldChange.of(
                                    changeData.get("before"),
                                    changeData.get("after"));
                        }));

        Map<String, Object> context = null;
        if (dataMap.containsKey("context")) {
            context = (Map<String, Object>) dataMap.get("context");
        }
        return context != null && !context.isEmpty()
                ? OperationData.forUpdate(context, fieldChanges)
                : OperationData.forUpdate(fieldChanges);
    }

    private String sanitizeText(String input) {
        if (input == null)
            return null;
        String sanitized = input.trim()
                .replaceAll("[\\n\\r\\t]", " ")
                .replaceAll("[<>]", "");
        return sanitized;
    }
}
