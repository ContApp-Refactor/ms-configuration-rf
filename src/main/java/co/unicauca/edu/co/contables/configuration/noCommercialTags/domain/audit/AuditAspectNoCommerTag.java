package co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models.Tag;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services.ITagService;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectNoCommerTag extends BaseAuditAspect {

    private final AuditEventBuilder auditEventBuilder;
    private final AuditEventPublisher auditEventPublisher;
    @Lazy
    private final ITagService tagService;

    public AuditAspectNoCommerTag(AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher, @Lazy ITagService tagService) {
        this.auditEventBuilder = auditEventBuilder;
        this.auditEventPublisher = auditEventPublisher;
        this.tagService = tagService;
    }

    @Override
    protected AuditEventBuilder getAuditEventBuilder() {
        return auditEventBuilder;
    }

    @Override
    protected AuditEventPublisher getAuditEventPublisher() {
        return auditEventPublisher;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "NO_COMMERCIAL_TAG" -> {
                if (auditable.operationType() == OperationType.DELETE) {
                    Long id = (Long) args[0];
                    yield tagService.getTagById(id)
                            .map(this::tagToMap)
                            .orElse(null);
                }
                Long id = (Long) args[0];
                Tag tag = (Tag) args[1];
                yield tagService.getTag(id, tag.getEnterpriseId())
                        .map(this::tagToMap)
                        .orElse(null);
            }
            default -> null;
        };
    }

    @Override
    protected Map<String, Object> buildDataObject(OperationType operationType, Object[] args, Object result,
            Map<String, Object> beforeData, Auditable auditable) {
        return switch (operationType) {
            case CREATE -> {
                Map<String, Object> data = new LinkedHashMap<>();
                if (result instanceof Tag t)
                    data.put("entity", tagToMap(t));
                yield data;
            }
            case UPDATE -> {
                Map<String, Object> afterData = fetchCurrentState(auditable, args);
                yield Map.of("changes", buildDiff(beforeData, afterData));
            }
            case ACTIVATE, INACTIVATE -> {
                if (beforeData != null) {
                    yield Map.of("changes", buildDiff(
                            Map.of("state", beforeData.get("state")),
                            Map.of("state", !((Boolean) beforeData.get("state")))));
                }
                yield Map.of();
            }
            case DELETE -> Map.of("entity", beforeData != null ? beforeData : Map.of("id", args[0]));
        };
    }

    @Override
    protected String resolveEnterpriseId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof Tag t ? t.getEnterpriseId() : "UNKNOWN";
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof Tag t ? String.valueOf(t.getId()) : "UNKNOWN";
            default -> beforeData != null ? String.valueOf(beforeData.get("id")) : "UNKNOWN";
        };
    }

    private Map<String, Object> tagToMap(Tag tag) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", tag.getId());
        data.put("entId", tag.getEnterpriseId());
        data.put("title", tag.getTitle());
        data.put("description", tag.getDescription());

        data.entrySet().removeIf(entry -> entry.getValue() == null);
        return data;
    }

}
