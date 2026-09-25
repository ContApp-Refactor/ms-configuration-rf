package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.rabbitConfig;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("!test")
public class RabbitThirdUsedConfig {
    public static final String THIRD_USED_EXCHANGE = "third.used.exchange";
    public static final String THIRD_USED_QUEUE = "third.used.queue";

    @Bean
    FanoutExchange thirdUsedExchange() {
        return new FanoutExchange(THIRD_USED_EXCHANGE, true, false);
    }

    @Bean
    Queue thirdUsedQueue() {
        return QueueBuilder.durable(THIRD_USED_QUEUE).build();
    }

    @Bean
    Binding thirdUsedQueueBinding() {
        return BindingBuilder.bind(thirdUsedQueue()).to(thirdUsedExchange());
    }
    
}
