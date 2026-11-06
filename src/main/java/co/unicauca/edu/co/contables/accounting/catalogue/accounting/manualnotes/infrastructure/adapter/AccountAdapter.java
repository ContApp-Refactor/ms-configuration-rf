package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.adapter;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.AccountPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adapter local de solo lectura al catálogo contable del monolito. */
@Component
public class AccountAdapter implements AccountPort {

    private final IAccountCatalogueSearchOutputPort accountSearch;

    public AccountAdapter(IAccountCatalogueSearchOutputPort accountSearch) {
        this.accountSearch = accountSearch;
    }

    @Override
    public Optional<Account> findByCode(String code, String enterpriseId) {
        AccountCatalogue account = accountSearch.getAccountCatalogueByCode(code, enterpriseId);
        if (account == null || Boolean.FALSE.equals(account.getStatus())) {
            return Optional.empty();
        }

        boolean auxiliary = account.getCode() != null && account.getCode().trim().length() == 8;
        return Optional.of(new Account(
                account.getCode(),
                account.getDescription(),
                auxiliary,
                Boolean.TRUE.equals(account.getCostCenter()),
                Boolean.TRUE.equals(account.getCrossing())));
    }
}
