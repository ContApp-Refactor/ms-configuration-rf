package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectTypesDocuments extends BaseAuditAspect {

    private final AuditEventBuilder auditEventBuilder;
    private final AuditEventPublisher auditEventPublisher;
    @Lazy
    private final IDocumentTypeService documentTypeService;

    public AuditAspectTypesDocuments(AuditEventBuilder auditEventBuilder, @Lazy AuditEventPublisher auditEventPublisher,
            IDocumentTypeService documentTypeService) {
        this.auditEventBuilder = auditEventBuilder;
        this.auditEventPublisher = auditEventPublisher;
        this.documentTypeService = documentTypeService;
    }

    @Override
    protected AuditEventBuilder getAuditEventBuilder() {
        return auditEventBuilder;
    }

    @Override
    protected AuditEventPublisher getAuditEventPublisher() {
        return auditEventPublisher;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "DOCUMENT_TYPE" -> {
                Long id = (args[0] instanceof Long) ? (Long) args[0] : ((DocumentTypeUpdateReq) args[0]).getId();
                String enterpriseId = (args[0] instanceof Long) ? (String) args[1]
                        : ((DocumentTypeUpdateReq) args[0]).getIdEnterprise();
                yield Optional.ofNullable(documentTypeService.findById(id, enterpriseId))
                        .map(this::documentTypeToMap)
                        .orElse(null);
            }
            default -> null;
        };
    }

    @Override
    protected Map<String, Object> buildDataObject(OperationType operationType, Object[] args,
            Object result, Map<String, Object> beforeData, Auditable auditable) {
        return switch (operationType) {
            case CREATE -> {
                Map<String, Object> data = new LinkedHashMap<>();
                if (result instanceof DocumentType d)
                    data.put("entity", documentTypeToMap(d));
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
            case CREATE -> result instanceof DocumentType d ? d.getIdEnterprise() : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> (String) args[auditable.enterpriseIdArgIndex()];
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof DocumentType d ? String.valueOf(d.getId()) : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> String.valueOf(args[auditable.idArgIndex()]);
            default -> beforeData != null ? String.valueOf(beforeData.get("id")) : "UNKNOWN";
        };
    }

    private Map<String, Object> documentTypeToMap(DocumentType d) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", d.getId());
        data.put("entId", d.getIdEnterprise());
        data.put("name", d.getName());
        data.put("prefix", d.getPrefix());
        data.put("documentClassId", d.getDocumentClassId());
        data.put("moduleId", d.getModuleId());
        data.put("state", d.getStatus());

        data.entrySet().removeIf(e -> e.getValue() == null);
        return data;
    }
}
