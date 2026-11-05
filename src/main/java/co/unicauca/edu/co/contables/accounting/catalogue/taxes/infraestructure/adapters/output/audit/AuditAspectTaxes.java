package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectTaxes extends BaseAuditAspect {

    @Lazy
    private final ITaxSearchOutputPort taxSearchOutputPort;

    public AuditAspectTaxes(AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher, @Lazy ITaxSearchOutputPort taxSearchOutputPort) {
        super(auditEventBuilder, auditEventPublisher);
        this.taxSearchOutputPort = taxSearchOutputPort;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "TAX" -> {
                Long id = (args[0] instanceof TaxDTO) ? (Long) args[1] : (Long) args[0];
                String enterpriseId = args[0] instanceof TaxDTO ? ((TaxDTO) args[0]).getIdEnterprise()
                        : (String) args[1];
                yield Optional.ofNullable(taxSearchOutputPort.getTaxByIdAndEnterprise(id, enterpriseId))
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
            case CREATE -> result instanceof Tax t ? t.getIdEnterprise() : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> (String) args[auditable.enterpriseIdArgIndex()];
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof Tax t ? String.valueOf(t.getId()) : "UNKNOWN";
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
        if (beforeData.get("description") != null)
            context.put("description", beforeData.get("description"));
        return context;
    }

    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (!(object instanceof Tax tax)) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", tax.getId());
        map.put("entId", tax.getIdEnterprise());
        map.put("code", tax.getCode());
        map.put("description", tax.getDescription());
        map.put("interest", tax.getInterest());
        map.put("salesTaxId", tax.getSalesTax() != null ? tax.getSalesTax().getId() : null);
        map.put("purchaseTaxId", tax.getPurchaseTax() != null ? tax.getPurchaseTax().getId() : null);
        map.put("salesTaxCode", tax.getSalesTax() != null ? tax.getSalesTax().getCode() : null);
        map.put("purchaseTaxCode", tax.getPurchaseTax() != null ? tax.getPurchaseTax().getCode() : null);
        map.put("state", tax.getStatus());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }

}
