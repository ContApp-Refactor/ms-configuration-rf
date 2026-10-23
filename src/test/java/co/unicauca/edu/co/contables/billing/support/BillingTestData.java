package co.unicauca.edu.co.contables.billing.support;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocumentState;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentKind;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.OriginalDocumentRef;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.Quantity;
import co.unicauca.edu.co.contables.billing.domain.model.TaxRate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Datos de prueba compartidos del modulo billing: constantes y builders de dominio (documentos,
 * lineas, snapshots). Las tandas siguientes extienden este archivo (devoluciones, etc.).
 */
public final class BillingTestData {

    public static final String TENANT_A = "tenant-a";
    public static final String TENANT_B = "tenant-b";
    public static final String ENTERPRISE_ID = "11111111-1111-1111-1111-111111111111";
    public static final String OTHER_ENTERPRISE_ID = "22222222-2222-2222-2222-222222222222";
    public static final String USER = "tester";
    public static final String OTHER_USER = "approver";

    public static final Instant NOW = Instant.parse("2026-01-15T15:00:00Z");
    public static final Instant LATER = Instant.parse("2026-01-15T16:30:00Z");
    public static final LocalDate TODAY = LocalDate.of(2026, 1, 15);

    public static final long CUSTOMER_ID = 10L;
    public static final long SUPPLIER_ID = 20L;
    public static final String SUPPLIER_INVOICE_NUMBER = "F-77";

    private BillingTestData() {
    }

    public static UUID uuid(int n) {
        return new UUID(0L, n);
    }

    // ---- valores ----

    public static Money money(String amount) {
        return Money.of(new BigDecimal(amount));
    }

    public static TaxRate rate(String percent) {
        return TaxRate.of(new BigDecimal(percent));
    }

    public static Quantity qty(String quantity) {
        return Quantity.of(new BigDecimal(quantity));
    }

    // ---- snapshots ----

    public static ProductSnapshot product(long id, String catalogPrice, String catalogRate) {
        return new ProductSnapshot(id, "P" + id, "Producto " + id, 1L, "Unidad",
                money(catalogPrice), rate(catalogRate), true);
    }

    /** Producto con precio de catalogo 100.00 e IVA 19. */
    public static ProductSnapshot product(long id) {
        return product(id, "100.00", "19");
    }

    public static ProductSnapshot inactiveProduct(long id) {
        return new ProductSnapshot(id, "P" + id, "Producto " + id, 1L, "Unidad",
                money("100.00"), rate("19"), false);
    }

    public static CounterpartySnapshot customer() {
        return customer(CUSTOMER_ID);
    }

    public static CounterpartySnapshot customer(long thirdId) {
        return new CounterpartySnapshot(thirdId, CounterpartyRole.CUSTOMER, "Cliente " + thirdId, "NIT",
                "9001" + thirdId, "7", "Calle 1 # 2-3", "cliente@example.com", "3000000000", true);
    }

    public static CounterpartySnapshot inactiveCustomer() {
        return new CounterpartySnapshot(CUSTOMER_ID, CounterpartyRole.CUSTOMER, "Cliente " + CUSTOMER_ID, "NIT",
                "9001" + CUSTOMER_ID, "7", "Calle 1 # 2-3", "cliente@example.com", "3000000000", false);
    }

    public static CounterpartySnapshot supplier() {
        return new CounterpartySnapshot(SUPPLIER_ID, CounterpartyRole.SUPPLIER, "Proveedor " + SUPPLIER_ID, "NIT",
                "8001" + SUPPLIER_ID, "3", "Carrera 5 # 6-7", "proveedor@example.com", "3111111111", true);
    }

    public static IssuerSnapshot issuer() {
        return new IssuerSnapshot(ENTERPRISE_ID, "Empresa Emisora SAS", "900123456", "1", true);
    }

    public static IssuerSnapshot inactiveIssuer() {
        return new IssuerSnapshot(ENTERPRISE_ID, "Empresa Emisora SAS", "900123456", "1", false);
    }

    // ---- lineas ----

    /** Linea de factura que sigue el catalogo (sin overrides). */
    public static DocumentLine lineOf(int lineNo, ProductSnapshot product, String quantity) {
        return DocumentLine.forInvoice(lineNo, product, qty(quantity), null, null);
    }

    // ---- documentos ----

    public static CounterpartySnapshot counterpartyFor(DocumentType type) {
        return type.kind().counterpartyRole() == CounterpartyRole.CUSTOMER ? customer() : supplier();
    }

    /** Borrador vacio de factura (uuid(1), ENTERPRISE_ID, TODAY, NOW, USER). */
    public static BillingDocument draft(DocumentType type) {
        return BillingDocument.newInvoiceDraft(uuid(1), ENTERPRISE_ID, type, TODAY, NOW, USER);
    }

    public static BillingDocument draftSale() {
        return draft(DocumentType.SALE_INVOICE);
    }

    public static BillingDocument draftPurchase() {
        return draft(DocumentType.PURCHASE_INVOICE);
    }

