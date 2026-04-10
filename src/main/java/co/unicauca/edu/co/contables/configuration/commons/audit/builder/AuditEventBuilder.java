package co.unicauca.edu.co.contables.configuration.commons.audit.builder;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.configuration.commons.security.JwtDecoder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditEventBuilder {

    private final IJwtUtils jwtUtils;
    private final JwtDecoder jwtDecoder;

    public OperationEventDto build(
            Auditable auditable,
            OperationType resolvedOperationType,
            String enterpriseId,
            String registerId,
            Map<String, Object> dataObject) {
        String token = jwtUtils.getToken();
        String userId = jwtDecoder.extractClaim(token, "sub");
        String userName = jwtDecoder.extractClaim(token, "preferred_username");
        String userRole = jwtDecoder.extractPrimaryRole(token);

        return OperationEventDto.builder()
                .enterpriseId(enterpriseId)
                .userId(userId)
                .userName(userName)
                .userRole(userRole)
                .operationType(resolvedOperationType.name())
                .operationAt(Instant.now())
                .moduleName(auditable.moduleName())
                .affectedTable(auditable.affectedTable())
                .registerId(registerId)
                .dataObject(dataObject)
                .build();
    }

}