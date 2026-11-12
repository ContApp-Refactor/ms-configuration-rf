package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import co.unicauca.edu.co.contables.billing.domain.event.BillingDocumentAnnulled;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDomainEvent;
import co.unicauca.edu.co.contables.billing.domain.event.ReturnEventContext;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.OriginalDocumentRef;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.dto.BillingDocumentEventPayload;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.dto.BillingEventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Convierte un evento de dominio en el JSON del sobre (EVT-3). Usa su propio {@link ObjectMapper}
 * (fechas ISO, sin dependencia del ObjectMapper global del monolito) para que el contrato no
 * cambie por configuracion ajena. El dominio no conoce Jackson: el mapeo vive aqui.
 */
@Component("billingEventSerializer")
public class BillingEventSerializer {

    private static final int EVENT_VERSION = 1;

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public String serialize(BillingDomainEvent event, String tenantId) {
        Objects.requireNonNull(event, "event");
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("El tenant del evento es obligatorio");
        }
        BillingDocument doc = event.document();
        BillingEventCatalog.Entry entry = BillingEventCatalog.resolve(event);
        BillingEventEnvelope envelope = new BillingEventEnvelope(
                event.eventId().toString(),
                entry.eventType(),
                EVENT_VERSION,
                event.occurredAt(),
                tenantId,
                doc.enterpriseId(),
                doc.id().toString(),
                new BillingEventEnvelope.SourceDocument(doc.type().name(), doc.id().toString(),
                        doc.number() == null ? null : doc.number().display()),
                payload(event));
        try {
            return mapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new BillingException(BillingErrorCode.BILLING_INTERNAL_ERROR,
                    "No se pudo serializar el evento " + entry.eventType());
        }
    }

    private BillingDocumentEventPayload payload(BillingDomainEvent event) {
        BillingDocument doc = event.document();
        return new BillingDocumentEventPayload(
                doc.id().toString(),
                doc.version(),
                doc.type().name(),
                doc.type().kind().name(),
                doc.number() == null ? null : doc.number().display(),
                doc.issueDate(),
                doc.emittedAt(),
                doc.emittedBy(),
                doc.currency(),
                issuer(doc.issuer()),
                counterparty(doc.counterparty()),
                doc.supplierInvoiceNumber(),
                lines(doc.lines()),
                new BillingDocumentEventPayload.Totals(pesos(doc.totals().subtotal()), pesos(doc.totals().tax()),
                        pesos(doc.totals().total())),
                null,
                original(doc.original()),
                returnInfo(doc, event.returnContext()),
                event instanceof BillingDocumentAnnulled
                        ? new BillingDocumentEventPayload.Annulment(doc.annulledAt(), doc.annulledBy(),
                                doc.annulReason())
                        : null);
    }

    private static BillingDocumentEventPayload.Issuer issuer(IssuerSnapshot issuer) {
        return issuer == null ? null
                : new BillingDocumentEventPayload.Issuer(issuer.enterpriseId(), issuer.name(), issuer.nit(),
                        issuer.dv());
    }

    private static BillingDocumentEventPayload.Counterparty counterparty(CounterpartySnapshot c) {
        return c == null ? null
                : new BillingDocumentEventPayload.Counterparty(c.role().name(), c.thirdId(), c.name(), c.idType(),
                        c.idNumber(), c.verificationNumber(), c.address(), c.email(), c.phone());
    }

    private static List<BillingDocumentEventPayload.Line> lines(List<DocumentLine> lines) {
        return lines.stream()
                .map(l -> new BillingDocumentEventPayload.Line(l.lineNo(), l.sourceLineNo(), l.productId(),
                        l.productCode(), l.productName(), l.unitOfMeasureId(), l.unitOfMeasureName(),
                        l.quantity().value().setScale(3, RoundingMode.HALF_UP), pesos(l.catalogUnitPrice()),
                        pesos(l.unitPrice()), l.taxRate().value().setScale(2, RoundingMode.HALF_UP),
                        pesos(l.subtotal()), pesos(l.taxAmount()), pesos(l.total())))
                .toList();
    }

    private static BillingDocumentEventPayload.Original original(OriginalDocumentRef ref) {
        return ref == null ? null
                : new BillingDocumentEventPayload.Original(ref.id().toString(), ref.type().name(),
                        ref.number() == null ? null : ref.number().display(), ref.issueDate());
    }

    private static BillingDocumentEventPayload.ReturnInfo returnInfo(BillingDocument doc, ReturnEventContext ctx) {
        if (!doc.type().isReturn()) {
            return null;
        }
        Integer ordinal = ctx == null ? null : ctx.ordinal();
        String status = ctx == null || ctx.originalStatusAfter() == null ? null : ctx.originalStatusAfter().name();
        return new BillingDocumentEventPayload.ReturnInfo(doc.returnReason(), ordinal, status);
    }

    private static BigDecimal pesos(Money money) {
        return money.value().setScale(2, RoundingMode.HALF_UP);
    }
}
