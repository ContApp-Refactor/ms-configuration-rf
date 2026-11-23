package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;

@Service
public class TestBankAccountAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "BANK_ACCOUNT", moduleName = "BANKS")
    public BankAccount create() {
        BankAccount ba = new BankAccount();
        ba.setId(1L);
        ba.setIdEnterprise("ENT-1");
        ba.setAccountNumber(123456L);
        ba.setStatus(true);
        return ba;
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "BANK_ACCOUNT", moduleName = "BANKS")
    public BankAccount update(BankAccountUpdateReq request) {
        BankAccount ba = new BankAccount();
        ba.setId(request.getId());
        ba.setIdEnterprise(request.getIdEnterprise());
        ba.setAccountNumber(request.getAccountNumber());
        ba.setStatus(true);
        return ba;
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "BANK_ACCOUNT", moduleName = "BANKS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public BankAccount changeState(Long id, String entId, Boolean status) {
        BankAccount ba = new BankAccount();
        ba.setId(id);
        ba.setIdEnterprise(entId);
        ba.setAccountNumber(123456L);
        ba.setStatus(status);
        return ba;
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "BANK_ACCOUNT", moduleName = "BANKS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public BankAccount delete(Long id, String entId) {
        BankAccount ba = new BankAccount();
        ba.setId(id);
        ba.setIdEnterprise(entId);
        ba.setAccountNumber(123456L);
        ba.setStatus(false);
        return ba;
    }
}
