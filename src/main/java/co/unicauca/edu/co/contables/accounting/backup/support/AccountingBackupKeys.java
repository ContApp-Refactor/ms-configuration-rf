package co.unicauca.edu.co.contables.accounting.backup.support;

/**
 * @brief Claves de módulo, orden y namespaces del {@code IdMap} de los participantes de
 * contabilidad ({@code accounting.*}).
 *
 * Viven aquí y no en {@code BackupKeys} porque solo los consumen los participantes de este
 * mismo bounded context. Los namespaces de OTROS módulos (cuentas, terceros, centros de costo,
 * productos) se toman de {@code BackupKeys}.
 *
 * Orden (justificado por FKs, difiere de la tabla indicativa del diseño §15):
 * <ul>
 * <li>35 banking: bancos, cuentas bancarias, medios de pago (necesita el PUC).</li>
 * <li>90 facture: facturas (legado + skeleton) y devoluciones (necesita terceros y productos).</li>
 * <li>100 debtPayments: cartera (réplicas de factura, recibos, castigos).</li>
 * <li>110 ledger: asientos, recibos contables y réplicas contables. Va DESPUÉS de debtPayments porque
 * {@code accounting_receipts.original_receipt_id} y los asientos de castigo apuntan a ids de cartera.
 * Recalcula los saldos del PUC (D5).</li>
 * <li>120 treasury: tesorería (comprobantes de egreso apuntan a asientos, bancos y medios de pago).</li>
 * </ul>
 */
public final class AccountingBackupKeys {

    private AccountingBackupKeys() {
    }

    /* ---------- moduleKey ---------- */
    public static final String MODULE_BANKING = "accounting.banking";
    public static final String MODULE_FACTURE = "accounting.facture";
    public static final String MODULE_DEBT_PAYMENTS = "accounting.debtPayments";
    public static final String MODULE_LEDGER = "accounting.ledger";
    public static final String MODULE_TREASURY = "accounting.treasury";

    /* ---------- order() ---------- */
    public static final int ORDER_BANKING = 35;
    public static final int ORDER_FACTURE = 90;
    public static final int ORDER_DEBT_PAYMENTS = 100;
    public static final int ORDER_LEDGER = 110;
    public static final int ORDER_TREASURY = 120;

    /* ---------- namespaces del IdMap (ids) ---------- */
    public static final String BANK = "BANK";
    public static final String BANK_ACCOUNT = "BANK_ACCOUNT";
    public static final String PAYMENT_METHOD = "PAYMENT_METHOD";
    /** {@code invoice_replica.id} de cartera (debt_payments). */
    public static final String DP_INVOICE = "DP_INVOICE";
    /** {@code receipts.id} de cartera. */
    public static final String DP_RECEIPT = "DP_RECEIPT";
    /** {@code portfolio_write_offs.id} de cartera. */
    public static final String DP_WRITE_OFF = "DP_WRITE_OFF";
    /** {@code accounting_invoice_replica.id}. */
    public static final String ACC_INVOICE = "ACC_INVOICE";
    /** {@code accounting_receipts.id}. */
    public static final String ACC_RECEIPT = "ACC_RECEIPT";
    /** {@code accounting_entries.id} (asientos). */
    public static final String ACC_ENTRY = "ACC_ENTRY";
    public static final String SUPPLIER_INVOICE = "SUPPLIER_INVOICE";
    public static final String PAYMENT_VOUCHER = "PAYMENT_VOUCHER";

    /* ---------- namespaces del IdMap (números de documento) ---------- */
    /**
     * Número de factura ({@code fact_code}) viejo→nuevo. Se conserva el original si está libre; si el
     * número ya existe en la BD (la empresa origen sigue ahí), se asigna uno nuevo y TODAS las tablas que
     * guardan ese número (facturas, réplicas, devoluciones) usan el mismo. Otros módulos que guarden el
     * número como etiqueta (p. ej. {@code kardex.factCode}) pueden consultarlo con {@code idMap.find}.
     */
    public static final String FACT_CODE = "FACT_CODE";
    /** Código de recibo de cartera ({@code RC-...}) viejo→nuevo (único global en BD). */
    public static final String RECEIPT_CODE = "RECEIPT_CODE";
}
