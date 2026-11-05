package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.GetDocumentUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.GetReturnAvailabilityUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListDocumentReturnsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListDocumentsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.ListInvoicesWithReturnsUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.InvoiceReturnAggregate;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.ProductSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import co.unicauca.edu.co.contables.billing.domain.service.ReturnAvailabilityCalculator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Consultas de documentos (QRY-1, QRY-2) y datos derivados de devoluciones (RET-9..11). Al leer
 * una factura en BORRADOR se re-resuelve el catalogo solo en memoria (DRF-8, D7): nada se
 * persiste. Los documentos emitidos o anulados se devuelven congelados. El estado de devolucion
 * de una factura EMITIDA y el ordinal de una devolucion numerada son DERIVADOS: se calculan al
 * leer, nunca se almacenan.
 */
public class BillingDocumentQueryService implements GetDocumentUseCase, ListDocumentsUseCase,
        GetReturnAvailabilityUseCase, ListDocumentReturnsUseCase, ListInvoicesWithReturnsUseCase {

    private final BillingDocumentRepositoryPort repository;
    private final BillingDocumentLoader loader;
    private final BillingReferenceResolver resolver;
    private final ReturnAvailabilityCalculator calculator = new ReturnAvailabilityCalculator();

    public BillingDocumentQueryService(BillingDocumentRepositoryPort repository, BillingDocumentLoader loader,
            BillingReferenceResolver resolver) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    @Override
    public BillingDocumentView get(String enterpriseId, DocumentType type, UUID id) {
        BillingDocument document = loader.load(enterpriseId, type, id);
        if (document.status() == DocumentStatus.BORRADOR && document.type().isInvoice()) {
            refreshInMemory(enterpriseId, document);
        }
        return derive(enterpriseId, document);
    }

    @Override
    public List<BillingDocumentSummary> list(String enterpriseId, DocumentType type, DocumentStatus status,
            UUID originalDocumentId) {
        return repository.findSummaries(enterpriseId, type, status, originalDocumentId);
    }

    @Override
    public ReturnAvailability availability(String enterpriseId, DocumentType invoiceType, UUID invoiceId) {
        requireInvoiceType(invoiceType);
        BillingDocument invoice = loader.load(enterpriseId, invoiceType, invoiceId);
        return calculator.availability(invoice, repository.findReturnsOf(enterpriseId, invoice.id()));
    }

    @Override
    public List<ReturnSummary> returnsOf(String enterpriseId, DocumentType invoiceType, UUID invoiceId) {
        requireInvoiceType(invoiceType);
        BillingDocument invoice = loader.load(enterpriseId, invoiceType, invoiceId);
        List<BillingDocument> returns = repository.findReturnsOf(enterpriseId, invoice.id());
        Map<UUID, Integer> ordinals = ReturnAvailabilityCalculator.ordinalsOf(returns);

        List<ReturnSummary> result = new ArrayList<>(returns.size());
        for (BillingDocument returnDocument : returns) {
            result.add(new ReturnSummary(BillingDocumentSummary.from(returnDocument),
                    ordinals.get(returnDocument.id())));
        }
        // numeradas primero por ordinal; los borradores (sin ordinal) al final por antiguedad
        result.sort(Comparator.comparing(ReturnSummary::ordinal, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(summary -> summary.summary().createdAt()));
        return result;
    }

    /**
     * Facturas con al menos una devolucion EMITIDA. Una factura cuyas devoluciones fueron todas
     * anuladas deja de aparecer (el agregado solo cuenta EMITIDA). El estado sale de comparar la
     * cantidad devuelta contra la vendida; sin dato de cantidad vendida la fila se omite en lugar de
     * clasificarla mal.
     */
    @Override
    public List<InvoiceWithReturnsSummary> list(String enterpriseId, DocumentType invoiceType) {
        requireInvoiceType(invoiceType);
        List<InvoiceReturnAggregate> aggregates =
                repository.aggregateEmittedReturns(enterpriseId, invoiceType.returnType());
        if (aggregates.isEmpty()) {
            return List.of();
        }
        Map<UUID, InvoiceReturnAggregate> byInvoice = new LinkedHashMap<>();
        for (InvoiceReturnAggregate aggregate : aggregates) {
            byInvoice.put(aggregate.originalId(), aggregate);
        }
        Set<UUID> invoiceIds = new LinkedHashSet<>(byInvoice.keySet());
        List<BillingDocumentSummary> summaries = repository.findSummariesByIds(enterpriseId, invoiceIds);
        Map<UUID, BigDecimal> sold = repository.soldQuantities(enterpriseId, invoiceIds);

        List<InvoiceWithReturnsSummary> result = new ArrayList<>(summaries.size());
        for (BillingDocumentSummary summary : summaries) {
            InvoiceReturnAggregate aggregate = byInvoice.get(summary.id());
            BigDecimal soldQuantity = sold.get(summary.id());
            if (aggregate == null || soldQuantity == null) {
                continue;
            }
            result.add(new InvoiceWithReturnsSummary(summary, (int) aggregate.count(), aggregate.total(),
                    stateOf(aggregate.quantity(), soldQuantity)));
        }
        return result;
    }

    // ---------- internos ----------

    /** Agrega al documento sus datos derivados de devoluciones (estado de la factura / ordinal). */
    private BillingDocumentView derive(String enterpriseId, BillingDocument document) {
        if (document.type().isInvoice() && document.status() == DocumentStatus.EMITIDA) {
            ReturnStatus status = calculator
                    .availability(document, repository.findReturnsOf(enterpriseId, document.id())).status();
            return new BillingDocumentView(document, status, null);
        }
        if (document.type().isReturn() && document.status() != DocumentStatus.BORRADOR
                && document.number() != null) {
            List<BillingDocument> returns = ReturnEventContextFactory.withDocument(
                    repository.findReturnsOf(enterpriseId, document.original().id()), document);
            Integer ordinal = ReturnAvailabilityCalculator.ordinalsOf(returns).get(document.id());
            return new BillingDocumentView(document, null, ordinal);
        }
        return BillingDocumentView.of(document);
    }

    private static ReturnStatus stateOf(BigDecimal returned, BigDecimal sold) {
        if (returned.signum() <= 0) {
            return ReturnStatus.SIN_DEVOLUCION;
        }
        return returned.compareTo(sold) >= 0 ? ReturnStatus.TOTAL : ReturnStatus.PARCIAL;
    }

    private static void requireInvoiceType(DocumentType type) {
        if (type == null || !type.isInvoice()) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "El tipo " + type + " no es una factura");
        }
    }

    private void refreshInMemory(String enterpriseId, BillingDocument document) {
        if (document.lines().isEmpty()) {
            return;
        }
        List<Long> productIds = new ArrayList<>(document.lines().size());
        for (DocumentLine line : document.lines()) {
            productIds.add(line.productId());
        }
        Map<Long, ProductSnapshot> catalog = resolver.tryProducts(enterpriseId, productIds,
                document.type().kind());
        document.refreshCatalog(catalog);
    }
}
