package co.unicauca.edu.co.contables.configuration.commons.audit.builder;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditEventBuilder {

    private final IJwtUtils jwtUtils;

    public OperationEventDto build(
            Auditable auditable,
            OperationType resolvedOperationType,
            String enterpriseId,
            String registerId,
            Map<String, Object> dataObject) {
        return OperationEventDto.builder()
                .enterpriseId(enterpriseId)
                .userId(jwtUtils.getId())
                .userName(jwtUtils.getUsername())
                .userRole(jwtUtils.getRealmRoles())
                .operationType(resolvedOperationType.name())
                .operationAt(Instant.now())
                .moduleName(auditable.moduleName())
                .affectedTable(auditable.affectedTable())
                .registerId(registerId)
                .dataObject(dataObject)
                .build();
    }

}