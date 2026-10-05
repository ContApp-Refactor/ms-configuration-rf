package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.rest;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IAccountCodeResolverPort;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.AccountCatalogueAccountSnapshot;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resuelve cuentas PUC contra el modulo catalogue <strong>en el mismo proceso</strong>.
 *
 * <p>Sustituye la auto-llamada HTTP a {@code /api/accountCatalogue/search/{enterpriseId}}. Ese
 * intercambio parseaba {@code ItemAccountCatalogueSearchRes.classification}, que el catalogo
 * serializa como {@link String} (por ejemplo {@code "Pasivo Corriente"}), hacia un {@code Integer},
 * y terminaba siempre en {@code InvalidFormatException}; la excepcion la tragaba el
 * {@code catch (Exception)} del adaptador, por lo que este puerto devolvia
 * {@code Optional.empty()} en todos los casos y {@code isPayableLiability()} jamas se ejecutaba.
 *
 * <p>Al leer el modelo de dominio directamente desaparece la frontera de serializacion.
 */
@Component
@Slf4j
public class AccountCatalogueAccountCodeAdapter implements IAccountCodeResolverPort {

    private final IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort;

    public AccountCatalogueAccountCodeAdapter(
            IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort) {
        this.accountCatalogueSearchInputPort = accountCatalogueSearchInputPort;
    }

    @Override
    public Optional<String> resolveCode(Long accountId, String enterpriseId) {
        return resolveAccount(accountId, enterpriseId).map(AccountCatalogueAccountSnapshot::code);
    }

    @Override
    public Optional<AccountCatalogueAccountSnapshot> resolveAccount(Long accountId, String enterpriseId) {
        if (accountId == null || enterpriseId == null || enterpriseId.isBlank()) {
            return Optional.empty();
        }
        try {
            return accountCatalogueSearchInputPort.getAllAccountsByEnterprise(enterpriseId).stream()
                    .filter(account -> accountId.equals(account.getId()))
                    .findFirst()
                    .map(AccountCatalogueAccountCodeAdapter::toSnapshot);
        } catch (Exception ex) {
            log.warn("No se pudo resolver cuenta PUC {}: {}", accountId, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * {@code classification} se persiste como ordinal del enum (ver
     * {@code AccountCatalogueSearchJpaAdapter}, que relee un {@code short} y hace
     * {@code ClassificationEnum.values()[ordinal]}), asi que el snapshot conserva esa misma
     * numeracion: 2 y 3 son {@code CURRENTLIABILITIES} y {@code NONCURRENTLIABILITIES}, que es
     * exactamente lo que compara {@link AccountCatalogueAccountSnapshot#isPayableLiability()}.
     */
    private static AccountCatalogueAccountSnapshot toSnapshot(AccountCatalogue account) {
        Integer classification =
                account.getClassification() != null ? account.getClassification().ordinal() : null;
        return new AccountCatalogueAccountSnapshot(
                account.getCode(),
                account.getDescription(),
                classification,
                Boolean.TRUE.equals(account.getStatus()));
    }
}
