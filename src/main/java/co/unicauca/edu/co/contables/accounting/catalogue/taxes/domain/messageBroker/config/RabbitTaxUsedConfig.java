package co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.messageBroker.config;

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
public class RabbitTaxUsedConfig {

    public static final String TAX_USED_EXCHANGE = "tax.used.exchange";
    public static final String TAX_USED_QUEUE = "tax.used.queue";

    @Bean
    Queue taxUsedQueue() {
        return QueueBuilder.durable(TAX_USED_QUEUE).build();
    }

    @Bean
    FanoutExchange taxUsedExchange() {
        return new FanoutExchange(TAX_USED_EXCHANGE, true, false);
    }

    @Bean
    Binding taxUsedQueueBinding() {
        return BindingBuilder.bind(taxUsedQueue()).to(taxUsedExchange());
    }
}
