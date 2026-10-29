package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.infrastructure.config.BillingProperties;
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

/** D8/H0-1: precio de catalogo = cost del producto (escala 2), IVA = billing.default-vat-rate. */
@ExtendWith(MockitoExtension.class)
class BillingProductAdapterTest {

    private static final String ENTERPRISE = "11111111-1111-1111-1111-111111111111";
    private static final long PRODUCT_ID = 5L;
    private static final long UOM_ID = 3L;

    @Mock
    private IProductServicePort productService;
    @Mock
    private IUnitOfMeasureServicePort unitOfMeasureService;

    private BillingProperties properties;
    private BillingProductAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new BillingProperties();
        adapter = new BillingProductAdapter(productService, unitOfMeasureService, properties);
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
    void PRODUCT_tax_rate_follows_configured_default_vat() {
        properties.setDefaultVatRate(new BigDecimal("5"));
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenReturn(baseProduct().build());
        when(unitOfMeasureService.findByIdAndEnterpriseId(UOM_ID, ENTERPRISE))
                .thenReturn(UnitOfMeasure.builder().id(UOM_ID).name("Unidad").build());

        ProductSnapshot snapshot = adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE).orElseThrow();

        assertThat(snapshot.catalogTaxRate().value()).isEqualByComparingTo("5.00");
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
    void PRODUCT_other_errors_propagate() {
        when(productService.findById(PRODUCT_ID, ENTERPRISE)).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> adapter.findProduct(ENTERPRISE, PRODUCT_ID, DocumentKind.SALE))
                .isInstanceOf(IllegalStateException.class);
    }
}
