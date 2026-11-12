package co.unicauca.edu.co.contables.billing.domain.model;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado unico de facturacion (D1), parametrizado por {@link DocumentType}. Java puro: sin
 * Spring, JPA, Jackson ni Lombok. Todo mutador exige estado BORRADOR salvo {@code annul}, que
 * exige EMITIDA (RN-05, STA-2). Cubre facturas (FV/FC) y devoluciones (NC/DC); los metodos
 * propios de cada familia validan el tipo.
 */
public class BillingDocument {

    public static final String CURRENCY = "COP";
    private static final int SUPPLIER_INVOICE_NUMBER_MAX = 50;
    private static final int ANNUL_REASON_MAX = 500;
    private static final int RETURN_REASON_MAX = 500;

    private final UUID id;
    private final String enterpriseId;
    private final DocumentType type;
    private DocumentStatus status;
    private DocumentNumber number;
    private LocalDate issueDate;
    private CounterpartySnapshot counterparty;
    private String supplierInvoiceNumber;
    private IssuerSnapshot issuer;
    private PaymentTerms payment;
    private List<Withholding> withholdings;
    private String verificationCode;
    private final OriginalDocumentRef original;
    private String returnReason;
    private List<DocumentLine> lines;
    private DocumentTotals totals;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private Instant emittedAt;
    private String emittedBy;
    private Instant annulledAt;
    private String annulledBy;
    private String annulReason;
    private final Long version;

    private BillingDocument(BillingDocumentState s) {
        this.id = Objects.requireNonNull(s.id(), "id");
        this.enterpriseId = Objects.requireNonNull(s.enterpriseId(), "enterpriseId");
        this.type = Objects.requireNonNull(s.type(), "type");
        this.status = Objects.requireNonNull(s.status(), "status");
        this.number = s.number();
        this.issueDate = Objects.requireNonNull(s.issueDate(), "issueDate");
        this.counterparty = s.counterparty();
        this.supplierInvoiceNumber = s.supplierInvoiceNumber();
        this.issuer = s.issuer();
        this.payment = this.type.supportsPayment() ? s.payment() : null;
        this.withholdings = this.type.isInvoice() ? s.withholdings() : List.of();
        this.verificationCode = this.type.isInvoice() ? s.verificationCode() : null;
        this.original = s.original();
        this.returnReason = s.returnReason();
        this.lines = s.lines();
        this.totals = DocumentTotals.fromLines(this.lines, this.withholdings);
        this.createdAt = Objects.requireNonNull(s.createdAt(), "createdAt");
        this.createdBy = s.createdBy();
        this.updatedAt = Objects.requireNonNull(s.updatedAt(), "updatedAt");
        this.updatedBy = s.updatedBy();
        this.emittedAt = s.emittedAt();
        this.emittedBy = s.emittedBy();
        this.annulledAt = s.annulledAt();
        this.annulledBy = s.annulledBy();
        this.annulReason = s.annulReason();
        this.version = s.version();
    }

    // ---------- fabricas ----------

    /** Nuevo borrador parcial de factura (DRF-1): sin tercero, sin lineas, sin numero. */
    public static BillingDocument newInvoiceDraft(UUID id, String enterpriseId, DocumentType type,
            LocalDate issueDate, Instant at, String by) {
        Objects.requireNonNull(type, "type");
        if (!type.isInvoice()) {
            throw new IllegalArgumentException(type + " no es una factura");
        }
        return new BillingDocument(new BillingDocumentState(id, enterpriseId, type, DocumentStatus.BORRADOR,
                null, issueDate, null, null, null, null, null, List.of(), at, by, at, by,
                null, null, null, null, null, null));
    }

