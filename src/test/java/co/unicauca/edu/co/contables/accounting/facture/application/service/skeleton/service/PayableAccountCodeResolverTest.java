package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * L-03: lab supplier account often has id == PUC code (e.g. 2205 / "2205").
 * The resolver must accept that numeric PUC code after a successful catalogue lookup.
 *
 * El catálogo se resuelve inyectando el puerto de entrada del módulo
 * catálogo: no hay llamadas HTTP internas.
 */
class PayableAccountCodeResolverTest {

    private IAccountCatalogueSearchInputPort catalogueSearch;
    private PayableAccountCodeResolver resolver;

    @BeforeEach
    void setUp() {
        catalogueSearch = mock(IAccountCatalogueSearchInputPort.class);
        resolver = new PayableAccountCodeResolver(catalogueSearch);
    }

    @Test
    void resolveAcceptsNumericPucCodeWhenItEqualsAccountId() {
        stubCatalogue("enterprise-local", List.of(account(2205L, "2205")));

        Optional<String> code = resolver.resolve(2205L, "enterprise-local");

        assertThat(code).contains("2205");
    }

    @Test
    void resolveStillReturnsDistinctCodeWhenIdAndCodeDiffer() {
        stubCatalogue("enterprise-local", List.of(account(99L, "2205")));

        assertThat(resolver.resolve(99L, "enterprise-local")).contains("2205");
    }

    @Test
    void returnsEmptyWhenAccountIsMissingFromCatalogue() {
        stubCatalogue("enterprise-local", List.of(account(10L, "1105")));

        assertThat(resolver.resolve(2205L, "enterprise-local")).isEmpty();
    }

    private void stubCatalogue(String enterpriseId, List<AccountCatalogue> accounts) {
        when(catalogueSearch.getAllAccountsByEnterprise(enterpriseId)).thenReturn(accounts);
    }

    private static AccountCatalogue account(Long id, String code) {
        return AccountCatalogue.builder().id(id).code(code).build();
    }
}
