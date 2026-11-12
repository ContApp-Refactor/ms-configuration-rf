package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.TaxComponent;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** D8/H0-1: precio de catalogo = cost del producto (escala 2), IVA = el de su categoria (resolver). */
@ExtendWith(MockitoExtension.class)
class BillingProductAdapterTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";
    private static final long PRODUCT_ID = 5L;
    private static final long UOM_ID = 3L;

    @Mock
    private IProductServicePort productService;
    @Mock
    private IUnitOfMeasureServicePort unitOfMeasureService;

    @Mock
    private BillingVatRateResolver vatRateResolver;

    @Mock
    private BillingLastPurchasePriceLookup lastPurchasePrices;

    private BillingProductAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(vatRateResolver.resolveBreakdown(any(), any())).thenReturn(rate("19"));
        adapter = new BillingProductAdapter(productService, unitOfMeasureService, vatRateResolver, lastPurchasePrices);
    }

    private static BillingVatRateResolver.Resolution rate(String value, TaxComponent... taxes) {
        return new BillingVatRateResolver.Resolution(new BigDecimal(value), List.of(taxes));
    }

    private static Product.ProductBuilder baseProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .code("PRO-1-5")
                .name("Cemento gris")
                .enterpriseId(ENTERPRISE)
                .unitOfMeasureId(UOM_ID)
                .cost(1234.5)
                .state(true);
    }

    @Test
    void PRODUCT_maps_price_from_cost_tax_from_properties_and_unit_name() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Kilogramo").abbreviation("kg").build());

        Optional<ProductSnapshot> result = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE);

        assertThat(result).isPresent();
        ProductSnapshot snapshot = result.get();
        assertThat(snapshot.productId()).isEqualTo(PRODUCT_ID);
        assertThat(snapshot.code()).isEqualTo("PRO-1-5");
        assertThat(snapshot.name()).isEqualTo("Cemento gris");
        assertThat(snapshot.unitOfMeasureId()).isEqualTo(UOM_ID);
        assertThat(snapshot.unitOfMeasureName()).isEqualTo("Kilogramo");
        assertThat(snapshot.catalogUnitPrice().value()).isEqualByComparingTo("1234.50");
        assertThat(snapshot.catalogUnitPrice().value().scale()).isEqualTo(2);
        assertThat(snapshot.catalogTaxRate().value()).isEqualByComparingTo("19.00");
        assertThat(snapshot.active()).isTrue();
    }

    @Test
    void PRODUCT_cost_is_rounded_half_up_to_two_decimals() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().cost(10.005).build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.PURCHASE).orElseThrow();

        assertThat(snapshot.catalogUnitPrice().value()).isEqualByComparingTo("10.01");
    }

    @Test
    void PRODUCT_same_catalog_price_for_sale_and_purchase_kinds() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        ProductSnapshot sale = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();
        ProductSnapshot purchase = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.PURCHASE).orElseThrow();

        assertThat(sale).isEqualTo(purchase);
    }

    @Test
    void PRODUCT_sale_never_queries_the_last_purchase_price() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().categoryId(10L).build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());
        when(vatRateResolver.resolveAllBreakdowns(eq(ENTERPRISE), any()))
                .thenReturn(new HashMap<>(Map.of(10L, rate("19"))));

        ProductSnapshot single = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();
        Map<Long, ProductSnapshot> batch = adapter.findProducts(ENTERPRISE, List.of(PRODUCT_ID), DocumentKind.SALE);

        assertThat(single.catalogUnitPrice().value()).isEqualByComparingTo("1234.50");
        assertThat(batch.get(PRODUCT_ID).catalogUnitPrice().value()).isEqualByComparingTo("1234.50");
        verifyNoInteractions(lastPurchasePrices);
    }

    @Test
    void PRODUCT_purchase_uses_the_last_purchase_price_instead_of_cost() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());
        when(lastPurchasePrices.findAll(ENTERPRISE, List.of(PRODUCT_ID)))
                .thenReturn(Map.of(PRODUCT_ID, Money.of(new BigDecimal("987.65"))));

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.PURCHASE).orElseThrow();

        assertThat(snapshot.catalogUnitPrice().value()).isEqualByComparingTo("987.65");
        assertThat(snapshot.catalogUnitPrice().value().scale()).isEqualTo(2);
        assertThat(snapshot.code()).isEqualTo("PRO-1-5");
    }

    @Test
    void PRODUCT_purchase_without_last_price_falls_back_to_cost() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());
        when(lastPurchasePrices.findAll(ENTERPRISE, List.of(PRODUCT_ID))).thenReturn(Map.of());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.PURCHASE).orElseThrow();

        assertThat(snapshot.catalogUnitPrice().value()).isEqualByComparingTo("1234.50");
    }

    @Test
    void PRODUCT_purchase_batch_mixes_last_price_and_cost_fallback_with_one_lookup() {
        when(vatRateResolver.resolveAllBreakdowns(eq(ENTERPRISE), any())).thenReturn(new HashMap<>(Map.of(10L, rate("19"))));
        when(productService.findById(1L, ENTERPRISE)).thenReturn(baseProduct().id(1L).categoryId(10L).build());
        when(productService.findById(2L, ENTERPRISE)).thenReturn(baseProduct().id(2L).categoryId(10L).cost(50.0).build());
        when(productService.findById(3L, ENTERPRISE)).thenThrow(new ProductNotFoundException(3L));
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());
        when(lastPurchasePrices.findAll(eq(ENTERPRISE), any()))
                .thenReturn(Map.of(1L, Money.of(new BigDecimal("77.10"))));

        Map<Long, ProductSnapshot> result = adapter.findProducts(ENTERPRISE, List.of(1L, 2L, 3L),
                DocumentKind.PURCHASE);

        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L).catalogUnitPrice().value()).isEqualByComparingTo("77.10");
        assertThat(result.get(2L).catalogUnitPrice().value()).isEqualByComparingTo("50.00");
        ArgumentCaptor<Collection<Long>> ids = ArgumentCaptor.forClass(Collection.class);
        verify(lastPurchasePrices, times(1)).findAll(eq(ENTERPRISE), ids.capture());
        assertThat(ids.getValue()).containsExactly(1L, 2L);
    }

    @Test
    void PRODUCT_tax_rate_follows_the_vat_resolved_for_the_product_category() {
        when(vatRateResolver.resolveBreakdown(ENTERPRISE, 10L)).thenReturn(rate("5"));
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().categoryId(10L).build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();

        assertThat(snapshot.catalogTaxRate().value()).isEqualByComparingTo("5.00");
    }

    @Test
    void PRODUCT_snapshot_carries_the_tax_breakdown_of_the_category() {
        when(vatRateResolver.resolveBreakdown(ENTERPRISE, 10L)).thenReturn(rate("25",
                TaxComponent.of("Iva", new BigDecimal("19")), TaxComponent.of("Ica", new BigDecimal("6"))));
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().categoryId(10L).build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();

        assertThat(snapshot.catalogTaxRate().value()).isEqualByComparingTo("25.00");
        assertThat(snapshot.catalogTaxes()).extracting(TaxComponent::name).containsExactly("Iva", "Ica");
    }

    @Test
    void PRODUCT_default_vat_snapshot_has_no_breakdown() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        assertThat(adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow().catalogTaxes())
                .isEmpty();
    }

    @Test
    void PRODUCT_missing_unit_of_measure_yields_null_name_but_keeps_id() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenThrow(new UnitOfMeasureNotFoundException(UOM_ID));

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();

        assertThat(snapshot.unitOfMeasureId()).isEqualTo(UOM_ID);
        assertThat(snapshot.unitOfMeasureName()).isNull();
    }

    @Test
    void PRODUCT_without_unit_of_measure_id_does_not_look_it_up() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().unitOfMeasureId(null).build());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();

        assertThat(snapshot.unitOfMeasureId()).isNull();
        assertThat(snapshot.unitOfMeasureName()).isNull();
        verifyNoInteractions(unitOfMeasureService);
    }

    @Test
    void PRODUCT_not_found_exception_becomes_empty() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenThrow(new ProductNotFoundException(PRODUCT_ID));

        assertThat(adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE)).isEmpty();
        verifyNoInteractions(unitOfMeasureService);
    }

    @Test
    void PRODUCT_inactive_product_is_returned_with_active_false() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().state(false).build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();

        assertThat(snapshot.active()).isFalse();
    }

    @Test
    void PRODUCT_batch_resolves_vat_once_for_the_distinct_categories() {
        when(vatRateResolver.resolveAllBreakdowns(eq(ENTERPRISE), any())).thenReturn(
                new HashMap<>(Map.of(10L, rate("5"), 11L, rate("16"))));
        when(productService.findById(1L, ENTERPRISE)).thenReturn(baseProduct().id(1L).categoryId(10L).build());
        when(productService.findById(2L, ENTERPRISE)).thenReturn(baseProduct().id(2L).categoryId(11L).build());
        when(productService.findById(3L, ENTERPRISE)).thenThrow(new ProductNotFoundException(3L));
        when(productService.findById(4L, ENTERPRISE)).thenReturn(baseProduct().id(4L).categoryId(10L).build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        Map<Long, ProductSnapshot> result = adapter.findProducts(ENTERPRISE, List.of(1L, 2L, 3L, 4L, 1L),
                DocumentKind.SALE);

        assertThat(result).containsOnlyKeys(1L, 2L, 4L);
        assertThat(result.get(1L).catalogTaxRate().value()).isEqualByComparingTo("5.00");
        assertThat(result.get(2L).catalogTaxRate().value()).isEqualByComparingTo("16.00");
        assertThat(result.get(4L).catalogTaxRate().value()).isEqualByComparingTo("5.00");
        ArgumentCaptor<Collection<Long>> categories = ArgumentCaptor.forClass(Collection.class);
        verify(vatRateResolver, times(1)).resolveAllBreakdowns(eq(ENTERPRISE), categories.capture());
        assertThat(categories.getValue()).containsExactlyInAnyOrder(10L, 11L);
        verify(productService, times(1)).findById(1L, ENTERPRISE);
    }

    @Test
    void PRODUCT_batch_of_five_products_in_one_category_queries_active_taxes_once() {
        ICategoryServicePort categoryService = mock(ICategoryServicePort.class);
        ITaxSearchInputPort taxSearch = mock(ITaxSearchInputPort.class);
        BillingVatRateResolver real = new BillingVatRateResolver(categoryService, taxSearch, new BillingProperties());
        BillingProductAdapter realAdapter = new BillingProductAdapter(productService, unitOfMeasureService, real, lastPurchasePrices);
        when(categoryService.findById(ENTERPRISE, 10L))
                .thenReturn(Category.builder().id(10L).taxes(List.of(7L)).build());
        when(taxSearch.getActiveTaxes(ENTERPRISE)).thenReturn(List.of(Tax.builder().id(7L).idEnterprise(ENTERPRISE)
                .code("IVA5").description("IVA").interest(5.0).status(true).build()));
        List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L);
        for (long id : ids) {
            when(productService.findById(id, ENTERPRISE)).thenReturn(baseProduct().id(id).categoryId(10L).build());
        }
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        Map<Long, ProductSnapshot> result = realAdapter.findProducts(ENTERPRISE, ids, DocumentKind.SALE);

        assertThat(result).hasSize(5);
        assertThat(result.values()).allSatisfy(s -> assertThat(s.catalogTaxRate().value()).isEqualByComparingTo("5.00"));
        assertThat(result.values()).allSatisfy(s -> assertThat(s.catalogTaxes())
                .containsExactly(TaxComponent.of("IVA5", new BigDecimal("5"))));
        verify(taxSearch, times(1)).getActiveTaxes(ENTERPRISE);
        verify(categoryService, times(1)).findById(ENTERPRISE, 10L);
    }

    @Test
    void PRODUCT_other_errors_propagate() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE))
                .isInstanceOf(IllegalStateException.class);
    }
}