    /**
     * Nuevo borrador de devolucion contra una factura EMITIDA (RET-1, RN-17/18). Copia el tercero
     * y el numero de factura del proveedor de la original y la referencia como
     * {@link OriginalDocumentRef}. La original no se modifica (RN-23). Una original de otro tipo
     * (otra clase de documento o devolucion) se comporta como inexistente; una no emitida es
     * ORIGINAL_NOT_EMITTED.
     */
    public static BillingDocument newReturnDraft(UUID id, DocumentType returnType, BillingDocument original,
            LocalDate issueDate, Instant at, String by) {
        Objects.requireNonNull(returnType, "returnType");
        Objects.requireNonNull(original, "original");
        if (!returnType.isReturn()) {
            throw new IllegalArgumentException(returnType + " no es una devolucion");
        }
        if (!original.type.isInvoice() || original.type.returnType() != returnType) {
            throw new BillingException(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND, "Documento no encontrado");
        }
        if (original.status != DocumentStatus.EMITIDA) {
            throw new BillingException(BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED,
                    "Solo se puede devolver contra una factura emitida (estado actual: " + original.status + ")");
        }
        OriginalDocumentRef ref = new OriginalDocumentRef(original.id, original.type, original.number,
                original.issueDate);
        return new BillingDocument(new BillingDocumentState(id, original.enterpriseId, returnType,
                DocumentStatus.BORRADOR, null, issueDate, original.counterparty, original.supplierInvoiceNumber,
                null, ref, null, List.of(), at, by, at, by, null, null, null, null, null, null));
    }

    /** Reconstruye un documento desde persistencia. */
    public static BillingDocument restore(BillingDocumentState state) {
        return new BillingDocument(Objects.requireNonNull(state, "state"));
    }

    // ---------- comandos sobre borrador de factura ----------

    /**
     * Reemplazo completo e idempotente de cabecera y lineas (DRF-2). Las lineas se renumeran 1..n
     * en el orden recibido (D9). Las ventas ignoran el numero de factura del proveedor. Una fecha
     * nula conserva la actual.
     */
    public void replaceInvoiceDraft(CounterpartySnapshot newCounterparty, LocalDate newIssueDate,
            String newSupplierInvoiceNumber, List<DocumentLine> newLines, Instant at, String by) {
        replaceInvoiceDraft(newCounterparty, newIssueDate, newSupplierInvoiceNumber, newLines, this.payment, at, by);
    }

    /** Reemplazo con metodo de pago que conserva las retenciones actuales. */
    public void replaceInvoiceDraft(CounterpartySnapshot newCounterparty, LocalDate newIssueDate,
            String newSupplierInvoiceNumber, List<DocumentLine> newLines, PaymentTerms newPayment, Instant at,
            String by) {
        replaceInvoiceDraft(newCounterparty, newIssueDate, newSupplierInvoiceNumber, newLines, newPayment,
                this.withholdings, at, by);
    }

    /**
     * Como el reemplazo sin retenciones, pero tambien reemplaza el metodo de pago y las retenciones.
     * Solo las facturas (venta y compra) los conservan: en las devoluciones se ignoran. Las
     * retenciones se validan (codigo permitido, sin repetir) y el total neto no puede ser negativo
     * (BILLING_TOTAL_NEGATIVE); si algo falla el documento queda intacto.
     */
    public void replaceInvoiceDraft(CounterpartySnapshot newCounterparty, LocalDate newIssueDate,
            String newSupplierInvoiceNumber, List<DocumentLine> newLines, PaymentTerms newPayment,
            List<Withholding> newWithholdings, Instant at, String by) {
        requireDraftInvoice();
        List<Withholding> checkedWithholdings = checkWithholdings(newWithholdings);
        if (newCounterparty != null) {
            requireRole(newCounterparty);
        }
        String normalizedSupplierNumber = normalizeSupplierInvoiceNumber(newSupplierInvoiceNumber);
        List<DocumentLine> renumbered = renumberAll(newLines);

        DocumentTotals newTotals = DocumentTotals.fromLines(renumbered, checkedWithholdings);
        assertTotalNotNegative(newTotals);

        this.counterparty = newCounterparty;
        if (newIssueDate != null) {
            this.issueDate = newIssueDate;
        }
        this.supplierInvoiceNumber = normalizedSupplierNumber;
        this.lines = renumbered;
        this.withholdings = checkedWithholdings;
        this.totals = newTotals;
        this.payment = type.supportsPayment() ? newPayment : null;
        touch(at, by);
    }

