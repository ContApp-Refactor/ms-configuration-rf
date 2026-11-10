package co.unicauca.edu.co.contables.billing.integration;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.CUSTOMER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.ENTERPRISE_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_ID;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.SUPPLIER_INVOICE_NUMBER;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.customer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.issuer;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.supplier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.output.EnterprisePort;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ProductPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.ThirdPartyPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.transaction.BillingTransactionalCommandUseCases;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration.BillingLastPurchasePriceLookup;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.StockRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Nota 17 de punta a punta con el contexto COMPLETO (H2): casos de uso transaccionales REALES de
 * Billing + adaptador real {@code BillingInventoryStockMovementAdapter} + servicios REALES de kardex
 * (promedio ponderado y PEPS) + modulo de stock real + persistencia real. Solo se simulan las
 * fronteras que no son de esta integracion: terceros, catalogo de productos y empresa emisora de
 * Billing, el metodo de valoracion de la empresa ({@link IEnterpriseService}), el calendario contable
 * de Inventario y la publicacion RabbitMQ de "producto usado" de PEPS.
 *
 * <p>Las filas de {@code kardex_products}/{@code peps_product}/{@code stock_entity} que en produccion
 * llegan por RabbitMQ al crear el producto se siembran directamente. Cada prueba usa productos
 * propios (los datos no se limpian entre pruebas del mismo contexto).
 *
 * <p>Demuestra: compra ingresa, venta descuenta, devolucion de venta repone, devolucion de compra
 * egresa; una venta sin existencias FALLA con {@code BILLING_STOCK_INSUFFICIENT} y el documento queda
 * en BORRADOR sin numero, sin consumir consecutivo, sin evento en el outbox y sin tocar kardex ni
 * stock; repetir la emision no mueve el kardex otra vez; anular se rechaza y deja todo igual.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class BillingInventoryStockIntegrationTest {

    private static final String TENANT = "tenant-stock-it";
    private static final AtomicLong NEXT_PRODUCT = new AtomicLong(980_000L);

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;
    @MockitoBean
    private ThirdPartyPort thirdParties;
    @MockitoBean
    private ProductPort products;
    @MockitoBean
    private EnterprisePort enterprises;
    @MockitoBean
    private PaymentCatalogPort paymentCatalog;
    @MockitoBean
    private IEnterpriseService enterpriseService;
    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.kardex.domain.port.config.IConfigClientPort waCalendar;
    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IConfigClientPort pepsCalendar;
    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductEventPort pepsUsageEvents;

    @Autowired
    private BillingTransactionalCommandUseCases billing;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository
            waProducts;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository
            pepsProducts;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort waQuery;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort pepsQuery;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort waCommand;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort pepsCommand;
    @Autowired
    private BillingLastPurchasePriceLookup lastPurchasePrices;
    @Autowired
    private StockRepository stocks;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private final Map<Long, ProductSnapshot> catalog = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT);
        when(paymentCatalog.findPaymentMethod(anyString(), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(Optional.of(new PaymentCatalogPort.PaymentMethodRef(1L, "Efectivo", true)));
        when(waCalendar.isValidAccountingDate(anyString(), any())).thenReturn(true);
        when(pepsCalendar.isValidAccountingDate(anyString(), any())).thenReturn(true);
        when(enterprises.findIssuer(ENTERPRISE_ID)).thenReturn(Optional.of(issuer()));
        when(thirdParties.findCounterparty(eq(ENTERPRISE_ID), eq(CUSTOMER_ID), eq(CounterpartyRole.CUSTOMER)))
                .thenReturn(Optional.of(customer()));
        when(thirdParties.findCounterparty(eq(ENTERPRISE_ID), eq(SUPPLIER_ID), eq(CounterpartyRole.SUPPLIER)))
                .thenReturn(Optional.of(supplier()));
        when(products.findProduct(eq(ENTERPRISE_ID), anyLong(), any()))
                .thenAnswer(inv -> Optional.ofNullable(catalog.get(inv.<Long>getArgument(1))));
        when(products.findProducts(eq(ENTERPRISE_ID), any(), any())).thenAnswer(inv -> {
            Map<Long, ProductSnapshot> found = new LinkedHashMap<>();
            for (Long id : inv.<Collection<Long>>getArgument(1)) {
                if (catalog.containsKey(id)) {
                    found.put(id, catalog.get(id));
                }
            }
            return found;
        });
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ---------- siembra y lectura ----------

    private void valuation(InventoryConfigurationTypeEnum method) {
        Enterprise enterprise = mock(Enterprise.class);
        when(enterprise.getInventoryConfigurationType()).thenReturn(method);
        when(enterpriseService.findById(UUID.fromString(ENTERPRISE_ID))).thenReturn(enterprise);
    }

    /** Producto replicado en Inventario (lo que hace RabbitMQ al crearlo), sin existencias. */
    private long newInventoryProduct(InventoryConfigurationTypeEnum method) {
        long productId = NEXT_PRODUCT.incrementAndGet();
        catalog.put(productId, product(productId, "100.00", "19"));
        inTx(() -> {
            if (method == InventoryConfigurationTypeEnum.PEPS) {
                var p = new co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity
                        .ProductEntity();
                p.setProductId(productId);
                p.setName("Producto " + productId);
                p.setReference("REF-" + productId);
                p.setPresentation("Unidad");
                p.setEnterpriseId(ENTERPRISE_ID);
                p.setState(true);
                pepsProducts.save(p);
            } else {
                var p = new co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity
                        .ProductEntity();
                p.setProductId(productId);
                p.setName("Producto " + productId);
                p.setReference("REF-" + productId);
                p.setPresentation("Unidad");
                p.setEnterpriseId(ENTERPRISE_ID);
                p.setState(true);
                waProducts.save(p);
            }
            StockEntity stock = new StockEntity();
            stock.setProductId(productId);
            stock.setName("Producto " + productId);
            stock.setEnterpriseId(ENTERPRISE_ID);
            stock.setQuantity(0);
            stock.setPrice(BigDecimal.ZERO);
            stock.setState(true);
            stocks.save(stock);
            return null;
        });
        return productId;
    }

    private <T> T inTx(java.util.function.Supplier<T> action) {
        return new TransactionTemplate(transactionManager).execute(status -> action.get());
    }

    private int stockOf(long productId) {
        return jdbc.queryForObject("select quantity from stock_entity where product_id = ?", Integer.class, productId);
    }

    /** Saldo del ultimo movimiento de kardex promedio ponderado. */
    private int waBalanceOf(long productId) {
        List<Integer> balances = jdbc.queryForList(
                "select balance_quantity from kardex where product_id = ? order by date desc, id desc",
                Integer.class, productId);
        return balances.isEmpty() ? 0 : balances.get(0);
    }

    private long waMovements(long productId) {
        return jdbc.queryForObject("select count(*) from kardex where product_id = ?", Long.class, productId);
    }

    private long outboxRows() {
        return jdbc.queryForObject("select count(*) from billing_outbox_events", Long.class);
    }

    private long lastNumber(DocumentType type) {
        Long value = jdbc.queryForObject("select coalesce(max(last_number), 0) from billing_document_sequences"
                + " where document_type = ?", Long.class, type.name());
        return value == null ? 0L : value;
    }

    // ---------- acciones de Billing ----------

    private BillingDocument invoice(DocumentType type, long productId, String quantity) {
        long counterparty = type == DocumentType.SALE_INVOICE ? CUSTOMER_ID : SUPPLIER_ID;
        String supplierNumber = type == DocumentType.PURCHASE_INVOICE ? SUPPLIER_INVOICE_NUMBER + "-" + productId + "-"
                + NEXT_SUPPLIER_INVOICE.getAndIncrement() : null; // unico por proveedor (regla de unicidad)
        BillingDocument draft = billing.create(ENTERPRISE_ID, type, new InvoiceDraftCommand(counterparty, null,
                supplierNumber, List.of(new InvoiceLineCommand(productId, new BigDecimal(quantity), null, null)),
                null, new InvoiceDraftCommand.PaymentCommand(1L)));
        return draft;
    }

    private BillingDocument emit(DocumentType type, BillingDocument draft) {
        return billing.emit(ENTERPRISE_ID, type, draft.id(), null);
    }

    private BillingDocument emittedInvoice(DocumentType type, long productId, String quantity) {
        return emit(type, invoice(type, productId, quantity));
    }

    private BillingDocument emittedReturn(DocumentType type, BillingDocument original, String quantity) {
        BillingDocument draft = billing.create(ENTERPRISE_ID, type, new ReturnDraftCommand(original.id(), null,
                "devolucion de prueba", List.of(new ReturnLineCommand(1, new BigDecimal(quantity))), null));
        return emit(type, draft);
    }

    /** Estado y numero persistidos (lectura directa: lo que quedo tras commit o rollback). */
    private record Persisted(DocumentStatus status, Long number) {
    }

    private Persisted reload(DocumentType type, UUID id) {
        return jdbc.queryForObject("select status, number_value from billing_documents where cast(id as varchar) = ?",
                (rs, n) -> new Persisted(DocumentStatus.valueOf(rs.getString("status")),
                        (Long) rs.getObject("number_value", Long.class)),
                id.toString());
    }

    // ---------- promedio ponderado ----------

    @Test
    void weighted_average_purchase_adds_sale_deducts_and_returns_move_stock_back() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);

        BillingDocument purchase = emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "10");
        assertThat(purchase.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(waBalanceOf(productId)).isEqualTo(10);
        assertThat(stockOf(productId)).isEqualTo(10);

        BillingDocument sale = emittedInvoice(DocumentType.SALE_INVOICE, productId, "4");
        assertThat(waBalanceOf(productId)).as("venta descuenta").isEqualTo(6);
        assertThat(stockOf(productId)).isEqualTo(6);

        emittedReturn(DocumentType.SALE_RETURN, sale, "1");
        assertThat(waBalanceOf(productId)).as("devolucion de venta repone").isEqualTo(7);
        assertThat(stockOf(productId)).isEqualTo(7);

        emittedReturn(DocumentType.PURCHASE_RETURN, purchase, "2");
        assertThat(waBalanceOf(productId)).as("devolucion de compra egresa").isEqualTo(5);
        assertThat(stockOf(productId)).isEqualTo(5);
        assertThat(waMovements(productId)).isEqualTo(4);
    }

    @Test
    void weighted_average_sale_without_enough_stock_rolls_back_everything() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "3");

        BillingDocument draft = invoice(DocumentType.SALE_INVOICE, productId, "5");
        long numberBefore = lastNumber(DocumentType.SALE_INVOICE);
        long outboxBefore = outboxRows();
        long movementsBefore = waMovements(productId);

        assertThatThrownBy(() -> emit(DocumentType.SALE_INVOICE, draft))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
                    assertThat(e.getMessage()).contains("producto " + productId);
                });

        Persisted after = reload(DocumentType.SALE_INVOICE, draft.id());
        assertThat(after.status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(after.number()).isNull();
        assertThat(lastNumber(DocumentType.SALE_INVOICE)).as("no consume consecutivo").isEqualTo(numberBefore);
        assertThat(outboxRows()).as("sin evento").isEqualTo(outboxBefore);
        assertThat(waMovements(productId)).isEqualTo(movementsBefore);
        assertThat(waBalanceOf(productId)).isEqualTo(3);
        assertThat(stockOf(productId)).isEqualTo(3);

        // el mismo borrador se puede emitir cuando hay existencias
        emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "2");
        BillingDocument retried = emit(DocumentType.SALE_INVOICE, draft);
        assertThat(retried.status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(waBalanceOf(productId)).isZero();
        assertThat(stockOf(productId)).isZero();
    }

    @Test
    void when_the_second_product_fails_the_kardex_and_stock_already_moved_for_the_first_are_rolled_back() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long withStock = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long withoutStock = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        emittedInvoice(DocumentType.PURCHASE_INVOICE, withStock, "5");
        emittedInvoice(DocumentType.PURCHASE_INVOICE, withoutStock, "1");
        BillingDocument draft = billing.create(ENTERPRISE_ID, DocumentType.SALE_INVOICE, new InvoiceDraftCommand(
                CUSTOMER_ID, null, null, List.of(
                        new InvoiceLineCommand(withStock, new BigDecimal("2"), null, null),
                        new InvoiceLineCommand(withoutStock, new BigDecimal("3"), null, null)), null,
                new InvoiceDraftCommand.PaymentCommand(1L)));

        assertThatThrownBy(() -> emit(DocumentType.SALE_INVOICE, draft))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
                    assertThat(e.getMessage()).contains("producto " + withoutStock);
                });

        // el primer producto SI se movio dentro de la transaccion (kardex + stock) y se revirtio con ella
        assertThat(waMovements(withStock)).isEqualTo(1);
        assertThat(waBalanceOf(withStock)).isEqualTo(5);
        assertThat(stockOf(withStock)).isEqualTo(5);
        assertThat(reload(DocumentType.SALE_INVOICE, draft.id()).status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void a_sale_of_a_product_never_purchased_is_insufficient_stock() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);

        BillingDocument draft = invoice(DocumentType.SALE_INVOICE, productId, "1");

        assertThatThrownBy(() -> emit(DocumentType.SALE_INVOICE, draft))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT));
        assertThat(reload(DocumentType.SALE_INVOICE, draft.id()).status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void a_product_not_replicated_to_inventory_is_not_available() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = NEXT_PRODUCT.incrementAndGet();
        catalog.put(productId, product(productId, "100.00", "19")); // existe en el catalogo, no en Inventario

        BillingDocument draft = invoice(DocumentType.PURCHASE_INVOICE, productId, "1");

        assertThatThrownBy(() -> emit(DocumentType.PURCHASE_INVOICE, draft))
                .isInstanceOfSatisfying(BillingException.class, e -> assertThat(e.errorCode())
                        .isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE));
        assertThat(reload(DocumentType.PURCHASE_INVOICE, draft.id()).status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void a_product_inactive_in_inventory_is_not_available() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        jdbc.update("update kardex_products set state = false where product_id = ?", productId);
        BillingDocument draft = invoice(DocumentType.PURCHASE_INVOICE, productId, "1");

        assertThatThrownBy(() -> emit(DocumentType.PURCHASE_INVOICE, draft))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
                    assertThat(e.getMessage()).contains("Product is not active: " + productId)
                            .doesNotContain("{0}");
                });
    }

    @Test
    void a_return_of_an_invoice_without_kardex_movement_reports_the_original_as_not_found() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "5");
        BillingDocument sale = emittedInvoice(DocumentType.SALE_INVOICE, productId, "2");
        // simula una factura emitida ANTES de la integracion: su venta no esta en el kardex
        jdbc.update("delete from kardex where product_id = ? and type = 'SALE'", productId);
        BillingDocument draft = billing.create(ENTERPRISE_ID, DocumentType.SALE_RETURN, new ReturnDraftCommand(
                sale.id(), null, "devolucion", List.of(new ReturnLineCommand(1, new BigDecimal("1"))), null));

        assertThatThrownBy(() -> emit(DocumentType.SALE_RETURN, draft))
                .isInstanceOfSatisfying(BillingException.class, e -> {
                    assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
                    assertThat(e.getMessage()).contains(sale.number().display());
                });
        assertThat(reload(DocumentType.SALE_RETURN, draft.id()).status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void stock_module_out_of_sync_with_kardex_is_reported_as_insufficient_and_rolls_back_the_kardex() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "5");
        jdbc.update("update stock_entity set quantity = 1 where product_id = ?", productId); // desincronizado
        BillingDocument draft = invoice(DocumentType.SALE_INVOICE, productId, "3");

        assertThatThrownBy(() -> emit(DocumentType.SALE_INVOICE, draft))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT));
        assertThat(waBalanceOf(productId)).as("el kardex tampoco se movio").isEqualTo(5);
        assertThat(stockOf(productId)).isEqualTo(1);
        assertThat(reload(DocumentType.SALE_INVOICE, draft.id()).status()).isEqualTo(DocumentStatus.BORRADOR);
    }

    @Test
    void repeating_the_emission_does_not_move_the_kardex_again() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        BillingDocument draft = invoice(DocumentType.PURCHASE_INVOICE, productId, "4");

        emit(DocumentType.PURCHASE_INVOICE, draft);
        emit(DocumentType.PURCHASE_INVOICE, draft);

        assertThat(waMovements(productId)).isEqualTo(1);
        assertThat(stockOf(productId)).isEqualTo(4);
    }

    @Test
    void annulment_is_rejected_and_leaves_document_and_stock_untouched() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        BillingDocument purchase = emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "4");
        long outboxBefore = outboxRows();

        assertThatThrownBy(() -> billing.annul(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, purchase.id(),
                "error de digitacion", null))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED));

        assertThat(reload(DocumentType.PURCHASE_INVOICE, purchase.id()).status()).isEqualTo(DocumentStatus.EMITIDA);
        assertThat(outboxRows()).isEqualTo(outboxBefore);
        assertThat(stockOf(productId)).isEqualTo(4);
    }

    // ---------- PEPS ----------

    @Test
    void peps_purchase_adds_sale_deducts_return_restores_and_shortage_rolls_back() {
        valuation(InventoryConfigurationTypeEnum.PEPS);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.PEPS);

        BillingDocument purchase = emittedInvoice(DocumentType.PURCHASE_INVOICE, productId, "6");
        assertThat(stockOf(productId)).isEqualTo(6);

        BillingDocument sale = emittedInvoice(DocumentType.SALE_INVOICE, productId, "4");
        assertThat(stockOf(productId)).as("venta descuenta").isEqualTo(2);

        emittedReturn(DocumentType.SALE_RETURN, sale, "1");
        assertThat(stockOf(productId)).as("devolucion de venta repone").isEqualTo(3);

        BillingDocument shortage = invoice(DocumentType.SALE_INVOICE, productId, "5");
        long numberBefore = lastNumber(DocumentType.SALE_INVOICE);
        assertThatThrownBy(() -> emit(DocumentType.SALE_INVOICE, shortage))
                .isInstanceOfSatisfying(BillingException.class,
                        e -> assertThat(e.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT));
        assertThat(reload(DocumentType.SALE_INVOICE, shortage.id()).status()).isEqualTo(DocumentStatus.BORRADOR);
        assertThat(lastNumber(DocumentType.SALE_INVOICE)).isEqualTo(numberBefore);
        assertThat(stockOf(productId)).isEqualTo(3);

        emittedReturn(DocumentType.PURCHASE_RETURN, purchase, "1");
        assertThat(stockOf(productId)).as("devolucion de compra egresa").isEqualTo(2);
    }

    // ---------- ultimo precio de compra (consulta de solo lectura, kardex real sobre H2) ----------

    private static final AtomicLong NEXT_SUPPLIER_INVOICE = new AtomicLong(1);

    /** Factura de compra emitida con precio unitario explicito (override). */
    private BillingDocument purchaseAt(long productId, String quantity, String unitPrice) {
        BillingDocument draft = billing.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, new InvoiceDraftCommand(
                SUPPLIER_ID, null, SUPPLIER_INVOICE_NUMBER + "-LP-" + NEXT_SUPPLIER_INVOICE.getAndIncrement(),
                List.of(new InvoiceLineCommand(productId, new BigDecimal(quantity), new BigDecimal(unitPrice), null)),
                null, new InvoiceDraftCommand.PaymentCommand(1L)));
        return emit(DocumentType.PURCHASE_INVOICE, draft);
    }

    private void assertLastPrice(Optional<BigDecimal> actual, String expected) {
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualByComparingTo(expected);
    }

    @Test
    void weighted_average_last_entry_price_follows_purchases_and_adjustment_entries_and_ignores_the_rest() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        assertThat(waQuery.findLastEntryUnitPrice(productId)).as("sin entradas").isEmpty();

        purchaseAt(productId, "10", "100.00");
        assertLastPrice(waQuery.findLastEntryUnitPrice(productId), "100.00");
        BillingDocument second = purchaseAt(productId, "10", "120.00");
        assertLastPrice(waQuery.findLastEntryUnitPrice(productId), "120.00");

        BillingDocument sale = emittedInvoice(DocumentType.SALE_INVOICE, productId, "4");
        emittedReturn(DocumentType.SALE_RETURN, sale, "1");
        emittedReturn(DocumentType.PURCHASE_RETURN, second, "2");
        assertLastPrice(waQuery.findLastEntryUnitPrice(productId), "120.00"); // venta/devoluciones no cuentan

        co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex adjustment =
                new co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex();
        adjustment.setFactCode("AJ-WA-" + productId);
        adjustment.setProductId(productId);
        adjustment.setType(co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType.ADJUSTMENTENTRY);
        adjustment.setDate(java.time.ZonedDateTime.now());
        adjustment.setQuantity(1);
        adjustment.setUnitPrice(new BigDecimal("130.00"));
        adjustment.setDetails("Ajuste de compra");
        inTx(() -> waCommand.registerAdjustmentEntry(adjustment));
        assertLastPrice(waQuery.findLastEntryUnitPrice(productId), "130.00");

        // empate por fecha: gana el id mas alto
        jdbc.update("update kardex set date = ? where product_id = ? and type in ('PURCHASE', 'ADJUSTMENTENTRY')",
                java.sql.Timestamp.valueOf("2020-01-01 10:00:00"), productId);
        assertLastPrice(waQuery.findLastEntryUnitPrice(productId), "130.00");
    }

    @Test
    void peps_last_entry_price_follows_purchases_and_adjustment_entries_and_ignores_the_rest() {
        valuation(InventoryConfigurationTypeEnum.PEPS);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.PEPS);
        assertThat(pepsQuery.findLastEntryUnitPrice(productId)).as("sin entradas").isEmpty();

        purchaseAt(productId, "10", "100.00");
        assertLastPrice(pepsQuery.findLastEntryUnitPrice(productId), "100.00");
        BillingDocument second = purchaseAt(productId, "10", "120.00");
        assertLastPrice(pepsQuery.findLastEntryUnitPrice(productId), "120.00");

        BillingDocument sale = emittedInvoice(DocumentType.SALE_INVOICE, productId, "4");
        emittedReturn(DocumentType.SALE_RETURN, sale, "1");
        emittedReturn(DocumentType.PURCHASE_RETURN, second, "2");
        assertLastPrice(pepsQuery.findLastEntryUnitPrice(productId), "120.00"); // venta/devoluciones no cuentan

        co.unicauca.edu.co.contables.inventory.peps.domain.model.Product pepsProduct =
                new co.unicauca.edu.co.contables.inventory.peps.domain.model.Product();
        pepsProduct.setProductId(productId);
        pepsProduct.setState(true);
        inTx(() -> pepsCommand.registerNonCommercialEntry(
                co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex.createNonCommercialEntry(
                        "NC-" + productId, "Sobrante", 1, new BigDecimal("999.00"), pepsProduct)));
        assertLastPrice(pepsQuery.findLastEntryUnitPrice(productId), "120.00"); // entrada no comercial no cuenta

        co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex adjustment =
                new co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex();
        adjustment.setFactCode("AJ-PEPS-" + productId);
        adjustment.setProduct(pepsProduct);
        adjustment.setDate(java.time.ZonedDateTime.now());
        adjustment.setQuantity(1);
        adjustment.setUnitPrice(new BigDecimal("130.00"));
        adjustment.setDetails("Ajuste de compra");
        inTx(() -> pepsCommand.registerAdjustmentEntry(adjustment));
        assertLastPrice(pepsQuery.findLastEntryUnitPrice(productId), "130.00");

        // empate por fecha: gana el id mas alto
        jdbc.update("update peps_kardex set date = ? where product_id = ? and type in ('PURCHASE', 'ADJUSTMENTENTRY')",
                java.sql.Timestamp.valueOf("2020-01-01 10:00:00"), productId);
        assertLastPrice(pepsQuery.findLastEntryUnitPrice(productId), "130.00");
    }

    @Test
    void lookup_uses_the_kardex_of_the_enterprise_valuation_method() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long wa = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        purchaseAt(wa, "10", "100.00");
        purchaseAt(wa, "10", "130.00");
        Map<Long, co.unicauca.edu.co.contables.billing.domain.model.Money> waPrices =
                lastPurchasePrices.findAll(ENTERPRISE_ID, List.of(wa, wa + 100_000L));
        assertThat(waPrices).containsOnlyKeys(wa);
        assertThat(waPrices.get(wa).value()).as("promedio ponderado vigente, no el ultimo (130)")
                .isEqualByComparingTo("115.00");

        valuation(InventoryConfigurationTypeEnum.PEPS);
        long peps = newInventoryProduct(InventoryConfigurationTypeEnum.PEPS);
        purchaseAt(peps, "5", "77.25");
        assertThat(lastPurchasePrices.findAll(ENTERPRISE_ID, List.of(peps)).get(peps).value())
                .isEqualByComparingTo("77.25");
    }

    @Test
    void weighted_average_current_average_cost_skips_rows_without_balance_and_falls_back_when_none() {
        valuation(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        long productId = newInventoryProduct(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        assertThat(waQuery.findCurrentAverageUnitCost(productId)).as("sin kardex").isEmpty();

        purchaseAt(productId, "10", "100.00");
        purchaseAt(productId, "10", "130.00");
        assertLastPrice(waQuery.findCurrentAverageUnitCost(productId), "115.00");

        // la BD real tiene filas antiguas con balance nulo; el DDL de H2 lo hace NOT NULL, se relaja aqui
        jdbc.execute("alter table kardex alter column balance_unit_price set null");
        // fila mas reciente sin balance (dato antiguo de BD real): se salta y gana la anterior
        jdbc.update("update kardex set balance_unit_price = null where id = "
                + "(select max(id) from kardex where product_id = ?)", productId);
        assertLastPrice(waQuery.findCurrentAverageUnitCost(productId), "100.00");

        // ninguna fila con balance: vacio, el lookup deja al producto fuera (cae a cost)
        jdbc.update("update kardex set balance_unit_price = null where product_id = ?", productId);
        assertThat(waQuery.findCurrentAverageUnitCost(productId)).isEmpty();
        assertThat(lastPurchasePrices.findAll(ENTERPRISE_ID, List.of(productId))).isEmpty();
    }

    // ---------- ultimo precio: una linea vs lineas repetidas (pruebas nuevas) ----------

    private BillingDocument purchaseTwoLines(long productId, String q1, String p1, String q2, String p2) {
        BillingDocument draft = billing.create(ENTERPRISE_ID, DocumentType.PURCHASE_INVOICE, new InvoiceDraftCommand(
                SUPPLIER_ID, null, SUPPLIER_INVOICE_NUMBER + "-LP2-" + NEXT_SUPPLIER_INVOICE.getAndIncrement(),
                List.of(new InvoiceLineCommand(productId, new BigDecimal(q1), new BigDecimal(p1), null),
                        new InvoiceLineCommand(productId, new BigDecimal(q2), new BigDecimal(p2), null)),
                null, new InvoiceDraftCommand.PaymentCommand(1L)));
        return emit(DocumentType.PURCHASE_INVOICE, draft);
    }

    /** PEPS: el sugerido es el ultimo precio de entrada; WA: el promedio vigente (balance del kardex). */
    private void assertThreeSingleLinePurchasesGiveSuggestedPrice(InventoryConfigurationTypeEnum method,
            java.util.function.LongFunction<Optional<BigDecimal>> query, String expected, String rejected) {
        valuation(method);
        long productId = newInventoryProduct(method);
        purchaseAt(productId, "10", "100.00");
        purchaseAt(productId, "10", "120.00");
        purchaseAt(productId, "10", "130.00");
        Optional<BigDecimal> kardexPrice = query.apply(productId);
        assertLastPrice(kardexPrice, expected);
        assertThat(kardexPrice.get()).isNotEqualByComparingTo(rejected);
        assertThat(lastPurchasePrices.findAll(ENTERPRISE_ID, List.of(productId)).get(productId).value())
                .isEqualByComparingTo(expected);
    }

    private void assertTwoLinesSameInvoiceStoreInvoiceWeightedAverage(InventoryConfigurationTypeEnum method,
            java.util.function.LongFunction<Optional<BigDecimal>> query) {
        valuation(method);
        long productId = newInventoryProduct(method);
        purchaseTwoLines(productId, "10", "100.00", "10", "130.00");
        // (10*100 + 10*130) / 20 = 115.00: groupByProduct fusiona las lineas en un solo movimiento
        assertLastPrice(query.apply(productId), "115.00");
        assertThat(query.apply(productId).get()).isNotEqualByComparingTo("130.00");
        assertThat(lastPurchasePrices.findAll(ENTERPRISE_ID, List.of(productId)).get(productId).value())
                .isEqualByComparingTo("115.00");
    }

    @Test
    void weighted_average_three_single_line_purchases_suggest_the_current_average_not_the_last_price() {
        // (10*100 + 10*120 + 10*130) / 30 = 116.67 (balance_unit_price, HALF_UP a 2 decimales)
        assertThreeSingleLinePurchasesGiveSuggestedPrice(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE,
                waQuery::findCurrentAverageUnitCost, "116.67", "130.00");
    }

    @Test
    void peps_three_single_line_purchases_suggest_the_last_price_not_the_average() {
        assertThreeSingleLinePurchasesGiveSuggestedPrice(InventoryConfigurationTypeEnum.PEPS,
                pepsQuery::findLastEntryUnitPrice, "130.00", "116.67");
    }

    @Test
    void weighted_average_two_lines_of_the_same_product_in_one_invoice_store_the_invoice_average() {
        assertTwoLinesSameInvoiceStoreInvoiceWeightedAverage(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE,
                waQuery::findCurrentAverageUnitCost);
    }

    @Test
    void peps_two_lines_of_the_same_product_in_one_invoice_store_the_invoice_average() {
        assertTwoLinesSameInvoiceStoreInvoiceWeightedAverage(InventoryConfigurationTypeEnum.PEPS,
                pepsQuery::findLastEntryUnitPrice);
    }
}
