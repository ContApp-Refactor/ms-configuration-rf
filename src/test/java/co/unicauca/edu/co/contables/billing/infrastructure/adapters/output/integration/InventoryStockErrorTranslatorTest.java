package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration.InventoryStockErrorTranslator.Operation;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.EntityDoesNotExistException;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;
import org.junit.jupiter.api.Test;

class InventoryStockErrorTranslatorTest {

    private static BillingErrorCode code(RuntimeException ex, Operation op) {
        return InventoryStockErrorTranslator.translate(ex, op, 5L).errorCode();
    }

    @Test
    void insufficient_stock_in_spanish_and_english() {
        assertThat(code(new BusinessRuleException(400, "Stock insuficiente. Solicitado: 3, Disponible: 1."),
                Operation.SALE)).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
        assertThat(code(new BusinessRuleException(400, "Insufficient stock. Requested: 3, Available: 1."),
                Operation.PURCHASE_RETURN)).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
    }

    @Test
    void sale_without_previous_kardex_means_no_stock() {
        assertThat(code(new BusinessRuleException(404, "No se encontro un registro para sale necesario para la operacion."),
                Operation.SALE)).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
    }

    @Test
    void return_without_original_movement() {
        assertThat(code(new EntityDoesNotExistException(404, "No se encontro el factCode=FV-1"),
                Operation.SALE_RETURN)).isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
        assertThat(code(new BusinessRuleException(400, "Sale to be returned not found"),
                Operation.SALE_RETURN)).isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
    }

    @Test
    void missing_or_inactive_product() {
        assertThat(code(new EntityDoesNotExistException(404, "Producto no encontrado: 5."), Operation.PURCHASE))
                .isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
        assertThat(code(new BusinessRuleException(400, "El producto con ID 5 esta inactivo."), Operation.SALE))
                .isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
    }

