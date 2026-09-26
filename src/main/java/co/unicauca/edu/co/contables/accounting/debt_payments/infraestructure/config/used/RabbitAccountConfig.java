package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.used;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @brief Config RabbitMQ for communication with Account Service, this is used to receive used accounts
 * Declares exchanges, queues and bindings
 */

@Configuration
@Slf4j
@Profile("!test")
public class RabbitAccountConfig {
    public static final String ACCOUNT_USED_EXCHANGE = "account.used.exchange";
    public static final String ACCOUNT_USED_QUEUE = "account.used.queue";

    //Statement EXCHANGES
    @Bean
    FanoutExchange accountUsedExchange() {
        return new FanoutExchange(ACCOUNT_USED_EXCHANGE, true, false);
    }

    // STATEMENT OF QUEUES AND BINDINGS
    @Bean
    Queue accountUsedQueue() {
        return new Queue(ACCOUNT_USED_QUEUE, true);
    }

    @Bean
    Binding accountUsedBinding(){
        return BindingBuilder.bind(accountUsedQueue()).to(accountUsedExchange());
    }
}
