package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectAccountCatalogue extends BaseAuditAspect {

    @Lazy
    private final IAccountCatalogueSearchOutputPort accountCatalogueServiceSearch;

    public AuditAspectAccountCatalogue(AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher,
            @Lazy IAccountCatalogueSearchOutputPort accountCatalogueServiceSearch) {
        super(auditEventBuilder, auditEventPublisher);
        this.accountCatalogueServiceSearch = accountCatalogueServiceSearch;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "ACCOUNT_CATALOGUE" -> {
                Long id = (Long) args[0];
                String enterpriseId = (args[1] instanceof String s) ? s
                        : ((AccountCatalogue) args[1]).getIdEnterprise();
                yield Optional
                        .ofNullable(
                                accountCatalogueServiceSearch.getAccountCatalogueByIdAndIdEnterprise(id, enterpriseId))
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
            case CREATE -> result instanceof AccountCatalogue a ? a.getIdEnterprise() : "UNKNOWN";
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof AccountCatalogue a ? String.valueOf(a.getId()) : "UNKNOWN";
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
        if (beforeData.get("description") != null)
            context.put("description", beforeData.get("description"));
        return context;
    }

    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (!(object instanceof AccountCatalogue a)) {
            return null;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", a.getId());
        data.put("entId", a.getIdEnterprise());
        data.put("code", a.getCode());
        data.put("description", a.getDescription());
        data.put("nature", a.getNature() != null ? a.getNature().name() : null);
        data.put("financialStatus", a.getFinancialStatus() != null ? a.getFinancialStatus().name() : null);
        data.put("classification", a.getClassification() != null ? a.getClassification().name() : null);
        data.put("parentId", a.getParent() != null ? a.getParent().getId() : null);
        data.put("parentCode", a.getParent() != null ? a.getParent().getCode() : null);
        data.put("crossing", a.getCrossing());
        data.put("costCenter", a.getCostCenter());
        data.put("state", a.getStatus());
        data.put("salesTaxesCount", a.getSalesTaxes() != null ? a.getSalesTaxes().size() : 0);
        data.put("purchaseTaxesCount", a.getPurchaseTaxes() != null ? a.getPurchaseTaxes().size() : 0);

        data.entrySet().removeIf(e -> e.getValue() == null);
        return data;
    }

}
