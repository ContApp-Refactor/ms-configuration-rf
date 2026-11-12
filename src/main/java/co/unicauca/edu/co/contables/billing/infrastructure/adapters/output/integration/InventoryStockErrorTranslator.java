package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Traduce las excepciones de Inventario a codigos de Billing.
 *
 * <p>Las excepciones de negocio de Inventario ({@code BusinessRuleException},
 * {@code EntityDoesNotExistException}, ...) viven en un paquete {@code ..infrastructure..} que
 * ArchUnit (R3b) prohibe importar, asi que se reconocen por NOMBRE SIMPLE de clase (recorriendo la
 * jerarquia), por su {@code status} (leido por reflexion, getter {@code getStatus}) y por palabras
 * clave de los mensajes REALES de Inventario (es/en, ver {@code messages*.properties}: claves
 * {@code kardex.error.*} y los textos fijos de PEPS y del dominio de stock). Inventario aun no expone
 * codigos estables, por lo que esta heuristica es el punto unico a ajustar cuando los exponga; sus
 * casos estan fijados con las excepciones reales en {@code InventoryStockErrorTranslatorTest} y
 * {@code BillingInventoryStockRealServicesTest}.
 *
 * <p>Reglas, en orden:
 * <ol>
 *   <li>"insuficiente"/"insufficient"/"already sold" -> {@code BILLING_STOCK_INSUFFICIENT};</li>
 *   <li>fecha contable no valida (mes cerrado en el calendario contable) -> {@code BILLING_STOCK_PERIOD_CLOSED};</li>
 *   <li>no encontrado en una DEVOLUCION referido al documento original (factCode, "original",
 *       "to be returned", registro/record previo) -> {@code BILLING_STOCK_ORIGINAL_NOT_FOUND};</li>
 *   <li>venta sin ningun registro previo del producto en kardex (no hubo entradas) -> INSUFFICIENT;</li>
 *   <li>otro no encontrado, o producto/stock inactivo -> {@code BILLING_STOCK_PRODUCT_NOT_AVAILABLE};</li>
 *   <li>error de cliente (status 4xx, regla de negocio, argumento invalido) -> {@code BILLING_STOCK_REJECTED};</li>
 *   <li>cualquier otra cosa -> {@code BILLING_STOCK_UNAVAILABLE} (503).</li>
 * </ol>
 */
final class InventoryStockErrorTranslator {

    /** Prefijo tecnico que Inventario antepone a sus mensajes ("GC-004: Business rule violation -> "). */
    private static final Pattern INVENTORY_PREFIX = Pattern.compile("^GC-\\d{3}:[^>]*->\\s*");

    /** Naturaleza del movimiento que fallo (cambia el significado de un "no encontrado"). */
    enum Operation {
        /** Salida por venta. */
        SALE,
        /** Entrada por compra. */
        PURCHASE,
        /** Reposicion por devolucion de venta (requiere la venta original en kardex). */
        SALE_RETURN,
        /** Salida por devolucion de compra (requiere la compra original en kardex). */
        PURCHASE_RETURN;

        boolean isReturn() {
            return this == SALE_RETURN || this == PURCHASE_RETURN;
        }
    }

    private InventoryStockErrorTranslator() {
    }

    /** Atajo para pruebas: producto sin etiqueta ni codigo de documento. */
    static BillingException translate(RuntimeException ex, Operation operation, long productId) {
        return translate(ex, operation, "producto " + productId, null);
    }

