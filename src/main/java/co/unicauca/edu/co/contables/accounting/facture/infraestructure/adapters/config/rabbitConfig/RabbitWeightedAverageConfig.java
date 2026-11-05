package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig;

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
public class RabbitWeightedAverageConfig {
    public static final String WEIGHTED_AVERAGE_EXCHANGE = "weighted.average.exchange";
    public static final String WEIGHTED_AVERAGE_QUEUE = "weighted.average.queue";



    @Bean
    FanoutExchange productExchange() {
        return new FanoutExchange(WEIGHTED_AVERAGE_EXCHANGE, true, false);
    }

    @Bean
    Queue productQueue() {
        return QueueBuilder.durable(WEIGHTED_AVERAGE_QUEUE).build();
    }

    @Bean
    Binding productBinding() {
        return BindingBuilder.bind(productQueue()).to(productExchange());
    }
}
