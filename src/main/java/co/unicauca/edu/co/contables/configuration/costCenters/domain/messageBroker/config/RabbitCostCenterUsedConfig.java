package co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
@Slf4j
public class RabbitCostCenterUsedConfig {
    public static final String COST_CENTER_USED_EXCHANGE = "costcenter.used.exchange";
    public static final String COST_CENTER_USED_QUEUE = "costcenter.used.queue";

    @Bean
    Queue costCenterUsedQueue() {
        return QueueBuilder.durable(COST_CENTER_USED_QUEUE).build();
    }

    @Bean
    FanoutExchange costCenterUsedExchange() {
        return new FanoutExchange(COST_CENTER_USED_EXCHANGE, true, false);
    }

    @Bean
    Binding costCenterUsedQueueBinding() {
        return BindingBuilder.bind(costCenterUsedQueue()).to(costCenterUsedExchange());
    }
}
