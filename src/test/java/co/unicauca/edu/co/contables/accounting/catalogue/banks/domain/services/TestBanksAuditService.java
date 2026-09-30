package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;

@Service
public class TestBanksAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "BANK", moduleName = "BANKS")
    public Bank create() {
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setIdEnterprise("ENT-1");
        bank.setCode("B-001");
        bank.setName("BANK TEST");
        bank.setStatus(true);
        return bank;
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "BANK", moduleName = "BANKS")
    public Bank update(BankUpdateReq request) {
        Bank bank = new Bank();
        bank.setId(request.getId());
        bank.setIdEnterprise(request.getIdEnterprise());
        bank.setCode("B-001");
        bank.setName(request.getName());
        bank.setStatus(true);
        return bank;
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "BANK", moduleName = "BANKS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public Bank changeState(Long id, String entId, Boolean newState) {
        Bank bank = new Bank();
        bank.setId(id);
        bank.setIdEnterprise(entId);
        bank.setCode("B-001");
        bank.setName("BANK TEST");
        bank.setStatus(newState);
        return bank;
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "BANK", moduleName = "BANKS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public Bank delete(Long id, String entId) {
        Bank bank = new Bank();
        bank.setId(id);
        bank.setIdEnterprise(entId);
        bank.setCode("B-001");
        bank.setName("BANK TEST");
        bank.setStatus(false);
        return bank;
    }
}
