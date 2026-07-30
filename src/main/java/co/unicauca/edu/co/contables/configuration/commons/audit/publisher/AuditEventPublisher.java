package co.unicauca.edu.co.contables.configuration.commons.audit.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.configuration.commons.config.RabbitAuditPublisherConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//Publisher
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @Async
    public void publish(OperationEventDto dto) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                    RabbitAuditPublisherConfig.OPERATION_EVENT_ROUTING_KEY,
                    dto);
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria [operacion={}, tabla={}]: {}",
                    dto.getOperationType(), dto.getAffectedTable(), e.getMessage(), e);
        }
    }
}
