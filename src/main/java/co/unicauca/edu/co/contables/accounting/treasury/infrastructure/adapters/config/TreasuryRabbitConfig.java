package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
public class TreasuryRabbitConfig {
    public static final String TREASURY_EXCHANGE = "treasury.exchange";
    public static final String TREASURY_TRANSACTION_QUEUE = "treasury.transaction.queue";

    @Bean
    Queue treasuryTransactionQueue() {
        return new Queue(TREASURY_TRANSACTION_QUEUE, true);
    }

    @Bean
    FanoutExchange treasuryExchange() {
        return new FanoutExchange(TREASURY_EXCHANGE, true, false);
    }

    @Bean
    Binding treasuryTransactionQueueBinding() {
        return BindingBuilder.bind(treasuryTransactionQueue()).to(treasuryExchange());
    }

}
