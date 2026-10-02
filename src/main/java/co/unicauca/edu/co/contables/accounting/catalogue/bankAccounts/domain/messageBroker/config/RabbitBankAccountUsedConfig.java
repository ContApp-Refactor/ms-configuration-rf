package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.config;

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
public class RabbitBankAccountUsedConfig {

    public static final String BANK_ACCOUNT_USED_EXCHANGE = "bankaccount.used.exchange";
    public static final String BANK_ACCOUNT_USED_QUEUE = "bankaccount.used.queue";

    @Bean
    Queue bankAccountUsedQueue() {
        return QueueBuilder.durable(BANK_ACCOUNT_USED_QUEUE).build();
    }

    @Bean
    FanoutExchange bankAccountUsedExchange() {
        return new FanoutExchange(BANK_ACCOUNT_USED_EXCHANGE, true, false);
    }

    @Bean
    Binding bankAccountUsedQueueBinding() {
        return BindingBuilder.bind(bankAccountUsedQueue()).to(bankAccountUsedExchange());
    }
}