    /**
     * @param productLabel texto que identifica el producto ante el usuario ("producto 7 (P-7 Tornillo)")
     * @param factCode     codigo de documento usado en el kardex (en devoluciones, el de la factura original)
     */
    static BillingException translate(RuntimeException ex, Operation operation, String productLabel,
            String factCode) {
        if (ex instanceof BillingException billing) {
            return billing;
        }
        String message = clean(ex.getMessage());
        String lower = message.toLowerCase(Locale.ROOT);
        Integer status = statusOf(ex);
        String detail = message.isBlank() ? "" : " (detalle de Inventario: " + message + ")";

        if (containsAny(lower, "insuficiente", "insufficient", "already sold")) {
            return new BillingException(BillingErrorCode.BILLING_STOCK_INSUFFICIENT,
                    "No hay existencias suficientes en Inventario para el " + productLabel + detail);
        }
        if (containsAny(lower, "accounting date", "fecha contable", "periodo contable", "período contable")) {
            // fecha del documento en un mes cerrado (calendario contable): causa frecuente, mensaje sin tecnicismos
            return new BillingException(BillingErrorCode.BILLING_STOCK_PERIOD_CLOSED,
                    "Inventario rechazo el movimiento debido a que el mes esta cerrado o no existe. "
                            + "Abre el mes en el calendario contable o cambia la fecha del documento.");
        }
        boolean notFound = (status != null && status == 404) || isNamed(ex, "EntityDoesNotExistException")
                || containsAny(lower, "not found", "no se encontr", "no encontrado");
        boolean aboutPreviousRecord = containsAny(lower, "registro", "record");
        if (operation.isReturn() && notFound
                && (aboutPreviousRecord || containsAny(lower, "factcode", "original", "to be returned", "origin"))) {
            return new BillingException(BillingErrorCode.BILLING_STOCK_ORIGINAL_NOT_FOUND,
                    "Inventario no tiene registrado el movimiento de la factura original"
                            + (factCode == null ? "" : " " + factCode) + " para el " + productLabel
                            + "; no se puede registrar la devolucion" + detail);
        }
        if (operation == Operation.SALE && notFound && aboutPreviousRecord) {
            // venta sin kardex previo del producto: nunca hubo entradas, no hay existencias
            return new BillingException(BillingErrorCode.BILLING_STOCK_INSUFFICIENT,
                    "No hay existencias en Inventario para el " + productLabel
                            + ": el producto no tiene entradas registradas" + detail);
        }
        if (notFound || containsAny(lower, "inactiv", "not active", "no esta activo", "no está activo")) {
            return new BillingException(BillingErrorCode.BILLING_STOCK_PRODUCT_NOT_AVAILABLE,
                    "El " + productLabel + " no existe o esta inactivo en Inventario" + detail);
        }
        boolean clientError = (status != null && status >= 400 && status < 500)
                || ex instanceof IllegalArgumentException
                || isNamed(ex, "BusinessRuleException")
                || isNamed(ex, "EntityAlreadyExists");
        if (clientError) {
            return new BillingException(BillingErrorCode.BILLING_STOCK_REJECTED,
                    "Inventario rechazo el movimiento del " + productLabel
                            + (message.isBlank() ? "" : ": " + message));
        }
        return new BillingException(BillingErrorCode.BILLING_STOCK_UNAVAILABLE,
                "Inventario no esta disponible para registrar el movimiento del " + productLabel
                        + "; intente de nuevo" + detail);
    }

    /** Mensaje sin el prefijo tecnico de Inventario ni espacios sobrantes. */
    static String clean(String message) {
        if (message == null) {
            return "";
        }
        return INVENTORY_PREFIX.matcher(message.trim()).replaceFirst("").trim();
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    /** Verdadero si la clase o algun ancestro tiene ese nombre simple. */
    private static boolean isNamed(Throwable ex, String simpleName) {
        for (Class<?> c = ex.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getSimpleName().equals(simpleName)) {
                return true;
            }
        }
        return false;
    }

    /** Status HTTP de las excepciones base de Inventario ({@code getStatus()}), o null si no tiene. */
    private static Integer statusOf(Throwable ex) {
        try {
            Method getter = ex.getClass().getMethod("getStatus");
            Object value = getter.invoke(ex);
            return value instanceof Integer i ? i : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
