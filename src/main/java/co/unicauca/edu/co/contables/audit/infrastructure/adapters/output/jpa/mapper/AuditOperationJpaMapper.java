package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;
import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;
import co.unicauca.edu.co.contables.audit.domain.model.OperationData;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;

@Component
public class AuditOperationJpaMapper {

    public AuditOperation toDomain(AuditOperationEntity entity) {
        OperationData operationData = parseOperationData(
                entity.getDataObject(),
                entity.getOperationType().name());

        return AuditOperation.reconstruct(
                entity.getId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserRole(),
                entity.getOperationType(),
                entity.getOperationAt(),
                entity.getModuleName(),
                entity.getAffectedTable(),
                entity.getRegisterId(),
                entity.getEnterpriseId(),
                operationData,
                entity.getCreatedAt());
    }

    public AuditOperationEntity toEntity(AuditOperation domain) {
        Map<String, Object> dataObjectMap = serializeOperationData(domain.getDataObject());

        return AuditOperationEntity.builder()
                .id(domain.getId())
                .enterpriseId(domain.getEnterpriseId())
                .userId(domain.getUserId())
                .userName(domain.getUserName())
                .userRole(domain.getUserRole())
                .operationType(domain.getOperationType())
                .operationAt(domain.getOperationAt())
                .moduleName(domain.getModuleName())
                .affectedTable(domain.getAffectedTable())
                .registerId(domain.getRegisterId())
                .dataObject(dataObjectMap)
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private OperationData parseOperationData(Map<String, Object> dataMap, String operationType) {
        if (dataMap == null || dataMap.isEmpty()) {
            throw new AuditMappingException("Stored audit data cannot be null or empty");
        }
        OperationType type = OperationType.valueOf(operationType);
        return switch (type) {
            case CREATE -> parseEntityOperation(dataMap, OperationData::forCreate);
            case DELETE -> parseEntityOperation(dataMap, OperationData::forDelete);
            case ACTIVATE -> parseUpdateData(dataMap);
            case INACTIVATE -> parseUpdateData(dataMap);
            case UPDATE -> parseUpdateData(dataMap);
        };
    }

    private OperationData parseEntityOperation(
            Map<String, Object> dataMap,
            Function<Map<String, Object>, OperationData> creator) {

        Map<String, Object> entity = extractEntityData(dataMap);

        if (entity == null || entity.isEmpty()) {
            throw new AuditMappingException("Entity data missing for operation");
        }

        return creator.apply(entity);
    }

    @SuppressWarnings("unchecked")
    private OperationData parseUpdateData(Map<String, Object> dataMap) {
        Map<String, Map<String, Object>> changesMap = (Map<String, Map<String, Object>>) dataMap.get("changes");

        if (changesMap == null || changesMap.isEmpty()) {
            throw new AuditMappingException("Update operation must contain 'changes'");
        }

        Map<String, OperationData.FieldChange> fieldChanges = changesMap.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractEntityData(Map<String, Object> dataMap) {
        if (dataMap.containsKey("entity")) {
            return (Map<String, Object>) dataMap.get("entity");
        }
        return dataMap;
    }

    private Map<String, Object> serializeOperationData(OperationData data) {
        if (!data.getEntity().isEmpty()) {
            return Map.of("entity", data.getEntity());
        }

        if (!data.getChanges().isEmpty()) {
            Map<String, Object> changesMap = data.getChanges().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Map.of(
                                    "before", e.getValue().getBefore(),
                                    "after", e.getValue().getAfter())));
            if (!data.getContext().isEmpty()) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("context", data.getContext());
                result.put("changes", changesMap);
                return result;
            }
            return Map.of("changes", changesMap);
        }

        throw new AuditMappingException("OperationData has no entity or changes");
    }
}
