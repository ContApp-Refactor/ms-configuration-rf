package co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;


@Configuration
public class RabbitProductUsedConfig {
    public static final String PRODUCT_USED_EXCHANGE = "product.used.exchange";
    public static final String PRODUCT_USED_QUEUE = "product.used.queue";

    @Bean
    FanoutExchange productUsedExchange() {
        return new FanoutExchange(PRODUCT_USED_EXCHANGE, true, false);
    }

    @Bean
    Queue productUsedQueue() {
        return QueueBuilder.durable(PRODUCT_USED_QUEUE).build();
    }

    @Bean
    Binding productUsedQueueBinding() {
        return BindingBuilder.bind(productUsedQueue()).to(productUsedExchange());
    }
    
}
