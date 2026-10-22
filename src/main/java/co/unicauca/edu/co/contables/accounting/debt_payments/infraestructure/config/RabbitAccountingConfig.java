package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @brief Config RabbitMQ for communication with Accounting Service, this is used to send receipts and write-offs to accounting
 * Declares exchanges, queues and bindings
 */

@Configuration
@Slf4j
@Profile("!test")
public class RabbitAccountingConfig {
    public static final String RECEIPT_EXCHANGE = "receipt.exchange";
    public static final String RECEIPT_ACCOUNTING_QUEUE = "receipt.accounting.queue";
    public static final String RECEIPT_ACCOUNTING_DLX = "receipt.accounting.dlx";
    public static final String RECEIPT_ACCOUNTING_DLQ = "receipt.accounting.dlq";
    public static final String RECEIPT_ACCOUNTING_RETRY_QUEUE = "receipt.accounting.retry.queue";
    
    public static final String WRITEOFF_EXCHANGE = "writeoff.exchange";
    public static final String WRITEOFF_ACCOUNTING_QUEUE = "writeoff.accounting.queue";
    public static final String WRITEOFF_ACCOUNTING_DLX = "writeoff.accounting.dlx";
    public static final String WRITEOFF_ACCOUNTING_DLQ = "writeoff.accounting.dlq";
    public static final String WRITEOFF_ACCOUNTING_RETRY_QUEUE = "writeoff.accounting.retry.queue";
    
    // STATEMENT EXCHANGES
    @Bean
    FanoutExchange receiptExchange() {
        return new FanoutExchange(RECEIPT_EXCHANGE, true, false);
    }

    @Bean
    FanoutExchange receiptAccountingDlx() {
        return new FanoutExchange(RECEIPT_ACCOUNTING_DLX, true, false);
    }
    
    @Bean
    FanoutExchange writeOffExchange() {
        return new FanoutExchange(WRITEOFF_EXCHANGE, true, false);
    }

    @Bean
    FanoutExchange writeOffAccountingDlx() {
        return new FanoutExchange(WRITEOFF_ACCOUNTING_DLX, true, false);
    }

    // STATEMENT OF QUEUES AND BINDINGS
    @Bean
    Queue receiptAccountingQueue() {
        return QueueBuilder.durable(RECEIPT_ACCOUNTING_QUEUE)
                .withArgument("x-dead-letter-exchange", RECEIPT_ACCOUNTING_DLX).build();
    }

    @Bean
    Queue receiptAccountingDlq() {
        return QueueBuilder.durable(RECEIPT_ACCOUNTING_DLQ).build();
    }

    @Bean
    Queue receiptAccountingRetryQueue() {
        return QueueBuilder.durable(RECEIPT_ACCOUNTING_RETRY_QUEUE)
                .withArgument("x-message-ttl", 10000)
                .withArgument("x-dead-letter-exchange", RECEIPT_ACCOUNTING_DLX).build();
    }

    @Bean
    Queue writeOffAccountingQueue() {
        return QueueBuilder.durable(WRITEOFF_ACCOUNTING_QUEUE)
                .withArgument("x-dead-letter-exchange", WRITEOFF_ACCOUNTING_DLX).build();
    }

    @Bean
    Queue writeOffAccountingDlq() {
        return QueueBuilder.durable(WRITEOFF_ACCOUNTING_DLQ).build();
    }

    @Bean
    Queue writeOffAccountingRetryQueue() {
        return QueueBuilder.durable(WRITEOFF_ACCOUNTING_RETRY_QUEUE)
                .withArgument("x-message-ttl", 10000) // 10 segundos de espera para reintento
                .withArgument("x-dead-letter-exchange", WRITEOFF_ACCOUNTING_DLX).build();
    }

    @Bean
    Binding receiptAccountingBinding() {
        return BindingBuilder.bind(receiptAccountingQueue()).to(receiptExchange());
    }

    @Bean
    Binding receiptAccountingDlqBinding() {
        return BindingBuilder.bind(receiptAccountingDlq()).to(receiptAccountingDlx());
    }

    @Bean
    Binding receiptAccountingRetryBinding() {
        return BindingBuilder.bind(receiptAccountingRetryQueue()).to(receiptAccountingDlx());
    }

    @Bean
    Binding writeOffAccountingBinding() {
        return BindingBuilder.bind(writeOffAccountingQueue()).to(writeOffExchange());
    }

    @Bean
    Binding writeOffAccountingDlqBinding() {
        return BindingBuilder.bind(writeOffAccountingDlq()).to(writeOffAccountingDlx());
    }

    @Bean
    Binding writeOffAccountingRetryBinding() {
        return BindingBuilder.bind(writeOffAccountingRetryQueue()).to(writeOffAccountingDlx());
    }
}