    /**
     * Congela el nombre del metodo de pago (snapshot, como el emisor). Solo en borrador de factura;
     * sin pago no hace nada.
     */
    public void freezePaymentMethodName(String methodName) {
        requireDraftInvoice();
        if (payment != null) {
            this.payment = payment.withMethodName(methodName);
        }
    }

    /**
     * Congela el pago: nombre del metodo elegido. Solo en borrador de factura; sin pago no hace nada.
     */
    public void freezePayment(String methodName) {
        requireDraftInvoice();
        if (payment != null) {
            this.payment = payment.withMethodName(methodName);
        }
    }

    /**
     * Actualiza nombre y tasa de las retenciones con el catalogo vigente (se hace al emitir, antes
     * de congelar el snapshot). Mismos codigos que ya tiene el borrador. Solo en borrador de factura.
     */
    public void refreshWithholdings(List<Withholding> fresh) {
        requireDraftInvoice();
        List<Withholding> checked = checkWithholdings(fresh);
        this.withholdings = checked;
        this.totals = DocumentTotals.fromLines(lines, checked);
    }

    /**
     * Refresca en memoria los datos de presentacion del emisor y de la contraparte (vista en vivo del
     * borrador). Un argumento nulo conserva el valor actual; la contraparte debe ser la misma y con el
     * rol del documento. No toca auditoria ni version: lo persiste quien decida guardar.
     */
    public void refreshParties(IssuerSnapshot liveIssuer, CounterpartySnapshot liveCounterparty) {
        requireDraftInvoice();
        if (liveIssuer != null) {
            this.issuer = liveIssuer;
        }
        if (liveCounterparty != null && counterparty != null && liveCounterparty.thirdId() == counterparty.thirdId()
                && liveCounterparty.role() == type.kind().counterpartyRole()) {
            this.counterparty = liveCounterparty;
        }
    }

