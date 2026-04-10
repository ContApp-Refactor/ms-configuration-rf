package co.unicauca.edu.co.contables.configuration.commons.audit.aspect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseAuditAspect {

    protected abstract AuditEventBuilder getAuditEventBuilder();

    protected abstract AuditEventPublisher getAuditEventPublisher();

    protected abstract Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args);

    protected abstract Map<String, Object> buildDataObject(OperationType operationType,
            Object[] args, Object result, Map<String, Object> beforeData, Auditable auditable);

    protected abstract String resolveEnterpriseId(Auditable auditable, Object[] args,
            Object result, Map<String, Object> beforeData);

    protected abstract String resolveRegisterId(Auditable auditable, Object[] args,
            Object result, Map<String, Object> beforeData);

    public Object executeAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Map<String, Object> beforeData = captureBeforeData(auditable, args);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            throw ex;
        }

        try {
            OperationType resolvedType = resolveFinalOperationType(auditable, beforeData);
            String enterpriseId = resolveEnterpriseId(auditable, args, result, beforeData);
            String registerId = resolveRegisterId(auditable, args, result, beforeData);
            Map<String, Object> dataObject = buildDataObject(resolvedType, args, result, beforeData, auditable);

            if (dataObject == null || dataObject.isEmpty()) {
                return result;
            }

            if (dataObject.containsKey("changes")) {
                Map<?, ?> changes = (Map<?, ?>) dataObject.get("changes");
                if (changes == null || changes.isEmpty()) {
                    return result;
                }
            }
            OperationEventDto dto = getAuditEventBuilder().build(
                    auditable, resolvedType, enterpriseId, registerId, dataObject);
            getAuditEventPublisher().publish(dto);

        } catch (Exception e) {
            log.error("Error construyendo evento de auditoría [{}]: {}",
                    auditable.operationType(), e.getMessage(), e);
        }
        return result;
    }

    protected Map<String, Object> captureBeforeData(Auditable auditable, Object[] args) {
        try {
            return switch (auditable.operationType()) {
                case UPDATE, INACTIVATE, DELETE -> fetchCurrentState(auditable, args);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("No se pudo capturar estado before para {}: {}",
                    auditable.operationType(), e.getMessage());
            return null;
        }
    }

    protected OperationType resolveFinalOperationType(Auditable auditable, Map<String, Object> beforeData) {
        if (auditable.operationType() == OperationType.INACTIVATE) {
            boolean wasActive = beforeData != null && Boolean.TRUE.equals(beforeData.get("state"));
            return wasActive ? OperationType.INACTIVATE : OperationType.ACTIVATE;
        }
        return auditable.operationType();
    }

    protected Map<String, Object> buildDiff(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> diff = new LinkedHashMap<>();
        if (before == null || after == null)
            return diff;
        after.forEach((key, afterValue) -> {
            Object beforeValue = before.get(key);
            if (!Objects.equals(beforeValue, afterValue)) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("before", beforeValue);
                change.put("after", afterValue);
                diff.put(key, change);
            }
        });
        return diff;
    }
}