    /**
     * Borrador completo y emitible: tercero del rol correcto, una linea (producto 1, cantidad 2,
     * 100.00 + 19% = 238.00) y, en compras, numero de factura del proveedor.
     */
    public static BillingDocument readyDraft(DocumentType type) {
        BillingDocument doc = draft(type);
        String supplierNumber = type.kind().counterpartyRole() == CounterpartyRole.SUPPLIER
                ? SUPPLIER_INVOICE_NUMBER : null;
        doc.replaceInvoiceDraft(counterpartyFor(type), TODAY, supplierNumber,
                List.of(lineOf(1, product(1L), "2")), NOW, USER);
        return doc;
    }

    public static BillingDocument readySaleDraft() {
        return readyDraft(DocumentType.SALE_INVOICE);
    }

    public static BillingDocument readyPurchaseDraft() {
        return readyDraft(DocumentType.PURCHASE_INVOICE);
    }

    /** Factura emitida con el consecutivo dado, emisor activo y sin refrescar el tercero. */
    public static BillingDocument emitted(DocumentType type, long numberValue) {
        BillingDocument doc = readyDraft(type);
        doc.emit(DocumentNumber.of(type, numberValue), issuer(), null, LATER, OTHER_USER);
        return doc;
    }

    /** Factura de venta emitida con numero FV-000007. */
    public static BillingDocument emittedSale() {
        return emitted(DocumentType.SALE_INVOICE, 7L);
    }

    public static BillingDocument emittedPurchase() {
        return emitted(DocumentType.PURCHASE_INVOICE, 7L);
    }

    public static BillingDocument annulledSale() {
        BillingDocument doc = emittedSale();
        doc.annul("error de digitacion", false, LATER.plusSeconds(60), OTHER_USER);
        return doc;
    }

    /** Copia del documento con otra version (simula lo leido de persistencia). */
    public static BillingDocument withVersion(BillingDocument doc, Long version) {
        return BillingDocument.restore(doc.state().withVersion(version));
    }

    // ---- devoluciones (H8a) ----

    /** Factura emitida (consecutivo dado) con las lineas indicadas; tercero y emisor por defecto. */
    public static BillingDocument emittedInvoice(UUID id, DocumentType type, long numberValue, List<DocumentLine> lines) {
        BillingDocument doc = BillingDocument.newInvoiceDraft(id, ENTERPRISE_ID, type, TODAY, NOW, USER);
        String supplierNumber = type.kind() == DocumentKind.PURCHASE ? SUPPLIER_INVOICE_NUMBER : null;
        doc.replaceInvoiceDraft(counterpartyFor(type), TODAY, supplierNumber, lines, NOW, USER);
        doc.emit(DocumentNumber.of(type, numberValue), issuer(), null, LATER, OTHER_USER);
        return doc;
    }

    /** Factura de venta emitida con varias lineas (se numeran 1..n en el orden dado). */
    public static BillingDocument emittedSaleWithLines(UUID id, long numberValue, DocumentLine... lines) {
        return emittedInvoice(id, DocumentType.SALE_INVOICE, numberValue, List.of(lines));
    }

    /** Linea de devolucion heredada de la linea {@code sourceLineNo} de la factura original. */
    public static DocumentLine returnLineOf(BillingDocument original, int sourceLineNo, String quantity) {
        DocumentLine source = original.lines().stream()
                .filter(l -> l.lineNo() == sourceLineNo)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La factura no tiene la linea " + sourceLineNo));
        return DocumentLine.forReturn(1, source, qty(quantity));
    }

    /** Borrador de devolucion (tipo derivado de la original) con motivo y lineas dadas. */
    public static BillingDocument returnDraftOf(BillingDocument original, UUID id, String reason,
            DocumentLine... lines) {
        BillingDocument doc = BillingDocument.newReturnDraft(id, original.type().returnType(), original,
                TODAY, NOW, USER);
        doc.replaceReturnDraft(TODAY, reason, List.of(lines), NOW, USER);
        return doc;
    }

    public static BillingDocument emittedReturnOf(BillingDocument original, UUID id, long numberValue,
            DocumentLine... lines) {
        BillingDocument doc = returnDraftOf(original, id, "producto defectuoso", lines);
        doc.emit(DocumentNumber.of(doc.type(), numberValue), issuer(), null, LATER, OTHER_USER);
        return doc;
    }

    public static BillingDocument annulledReturnOf(BillingDocument original, UUID id, long numberValue,
            DocumentLine... lines) {
        BillingDocument doc = emittedReturnOf(original, id, numberValue, lines);
        doc.annul("cliente desistio", false, LATER.plusSeconds(60), OTHER_USER);
        return doc;
    }

    /**
     * Estado de una devolucion de venta en BORRADOR (builder interino anterior a H8a, aun valido):
     * una linea heredada con sourceLineNo 1.
     */
    public static BillingDocumentState draftSaleReturnState(String reason) {
        DocumentLine line = DocumentLine.restore(1, 1, 1L, "P1", "Producto 1", 1L, "Unidad", qty("1"),
                money("100.00"), rate("19"), null, null);
        return new BillingDocumentState(uuid(2), ENTERPRISE_ID, DocumentType.SALE_RETURN, DocumentStatus.BORRADOR,
                null, TODAY, customer(), null, null,
                new OriginalDocumentRef(uuid(1), DocumentType.SALE_INVOICE,
                        DocumentNumber.of(DocumentType.SALE_INVOICE, 7L), TODAY),
                reason, List.of(line), NOW, USER, NOW, USER, null, null, null, null, null, null);
    }
}
