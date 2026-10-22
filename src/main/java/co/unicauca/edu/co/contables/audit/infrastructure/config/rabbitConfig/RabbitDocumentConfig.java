package co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig;

import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;

@Configuration
@Profile("!test")
public class RabbitDocumentConfig {

    public static final String DOCUMENT_AUDIT_QUEUE = "audit.document.queue";
    public static final String DOCUMENT_EVENT_ROUTING_KEY = "document.event";

    @Bean
    public Queue documentAuditQueue() {
        return QueueBuilder.durable(DOCUMENT_AUDIT_QUEUE).build();
    }

    @Bean
    public Binding documentEventBinding(Queue documentAuditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(documentAuditQueue)
                .to(auditExchange)
                .with(DOCUMENT_EVENT_ROUTING_KEY);
    }
}
