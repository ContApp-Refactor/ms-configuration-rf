package co.unicauca.edu.co.contables.configuration.costCenters.domain.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.configuration.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.configuration.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectCostCenters extends BaseAuditAspect {

    @Lazy
    private final ICostCenterService costCenterService;

    public AuditAspectCostCenters(AuditEventBuilder auditEventBuilder,
            @Lazy AuditEventPublisher auditEventPublisher,
            ICostCenterService costCenterService) {
        super(auditEventBuilder, auditEventPublisher);
        this.costCenterService = costCenterService;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.configuration.costCenters.domain.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "COST_CENTER" -> {
                Long id = (args[0] instanceof Long) ? (Long) args[0] : ((CostCenterUpdateReq) args[0]).getId();
                String enterpriseId = (args[0] instanceof Long) ? (String) args[1]
                        : ((CostCenterUpdateReq) args[0]).getIdEnterprise();
                yield Optional.ofNullable(costCenterService.findById(id, enterpriseId))
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
            case CREATE -> result instanceof CostCenter c ? c.getIdEnterprise() : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> (String) args[auditable.enterpriseIdArgIndex()];
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof CostCenter c ? String.valueOf(c.getId()) : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> String.valueOf(args[auditable.idArgIndex()]);
            default -> beforeData != null ? String.valueOf(beforeData.get("id")) : "UNKNOWN";
        };
    }

    @Override
    protected Map<String, Object> buildContext(Object[] args, Object result, Map<String, Object> beforeData) {
        if (beforeData == null)
            return Map.of();
        Map<String, Object> context = new LinkedHashMap<>();
        if (beforeData.get("code") != null)
            context.put("code", beforeData.get("code"));
        if (beforeData.get("name") != null)
            context.put("name", beforeData.get("name"));
        return context;
    }

    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (!(object instanceof CostCenter costCenter)) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", costCenter.getId());
        data.put("entId", costCenter.getIdEnterprise());
        data.put("code", costCenter.getCode());
        data.put("name", costCenter.getName());
        data.put("parentId", costCenter.getParent() != null ? costCenter.getParent().getId() : null);
        data.put("state", costCenter.getStatus());

        data.entrySet().removeIf(entry -> entry.getValue() == null);
        return data;
    }
}
