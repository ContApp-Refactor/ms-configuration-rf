package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryEventPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.TreasuryEvent;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.OutboxEventEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository.IOutboxEventRepository;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker.support.TreasuryOutboxMessenger;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publica los eventos de tesoreria en outbox.
 *
 * <p>El publicado es <em>fire-and-forget</em>: el evento se encola en la base y se envia a
 * RabbitMQ despues del commit, pero su fallo nunca rompe la request del cliente. El voucher ya
 * esta persistido y en estado {@code POSTING}; el resultado de contabilidad llega de forma
 * asincrona por {@code accounting.result.exchange} y lo aplica {@code TreasuryEventListener}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventAdapter implements ITreasuryEventPublisher {
    private final IOutboxEventRepository repository;
    private final ObjectMapper mapper;
    private final TreasuryOutboxMessenger messenger;

    @Override
    public void enqueue(TreasuryEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(event.eventId());
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setEventType(event.eventType());
        entity.setPayload(buildPayload(event));
        entity.setTenantId(event.tenantId());
        repository.save(entity);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishSaved(event.eventId());
                }
            });
        } else {
            publishSaved(event.eventId());
        }
    }

    /**
     * Intenta publicar el evento y <strong>nunca</strong> propaga la excepcion.
     *
     * <p>Corre en {@code afterCommit}, donde el {@code EntityManager} de la transaccion original
     * ya cerro, por lo que el estado se actualiza en una transaccion propia. Si RabbitMQ no tiene
     * ruta para el exchange (por ejemplo {@code treasury.accounting.exchange} sin consumidores)
     * el evento queda {@code FAILED} y se reintenta, pero la operacion del cliente ya exitosa
     * no se ve afectada.
     */
    private void publishSaved(String eventId) {
        OutboxEventEntity entity = repository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalStateException("Outbox no encontrado: " + eventId));
        try {
            messenger.publishNow(entity);
            entity.setPublishedAt(Instant.now());
            entity.setStatus(OutboxEventEntity.Status.PUBLISHED);
            entity.setLastError(null);
            repository.save(entity);
        } catch (Exception ex) {
            log.warn("No fue posible publicar el evento de tesoreria {} ({}); queda en outbox para reintento",
                    eventId, entity.getEventType(), ex);
            entity.setStatus(OutboxEventEntity.Status.FAILED);
            entity.setLastError(ex.getMessage());
            entity.setAttempts(entity.getAttempts() + 1);
            repository.save(entity);
        }
    }

    private String buildPayload(TreasuryEvent event) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", event.eventId());
            envelope.put("eventType", event.eventType());
            envelope.put("eventVersion", event.eventVersion());
            envelope.put("occurredAt", event.occurredAt());
            envelope.put("tenantId", event.tenantId());
            envelope.put("enterpriseId", event.enterpriseId());
            envelope.put("correlationId", event.correlationId());
            envelope.put("sourceDocument", Map.of("type", event.aggregateType(), "id", event.aggregateId()));
            envelope.put("payload", event.payload());
            return mapper.writeValueAsString(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible serializar el evento", ex);
        }
    }
}