    /**
     * Reemplazo completo e idempotente de fecha, motivo y lineas de un borrador de devolucion
     * (RET-6). Las lineas deben ser de devolucion ({@code sourceLineNo} presente y sin repetir) y se
     * renumeran 1..n conservando su referencia a la original. El motivo se recorta (en blanco queda
     * nulo; maximo 500) y el borrador puede quedar parcial. La disponibilidad contra la original la
     * valida {@code ReturnAvailabilityCalculator}. Una fecha nula conserva la actual.
     */
    public void replaceReturnDraft(LocalDate newIssueDate, String reason, List<DocumentLine> newLines,
            Instant at, String by) {
        requireDraftReturn();
        String normalizedReason = normalizeReturnReason(reason);
        List<DocumentLine> renumbered = renumberAll(newLines);
        Set<Integer> seen = new HashSet<>();
        for (DocumentLine line : renumbered) {
            if (line.sourceLineNo() == null) {
                throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                        "Todo item de devolucion debe referenciar un item de la factura original");
            }
            if (!seen.add(line.sourceLineNo())) {
                throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                        "El item " + line.sourceLineNo() + " de la factura original esta repetido");
            }
        }

        if (newIssueDate != null) {
            this.issueDate = newIssueDate;
        }
        this.returnReason = normalizedReason;
        this.lines = renumbered;
        this.totals = DocumentTotals.fromLines(renumbered);
        touch(at, by);
    }

    /**
     * Refresca las referencias de catalogo de las lineas (D7, DRF-8) conservando overrides. Los
     * productos ausentes del mapa conservan su snapshot. No toca auditoria ni version: lo persiste
     * quien decida guardar.
     */
    public void refreshCatalog(Map<Long, ProductSnapshot> catalog) {
        requireDraftInvoice();
        if (catalog == null || catalog.isEmpty()) {
            return;
        }
        List<DocumentLine> refreshed = new ArrayList<>(lines.size());
        for (DocumentLine line : lines) {
            ProductSnapshot snapshot = catalog.get(line.productId());
            refreshed.add(snapshot == null ? line : line.withCatalog(snapshot));
        }
        this.lines = List.copyOf(refreshed);
        this.totals = DocumentTotals.fromLines(this.lines, this.withholdings);
    }

    /**
     * Nuevo borrador con el mismo tercero y lineas (overrides preservados), fecha de hoy, sin
     * numero y sin numero de factura del proveedor (DRF-6). Solo facturas en BORRADOR.
     */
    public BillingDocument duplicateAsDraft(UUID newId, LocalDate today, Instant at, String by) {
        requireDraftInvoice();
        return new BillingDocument(new BillingDocumentState(newId, enterpriseId, type, DocumentStatus.BORRADOR,
                null, today, counterparty, null, null, null, null, lines, at, by, at, by,
                null, null, null, null, null, null,
                payment == null ? null : payment.withoutSnapshot(), withholdings));
    }

    // ---------- reglas de transicion ----------

    /** Solo un BORRADOR puede eliminarse (DRF-5). */
    public void assertDeletable() {
        requireDraft();
    }

    /** Precondiciones de emision (EMT-1), sin modificar el documento. */
    public void assertEmittable() {
        requireDraft();
        assertEmittableWith(counterparty);
    }

    /**
     * Emite el documento (EMT-2): congela numero, emisor y tercero. {@code freshCounterparty}
     * (opcional, solo facturas) reemplaza el tercero con datos recien leidos. Si algo falla el
     * documento queda intacto.
     */
    public void emit(DocumentNumber newNumber, IssuerSnapshot newIssuer, CounterpartySnapshot freshCounterparty,
            Instant at, String by) {
        requireDraft();
        Objects.requireNonNull(newNumber, "number");
        if (!newNumber.prefix().equals(type.prefix())) {
            throw new IllegalArgumentException("El prefijo " + newNumber.prefix() + " no corresponde a " + type);
        }
        CounterpartySnapshot candidate = freshCounterparty != null && type.isInvoice()
                ? freshCounterparty : counterparty;
        assertEmittableWith(candidate);
        if (newIssuer == null) {
            throw new BillingException(BillingErrorCode.BILLING_ENTERPRISE_NOT_FOUND,
                    "La empresa emisora no existe");
        }
        if (!newIssuer.active()) {
            throw new BillingException(BillingErrorCode.BILLING_INACTIVE_REFERENCE,
                    "La empresa emisora esta inactiva");
        }
        transitionTo(DocumentStatus.EMITIDA);
        this.number = newNumber;
        this.issuer = newIssuer;
        this.counterparty = candidate;
        this.emittedAt = at;
        this.emittedBy = by;
        touch(at, by);
    }

    /**
     * Asigna el codigo de verificacion (QR) de una factura recien emitida. Se fija una sola vez: si ya
     * existe, o el documento no es una factura emitida, no hace nada (el codigo nunca cambia).
     */
    public void assignVerificationCode(String code) {
        if (!type.isInvoice() || status != DocumentStatus.EMITIDA || verificationCode != null
                || code == null || code.isBlank()) {
            return;
        }
        this.verificationCode = code;
    }

    /**
     * Anula un documento EMITIDA (ANL-1): motivo recortado de 1 a 500 caracteres, conserva el
     * numero. Una factura con devoluciones emitidas no puede anularse (ANL-2).
     */
    public void annul(String reason, boolean hasEmittedReturns, Instant at, String by) {
        if (status != DocumentStatus.EMITIDA) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_STATE,
                    "Solo se puede anular un documento emitido (estado actual: " + status + ")");
        }
        String trimmed = reason == null ? "" : reason.trim();
        if (trimmed.isEmpty() || trimmed.length() > ANNUL_REASON_MAX) {
            throw new BillingException(BillingErrorCode.BILLING_ANNUL_REASON_REQUIRED,
                    "El motivo de anulacion es obligatorio y admite maximo 500 caracteres");
        }
        if (type.isInvoice() && hasEmittedReturns) {
            throw new BillingException(BillingErrorCode.BILLING_HAS_EMITTED_RETURNS,
                    "La factura ya tiene devoluciones emitidas y no puede anularse");
        }
        transitionTo(DocumentStatus.ANULADA);
        this.annulReason = trimmed;
        this.annulledAt = at;
        this.annulledBy = by;
        touch(at, by);
    }

    // ---------- guardas ----------

    /** Un documento de otro tipo se comporta como inexistente para el recurso que lo consulta. */
    public void ensureType(DocumentType expected) {
        if (type != expected) {
            throw new BillingException(BillingErrorCode.BILLING_DOCUMENT_NOT_FOUND,
                    "Documento no encontrado");
        }
    }

    /** Control optimista: {@code null} omite la verificacion (DRF-3). */
    public void ensureVersion(Long expected) {
        if (expected == null) {
            return;
        }
        if (version == null || !version.equals(expected)) {
            throw new BillingException(BillingErrorCode.BILLING_CONCURRENT_MODIFICATION,
                    "El documento fue modificado por otro usuario");
        }
    }

    // ---------- lectura ----------

    public UUID id() {
        return id;
    }

    public String enterpriseId() {
        return enterpriseId;
    }

    public DocumentType type() {
        return type;
    }

    public DocumentStatus status() {
        return status;
    }

    /** Numero asignado al emitir; {@code null} mientras sea borrador. */
    public DocumentNumber number() {
        return number;
    }

    public LocalDate issueDate() {
        return issueDate;
    }

    public CounterpartySnapshot counterparty() {
        return counterparty;
    }

    public String supplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public IssuerSnapshot issuer() {
        return issuer;
    }

    /** Condiciones de pago: solo existen en la factura de venta y pueden ser nulas (borrador o historico). */
    public PaymentTerms payment() {
        return payment;
    }

    /**
     * Retenciones de la factura (a lo sumo una por codigo), en orden. En borrador llevan la tasa
     * vigente; en una factura emitida o anulada son el snapshot congelado. Vacia en devoluciones.
     */
    public List<Withholding> withholdings() {
        return withholdings;
    }

    /** Codigo de verificacion opaco (QR): solo facturas emitidas; nulo en borrador, devoluciones y anteriores. */
    public String verificationCode() {
        return verificationCode;
    }

    public OriginalDocumentRef original() {
        return original;
    }

    public String returnReason() {
        return returnReason;
    }

    /** Lineas en orden, inmutables. */
    public List<DocumentLine> lines() {
        return lines;
    }

    public DocumentTotals totals() {
        return totals;
    }

    public String currency() {
        return CURRENCY;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String createdBy() {
        return createdBy;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String updatedBy() {
        return updatedBy;
    }

    public Instant emittedAt() {
        return emittedAt;
    }

    public String emittedBy() {
        return emittedBy;
    }

    public Instant annulledAt() {
        return annulledAt;
    }

    public String annulledBy() {
        return annulledBy;
    }

    public String annulReason() {
        return annulReason;
    }

    /** Version de control optimista; {@code null} mientras no se haya persistido. */
    public Long version() {
        return version;
    }

    /** Copia del estado completo, apta para persistir o comparar. */
    public BillingDocumentState state() {
        return new BillingDocumentState(id, enterpriseId, type, status, number, issueDate, counterparty,
                supplierInvoiceNumber, issuer, original, returnReason, lines, createdAt, createdBy, updatedAt,
                updatedBy, emittedAt, emittedBy, annulledAt, annulledBy, annulReason, version, payment, withholdings,
                verificationCode);
    }

    // ---------- internos ----------

    private void requireDraft() {
        if (status != DocumentStatus.BORRADOR) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_STATE,
                    "El documento esta en estado " + status + " y no admite esta operacion");
        }
    }

    private void requireDraftInvoice() {
        requireDraft();
        if (!type.isInvoice()) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_STATE,
                    "La operacion solo aplica a facturas, no a " + type);
        }
    }

    private void requireDraftReturn() {
        requireDraft();
        if (!type.isReturn()) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_STATE,
                    "La operacion solo aplica a devoluciones, no a " + type);
        }
    }

    private String normalizeReturnReason(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > RETURN_REASON_MAX) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "El motivo de la devolucion admite maximo 500 caracteres");
        }
        return trimmed;
    }

    private void requireRole(CounterpartySnapshot candidate) {
        if (candidate.role() != type.kind().counterpartyRole()) {
            throw new BillingException(BillingErrorCode.BILLING_COUNTERPARTY_NOT_FOUND,
                    "El tercero no tiene el rol " + type.kind().counterpartyRole());
        }
    }

    private void assertEmittableWith(CounterpartySnapshot candidate) {
        if (candidate == null) {
            throw incomplete("El documento requiere tercero");
        }
        if (lines.isEmpty()) {
            throw incomplete("El documento requiere al menos un item");
        }
        if (type.isInvoice()) {
            assertTotalNotNegative(totals);
            if (!totals.total().isPositive()) {
                throw incomplete("El total del documento debe ser mayor que cero");
            }
            if (type == DocumentType.PURCHASE_INVOICE && isBlank(supplierInvoiceNumber)) {
                throw incomplete("La factura de compra requiere el numero de factura del proveedor");
            }

        } else if (isBlank(returnReason)) {
            throw incomplete("La devolucion requiere un motivo");
        }
        requireRole(candidate);
        if (!candidate.active()) {
            throw new BillingException(BillingErrorCode.BILLING_INACTIVE_REFERENCE, "El tercero esta inactivo");
        }
        if (type.supportsPayment()) {
            assertPaymentComplete();
        }
    }

    private static void assertTotalNotNegative(DocumentTotals candidate) {
        if (candidate.total().isNegative()) {
            throw new BillingException(BillingErrorCode.BILLING_TOTAL_NEGATIVE,
                    "Las retenciones superan el subtotal mas los impuestos: el total no puede ser negativo");
        }
    }

    /** Codigo permitido para el tipo y a lo sumo una retencion por codigo; nulo = ninguna. */
    private List<Withholding> checkWithholdings(List<Withholding> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        Set<WithholdingCode> seen = new HashSet<>();
        for (Withholding withholding : candidates) {
            Objects.requireNonNull(withholding, "withholding").code().requireAllowedFor(type);
            if (!seen.add(withholding.code())) {
                throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_INVALID,
                        "La retencion " + withholding.code() + " esta repetida");
            }
        }
        return List.copyOf(candidates);
    }

    private void assertPaymentComplete() {
        if (payment == null) {
            throw new BillingException(BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED,
                    "La factura requiere un metodo de pago");
        }
        payment.assertComplete();
    }

    private void transitionTo(DocumentStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_STATE,
                    "Transicion no permitida: " + status + " -> " + target);
        }
        this.status = target;
    }

    private void touch(Instant at, String by) {
        this.updatedAt = at;
        this.updatedBy = by;
    }

    private String normalizeSupplierInvoiceNumber(String raw) {
        if (type.kind() != DocumentKind.PURCHASE || raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > SUPPLIER_INVOICE_NUMBER_MAX) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "El numero de factura del proveedor admite maximo 50 caracteres");
        }
        return trimmed;
    }

    private static List<DocumentLine> renumberAll(List<DocumentLine> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<DocumentLine> renumbered = new ArrayList<>(source.size());
        int lineNo = 1;
        for (DocumentLine line : source) {
            renumbered.add(Objects.requireNonNull(line, "line").renumber(lineNo++));
        }
        return List.copyOf(renumbered);
    }

    private static BillingException incomplete(String detail) {
        return new BillingException(BillingErrorCode.BILLING_DRAFT_INCOMPLETE, detail);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
