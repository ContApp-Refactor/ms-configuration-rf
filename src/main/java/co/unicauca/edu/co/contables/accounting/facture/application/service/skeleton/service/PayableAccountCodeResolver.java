package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resuelve el código PUC auxiliar a partir del id de cuenta contable
 * consultando el catálogo. Confía en el {@code code} del catálogo aunque
 * coincida numéricamente con el id (códigos PUC enteramente numéricos).
 *
 * El catálogo se consulta inyectando el puerto de entrada del módulo catálogo;
 * no hay llamadas HTTP internas.
 */
@Component
@Slf4j
public class PayableAccountCodeResolver {
    private final IAccountCatalogueSearchInputPort catalogueSearchInputPort;

    public PayableAccountCodeResolver(IAccountCatalogueSearchInputPort catalogueSearchInputPort) {
        this.catalogueSearchInputPort = catalogueSearchInputPort;
    }

    public String resolveOrFail(Long accountId, String enterpriseId) {
        return resolve(accountId, enterpriseId).orElseThrow(() ->
                new IllegalStateException(
                        "No se pudo resolver el código PUC de la cuenta contable " + accountId
                                + " para la empresa " + enterpriseId));
    }

    public Optional<String> resolve(Long accountId, String enterpriseId) {
        if (accountId == null || enterpriseId == null || enterpriseId.isBlank()) {
            return Optional.empty();
        }
        try {
            List<AccountCatalogue> accounts = catalogueSearchInputPort.getAllAccountsByEnterprise(enterpriseId);
            if (accounts == null) {
                return Optional.empty();
            }
            return accounts.stream()
                    .filter(a -> accountId.equals(a.getId()))
                    .map(AccountCatalogue::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .findFirst();
        } catch (Exception ex) {
            log.warn("Lookup de cuenta {} en catálogo falló: {}", accountId, ex.getMessage());
            return Optional.empty();
        }
    }
}
