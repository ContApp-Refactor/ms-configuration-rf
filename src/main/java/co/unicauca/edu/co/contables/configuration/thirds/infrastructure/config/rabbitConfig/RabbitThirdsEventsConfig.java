package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.rabbitConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitThirdsEventsConfig {
    public static final String THIRD_UPDATED_EXCHANGE = "third.updated.exchange";
    public static final String THIRD_UPDATED_QUEUE = "third.updated.queue";

    @Bean
    FanoutExchange thirdUpdatedExchange() {
        return new FanoutExchange(THIRD_UPDATED_EXCHANGE, true, false);
    }

    @Bean
    Queue thirdUpdatedQueue() {
        return new Queue(THIRD_UPDATED_QUEUE, true);
    }

    @Bean
    Binding thirdUpdatedQueueBinding() {
        return org.springframework.amqp.core.BindingBuilder.bind(thirdUpdatedQueue()).to(thirdUpdatedExchange());
    }
}
