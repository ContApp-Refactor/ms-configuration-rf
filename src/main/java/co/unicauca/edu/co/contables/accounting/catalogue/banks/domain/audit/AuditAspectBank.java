package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services.IBankService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectBank extends BaseAuditAspect {

    @Lazy
    private final IBankService bankService;

    public AuditAspectBank(AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher, @Lazy IBankService bankService) {
        super(auditEventBuilder, auditEventPublisher);
        this.bankService = bankService;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "BANK" -> {
                Long id = (args[0] instanceof Long) ? (Long) args[0] : ((BankUpdateReq) args[0]).getId();
                String enterpriseId = (args[0] instanceof Long) ? (String) args[1]
                        : ((BankUpdateReq) args[0]).getIdEnterprise();
                yield Optional.ofNullable(bankService.findById(id, enterpriseId))
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
            case CREATE -> result instanceof Bank b ? b.getIdEnterprise() : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> (String) args[auditable.enterpriseIdArgIndex()];
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof Bank b ? String.valueOf(b.getId()) : "UNKNOWN";
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
        if (!(object instanceof Bank bank)) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", bank.getId());
        map.put("entId", bank.getIdEnterprise());
        map.put("code", bank.getCode());
        map.put("name", bank.getName());
        map.put("currencies", bank.getCurrencies());
        map.put("state", bank.getStatus());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }

}
