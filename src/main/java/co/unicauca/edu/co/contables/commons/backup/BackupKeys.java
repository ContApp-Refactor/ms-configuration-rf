package co.unicauca.edu.co.contables.commons.backup;

import java.util.List;

/**
 * @brief Constantes compartidas de la copia de seguridad: claves de módulo,
 * orden de ejecución y namespaces del {@link IdMap}.
 *
 * Es lo ÚNICO que un participante necesita conocer de otro módulo: una FK
 * cruzada se remapea con {@code idMap.require(BackupKeys.ACCOUNT, oldId)} sin
 * importar clases del módulo PUC.
 */
public final class BackupKeys {

    private BackupKeys() {
    }

    /* ---------- moduleKey ---------- */
    public static final String MODULE_ENTERPRISE = "enterprise";
    public static final String MODULE_CONFIGURATION = "configuration";
    public static final String MODULE_THIRDS = "thirds";
    public static final String MODULE_THIRD_REPLICAS = "notifications.thirdReplicas";
    public static final String MODULE_PUC = "puc";
    public static final String MODULE_TAXES = "taxes";
    public static final String MODULE_PRODUCTS = "products";
    public static final String MODULE_INVENTORY_STOCK = "inventory.stock";
    public static final String MODULE_INVENTORY_KARDEX = "inventory.kardex";
    public static final String MODULE_INVENTORY_PEPS = "inventory.peps";
    /** Movimientos (fase 2): kardex de promedio ponderado. Requiere las réplicas {@code inventory.*}. */
    public static final String MODULE_INVENTORY_KARDEX_MOVEMENTS = "inventory.kardex.movements";
    /** Movimientos (fase 2): capas PEPS y su detalle de salidas. */
    public static final String MODULE_INVENTORY_PEPS_MOVEMENTS = "inventory.peps.movements";
    /** Estados financieros generados y sus plantillas. */
    public static final String MODULE_FINANCIAL = "financial";
    /** Libros auxiliares generados y reportes programados. */
    public static final String MODULE_AUXILIARY = "auxiliary";
    /** Facturación (billing): facturas/devoluciones de venta y compra, líneas, retenciones y numeración. */
    public static final String MODULE_BILLING = "billing";
    /** Bancos, cuentas bancarias y medios de pago (moduleKey del participante de contabilidad; billing lo requiere por {@code payment_method_id}). */
    public static final String MODULE_ACCOUNTING_BANKING = "accounting.banking";

    /* ---------- order() ---------- */
    public static final int ORDER_ENTERPRISE = 0;
    public static final int ORDER_CONFIGURATION = 10;
    public static final int ORDER_THIRDS = 20;
    public static final int ORDER_THIRD_REPLICAS = 25;
    public static final int ORDER_PUC = 30;
    public static final int ORDER_TAXES = 40;
    public static final int ORDER_PRODUCTS = 50;
    public static final int ORDER_INVENTORY_STOCK = 60;
    public static final int ORDER_INVENTORY_KARDEX = 61;
    public static final int ORDER_INVENTORY_PEPS = 62;
    public static final int ORDER_INVENTORY_KARDEX_MOVEMENTS = 70;
    public static final int ORDER_INVENTORY_PEPS_MOVEMENTS = 80;
    /** Billing no depende de contabilidad (sin FKs a asientos): va antes que accounting.facture (90). */
    public static final int ORDER_BILLING = 85;
    public static final int ORDER_FINANCIAL = 130;
    public static final int ORDER_AUXILIARY = 131;

    /* ---------- namespaces del IdMap ---------- */
    /** Empresa (UUID). */
    public static final String ENTERPRISE = "ENTERPRISE";
    public static final String DOC_CLASS = "DOC_CLASS";
    public static final String DOC_TYPE = "DOC_TYPE";
    public static final String COST_CENTER = "COST_CENTER";
    public static final String TYPE_ID = "TYPE_ID";
    public static final String THIRD_TYPE = "THIRD_TYPE";
    public static final String THIRD = "THIRD";
    public static final String ACCOUNT = "ACCOUNT";
    public static final String TAX = "TAX";
    public static final String UOM = "UOM";
    public static final String PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String CATEGORY = "CATEGORY";
    public static final String PRODUCT = "PRODUCT";
    /** Medio de pago (lo registra accounting.banking; mismo valor que {@code AccountingBackupKeys.PAYMENT_METHOD}). */
    public static final String PAYMENT_METHOD = "PAYMENT_METHOD";
    /** {@code billing_documents.id} (UUID) viejo→nuevo; lo registra billing y lo consumen las devoluciones. */
    public static final String BILLING_DOCUMENT = "BILLING_DOCUMENT";
    /** Fase 2. */
    public static final String PEPS_KARDEX = "PEPS_KARDEX";

    /**
     * Lo que no viaja en la copia (se declara en {@code manifest.excluded}).
     * Los asientos contables ({@code accounting.ledger}) y la tesorería
     * ({@code accounting.treasury}) SÍ viajan: ya no figuran aquí.
     * <ul>
     * <li>{@code outbox}, {@code processed_events}, {@code message_processing_errors},
     * {@code notifications.inbox}, {@code reports.executions}: estado transitorio de
     * mensajería/ejecución, sin valor al restaurar.</li>
     * <li>{@code keycloak}: usuarios y credenciales viven en el IdP, no en la BD.</li>
     * <li>{@code audit}: PENDIENTE DE DECISIÓN DEL USUARIO (copiar o no la bitácora de
     * auditoría de la empresa origen). Por defecto queda excluido.</li>
     * </ul>
     */
    public static final List<String> EXCLUDED = List.of(
            "audit", "outbox", "processed_events", "message_processing_errors",
            "notifications.inbox", "reports.executions", "keycloak");
}
