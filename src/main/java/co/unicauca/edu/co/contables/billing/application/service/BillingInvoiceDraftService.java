package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.DuplicateInvoiceDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.SaveInvoiceDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.TaxCatalogPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.PaymentTerms;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.Quantity;
import co.unicauca.edu.co.contables.billing.domain.model.TaxRate;
import co.unicauca.edu.co.contables.billing.domain.model.Withholding;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Crea, actualiza (autosave) y duplica borradores de factura. Java plano: la transaccion la
 * aporta el adaptador de entrada (D3). Orden de cada guardado (DRF-2, DRF-4): tercero -> productos
 * -> reemplazo en el dominio -> persistencia. Los productos inactivos se admiten en borrador;
 * solo la emision los rechaza.
 */
public class BillingInvoiceDraftService implements SaveInvoiceDraftUseCase, DuplicateInvoiceDraftUseCase {

    private final BillingDocumentRepositoryPort repository;
    private final BillingDocumentLoader loader;
    private final BillingReferenceResolver resolver;
    private final BillingExecutionContextPort context;
    private final BillingClockPort clock;
    private final BillingIdGeneratorPort ids;
    private final SupplierInvoiceNumberGuard supplierNumbers;
    /** Opcional: si esta, el borrador descarta la cuenta bancaria de un metodo que no la exige. */
    private final PaymentCatalogPort paymentCatalog;
    /** Resuelve el impuesto de cada retencion; sin catalogo de impuestos no se admiten retenciones. */
    private final WithholdingResolver withholdingResolver;

    public BillingInvoiceDraftService(BillingDocumentRepositoryPort repository, BillingDocumentLoader loader,
            BillingReferenceResolver resolver, BillingExecutionContextPort context, BillingClockPort clock,
            BillingIdGeneratorPort ids) {
        this(repository, loader, resolver, context, clock, ids, null);
    }

    public BillingInvoiceDraftService(BillingDocumentRepositoryPort repository, BillingDocumentLoader loader,
            BillingReferenceResolver resolver, BillingExecutionContextPort context, BillingClockPort clock,
            BillingIdGeneratorPort ids, PaymentCatalogPort paymentCatalog) {
        this(repository, loader, resolver, context, clock, ids, paymentCatalog, null);
    }

    public BillingInvoiceDraftService(BillingDocumentRepositoryPort repository, BillingDocumentLoader loader,
            BillingReferenceResolver resolver, BillingExecutionContextPort context, BillingClockPort clock,
            BillingIdGeneratorPort ids, PaymentCatalogPort paymentCatalog, TaxCatalogPort taxCatalog) {
        this.paymentCatalog = paymentCatalog;
        this.withholdingResolver = new WithholdingResolver(taxCatalog);
        this.repository = Objects.requireNonNull(repository, "repository");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.context = Objects.requireNonNull(context, "context");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.ids = Objects.requireNonNull(ids, "ids");
        this.supplierNumbers = new SupplierInvoiceNumberGuard(repository);
    }

    @Override
    public BillingDocument create(String enterpriseId, DocumentType type, InvoiceDraftCommand command) {
        requireInvoiceType(type);
        Objects.requireNonNull(command, "command");
        Resolved resolved = resolve(enterpriseId, type, command);
        List<Withholding> withholdings = withholdingResolver.resolve(enterpriseId, type, command.withholdings());
        assertSupplierNumberUnique(enterpriseId, type, resolved, command, null);

        Instant now = clock.now();
        String user = context.username();
        BillingDocument document = BillingDocument.newInvoiceDraft(ids.newId(), enterpriseId, type,
                command.issueDate() != null ? command.issueDate() : clock.today(), now, user);
        document.replaceInvoiceDraft(resolved.counterparty(), command.issueDate(),
                command.supplierInvoiceNumber(), resolved.lines(), paymentOf(type, command), withholdings, now, user);
        return repository.save(document);
    }

    @Override
    public BillingDocument update(String enterpriseId, DocumentType type, UUID id, InvoiceDraftCommand command) {
        requireInvoiceType(type);
        Objects.requireNonNull(command, "command");
        BillingDocument document = loader.load(enterpriseId, type, id, command.expectedVersion());
        Resolved resolved = resolve(enterpriseId, type, command);
        List<Withholding> withholdings = withholdingResolver.resolve(enterpriseId, type, command.withholdings());
        assertSupplierNumberUnique(enterpriseId, type, resolved, command, id);

        document.replaceInvoiceDraft(resolved.counterparty(), command.issueDate(),
                command.supplierInvoiceNumber(), resolved.lines(), paymentOf(type, command), withholdings,
                clock.now(), context.username());
        return repository.save(document);
    }

    @Override
    public BillingDocument duplicate(String enterpriseId, DocumentType type, UUID id) {
        requireInvoiceType(type);
        BillingDocument source = loader.load(enterpriseId, type, id);
        BillingDocument copy = source.duplicateAsDraft(ids.newId(), clock.today(), clock.now(),
                context.username());
        return repository.save(copy);
    }

    // ---------- internos ----------

    private Resolved resolve(String enterpriseId, DocumentType type, InvoiceDraftCommand command) {
        CounterpartySnapshot counterparty = command.counterpartyId() == null ? null
                : resolver.requireCounterparty(enterpriseId, command.counterpartyId(), type.kind());

        List<InvoiceLineCommand> requested = command.lines() == null ? List.of() : command.lines();
        List<Long> productIds = new ArrayList<>(requested.size());
        for (InvoiceLineCommand line : requested) {
            productIds.add(Objects.requireNonNull(line, "line").productId());
        }
        Map<Long, ProductSnapshot> catalog = resolver.requireProducts(enterpriseId, productIds, type.kind());

        List<DocumentLine> lines = new ArrayList<>(requested.size());
        int lineNo = 1;
        for (InvoiceLineCommand line : requested) {
            lines.add(DocumentLine.forInvoice(lineNo++, catalog.get(line.productId()),
                    Quantity.of(line.quantity()),
                    line.unitPriceOverride() == null ? null : Money.of(line.unitPriceOverride()),
                    line.taxRateOverride() == null ? null : TaxRate.of(line.taxRateOverride())));
        }
        return new Resolved(counterparty, lines);
    }

    /**
     * Metodo de pago del borrador. Solo las facturas (venta y compra) lo guardan: en las devoluciones el dato
     * se ignora (queda nulo). El borrador puede estar incompleto; la validez del metodo se exige al
     * emitir.
     */
    private PaymentTerms paymentOf(DocumentType type, InvoiceDraftCommand command) {
        InvoiceDraftCommand.PaymentCommand payment = command.payment();
        if (!type.supportsPayment() || payment == null) {
            return null;
        }
        return PaymentTerms.draft(payment.paymentMethodId());
    }

    /** Unicidad del N.º de factura del proveedor (solo compras); ver {@link SupplierInvoiceNumberGuard}. */
    private void assertSupplierNumberUnique(String enterpriseId, DocumentType type, Resolved resolved,
            InvoiceDraftCommand command, UUID excludeId) {
        supplierNumbers.assertUnique(enterpriseId, type,
                resolved.counterparty() == null ? null : resolved.counterparty().thirdId(),
                command.supplierInvoiceNumber(), excludeId);
    }

    private static void requireInvoiceType(DocumentType type) {
        if (type == null || !type.isInvoice()) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "El tipo " + type + " no es una factura");
        }
    }

    private record Resolved(CounterpartySnapshot counterparty, List<DocumentLine> lines) {
    }
}
