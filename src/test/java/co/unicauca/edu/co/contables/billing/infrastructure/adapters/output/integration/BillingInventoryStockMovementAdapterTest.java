package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emitted;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedInvoice;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Integracion de stock: cada documento mueve el kardex que corresponde al metodo de valoracion de la empresa. */
class BillingInventoryStockMovementAdapterTest {

    private IEnterpriseService enterprises;
    private co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort wa;
    private co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort peps;
    private BillingInventoryStockMovementAdapter adapter;

    @BeforeEach
    void setUp() {
        enterprises = mock(IEnterpriseService.class);
        wa = mock(co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort.class);
        peps = mock(co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort.class);
        adapter = new BillingInventoryStockMovementAdapter(enterprises, wa, peps);
        useMethod(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
    }

    private void useMethod(InventoryConfigurationTypeEnum method) {
        Enterprise enterprise = mock(Enterprise.class);
        when(enterprise.getInventoryConfigurationType()).thenReturn(method);
        when(enterprises.findById(UUID.fromString(ENTERPRISE_ID))).thenReturn(enterprise);
    }

    private static BillingDocument returnOf(DocumentType invoiceType) {
        BillingDocument original = emitted(invoiceType, 7L);
        return emittedReturnOf(original, uuid(9), 3L, returnLineOf(original, 1, "1"));
    }

    private static ArgumentCaptor<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex> waCaptor() {
        return ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex.class);
    }

