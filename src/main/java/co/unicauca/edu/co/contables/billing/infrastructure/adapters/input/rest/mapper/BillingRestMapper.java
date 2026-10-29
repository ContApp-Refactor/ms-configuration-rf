package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentView;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.InvoiceWithReturnsSummary;
import co.unicauca.edu.co.contables.billing.application.ports.input.view.ReturnSummary;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentTotals;
import co.unicauca.edu.co.contables.billing.domain.model.OriginalDocumentRef;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnAvailability;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnLineAvailability;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingDocumentSummaryResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingInvoiceLineResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingInvoiceResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingReturnLineResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingReturnResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.BillingTotalsResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.InvoiceDraftRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.InvoiceLineRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.InvoiceWithReturnsResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnAvailabilityLineResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnAvailabilityResponse;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnDraftRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnLineRequest;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto.ReturnSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Mapeo manual DTO <-> casos de uso (D6). Solo lee del request los campos editables; totales,
 * nombres y referencias de catalogo que envie el cliente nunca se consultan (DOC-4). Las
 * respuestas expresan el dinero en centavos exactos (DOC-6).
 */
@Component("billingRestMapper")
public class BillingRestMapper {

    // ---------- request -> command ----------

    public InvoiceDraftCommand toCommand(InvoiceDraftRequest request) {
        List<InvoiceLineCommand> lines = request.lines() == null
                ? List.of()
                : request.lines().stream().map(this::toLineCommand).toList();
        return new InvoiceDraftCommand(
                parseOptionalId(request.clientId(), "clientId"),
                request.date(),
                request.supplierInvoiceNumber(),
                lines,
                request.version());
    }

    private InvoiceLineCommand toLineCommand(InvoiceLineRequest line) {
        Long productId = parseOptionalId(line.productId(), "productId");
        if (productId == null) {
            throw invalid("productId es obligatorio");
        }
        return new InvoiceLineCommand(
                productId,
                line.cantidad(),
                BillingCents.pesos(line.precioUnitarioCents()),
                line.ivaPct());
    }

