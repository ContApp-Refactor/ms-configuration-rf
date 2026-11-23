package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * El resolutor de CxP consulta el catálogo a través del puerto de entrada del
 * módulo catálogo: no hay llamadas HTTP internas ni servidor embebido.
 */
class PayableAccountDefaultResolverUnitTest {

    private static final String ENTERPRISE = "enterprise-a";

    private IAccountCatalogueSearchInputPort catalogueSearch;
    private PayableAccountDefaultResolver resolver;

    @BeforeEach
    void setUp() {
        catalogueSearch = mock(IAccountCatalogueSearchInputPort.class);
        resolver = new PayableAccountDefaultResolver(catalogueSearch);
    }

    @Test
    void resolvesDefaultSupplierPayableAccountFromCatalogue() {
        stubCatalogue(
                account(10L, "1105", "Caja", ClassificationEnum.CURRENTASSETS, true),
                account(24L, "2206", "Beneficios a empleados a largo plazo",
                        ClassificationEnum.NONCURRENTLIABILITIES, true),
                account(21L, "2105", "Cuentas por pagar",
                        ClassificationEnum.CURRENTLIABILITIES, true));

        assertThat(resolver.resolveForPurchase(null, ENTERPRISE)).isEqualTo(21L);
    }

    @Test
    void acceptsAccountsPayableDescription() {
        stubCatalogue(
                account(21L, "2105", "Cuentas por pagar",
                        ClassificationEnum.CURRENTLIABILITIES, true));

        assertThat(resolver.resolveForPurchase(null, ENTERPRISE)).isEqualTo(21L);
    }

    @Test
    void acceptsSuppliersDescription() {
        stubCatalogue(
                account(30L, "220501", "Proveedores nacionales",
                        ClassificationEnum.CURRENTLIABILITIES, true));

        assertThat(resolver.resolveForPurchase(null, ENTERPRISE)).isEqualTo(30L);
    }

    @Test
    void rejectsCashAccount() {
        stubCatalogue(
                account(10L, "1105", "Caja", ClassificationEnum.CURRENTASSETS, true));

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void rejectsEmployeeBenefitsAccount() {
        stubCatalogue(
                account(24L, "2206", "Beneficios a empleados a largo plazo",
                        ClassificationEnum.NONCURRENTLIABILITIES, true));

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void rejectsLaborObligationsAccount() {
        stubCatalogue(
                account(31L, "2510", "Obligaciones laborales",
                        ClassificationEnum.CURRENTLIABILITIES, true));

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void rejectsInactiveExplicitPayableAccount() {
        stubCatalogue(
                account(55L, "2105", "Cuentas por pagar",
                        ClassificationEnum.CURRENTLIABILITIES, false));

        assertThatThrownBy(() -> resolver.resolveForPurchase(55L, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void rejectsWhenCatalogueIsEmpty() {
        stubCatalogue();

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catálogo de cuentas válido");
    }

    @Test
    void rejectsWhenNoSupplierPayableExists() {
        stubCatalogue(
                account(1L, "1105", "Caja", ClassificationEnum.CURRENTASSETS, true),
                account(2L, "2206", "Beneficios a empleados a largo plazo",
                        ClassificationEnum.NONCURRENTLIABILITIES, true),
                account(3L, "2205", "Obligaciones financieras no corrientes",
                        ClassificationEnum.NONCURRENTLIABILITIES, true));

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cuenta por Pagar activa");
    }

    @Test
    void acceptsExplicitActiveSupplierPayableAccount() {
        stubCatalogue(
                account(77L, "2105", "Cuentas por pagar",
                        ClassificationEnum.CURRENTLIABILITIES, true));

        assertThat(resolver.resolveForPurchase(77L, ENTERPRISE)).isEqualTo(77L);
    }

    @Test
    void rejectsExplicitNonSupplierPayableAccount() {
        stubCatalogue(
                account(24L, "2206", "Beneficios a empleados a largo plazo",
                        ClassificationEnum.NONCURRENTLIABILITIES, true));

        assertThatThrownBy(() -> resolver.resolveForPurchase(24L, ENTERPRISE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no es una cuenta por pagar válida");
    }

    @Test
    void prefersAccountsPayableOverSuppliersWhenBothExist() {
        stubCatalogue(
                account(30L, "220501", "Proveedores",
                        ClassificationEnum.CURRENTLIABILITIES, true),
                account(21L, "2105", "Cuentas por pagar",
                        ClassificationEnum.CURRENTLIABILITIES, true));

        assertThat(resolver.resolveForPurchase(null, ENTERPRISE)).isEqualTo(21L);
    }

    @Test
    void rejectsNullEnterpriseIdWithoutQueryingCatalogue() {
        assertThatThrownBy(() -> resolver.resolveForPurchase(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresa");

        verify(catalogueSearch, never()).getAllAccountsByEnterprise(any());
    }

    @Test
    void rejectsBlankEnterpriseIdWithoutQueryingCatalogue() {
        assertThatThrownBy(() -> resolver.resolveForPurchase(null, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresa");

        verify(catalogueSearch, never()).getAllAccountsByEnterprise(any());
    }

    @Test
    void reportsNullPortResultAsContractViolationNotEmptyCatalogue() {
        when(catalogueSearch.getAllAccountsByEnterprise(ENTERPRISE)).thenReturn(null);

        assertThatThrownBy(() -> resolver.resolveForPurchase(null, ENTERPRISE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("null");
    }

    private void stubCatalogue(AccountCatalogue... accounts) {
        when(catalogueSearch.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(accounts));
    }

    private static AccountCatalogue account(
            Long id, String code, String description, ClassificationEnum classification,
            Boolean status) {
        return AccountCatalogue.builder()
                .id(id)
                .code(code)
                .description(description)
                .classification(classification)
                .status(status)
                .build();
    }
}
