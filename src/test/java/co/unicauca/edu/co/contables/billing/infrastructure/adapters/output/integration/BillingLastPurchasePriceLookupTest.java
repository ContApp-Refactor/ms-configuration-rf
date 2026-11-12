package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Precio sugerido de la factura de compra: ultimo precio de entrada en PEPS y costo promedio
 * ponderado vigente en promedio ponderado; ante cualquier problema el producto queda fuera
 * (fallback a cost, sin error).
 */
@ExtendWith(MockitoExtension.class)
class BillingLastPurchasePriceLookupTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";
    private static final UUID ENTERPRISE_UUID = UUID.fromString(ENTERPRISE);

    @Mock
    private IEnterpriseService enterpriseService;
    @Mock
    private co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort waKardex;
    @Mock
    private co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort pepsKardex;

    private BillingLastPurchasePriceLookup lookup() {
        return new BillingLastPurchasePriceLookup(enterpriseService, waKardex, pepsKardex);
    }

    private void method(InventoryConfigurationTypeEnum method) {
        Enterprise enterprise = org.mockito.Mockito.mock(Enterprise.class);
        when(enterprise.getInventoryConfigurationType()).thenReturn(method);
        when(enterpriseService.findById(ENTERPRISE_UUID)).thenReturn(enterprise);
    }

    @Test
    void weighted_average_reads_the_current_average_cost_and_rounds_half_up_to_cents() {
        method(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        when(waKardex.findCurrentAverageUnitCost(7L)).thenReturn(Optional.of(new BigDecimal("1234.565")));

        Map<Long, Money> prices = lookup().findAll(ENTERPRISE, List.of(7L));

        assertThat(prices).containsOnlyKeys(7L);
        assertThat(prices.get(7L).value()).isEqualByComparingTo("1234.57");
        assertThat(prices.get(7L).toCents()).isEqualTo(123457L);
        verifyNoInteractions(pepsKardex);
        verify(waKardex, never()).findLastEntryUnitPrice(any());
    }

    @Test
    void peps_reads_the_peps_kardex() {
        method(InventoryConfigurationTypeEnum.PEPS);
        when(pepsKardex.findLastEntryUnitPrice(7L)).thenReturn(Optional.of(new BigDecimal("80.00")));

        Map<Long, Money> prices = lookup().findAll(ENTERPRISE, List.of(7L));

        assertThat(prices.get(7L).value()).isEqualByComparingTo("80.00");
        verifyNoInteractions(waKardex);
    }

    @Test
    void peps_never_uses_the_average_cost() {
        method(InventoryConfigurationTypeEnum.PEPS);
        when(pepsKardex.findLastEntryUnitPrice(7L)).thenReturn(Optional.of(new BigDecimal("130.00")));

        lookup().findAll(ENTERPRISE, List.of(7L));

        verify(waKardex, never()).findCurrentAverageUnitCost(any());
    }

    @Test
    void weighted_average_without_a_positive_average_falls_back_to_cost() {
        method(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        when(waKardex.findCurrentAverageUnitCost(7L)).thenReturn(Optional.empty());

        assertThat(lookup().findAll(ENTERPRISE, List.of(7L))).isEmpty();
        verify(waKardex, never()).findLastEntryUnitPrice(any());
    }

    @Test
    void product_without_entries_is_left_out_so_the_caller_falls_back_to_cost() {
        method(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        when(waKardex.findCurrentAverageUnitCost(7L)).thenReturn(Optional.empty());
        when(waKardex.findCurrentAverageUnitCost(8L)).thenReturn(Optional.of(new BigDecimal("5")));

        Map<Long, Money> prices = lookup().findAll(ENTERPRISE, List.of(7L, 8L));

        assertThat(prices).containsOnlyKeys(8L);
    }

    @Test
    void negative_price_is_discarded() {
        method(InventoryConfigurationTypeEnum.PEPS);
        when(pepsKardex.findLastEntryUnitPrice(7L)).thenReturn(Optional.of(new BigDecimal("-1")));

        assertThat(lookup().findAll(ENTERPRISE, List.of(7L))).isEmpty();
    }

    @Test
    void zero_price_is_discarded_so_the_caller_falls_back_to_cost() {
        method(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        when(waKardex.findCurrentAverageUnitCost(7L)).thenReturn(Optional.of(new BigDecimal("0.00")));

        assertThat(lookup().findAll(ENTERPRISE, List.of(7L))).isEmpty();
    }

    @Test
    void missing_valuation_method_falls_back_without_querying_any_kardex() {
        method(null);

        assertThat(lookup().findAll(ENTERPRISE, List.of(7L))).isEmpty();
        verifyNoInteractions(waKardex, pepsKardex);
    }

    @Test
    void unknown_enterprise_or_invalid_id_falls_back_without_error() {
        when(enterpriseService.findById(ENTERPRISE_UUID)).thenReturn(null);

        assertThat(lookup().findAll(ENTERPRISE, List.of(7L))).isEmpty();
        assertThat(lookup().findAll("not-a-uuid", List.of(7L))).isEmpty();
        verifyNoInteractions(waKardex, pepsKardex);
    }

    @Test
    void enterprise_service_failure_falls_back_without_error() {
        when(enterpriseService.findById(ENTERPRISE_UUID)).thenThrow(new IllegalStateException("boom"));

        assertThat(lookup().findAll(ENTERPRISE, List.of(7L))).isEmpty();
    }

    @Test
    void kardex_query_failure_falls_back_for_that_product_only() {
        method(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        when(waKardex.findCurrentAverageUnitCost(7L)).thenThrow(new IllegalStateException("db down"));
        when(waKardex.findCurrentAverageUnitCost(8L)).thenReturn(Optional.of(new BigDecimal("9.50")));

        Map<Long, Money> prices = lookup().findAll(ENTERPRISE, List.of(7L, 8L));

        assertThat(prices).containsOnlyKeys(8L);
    }

    @Test
    void empty_input_does_not_touch_anything() {
        assertThat(lookup().findAll(ENTERPRISE, List.of())).isEmpty();
        assertThat(lookup().findAll(ENTERPRISE, null)).isEmpty();
        verify(enterpriseService, never()).findById(any());
        verifyNoInteractions(waKardex, pepsKardex);
    }
}
