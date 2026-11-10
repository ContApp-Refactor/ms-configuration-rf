package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.billing.domain.model.TaxComponent;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * M2/M3/R6: el IVA de referencia sale de la categoria del producto (impuesto identificado como IVA
 * por codigo o descripcion); sin IVA identificable o ante cualquier fallo se usa
 * billing.default-vat-rate.
 */
@ExtendWith(MockitoExtension.class)
class BillingVatRateResolverTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";
    private static final long CATEGORY_ID = 10L;

    @Mock
    private ICategoryServicePort categoryService;
    @Mock
    private ITaxSearchInputPort taxSearch;

    private BillingProperties properties;
    private BillingVatRateResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new BillingProperties();
        resolver = new BillingVatRateResolver(categoryService, taxSearch, properties);
    }

    private static Tax tax(long id, String code, String description, Double interest) {
        return Tax.builder().id(id).idEnterprise(ENTERPRISE).code(code).description(description)
                .interest(interest).status(true).build();
    }

    private void category(Long... taxIds) {
        when(categoryService.findById(ENTERPRISE, CATEGORY_ID))
                .thenReturn(Category.builder().id(CATEGORY_ID).taxes(List.of(taxIds)).build());
    }

    private void activeTaxes(Tax... taxes) {
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(List.of(taxes));
    }

    @Test
    void VAT_uses_the_iva_tax_of_the_category() {
        category(7L);
        activeTaxes(tax(7L, "IVA5", "Impuesto", 5.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("5.00");
    }

    @Test
    void VAT_identifies_iva_by_description_ignoring_case_and_accents() {
        category(7L);
        activeTaxes(tax(7L, "T-01", "Impuesto al Valor Agregado", 19.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19.00");

        category(8L);
        activeTaxes(tax(8L, "x", "iva general", 16.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("16.00");
    }

    @Test
    void VAT_sums_every_tax_of_the_category_whatever_its_name() {
        category(7L, 8L);
        activeTaxes(tax(7L, "RTF", "Retencion en la fuente", 3.5), tax(8L, "IVA19", "IVA general", 19.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("22.50");
    }

    @Test
    void VAT_a_single_tax_is_used_whatever_its_name() {
        category(7L);
        activeTaxes(tax(7L, "RETEIVA", "Retencion", 15.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("15.00");
    }

    @Test
    void VAT_taxes_with_iva_in_the_name_are_summed_like_any_other() {
        category(7L, 8L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "RETEIVA", "Retencion de IVA", 15.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("20.00");
    }

    @Test
    void VAT_three_taxes_are_summed() {
        category(7L, 8L, 9L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "RETEIVA", "Retencion de IVA", 15.0),
                tax(9L, "RTF", "Retencion en la fuente", 3.5));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("23.50");
    }

    @Test
    void VAT_names_with_accents_are_just_summed() {
        category(7L, 8L, 9L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "X-1", "Retención de IVA", 15.0),
                tax(9L, "RETEFUENTE", "Servicios", 4.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("24.00");
    }

    @Test
    void VAT_several_ivas_and_another_tax_are_summed() {
        category(7L, 8L, 9L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "IVA19", "IVA general", 19.0),
                tax(9L, "RETEIVA", "Retencion de IVA", 15.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("39.00");
    }

    @Test
    void VAT_iva_mixed_with_a_consumption_tax_is_summed() {
        category(7L, 8L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "INC", "Impuesto al consumo", 8.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("13.00");
    }

    @Test
    void VAT_glued_code_iva19_counts_as_iva() {
        category(7L);
        activeTaxes(tax(7L, "IVA19", "General", 19.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19.00");
    }

    @Test
    void VAT_two_withholding_named_taxes_are_summed() {
        category(7L, 8L);
        activeTaxes(tax(7L, "RETEIVA", "Retencion de IVA", 15.0), tax(8L, "RTF", "Retencion en la fuente", 3.5));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("18.50");
    }

    @Test
    void VAT_several_ivas_are_summed_not_the_highest() {
        category(7L, 8L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "IVA19", "IVA general", 19.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("24.00");
    }

    @Test
    void VAT_several_ivas_plus_consumption_are_summed() {
        category(7L, 8L, 9L);
        activeTaxes(tax(7L, "IVA5", "IVA reducido", 5.0), tax(8L, "IVA16", "IVA general", 16.0),
                tax(9L, "INC", "Impuesto al consumo", 8.0)); // H6: una retencion ya no cuenta como "otro impuesto"

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("29.00");
    }

    @Test
    void VAT_category_with_ica_salud_and_iva_sums_26() {
        category(7L, 8L, 9L);
        activeTaxes(tax(7L, "Ica", "impuesto Salud", 6.0), tax(8L, "SalD", "impuesto Salud", 8.0),
                tax(9L, "Iva", "Impuesto del gobierno", 12.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("26.00");
    }

    @Test
    void VAT_sum_over_100_falls_back() {
        category(7L, 8L);
        activeTaxes(tax(7L, "A", "a", 60.0), tax(8L, "B", "b", 50.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19");
    }

    @Test
    void VAT_inactive_taxes_are_not_considered() {
        category(7L);
        activeTaxes(); // getActiveTaxes no devuelve el impuesto 7 (inactivo)

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19");
    }

    @Test
    void VAT_category_with_a_non_iva_named_tax_uses_its_rate() {
        category(7L);
        activeTaxes(tax(7L, "RTF", "Retencion en la fuente", 3.5));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("3.50");
    }

    @Test
    void VAT_category_without_taxes_falls_back_to_default_rate() {
        category();
        properties.setDefaultVatRate(new BigDecimal("4"));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("4");
    }

    @Test
    void VAT_product_without_category_falls_back_without_querying_inventory() {
        assertThat(resolver.resolve(ENTERPRISE, null)).isEqualByComparingTo("19");

        verifyNoInteractions(categoryService, taxSearch);
    }

    @Test
    void VAT_missing_category_falls_back() {
        when(categoryService.findById(ENTERPRISE, CATEGORY_ID)).thenReturn(null);

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19");
    }

    @Test
    void VAT_category_lookup_failure_falls_back() {
        when(categoryService.findById(ENTERPRISE, CATEGORY_ID)).thenThrow(new IllegalStateException("boom"));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19");
    }

    @Test
    void VAT_tax_lookup_failure_falls_back() {
        category(7L);
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19");
    }

    @Test
    void VAT_invalid_rate_in_master_falls_back_and_extra_decimals_are_rounded() {
        category(7L);
        activeTaxes(tax(7L, "IVA", "IVA", 150.0));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("19");

        category(8L);
        activeTaxes(tax(8L, "IVA", "IVA", 3.456));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("3.46");
    }

    @Test
    void VAT_resolveAll_queries_active_taxes_once_and_each_category_once() {
        when(categoryService.findById(ENTERPRISE, 10L))
                .thenReturn(Category.builder().id(10L).taxes(List.of(7L)).build());
        when(categoryService.findById(ENTERPRISE, 11L))
                .thenReturn(Category.builder().id(11L).taxes(List.of(8L)).build());
        activeTaxes(tax(7L, "IVA5", "IVA", 5.0), tax(8L, "IVA19", "IVA", 19.0));

        Map<Long, BigDecimal> rates = resolver.resolveAll(ENTERPRISE, Arrays.asList(10L, 11L, 10L, null, 10L));

        assertThat(rates.get(10L)).isEqualByComparingTo("5.00");
        assertThat(rates.get(11L)).isEqualByComparingTo("19.00");
        assertThat(rates.get(null)).isEqualByComparingTo("19");
        verify(taxSearch, times(1)).getActiveTaxes(ENTERPRISE);
        verify(categoryService, times(1)).findById(ENTERPRISE, 10L);
        verify(categoryService, times(1)).findById(ENTERPRISE, 11L);
    }

    @Test
    void VAT_resolveAll_does_not_query_active_taxes_when_no_category_has_taxes() {
        category();

        Map<Long, BigDecimal> rates = resolver.resolveAll(ENTERPRISE, List.of(CATEGORY_ID));

        assertThat(rates.get(CATEGORY_ID)).isEqualByComparingTo("19");
        verifyNoInteractions(taxSearch);
    }

    @Test
    void VAT_resolveAll_failure_of_taxes_falls_back_for_every_category() {
        category(7L);
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        Map<Long, BigDecimal> rates = resolver.resolveAll(ENTERPRISE, List.of(CATEGORY_ID));

        assertThat(rates.get(CATEGORY_ID)).isEqualByComparingTo("19");
        verify(taxSearch, times(1)).getActiveTaxes(ENTERPRISE);
    }

    // ---- desglose por impuesto (presentacion; el calculo sigue con la suma) ----

    @Test
    void BREAKDOWN_three_taxes_give_three_components_with_name_and_rate_and_the_same_sum() {
        category(7L, 8L, 9L);
        activeTaxes(tax(7L, "Ica", "impuesto Salud", 6.0), tax(8L, "SalD", "impuesto Salud", 8.0),
                tax(9L, "Iva", "Impuesto del gobierno", 12.0));

        BillingVatRateResolver.Resolution resolution = resolver.resolveBreakdown(ENTERPRISE, CATEGORY_ID);

        assertThat(resolution.rate()).isEqualByComparingTo("26.00");
        assertThat(resolution.taxes()).extracting(TaxComponent::name).containsExactly("Ica", "SalD", "Iva");
        assertThat(resolution.taxes()).extracting(TaxComponent::rate)
                .containsExactly(new BigDecimal("6.00"), new BigDecimal("8.00"), new BigDecimal("12.00"));
        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo(resolution.rate());
    }

    @Test
    void BREAKDOWN_only_active_taxes_of_the_category_are_listed() {
        category(7L, 99L);
        activeTaxes(tax(7L, "Iva", "IVA", 19.0), tax(8L, "Ica", "otra categoria", 6.0));

        BillingVatRateResolver.Resolution resolution = resolver.resolveBreakdown(ENTERPRISE, CATEGORY_ID);

        assertThat(resolution.rate()).isEqualByComparingTo("19.00");
        assertThat(resolution.taxes()).containsExactly(TaxComponent.of("Iva", new BigDecimal("19")));
    }

    @Test
    void BREAKDOWN_tax_without_code_uses_its_description() {
        category(7L);
        activeTaxes(tax(7L, " ", "Impuesto al consumo", 8.0));

        assertThat(resolver.resolveBreakdown(ENTERPRISE, CATEGORY_ID).taxes())
                .extracting(TaxComponent::name).containsExactly("Impuesto al consumo");
    }

    @Test
    void BREAKDOWN_fallbacks_have_no_components() {
        assertThat(resolver.resolveBreakdown(ENTERPRISE, null).taxes()).isEmpty();

        category();
        assertThat(resolver.resolveBreakdown(ENTERPRISE, CATEGORY_ID).taxes()).isEmpty();

        category(7L, 8L);
        activeTaxes(tax(7L, "A", "a", 60.0), tax(8L, "B", "b", 50.0));
        BillingVatRateResolver.Resolution overRange = resolver.resolveBreakdown(ENTERPRISE, CATEGORY_ID);
        assertThat(overRange.rate()).isEqualByComparingTo("19");
        assertThat(overRange.taxes()).isEmpty();
    }

    @Test
    void BREAKDOWN_lookup_failure_has_no_components() {
        category(7L);
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        BillingVatRateResolver.Resolution resolution = resolver.resolveBreakdown(ENTERPRISE, CATEGORY_ID);

        assertThat(resolution.rate()).isEqualByComparingTo("19");
        assertThat(resolution.taxes()).isEmpty();
    }

    @Test
    void BREAKDOWN_resolveAllBreakdowns_keeps_the_single_query_per_operation() {
        when(categoryService.findById(ENTERPRISE, 10L))
                .thenReturn(Category.builder().id(10L).taxes(List.of(7L, 8L)).build());
        activeTaxes(tax(7L, "Iva", "IVA", 19.0), tax(8L, "Ica", "ICA", 6.0));

        Map<Long, BillingVatRateResolver.Resolution> all =
                resolver.resolveAllBreakdowns(ENTERPRISE, Arrays.asList(10L, null, 10L));

        assertThat(all.get(10L).rate()).isEqualByComparingTo("25.00");
        assertThat(all.get(10L).taxes()).extracting(TaxComponent::name).containsExactly("Iva", "Ica");
        assertThat(all.get(null).taxes()).isEmpty();
        verify(taxSearch, times(1)).getActiveTaxes(ENTERPRISE);
        verify(categoryService, times(1)).findById(ENTERPRISE, 10L);
    }

    @Test
    void VAT_iva_without_interest_counts_as_zero_like_the_front() {
        category(7L);
        activeTaxes(tax(7L, "IVA0", "IVA excluido", null));

        assertThat(resolver.resolve(ENTERPRISE, CATEGORY_ID)).isEqualByComparingTo("0");
    }
}
