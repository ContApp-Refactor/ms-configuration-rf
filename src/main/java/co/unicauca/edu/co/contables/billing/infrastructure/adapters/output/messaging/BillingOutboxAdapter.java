package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingOutboxPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingOutboxRelayPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.OutboxClaim;
import co.unicauca.edu.co.contables.billing.application.ports.output.model.PendingOutboxMessage;
import co.unicauca.edu.co.contables.billing.domain.event.BillingDomainEvent;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingOutboxEventEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingOutboxEventJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Outbox de facturacion.
 *
 * <p><b>Alta (EVT-1)</b>: {@link #append} agrega la fila PENDING dentro de la transaccion del
 * llamador (no es {@code @Transactional}; los casos de uso deben invocarlo desde su transaccion). El
 * {@code saveAndFlush} hace visible de inmediato un choque de unicidad (tenant, event_id).
 *
 * <p><b>Relay (EVT-4)</b>: {@link #findDueClaims} es SQL nativo entre tenants y debe llamarse SIN
 * tenant en el contexto; {@link #lockPending}, {@link #markPublished} y {@link #markAttemptFailed}
 * deben llamarse dentro de una transaccion nueva abierta DESPUES de fijar el tenant de la fila (el
 * filtro {@code @TenantId} hace que la fila de otro tenant "no exista"). No son
 * {@code @Transactional}: la transaccion la abre el relay por fila.
 */
@Component("billingOutboxAdapter")
public class BillingOutboxAdapter implements BillingOutboxPort, BillingOutboxRelayPort {

    private static final int EVENT_VERSION = 1;
    /** Longitud de la columna {@code last_error}. */
    static final int LAST_ERROR_MAX_LENGTH = 1000;

    private final BillingOutboxEventJpaRepository repository;
    private final BillingEventSerializer serializer;
    private final BillingClockPort clock;
    private final BillingExecutionContextPort context;

    public BillingOutboxAdapter(BillingOutboxEventJpaRepository repository, BillingEventSerializer serializer,
            BillingClockPort clock, BillingExecutionContextPort context) {
        this.repository = repository;
        this.serializer = serializer;
        this.clock = clock;
        this.context = context;
    }

    @Override
    public void append(BillingDomainEvent event) {
        Objects.requireNonNull(event, "event");
        BillingDocument doc = event.document();
        BillingEventCatalog.Entry entry = BillingEventCatalog.resolve(event);
        String payload = serializer.serialize(event, context.tenantId());
        Instant now = clock.now();

        BillingOutboxEventEntity row = new BillingOutboxEventEntity();
        row.setEventId(event.eventId().toString());
        row.setEventType(entry.eventType());
        row.setEventVersion(EVENT_VERSION);
        row.setAggregateType(doc.type().name());
        row.setAggregateId(doc.id());
        row.setEnterpriseId(doc.enterpriseId());
        row.setExchangeName(BillingMessagingTopology.DOCUMENTS_EXCHANGE);
        row.setRoutingKey(entry.routingKey());
        row.setPayload(payload);
        row.setStatus(BillingOutboxEventEntity.STATUS_PENDING);
        row.setAttempts(0);
        row.setNextAttemptAt(now);
        row.setCreatedAt(now);
        repository.saveAndFlush(row);
    }

    // ---------- relay ----------

    @Override
    public List<OutboxClaim> findDueClaims(Instant now, int limit) {
        return repository.findDueClaims(now, limit).stream()
                .map(columns -> new OutboxClaim(((Number) columns[0]).longValue(), (String) columns[1]))
                .toList();
    }

    @Override
    public Optional<PendingOutboxMessage> lockPending(long id) {
        Instant now = clock.now();
        return repository.findByIdForUpdate(id)
                .filter(row -> BillingOutboxEventEntity.STATUS_PENDING.equals(row.getStatus()))
                .filter(row -> !row.getNextAttemptAt().isAfter(now))
                .map(row -> new PendingOutboxMessage(row.getId(), row.getEventId(), row.getEventType(),
                        row.getTenantId(), row.getExchangeName(), row.getRoutingKey(), row.getPayload(),
                        row.getAttempts(), row.getNextAttemptAt()));
    }

    @Override
    public void markPublished(long id, Instant at) {
        BillingOutboxEventEntity row = requireRow(id);
        row.setStatus(BillingOutboxEventEntity.STATUS_PUBLISHED);
        row.setPublishedAt(at);
        row.setLastError(null);
        repository.saveAndFlush(row);
    }

    @Override
    public void markAttemptFailed(long id, int attempts, Instant nextAttemptAt, boolean exhausted, String error) {
        BillingOutboxEventEntity row = requireRow(id);
        row.setAttempts(attempts);
        row.setNextAttemptAt(nextAttemptAt);
        row.setStatus(exhausted ? BillingOutboxEventEntity.STATUS_FAILED : BillingOutboxEventEntity.STATUS_PENDING);
        row.setLastError(truncate(error));
        repository.saveAndFlush(row);
    }

    private BillingOutboxEventEntity requireRow(long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalStateException(
                "Fila del outbox " + id + " no encontrada para el tenant actual"));
    }

    private static String truncate(String error) {
        if (error == null || error.length() <= LAST_ERROR_MAX_LENGTH) {
            return error;
        }
        return error.substring(0, LAST_ERROR_MAX_LENGTH);
    }
}
