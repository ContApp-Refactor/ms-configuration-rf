package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.AccountCatalogueAccountSnapshot;
import java.util.Optional;

public interface IAccountCodeResolverPort {
    Optional<String> resolveCode(Long accountId, String enterpriseId);

    Optional<AccountCatalogueAccountSnapshot> resolveAccount(Long accountId, String enterpriseId);
}