    private static ArgumentCaptor<co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex> pepsCaptor() {
        return ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex.class);
    }

    // ---------- promedio ponderado: un caso por tipo de documento ----------

    @Test
    void weighted_average_sale_invoice_calls_registerSale() {
        adapter.onEmitted(emitted(DocumentType.SALE_INVOICE, 7L));

        var captor = waCaptor();
        verify(wa).registerSale(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FV-000007");
        assertThat(kardex.getType()).isEqualTo(MovementType.SALE);
        assertThat(kardex.getProductId()).isEqualTo(1L);
        assertThat(kardex.getQuantity()).isEqualTo(2);
        assertThat(kardex.getUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(kardex.getDate()).isNull();
        verifyNoInteractions(peps);
    }

    @Test
    void weighted_average_purchase_invoice_calls_registerPurchase() {
        adapter.onEmitted(emitted(DocumentType.PURCHASE_INVOICE, 7L));

        var captor = waCaptor();
        verify(wa).registerPurchase(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FC-000007");
        assertThat(kardex.getType()).isEqualTo(MovementType.PURCHASE);
        assertThat(kardex.getQuantity()).isEqualTo(2);
        verifyNoInteractions(peps);
    }

    @Test
    void weighted_average_sale_return_calls_registerReturnOnSale_with_the_original_invoice_number() {
        adapter.onEmitted(returnOf(DocumentType.SALE_INVOICE));

        var captor = waCaptor();
        verify(wa).registerReturnOnSale(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FV-000007");
        assertThat(kardex.getType()).isEqualTo(MovementType.SALESRETURN);
        assertThat(kardex.getQuantity()).isEqualTo(1);
        assertThat(kardex.getDetails()).contains("NC-000003");
        verifyNoInteractions(peps);
    }

    @Test
    void weighted_average_purchase_return_calls_registerReturnOnPurchase_with_the_original_invoice_number() {
        adapter.onEmitted(returnOf(DocumentType.PURCHASE_INVOICE));

        var captor = waCaptor();
        verify(wa).registerReturnOnPurchase(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FC-000007");
        assertThat(kardex.getType()).isEqualTo(MovementType.PURCHASERETURN);
        assertThat(kardex.getQuantity()).isEqualTo(1);
        verifyNoInteractions(peps);
    }

    // ---------- PEPS: un caso por tipo de documento ----------

    @Test
    void peps_sale_invoice_calls_registerSale() {
        useMethod(InventoryConfigurationTypeEnum.PEPS);
        adapter.onEmitted(emitted(DocumentType.SALE_INVOICE, 7L));

        var captor = pepsCaptor();
        verify(peps).registerSale(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FV-000007");
        assertThat(kardex.getProduct().getProductId()).isEqualTo(1L);
        assertThat(kardex.getQuantity()).isEqualTo(2);
        assertThat(kardex.getUnitPrice()).isEqualByComparingTo("100.00");
        verifyNoInteractions(wa);
    }

    @Test
    void peps_purchase_invoice_calls_registerPurchase() {
        useMethod(InventoryConfigurationTypeEnum.PEPS);
        adapter.onEmitted(emitted(DocumentType.PURCHASE_INVOICE, 7L));

        var captor = pepsCaptor();
        verify(peps).registerPurchase(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FC-000007");
        assertThat(kardex.getQuantity()).isEqualTo(2);
        verifyNoInteractions(wa);
    }

    @Test
    void peps_sale_return_calls_registerSaleReturn_with_the_original_invoice_number() {
        useMethod(InventoryConfigurationTypeEnum.PEPS);
        adapter.onEmitted(returnOf(DocumentType.SALE_INVOICE));

        var captor = pepsCaptor();
        verify(peps).registerSaleReturn(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FV-000007");
        assertThat(kardex.getQuantity()).isEqualTo(1);
        verifyNoInteractions(wa);
    }

    @Test
    void peps_purchase_return_calls_registerPurchaseReturn_with_the_original_invoice_number() {
        useMethod(InventoryConfigurationTypeEnum.PEPS);
        adapter.onEmitted(returnOf(DocumentType.PURCHASE_INVOICE));

        var captor = pepsCaptor();
        verify(peps).registerPurchaseReturn(captor.capture());
        var kardex = captor.getValue();
        assertThat(kardex.getFactCode()).isEqualTo("FC-000007");
        assertThat(kardex.getQuantity()).isEqualTo(1);
        verifyNoInteractions(wa);
    }

    // ---------- agrupacion, cantidades, configuracion ----------

    @Test
    void lines_of_the_same_product_are_grouped_with_summed_quantity_and_weighted_price() {
        BillingDocument invoice = emittedInvoice(uuid(1), DocumentType.SALE_INVOICE, 7L, List.of(
                lineOf(1, product(1L, "100.00", "19"), "2"),
                lineOf(2, product(1L, "200.00", "19"), "3"),
                lineOf(3, product(2L, "50.00", "19"), "1")));

        adapter.onEmitted(invoice);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex> captor =
                ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex.class);
        verify(wa, org.mockito.Mockito.times(2)).registerSale(captor.capture());
        var first = captor.getAllValues().get(0);
        var second = captor.getAllValues().get(1);
        assertThat(first.getProductId()).isEqualTo(1L);
        assertThat(first.getQuantity()).isEqualTo(5);
        assertThat(first.getUnitPrice()).isEqualByComparingTo(new BigDecimal("160.00"));
        assertThat(second.getProductId()).isEqualTo(2L);
        assertThat(second.getQuantity()).isEqualTo(1);
    }

    @Test
    void whole_quantities_with_decimal_scale_are_accepted() {
        BillingDocument invoice = emittedInvoice(uuid(1), DocumentType.SALE_INVOICE, 7L,
                List.of(lineOf(1, product(1L), "3.000")));

        adapter.onEmitted(invoice);

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex> captor =
                ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex.class);
        verify(wa).registerSale(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
    }

    @Test
    void a_non_integer_quantity_is_rejected_before_calling_inventory() {
        BillingDocument invoice = emittedInvoice(uuid(1), DocumentType.SALE_INVOICE, 7L, List.of(
                lineOf(1, product(1L), "1"),
                lineOf(2, product(2L), "1.500")));

        assertThatThrownBy(() -> adapter.onEmitted(invoice))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_QUANTITY_NOT_SUPPORTED));
        verifyNoInteractions(wa, peps);
    }

    @Test
    void a_null_inventory_configuration_is_rejected() {
        useMethod(null);

        assertThatThrownBy(() -> adapter.onEmitted(emitted(DocumentType.SALE_INVOICE, 7L)))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_CONFIG_MISSING));
        verifyNoInteractions(wa, peps);
    }

    @Test
    void a_missing_enterprise_is_rejected() {
        when(enterprises.findById(any(UUID.class))).thenReturn(null);

        assertThatThrownBy(() -> adapter.onEmitted(emitted(DocumentType.SALE_INVOICE, 7L)))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_ENTERPRISE_NOT_FOUND));
        verifyNoInteractions(wa, peps);
    }

    // ---------- traduccion de errores ----------

    @Test
    void inventory_business_errors_are_translated_to_billing_codes() {
        when(wa.registerSale(any())).thenThrow(
                new BusinessRuleException(400, "Regla de negocio violada: Stock insuficiente. Solicitado: 5, Disponible: 2."));

        assertThatThrownBy(() -> adapter.onEmitted(emitted(DocumentType.SALE_INVOICE, 7L)))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
                    assertThat(e.getMessage()).contains("producto 1");
                });
    }

    @Test
    void an_unknown_inventory_failure_is_never_a_generic_error() {
        when(wa.registerPurchase(any())).thenThrow(new IllegalStateException("conexion perdida"));

        assertThatThrownBy(() -> adapter.onEmitted(emitted(DocumentType.PURCHASE_INVOICE, 7L)))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_UNAVAILABLE));
    }

    @Test
    void the_first_failing_product_stops_the_remaining_calls() {
        BillingDocument invoice = emittedInvoice(uuid(1), DocumentType.SALE_INVOICE, 7L, List.of(
                lineOf(1, product(1L), "1"), lineOf(2, product(2L), "1")));
        when(wa.registerSale(any())).thenThrow(new BusinessRuleException(400, "Stock insuficiente"));

        assertThatThrownBy(() -> adapter.onEmitted(invoice)).isInstanceOf(BillingException.class);
        verify(wa, org.mockito.Mockito.times(1)).registerSale(any());
    }

    // ---------- anulacion ----------

    @Test
    void annulment_is_rejected_because_inventory_has_no_inverse_operation() {
        BillingDocument invoice = emitted(DocumentType.SALE_INVOICE, 7L);

        assertThatThrownBy(() -> adapter.onAnnulled(invoice))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED);
                    // numero legible (antes salia el toString del record DocumentNumber)
                    assertThat(e.getMessage()).contains("FV-000007").doesNotContain("DocumentNumber[")
                            .contains("registre una devolucion");
                });
        verify(wa, never()).registerSale(any());
        verifyNoInteractions(peps);
    }

    @Test
    void annulment_of_a_return_does_not_suggest_another_return() {
        assertThatThrownBy(() -> adapter.onAnnulled(returnOf(DocumentType.SALE_INVOICE)))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED);
                    assertThat(e.getMessage()).contains("NC-000003").doesNotContain("registre una devolucion");
                });
    }

    @Test
    void errors_name_the_product_by_id_code_and_name_and_returns_cite_the_original_invoice() {
        when(wa.registerReturnOnSale(any())).thenThrow(
                new co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized
                        .EntityDoesNotExistException(404, "GC-003: Entity not found -> Original sale not found"));

        assertThatThrownBy(() -> adapter.onEmitted(returnOf(DocumentType.SALE_INVOICE)))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
                    assertThat(e.getMessage()).contains("producto 1 (P1 Producto 1)").contains("FV-000007");
                });
    }
}
