package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsTreasuryAccountingResultPublisher
        implements ITreasuryAccountingResultPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(AccountingResultReply reply) {
        String eventId = UUID.randomUUID().toString();
        TreasuryRabbitDtos.AccountingResult payload = new TreasuryRabbitDtos.AccountingResult(
                eventId,
                reply.sourceEventId(),
                reply.operation(),
                reply.documentType(),
                reply.documentId(),
                reply.accepted(),
                reply.accountingEntryId(),
                reply.reason(),
                reply.tenantId(),
                reply.enterpriseId());
        eventPublisher.publishEvent(payload);
        log.info("Resultado contable {} {} {} publicado (aceptado={})",
                reply.operation(), reply.documentType(), reply.documentId(), reply.accepted());
    }
}
