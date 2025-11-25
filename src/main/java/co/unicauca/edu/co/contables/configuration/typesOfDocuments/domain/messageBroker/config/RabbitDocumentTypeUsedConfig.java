package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
@Slf4j
@Profile("!test")
public class RabbitDocumentTypeUsedConfig {

    public static final String DOCUMENT_TYPE_USED_EXCHANGE = "documenttype.used.exchange";
    public static final String DOCUMENT_TYPE_USED_QUEUE = "documenttype.used.queue";

    @Bean
    Queue documentTypeUsedQueue() {
        return QueueBuilder.durable(DOCUMENT_TYPE_USED_QUEUE).build();
    }

    @Bean
    FanoutExchange documentTypeUsedExchange() {
        return new FanoutExchange(DOCUMENT_TYPE_USED_EXCHANGE, true, false);
    }

    @Bean
    Binding documentTypeUsedQueueBinding() {
        return BindingBuilder.bind(documentTypeUsedQueue()).to(documentTypeUsedExchange());
    }
}