    private static Long parseOptionalId(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw invalid(field + " invalido: " + raw);
        }
    }

    private static BillingException invalid(String detail) {
        return new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR, detail);
    }

    // ---------- document -> response ----------

    public BillingInvoiceResponse toResponse(BillingDocumentView view) {
        BillingDocument doc = view.doc();
        CounterpartySnapshot client = doc.counterparty();
        return new BillingInvoiceResponse(
                doc.id().toString(),
                display(doc.number()),
                doc.status().name(),
                doc.type().kind().name(),
                doc.type().name(),
                client == null ? null : String.valueOf(client.thirdId()),
                client == null ? null : client.name(),
                client == null ? null : client.idNumber(),
                doc.supplierInvoiceNumber(),
                doc.issueDate(),
                doc.lines().stream().map(this::toLineResponse).toList(),
                toTotals(doc.totals()),
                view.returnStatus() == null ? null : view.returnStatus().name(),
                doc.createdAt(),
                doc.updatedAt(),
                doc.emittedAt(),
                doc.annulledAt(),
                doc.annulReason(),
                doc.version());
    }

    public BillingDocumentSummaryResponse toSummaryResponse(BillingDocumentSummary summary) {
        return new BillingDocumentSummaryResponse(
                summary.id().toString(),
                display(summary.number()),
                summary.status().name(),
                summary.type().kind().name(),
                summary.type().name(),
                summary.counterpartyId() == null ? null : String.valueOf(summary.counterpartyId()),
                summary.counterpartyName(),
                summary.supplierInvoiceNumber(),
                summary.issueDate(),
                toTotals(summary.totals()),
                summary.createdAt(),
                summary.updatedAt(),
                summary.emittedAt(),
                summary.annulledAt(),
                summary.version());
    }

    private BillingInvoiceLineResponse toLineResponse(DocumentLine line) {
        return new BillingInvoiceLineResponse(
                line.lineNo(),
                String.valueOf(line.productId()),
                line.productCode(),
                line.productName(),
                line.unitOfMeasureName(),
                line.quantity().value(),
                BillingCents.cents(line.catalogUnitPrice()),
                line.catalogTaxRate().value(),
                BillingCents.cents(line.unitPrice()),
                line.taxRate().value(),
                line.priceOverridden(),
                line.taxOverridden(),
                BillingCents.cents(line.subtotal()),
                BillingCents.cents(line.taxAmount()),
                BillingCents.cents(line.total()));
    }

    private static BillingTotalsResponse toTotals(DocumentTotals totals) {
        return new BillingTotalsResponse(
                BillingCents.cents(totals.subtotal()),
                BillingCents.cents(totals.tax()),
                BillingCents.cents(totals.total()));
    }

    private static String display(DocumentNumber number) {
        return number == null ? null : number.display();
    }

    // ---------- devoluciones: request -> command (H8d) ----------

    /**
     * La factura original debe ser un UUID valido y cada linea trae linea de origen y cantidad
     * (se validan aqui para no desempaquetar nulos hacia el {@code int} del comando).
     */
    public ReturnDraftCommand toCommand(ReturnDraftRequest request) {
        List<ReturnLineCommand> lines = request.lines() == null
                ? List.of()
                : request.lines().stream().map(BillingRestMapper::toReturnLineCommand).toList();
        return new ReturnDraftCommand(
                parseRequiredUuid(request.invoiceId(), "invoiceId"),
                request.date(),
                request.reason(),
                lines,
                request.version());
    }

    private static ReturnLineCommand toReturnLineCommand(ReturnLineRequest line) {
        if (line == null || line.sourceLineNo() == null) {
            throw invalid("sourceLineNo es obligatorio");
        }
        if (line.cantidad() == null) {
            throw invalid("cantidad es obligatoria");
        }
        return new ReturnLineCommand(line.sourceLineNo(), line.cantidad());
    }

    private static UUID parseRequiredUuid(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw invalid(field + " es obligatorio");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            throw invalid(field + " invalido: " + raw);
        }
    }

    // ---------- devoluciones: documento -> response (H8d) ----------

    public BillingReturnResponse toReturnResponse(BillingDocumentView view) {
        BillingDocument doc = view.doc();
        CounterpartySnapshot client = doc.counterparty();
        OriginalDocumentRef original = doc.original();
        return new BillingReturnResponse(
                doc.id().toString(),
                display(doc.number()),
                doc.status().name(),
                doc.type().kind().name(),
                doc.type().name(),
                original == null ? null : original.id().toString(),
                original == null ? null : display(original.number()),
                original == null ? null : original.issueDate(),
                client == null ? null : String.valueOf(client.thirdId()),
                client == null ? null : client.name(),
                client == null ? null : client.idNumber(),
                doc.issueDate(),
                doc.returnReason(),
                view.returnOrdinal(),
                doc.lines().stream().map(this::toReturnLineResponse).toList(),
                toTotals(doc.totals()),
                doc.createdAt(),
                doc.updatedAt(),
                doc.emittedAt(),
                doc.annulledAt(),
                doc.annulReason(),
                doc.version());
    }

    private BillingReturnLineResponse toReturnLineResponse(DocumentLine line) {
        return new BillingReturnLineResponse(
                line.lineNo(),
                line.sourceLineNo(),
                String.valueOf(line.productId()),
                line.productCode(),
                line.productName(),
                line.unitOfMeasureName(),
                line.quantity().value(),
                BillingCents.cents(line.unitPrice()),
                line.taxRate().value(),
                BillingCents.cents(line.subtotal()),
                BillingCents.cents(line.taxAmount()),
                BillingCents.cents(line.total()));
    }

    public ReturnSummaryResponse toReturnSummaryResponse(ReturnSummary returnSummary) {
        return toReturnSummaryResponse(returnSummary.summary(), returnSummary.ordinal());
    }

    public ReturnSummaryResponse toReturnSummaryResponse(BillingDocumentSummary summary, Integer ordinal) {
        return new ReturnSummaryResponse(
                summary.id().toString(),
                display(summary.number()),
                summary.status().name(),
                summary.type().kind().name(),
                summary.type().name(),
                summary.originalDocumentId() == null ? null : summary.originalDocumentId().toString(),
                display(summary.originalNumber()),
                ordinal,
                summary.counterpartyId() == null ? null : String.valueOf(summary.counterpartyId()),
                summary.counterpartyName(),
                summary.issueDate(),
                toTotals(summary.totals()),
                summary.createdAt(),
                summary.updatedAt(),
                summary.emittedAt(),
                summary.annulledAt(),
                summary.version());
    }

    public ReturnAvailabilityResponse toAvailabilityResponse(ReturnAvailability availability) {
        return new ReturnAvailabilityResponse(
                availability.invoiceId().toString(),
                availability.status().name(),
                availability.returnable(),
                availability.lines().stream().map(BillingRestMapper::toAvailabilityLineResponse).toList());
    }

    private static ReturnAvailabilityLineResponse toAvailabilityLineResponse(ReturnLineAvailability line) {
        return new ReturnAvailabilityLineResponse(
                line.lineNo(),
                String.valueOf(line.productId()),
                line.productName(),
                line.sold(),
                line.returned(),
                line.available(),
                BillingCents.cents(line.unitPrice()),
                line.taxRate().value());
    }

    public InvoiceWithReturnsResponse toWithReturnsResponse(InvoiceWithReturnsSummary row) {
        BillingDocumentSummary invoice = row.summary();
        return new InvoiceWithReturnsResponse(
                invoice.id().toString(),
                display(invoice.number()),
                invoice.type().kind().name(),
                invoice.supplierInvoiceNumber(),
                invoice.counterpartyName(),
                invoice.issueDate(),
                BillingCents.cents(invoice.totals().total()),
                row.returnsCount(),
                BillingCents.cents(row.returnedTotal()),
                row.state().name());
    }
}
