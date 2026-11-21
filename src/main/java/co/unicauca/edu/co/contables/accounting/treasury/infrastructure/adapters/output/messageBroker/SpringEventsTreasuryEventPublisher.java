package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryEventPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.TreasuryEvent;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.SourceDocument;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.TreasuryAccountingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsTreasuryEventPublisher implements ITreasuryEventPublisher {

    private static final String EVENT_PAYMENT_VOUCHER_POSTED = "PAYMENT_VOUCHER_POSTED";

    private final ObjectMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void enqueue(TreasuryEvent event) {
        if (EVENT_PAYMENT_VOUCHER_POSTED.equals(event.eventType())) {
            log.info("treasury event [{}] — transport TBD", event.eventType());
            return;
        }

        TreasuryAccountingEvent accountingEvent = new TreasuryAccountingEvent(
                event.eventId(),
                event.eventType(),
                event.eventVersion(),
                event.occurredAt(),
                event.tenantId(),
                event.enterpriseId(),
                event.correlationId(),
                new SourceDocument(event.aggregateType(), event.aggregateId()),
                mapper.valueToTree(event.payload()));

        eventPublisher.publishEvent(accountingEvent);
    }
}
