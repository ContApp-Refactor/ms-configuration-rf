package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;

@Service
public class TestCatalogueAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "ACCOUNT_CATALOGUE", moduleName = "ACCOUNT_CATALOGUE")
    public AccountCatalogue create() {

        AccountCatalogue account = new AccountCatalogue();

        account.setId(1L);
        account.setIdEnterprise("ENT-1");
        account.setCode("100");
        account.setDescription("TEST");
        account.setStatus(true);

        return account;
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "ACCOUNT_CATALOGUE", moduleName = "ACCOUNT_CATALOGUE", idArgIndex = 0)
    public AccountCatalogue update(long id, AccountCatalogue request) {
        AccountCatalogue account = new AccountCatalogue();
        account.setId(id);
        account.setIdEnterprise(request.getIdEnterprise());
        account.setCode("100");
        account.setDescription(request.getDescription());
        account.setStatus(true);
        return account;
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "ACCOUNT_CATALOGUE", moduleName = "ACCOUNT_CATALOGUE", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public AccountCatalogue changeState(
            Long id,
            String entId,
            Boolean status) {

        AccountCatalogue account = new AccountCatalogue();

        account.setId(id);
        account.setIdEnterprise(entId);
        account.setCode("100");
        account.setDescription("TEST");
        account.setStatus(status);

        return account;
    }
}
