package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @brief Config RabbitMQ for communication with Notifications Service, this is used to send notifications
 * Declares exchanges, queues and bindings
 */

@Configuration
@Slf4j
@Profile("!test")
public class RabbitNotificationsConfig {
    public static final String NOTIFICATIONS_EXCHANGE = "notifications.exchange";
    public static final String NOTIFICATIONS_QUEUE = "notifications.queue";

    // STATEMENT EXCHANGES
    @Bean
    FanoutExchange notificationsExchange() {
        return new FanoutExchange(NOTIFICATIONS_EXCHANGE, true, false);
    }

    // STATEMENT OF QUEUES AND BINDINGS
    @Bean
    Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE).build();
    }

    @Bean
    Binding notificationsBinding(FanoutExchange notificationsExchange, Queue notificationsQueue) {
        return BindingBuilder.bind(notificationsQueue).to(notificationsExchange);
    }
}
