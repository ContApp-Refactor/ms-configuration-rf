package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspectBankAccount extends BaseAuditAspect {

    @Lazy
    private final IBankAccountService bankAccountService;

    public AuditAspectBankAccount(AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher, @Lazy IBankAccountService bankAccountService) {
        super(auditEventBuilder, auditEventPublisher);
        this.bankAccountService = bankAccountService;
    }

    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "BANK_ACCOUNT" -> {
                Long id = (args[0] instanceof Long) ? (Long) args[0] : ((BankAccountUpdateReq) args[0]).getId();
                String enterpriseId = (args[0] instanceof Long) ? (String) args[1]
                        : ((BankAccountUpdateReq) args[0]).getIdEnterprise();
                yield Optional.ofNullable(bankAccountService.findById(id, enterpriseId))
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
            case CREATE -> result instanceof BankAccount ba ? ba.getIdEnterprise() : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> (String) args[auditable.enterpriseIdArgIndex()];
            default -> beforeData != null ? (String) beforeData.get("entId") : "UNKNOWN";
        };
    }

    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> result instanceof BankAccount ba ? String.valueOf(ba.getId()) : "UNKNOWN";
            case DELETE, INACTIVATE, ACTIVATE -> String.valueOf(args[auditable.idArgIndex()]);
            default -> beforeData != null ? String.valueOf(beforeData.get("id")) : "UNKNOWN";
        };
    }

    @Override
    protected Map<String, Object> buildContext(Object[] args, Object result, Map<String, Object> beforeData) {
        if (beforeData == null)
            return Map.of();
        Map<String, Object> context = new LinkedHashMap<>();
        if (beforeData.get("accountNumber") != null)
            context.put("accountNumber", beforeData.get("accountNumber"));
        if (beforeData.get("accountType") != null)
            context.put("accountType", beforeData.get("accountType"));
        return context;
    }

    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (!(object instanceof BankAccount bankAccount)) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", bankAccount.getId());
        map.put("entId", bankAccount.getIdEnterprise());
        map.put("accountNumber", bankAccount.getAccountNumber());
        map.put("bank",
                bankAccount.getBank() != null
                        ? bankAccount.getBank().getCode() + " - " + bankAccount.getBank().getName()
                        : null);
        map.put("accountType", bankAccount.getAccountType() != null ? bankAccount.getAccountType().name() : null);
        map.put("accountingAccountId", bankAccount.getAccountingAccountId());
        map.put("state", bankAccount.getStatus());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }

}
