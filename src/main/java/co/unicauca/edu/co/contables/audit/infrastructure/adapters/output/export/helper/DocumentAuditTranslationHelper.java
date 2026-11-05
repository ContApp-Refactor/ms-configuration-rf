package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DocumentAuditTranslationHelper {

    private DocumentAuditTranslationHelper() {
    }

    private static final Set<String> HIDDEN_FIELDS = Set.of(
            "id",
            "entId",
            "documentId",
            "parentId");

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(

            // Receipt
            Map.entry("receiptCode", "Código recibo"),
            Map.entry("receiptType", "Tipo recibo"),
            Map.entry("paymentMethodId", "Método de pago"),
            Map.entry("paymentMethodAccount", "Cuenta método de pago"),
            Map.entry("thirdPartName", "Nombre tercero"),
            Map.entry("thirdPartyName", "Nombre tercero"),
            Map.entry("thirdPartyId", "Id tercero"),
            Map.entry("thirdId", "Id tercero"),
            Map.entry("issueDate", "Fecha emisión"),
            Map.entry("observations", "Observaciones"),
            Map.entry("ledgerAccountId", "Cuenta contable"),
            Map.entry("centerCostId", "Id centro de costo"),
            Map.entry("costCenterId", "Id centro de costo"),

            Map.entry("code", "Código"),
            Map.entry("justification", "Justificación"),
            Map.entry("writeOffDate", "Fecha castigo"),
            Map.entry("status", "Estado"),
            Map.entry("debitAuxiliaryAccount", "Cuenta auxiliar débito"),

            Map.entry("accountingAccount", "Cuenta contable"),
            Map.entry("invoiceId", "Id factura"),
            Map.entry("invoiceCode", "Código factura"),

            Map.entry("amountPaid", "Valor pagado"),
            Map.entry("amountReversed", "Valor reversado"),

            Map.entry("amountWrittenOff", "Valor castigado"),

            Map.entry("totalAmount", "Valor total"),
            Map.entry("totalAmountReversed", "Valor total reversado"),
            Map.entry("totalAmountRestored", "Valor restaurado"),

            Map.entry("operationType", "Tipo operación"),
            Map.entry("documentSubtype", "Subtipo documento"),
            Map.entry("affectsInvoices", "Afecta facturas"),

            Map.entry("invoiceStatus", "Estado factura"),
            Map.entry("pendingValue", "Saldo pendiente"),

            Map.entry("factCode", "Código factura"),
            Map.entry("factureType", "Tipo factura"),
            Map.entry("expirationDate", "Fecha vencimiento"),

            Map.entry("productId", "Producto"),
            Map.entry("description", "Descripción"),
            Map.entry("amount", "Cantidad"),
            Map.entry("quantity", "Cantidad"),
            Map.entry("unitPrice", "Precio unitario"),
            Map.entry("discount", "Descuento"),
            Map.entry("taxPercentage", "Impuesto"),
            Map.entry("subtotal", "Subtotal"),

            Map.entry("totalValue", "Valor total"),
            Map.entry("totalPay", "Valor pagado"),

            Map.entry("originalFactCode", "Factura origen"),
            Map.entry("returnType", "Tipo devolución"),
            Map.entry("reason", "Motivo"),

            Map.entry("documentCode", "Código documento"),
            Map.entry("voidedDocument", "Documento anulado"));

    private static final Map<String, String> VALUE_LABELS = Map.ofEntries(

            Map.entry("INVOICE_PAYMENT", "Pago de factura"),
            Map.entry("DIRECT_INCOME", "Ingreso directo"),

            Map.entry("PENDING_CONFIRMATION", "Pendiente confirmación"),
            Map.entry("CONFIRMED", "Confirmado"),
            Map.entry("VOIDED", "Anulado"),

            Map.entry("PENDING", "Pendiente"),
            Map.entry("PAID", "Pagado"),
            Map.entry("PENDING_WRITTEN_OFF", "Pendiente castigo"),
            Map.entry("WRITTEN_OFF", "Castigado"),

            Map.entry("CREATE", "Creación"),
            Map.entry("APPROVE", "Aprobación"),
            Map.entry("UPDATE", "Actualización"),
            Map.entry("VOID", "Anulación"),
            Map.entry("DELETE", "Eliminación"),

            Map.entry("PURCHASE", "Factura Compra"),
            Map.entry("SALE", "Factura Venta"),
            Map.entry("RETURN_ON_SALE", "Devolución en venta"),
            Map.entry("RETURN_ON_PURCHASE", "Devolución en compra"),
            Map.entry("NON_COMMERCIAL_ENTRY", "Entrada no comercial"),
            Map.entry("NON_COMMERCIAL_EXIT", "Salida no comercial"),
            Map.entry("RECEIPT_INVOICE_PAYMENT", "Recibo de caja"),
            Map.entry("RECEIPT_DIRECT_INCOME", "Recibo de caja"),
            Map.entry("WRITE_OFF", "Castigo Cartera"),

            Map.entry("PEPS", "PEPS"),
            Map.entry("WEIGHTED_AVERAGE", "Promedio ponderado"),

            Map.entry("true", "Sí"),
            Map.entry("false", "No"),

            Map.entry("WALLET", "Cartera"),
            Map.entry("INVOICES", "Facturas"),
            Map.entry("TREASURY", "Tesorería"),
            Map.entry("ACCOUNTING", "Contabilidad"));

    public static String translateField(String key) {
        return FIELD_LABELS.getOrDefault(key, key);
    }

    public static String translateValue(Object value) {

        if (value == null) {
            return "—";
        }

        if (value instanceof Boolean b) {
            return b ? "Sí" : "No";
        }

        if (value instanceof List<?> list) {
            return list.stream()
                    .map(DocumentAuditTranslationHelper::translateValue)
                    .collect(Collectors.joining(", "));
        }

        return VALUE_LABELS.getOrDefault(
                String.valueOf(value),
                String.valueOf(value));
    }

    public static boolean isVisibleField(String key) {
        return !HIDDEN_FIELDS.contains(key);
    }
}