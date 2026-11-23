package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n;

/**
 * Constantes para las claves de mensajes internacionalizados.
 * Centraliza todas las claves de mensajes usadas en la aplicación.
 */
public final class MessageKeys {

    private MessageKeys() {
        // Clase de constantes, no debe ser instanciada
    }

    //ERROR MESSAGES
     public static final String ERROR_NOT_FOUND = "kardex.error.not.found";
    public static final String ERROR_MISSING_RECORD = "kardex.error.missing.record";
    public static final String ERROR_NOT_FOUND_PURCHASE_ORIGIN = "kardex.error.not.found.purchase.origin";
    public static final String ERROR_VALIDATION_PURCHASE_ORIGIN = "kardex.error.validation.purchase.origin";
    public static final String ERROR_NOT_FOUND_SALE_ORIGIN = "kardex.error.not.found.sale.origin";
    public static final String ERROR_VALIDATION_SALE_ORIGIN = "kardex.error.validation.sale.origin";
    public static final String ERROR_SALE_NODETAILS_TO_RETURN = "kardex.error.sale.no.details.to.return";
    public static final String ERROR_NOT_FOUND_PRODUCT = "kardex.error.not.found.product";
    public static final String ERROR_PRODUCT_NOT_ACTIVE = "kardex.error.product.not.active";
    public static final String ERROR_INSUFFICIENT_STOCK = "kardex.error.insufficient.stock";
    public static final String ERROR_SYNC_PRODUCTS = "kardex.error.sync.products";
    public static final String INVALID_ACCOUNTING_DATE = "kardex.error.invalid.accounting.date"; 
    public static final String DATE_CANNOT_BE_FUTURE = "kardex.error.date.cannot.be.future";
    public static final String DATE_CANNOT_BE_BEFORE_LAST_RECORD = "kardex.error.date.cannot.be.before.last.record";
  

   
    public static final String LOG_PRODUCT_QUERY_ALL = "kardex.log.product.query.all";
    public static final String LOG_SYNC_ERROR = "kardex.log.sync.error";
}
