package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentEventDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.publisher.FactureAuditEventPublisher;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class SpringEventsFactureAuditEventPublisher extends FactureAuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsFactureAuditEventPublisher(ApplicationEventPublisher eventPublisher) {

        super(null);
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Async
    public void publish(DocumentEventDto dto) {
        try {
            eventPublisher.publishEvent(dto);
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria del documento", e);
        }
    }
}