    @Test
    void other_business_rule_errors_are_rejected_and_server_errors_unavailable() {
        assertThat(code(new BusinessRuleException(400, "La fecha contable no es valida"), Operation.SALE))
                .isEqualTo(BillingErrorCode.BILLING_STOCK_PERIOD_CLOSED);
        assertThat(code(new BusinessRuleException(400, "Registro duplicado para la factura"), Operation.SALE))
                .isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED);
        assertThat(code(new IllegalArgumentException("Quantity must be greater than 0"), Operation.PURCHASE))
                .isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED);
        assertThat(code(new GenericErrorException(500, "Unexpected error communicating with stock service"),
                Operation.SALE)).isEqualTo(BillingErrorCode.BILLING_STOCK_UNAVAILABLE);
        assertThat(code(new IllegalStateException("boom"), Operation.SALE))
                .isEqualTo(BillingErrorCode.BILLING_STOCK_UNAVAILABLE);
    }

    @Test
    void closed_accounting_period_is_explained_in_plain_spanish() {
        BillingException ex = InventoryStockErrorTranslator.translate(
                new BusinessRuleException(400, "The accounting date is not valid for the operation."),
                Operation.PURCHASE, 7L);
        assertThat(ex.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PERIOD_CLOSED);
        assertThat(ex.getMessage()).startsWith("Inventario rechazo el movimiento debido a que el mes esta cerrado")
                .contains("Abre el mes en el calendario contable")
                .doesNotContain("accounting date").doesNotContain("detalle de Inventario");
    }

    @Test
    void a_billing_exception_passes_through_untouched() {
        BillingException original = new BillingException(BillingErrorCode.BILLING_INVALID_STATE, "x");
        assertThat(InventoryStockErrorTranslator.translate(original, Operation.SALE, 1L)).isSameAs(original);
    }

    // ---------- excepciones REALES de Inventario (formatter real + messages*.properties reales) ----------

    /**
     * Reproduce exactamente lo que hace Inventario: {@code FormatterResultOutputPort} real (antepone
     * "GC-00X: ... -> ") con el texto resuelto por el {@code MessageService} real de stock sobre los
     * {@code messages*.properties} del proyecto, en espanol y en ingles.
     */
    @org.junit.jupiter.api.Nested
    class RealInventoryExceptions {

        private final co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception
                .FormatterResultOutputPort formatter = new co.unicauca.edu.co.contables.inventory.stock.infrastructure
                .adapters.output.exception.FormatterResultOutputPort();
        private co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n.MessageService messages;

        @org.junit.jupiter.api.AfterEach
        void resetLocale() {
            org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
        }

        private void locale(String tag) {
            org.springframework.context.support.ResourceBundleMessageSource source =
                    new org.springframework.context.support.ResourceBundleMessageSource();
            source.setBasename("messages");
            source.setDefaultEncoding("UTF-8");
            source.setFallbackToSystemLocale(false);
            messages = new co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n
                    .MessageService(source);
            org.springframework.context.i18n.LocaleContextHolder.setLocale(java.util.Locale.forLanguageTag(tag));
        }

        private RuntimeException businessRule(int status, String key, Object... args) {
            return catchIt(() -> formatter.returnBusinessRuleErrorResponse(status, messages.getMessage(key, args)));
        }

        private RuntimeException notFound(int status, String message) {
            return catchIt(() -> formatter.returnEntityDoesNotExistErrorResponse(status, message));
        }

        private RuntimeException catchIt(Runnable action) {
            try {
                action.run();
            } catch (RuntimeException ex) {
                return ex;
            }
            throw new AssertionError("el formatter real siempre lanza");
        }

        private BillingException translated(RuntimeException ex, Operation op) {
            return InventoryStockErrorTranslator.translate(ex, op, "producto 7 (P7 Tornillo)", "FV-000001");
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.ValueSource(strings = {"es", "en"})
        void weighted_average_and_peps_messages_map_to_the_right_code(String tag) {
            locale(tag);
            // KardexCommandService.rejectIfInsufficientBalance / PEPS validateSufficientStock
            assertThat(translated(businessRule(400, "kardex.error.insufficient.stock", 5, 2), Operation.SALE)
                    .errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
            assertThat(translated(businessRule(400, "kardex.error.insufficient.stock", 5, 2), Operation.PURCHASE_RETURN)
                    .errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
            // KardexValidationService.validatePreviousKardexExists (venta sin entradas): BusinessRuleException 404
            RuntimeException missingRecord = new co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters
                    .output.exception.customized.BusinessRuleException(404,
                    messages.getMessage("kardex.error.missing.record", (Object) "sale"));
            assertThat(translated(missingRecord, Operation.SALE).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
            // KardexValidationService: producto ausente de kardex_products (BusinessRuleException 404)
            RuntimeException waProductMissing = new co.unicauca.edu.co.contables.inventory.stock.infrastructure
                    .adapters.output.exception.customized.BusinessRuleException(404,
                    messages.getMessage("kardex.error.not.found", 7L, "Product"));
            assertThat(translated(waProductMissing, Operation.SALE).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            assertThat(translated(waProductMissing, Operation.SALE_RETURN).errorCode())
                    .as("en una devolucion, producto ausente NO es 'original no encontrado'")
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            // KardexValidationService: producto inactivo (operation.not.allowed)
            assertThat(translated(new co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output
                    .exception.customized.BusinessRuleException(400, messages.getMessage(
                    "kardex.error.operation.not.allowed", (Object) "Product is not active: 7")), Operation.SALE)
                    .errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            // PEPS getValidatedProduct: no encontrado (404) e inactivo (400)
            assertThat(translated(notFound(404, messages.getMessage("kardex.error.not.found.product", 7L)),
                    Operation.PURCHASE).errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            assertThat(translated(businessRule(400, "kardex.error.product.not.active", 7L), Operation.SALE)
                    .errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            // KardexReturnService: movimiento original inexistente (EntityDoesNotExistException 404)
            assertThat(translated(notFound(404, messages.getMessage("kardex.error.not.found",
                    (Object) "factCode=FV-000001, productId=7, type=SALE")), Operation.SALE_RETURN).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
            // KardexValidationService: duplicado; KardexReturnService: cantidad excedida; calendario contable
            assertThat(translated(new co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output
                    .exception.customized.BusinessRuleException(400, messages.getMessage(
                    "kardex.error.duplicate.record", (Object) "factCode=FV-000001, productId=7, type=Venta")),
                    Operation.SALE)
                    .errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED);
            assertThat(translated(businessRule(400, "kardex.error.quantity.exceeded", "Quantity"),
                    Operation.SALE_RETURN).errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_REJECTED);
            BillingException calendar = translated(businessRule(400, "kardex.error.invalid.accounting.date"),
                    Operation.SALE);
            assertThat(calendar.errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PERIOD_CLOSED);
            // el mensaje explica la causa (mes cerrado) sin el texto tecnico de Inventario
            assertThat(calendar.getMessage()).doesNotContain("GC-004")
                    .startsWith("Inventario rechazo el movimiento debido a que el mes esta cerrado")
                    .contains("calendario contable")
                    .doesNotContain(messages.getMessage("kardex.error.invalid.accounting.date"))
                    .doesNotContain("detalle de Inventario");
        }

        @Test
        void peps_fixed_texts_and_stock_module_errors_map_to_the_right_code() {
            locale("es");
            // PEPS registerPurchaseReturn / registerSaleReturn sin original
            assertThat(translated(notFound(404, "Original purchase not found"), Operation.PURCHASE_RETURN).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
            assertThat(translated(catchIt(() -> formatter.returnBusinessRuleErrorResponse(400,
                    "Sale to be returned not found")), Operation.SALE_RETURN).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND);
            // PEPS validateForPurchaseReturn: el lote ya se vendio
            assertThat(translated(catchIt(() -> formatter.returnBusinessRuleErrorResponse(400,
                    "Cannot return products already sold. Available: 0, Attempting to return: 1")),
                    Operation.PURCHASE_RETURN).errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
            // StockClientAdapter (kardex/peps) tras el ajuste: stock insuficiente, inactivo, inexistente
            assertThat(translated(catchIt(() -> formatter.returnBusinessRuleErrorResponse(400,
                    "Insufficient stock in stock module: Invalid amount for exit")), Operation.SALE).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_INSUFFICIENT);
            assertThat(translated(catchIt(() -> formatter.returnBusinessRuleErrorResponse(400, "Stock is not active")),
                    Operation.PURCHASE).errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            assertThat(translated(notFound(404, messages.getMessage("stock.error.not.found", 7L)), Operation.SALE)
                    .errorCode()).isEqualTo(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE);
            // ConfigClientAdapter / StockClientAdapter: error inesperado
            assertThat(translated(catchIt(() -> formatter.returnErrorGenericResponse(500,
                    "Unexpected error communicating with configuration service")), Operation.SALE).errorCode())
                    .isEqualTo(BillingErrorCode.BILLING_STOCK_UNAVAILABLE);
        }

        @Test
        void user_messages_are_spanish_name_the_product_and_drop_the_technical_prefix() {
            locale("es");
            BillingException insufficient = translated(businessRule(400, "kardex.error.insufficient.stock", 5, 2),
                    Operation.SALE);
            assertThat(insufficient.getMessage())
                    .startsWith("No hay existencias suficientes en Inventario para el producto 7 (P7 Tornillo)")
                    .contains("Solicitado: 5, Disponible: 2")
                    .doesNotContain("GC-004");
            BillingException original = translated(notFound(404, "Original purchase not found"),
                    Operation.PURCHASE_RETURN);
            assertThat(original.getMessage()).contains("factura original FV-000001").contains("producto 7");
        }
    }
}
