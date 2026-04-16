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
    @Lazy
    private final ITagService tagService;

    public AuditAspectNoCommerTag(AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher, @Lazy ITagService tagService) {
        super(auditEventBuilder, auditEventPublisher);
        this.tagService = tagService;
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
                            .map(this::entityToMap)
                            .orElse(null);
                }
                Long id = (Long) args[0];
                Tag tag = (Tag) args[1];
                yield tagService.getTag(id, tag.getEnterpriseId())
                        .map(this::entityToMap)
                        .orElse(null);
            }
            default -> null;
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

    @Override
    protected Map<String, Object> buildContext(Object[] args, Object result, Map<String, Object> beforeData) {
        if (beforeData == null)
            return Map.of();
        Map<String, Object> context = new LinkedHashMap<>();
        if (beforeData.get("title") != null)
            context.put("title", beforeData.get("title"));
        return context;
    }

    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (!(object instanceof Tag tag)) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", tag.getId());
        data.put("entId", tag.getEnterpriseId());
        data.put("title", tag.getTitle());
        data.put("description", tag.getDescription());

        data.entrySet().removeIf(entry -> entry.getValue() == null);
        return data;
    }

}
