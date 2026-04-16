package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.IDocumentClassService;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectClassesDocuments extends BaseAuditAspect {

    @Lazy
    private final IDocumentClassService documentClassService;

    public AuditAspectClassesDocuments(AuditEventBuilder auditEventBuilder,
            @Lazy AuditEventPublisher auditEventPublisher,
            IDocumentClassService documentClassService) {
        super(auditEventBuilder, auditEventPublisher);
        this.documentClassService = documentClassService;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "DOCUMENT_CLASS" -> {
                Long id = (args[0] instanceof Long) ? (Long) args[0] : ((DocumentClassUpdateReq) args[0]).getId();
                String enterpriseId = (args[0] instanceof Long) ? (String) args[1]
                        : ((DocumentClassUpdateReq) args[0]).getIdEnterprise();
                yield Optional.ofNullable(documentClassService.findById(id, enterpriseId))
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
            case CREATE -> result instanceof DocumentClass d ? d.getIdEnterprise() : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> (String) args[auditable.enterpriseIdArgIndex()];
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof DocumentClass d ? String.valueOf(d.getId()) : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> String.valueOf(args[auditable.idArgIndex()]);
            default -> beforeData != null ? String.valueOf(beforeData.get("id")) : "UNKNOWN";
        };
    }

    @Override
    protected Map<String, Object> buildContext(Object[] args, Object result, Map<String, Object> beforeData) {
        if (beforeData == null)
            return Map.of();
        Map<String, Object> context = new LinkedHashMap<>();
        if (beforeData.get("name") != null)
            context.put("name", beforeData.get("name"));
        return context;
    }

    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (!(object instanceof DocumentClass documentClass)) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", documentClass.getId());
        data.put("entId", documentClass.getIdEnterprise());
        data.put("name", documentClass.getName());
        data.put("state", documentClass.getStatus());

        data.entrySet().removeIf(entry -> entry.getValue() == null);
        return data;
    }
}
