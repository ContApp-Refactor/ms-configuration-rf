package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentEventDto;
import co.unicauca.edu.co.contables.commons.config.RabbitAuditPublisherConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactureAuditEventPublisher {

    /**
     * Routing key propio del módulo de facturas. El de `commons` es
     * `operation.event` (eventos de operación); los documentos de facturas se
     * publican aparte bajo `document.event`.
     */
    static final String DOCUMENT_EVENT_ROUTING_KEY = "document.event";

    private final AmqpTemplate amqpTemplate;

    @Async
    public void publish(DocumentEventDto dto) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                    DOCUMENT_EVENT_ROUTING_KEY,
                    dto);
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria del documento", e);
        }
    }
}
