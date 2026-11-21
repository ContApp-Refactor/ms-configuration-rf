package co.unicauca.edu.co.contables.commons.audit.publisher;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class SpringEventsAuditEventPublisher extends AuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsAuditEventPublisher(ApplicationEventPublisher eventPublisher) {

        super(null);
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Async
    public void publish(OperationEventDto dto) {
        try {
            eventPublisher.publishEvent(dto);
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria [operacion={}, tabla={}]: {}",
                    dto.getOperationType(), dto.getAffectedTable(), e.getMessage(), e);
        }
    }
}
