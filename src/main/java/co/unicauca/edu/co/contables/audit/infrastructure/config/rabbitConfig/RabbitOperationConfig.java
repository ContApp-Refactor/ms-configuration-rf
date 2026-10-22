package co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig;

import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;

@Configuration
@Profile("!test")
public class RabbitOperationConfig {
    public static final String OPERATION_AUDIT_QUEUE = "audit.operation.queue";
    public static final String OPERATION_EVENT_ROUTING_KEY = "operation.event";

    @Bean
    public Queue operationAuditQueue() {
        return QueueBuilder.durable(OPERATION_AUDIT_QUEUE).build();
    }

    @Bean
    public Binding operationEventBinding(Queue operationAuditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(operationAuditQueue)
                .to(auditExchange)
                .with(OPERATION_EVENT_ROUTING_KEY);
    }

}
