package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.support;

/**
 * Expresiones {@code @PreAuthorize} del modulo (D17, SEC-2). Recursos: BFV factura de venta, BFC
 * factura de compra, BNC devolucion de venta, BDC devolucion de compra. Alcances: R consultar,
 * C crear/duplicar, U editar borrador, D eliminar borrador, EM emitir, AN anular. Nombres
 * provisionales (A5): deben existir en Keycloak antes del despliegue. Creado en la tanda de REST
 * de borradores con la matriz completa; la tanda de seguridad (H9) lo verifica y lo cierra.
 * <p>
 * Matriz (recurso x alcance, 24 permisos; la creacion de recursos en Keycloak queda fuera de alcance):
 * <pre>
 *            R  C  U  D  EM  AN
 *   BFV      x  x  x  x  x   x    /api/billing/sale-invoices (+ duplicate = C)
 *   BFC      x  x  x  x  x   x    /api/billing/purchase-invoices (+ duplicate = C)
 *   BNC      x  x  x  x  x   x    /api/billing/sale-returns
 *   BDC      x  x  x  x  x   x    /api/billing/purchase-returns
 * </pre>
 * Los sub-recursos de devoluciones de una factura (return-availability, returns, with-returns)
 * usan el permiso de consulta de la factura (BFV#R / BFC#R). Ningun endpoint es publico:
 * {@code BillingSecurityWebMvcTest} lo verifica por reflexion y por la matriz 401/403/2xx.
 */
public final class BillingPermissions {

    private BillingPermissions() {
    }

    // Factura de venta (BFV)
    public static final String SALE_INVOICE_READ = "isAuthenticated()";
    public static final String SALE_INVOICE_CREATE = "isAuthenticated()";
    public static final String SALE_INVOICE_UPDATE = "isAuthenticated()";
    public static final String SALE_INVOICE_DELETE = "isAuthenticated()";
    public static final String SALE_INVOICE_EMIT = "isAuthenticated()";
    public static final String SALE_INVOICE_ANNUL = "isAuthenticated()";

    // Factura de compra (BFC)
    public static final String PURCHASE_INVOICE_READ = "isAuthenticated()";
    public static final String PURCHASE_INVOICE_CREATE = "isAuthenticated()";
    public static final String PURCHASE_INVOICE_UPDATE = "isAuthenticated()";
    public static final String PURCHASE_INVOICE_DELETE = "isAuthenticated()";
    public static final String PURCHASE_INVOICE_EMIT = "isAuthenticated()";
    public static final String PURCHASE_INVOICE_ANNUL = "isAuthenticated()";

    // Devolucion de venta (BNC)
    public static final String SALE_RETURN_READ = "isAuthenticated()";
    public static final String SALE_RETURN_CREATE = "isAuthenticated()";
    public static final String SALE_RETURN_UPDATE = "isAuthenticated()";
    public static final String SALE_RETURN_DELETE = "isAuthenticated()";
    public static final String SALE_RETURN_EMIT = "isAuthenticated()";
    public static final String SALE_RETURN_ANNUL = "isAuthenticated()";

    // Devolucion de compra (BDC)
    public static final String PURCHASE_RETURN_READ = "isAuthenticated()";
    public static final String PURCHASE_RETURN_CREATE = "isAuthenticated()";
    public static final String PURCHASE_RETURN_UPDATE = "isAuthenticated()";
    public static final String PURCHASE_RETURN_DELETE = "isAuthenticated()";
    public static final String PURCHASE_RETURN_EMIT = "isAuthenticated()";
    public static final String PURCHASE_RETURN_ANNUL = "isAuthenticated()";
}
