package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.billing.application.ports.output.TaxCatalogPort.TaxRef;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** El adaptador del catalogo de impuestos llama en proceso al puerto de entrada del maestro (solo ACTIVOS). */
@ExtendWith(MockitoExtension.class)
class BillingTaxCatalogAdapterTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";

    @Mock
    private ITaxSearchInputPort taxSearch;

    private BillingTaxCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BillingTaxCatalogAdapter(taxSearch);
    }

    private static Tax tax(Long id, String code, String description, Double interest) {
        return Tax.builder().id(id).code(code).description(description).interest(interest).build();
    }

    @Test
    void active_taxes_are_mapped_with_code_as_name_and_interest_as_percentage() {
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(List.of(tax(1L, "ReteICA", "desc", 0.966)));

        List<TaxRef> taxes = adapter.listActiveTaxes(ENTERPRISE);

        assertThat(taxes).hasSize(1);
        assertThat(taxes.get(0).id()).isEqualTo(1L);
        assertThat(taxes.get(0).name()).isEqualTo("ReteICA");
        assertThat(taxes.get(0).ratePct()).isEqualByComparingTo("0.966");
    }

    @Test
    void without_a_code_the_description_is_the_name_and_without_either_a_generic_one() {
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(List.of(
                tax(1L, " ", "Retencion en la fuente", 10.0), tax(2L, null, null, 5.0)));

        List<TaxRef> taxes = adapter.listActiveTaxes(ENTERPRISE);

        assertThat(taxes).extracting(TaxRef::name).containsExactly("Retencion en la fuente", "Impuesto");
    }

    @Test
    void a_null_interest_is_zero_and_rows_without_id_are_skipped() {
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(Arrays.asList(
                tax(1L, "X", null, null), tax(null, "Y", null, 5.0), null));

        List<TaxRef> taxes = adapter.listActiveTaxes(ENTERPRISE);

        assertThat(taxes).hasSize(1);
        assertThat(taxes.get(0).ratePct()).isEqualByComparingTo("0");
    }

    @Test
    void a_null_list_is_an_empty_catalog() {
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(null);

        assertThat(adapter.listActiveTaxes(ENTERPRISE)).isEmpty();
    }

    @Test
    void an_inactive_or_foreign_tax_is_not_found() {
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(List.of(tax(1L, "X", null, 5.0)));

        assertThat(adapter.findActiveTax(ENTERPRISE, 1L)).isPresent();
        assertThat(adapter.findActiveTax(ENTERPRISE, 2L)).isEmpty();
    }
}
