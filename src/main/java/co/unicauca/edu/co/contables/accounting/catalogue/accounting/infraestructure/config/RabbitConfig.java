package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitConfig {
    //Constants for invoice
    public static final String INVOICE_EXCHANGE = "invoice.exchange";
    public static final String INVOICE_ACCOUNTING_QUEUE = "invoice.accounting.queue";
    public static final String INVOICE_ACCOUNTING_DLX = "invoice.accounting.dlx";
    public static final String INVOICE_ACCOUNTING_DLQ = "invoice.accounting.dlq";
    public static final String INVOICE_ACCOUNTING_RETRY_QUEUE = "invoice.accounting.retry.queue";

    // STATEMENT EXCHANGES
    // invoiceExchange, jsonMessageConverter, rabbitTemplate and
    // rabbitListenerContainerFactory are provided by RabbitInvoiceConfig /
    // RabbitCommonConfig in the monolith and must not be redeclared here.
    @Bean
    FanoutExchange invoiceAccountingDlx() {
        return new FanoutExchange(INVOICE_ACCOUNTING_DLX, true, false);
    }

    // STATEMENT OF QUEUES AND BINDINGS
    // Invoice Queues and Bindings
    @Bean
    Queue invoiceAccountingQueue() {
        return QueueBuilder.durable(INVOICE_ACCOUNTING_QUEUE)
                .withArgument("x-dead-letter-exchange", INVOICE_ACCOUNTING_DLX)
                .build();
    }

    @Bean
    Queue invoiceAccountingDlq() {
        return QueueBuilder.durable(INVOICE_ACCOUNTING_DLQ).build();
    }

    @Bean
    Queue invoiceAccountingRetryQueue() {
        return QueueBuilder.durable(INVOICE_ACCOUNTING_RETRY_QUEUE)
                .withArgument("x-message-ttl", 60000) // 1 minuto de espera para reintento
                .withArgument("x-dead-letter-exchange", INVOICE_ACCOUNTING_DLX) // Si falla después de reintento, va al DLX
                .build();
    }

    @Bean
    Binding invoiceAccountingBinding(
            @Qualifier("invoiceExchange") FanoutExchange invoiceExchange) {
        return BindingBuilder.bind(invoiceAccountingQueue()).to(invoiceExchange);
    }

    @Bean
    Binding invoiceAccountingDlqBinding() {
        return BindingBuilder.bind(invoiceAccountingDlq()).to(invoiceAccountingDlx());
    }

    @Bean
    Binding invoiceAccountingRetryBinding() {
        return BindingBuilder.bind(invoiceAccountingRetryQueue()).to(invoiceAccountingDlx());
    }
}
