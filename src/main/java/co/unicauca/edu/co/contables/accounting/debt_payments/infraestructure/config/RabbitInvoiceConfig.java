package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @brief Config RabbitMQ for communication with Debt Payments Service, this is used to send and receive debt payment messages
 * Declares exchanges, queues and bindings
 */

@Configuration
@Slf4j
@Profile("!test")
public class RabbitInvoiceConfig {
    //Constants for invoice
    public static final String INVOICE_EXCHANGE = "invoice.exchange";
    public static final String INVOICE_PAYMENTS_QUEUE = "invoice.payments.queue";
    public static final String INVOICE_PAYMENTS_DLX = "invoice.payments.dlx";
    public static final String INVOICE_PAYMENTS_DLQ = "invoice.payments.dlq";
    public static final String INVOICE_PAYMENTS_RETRY_QUEUE = "invoice.payments.retry.queue";

    //Jackson2JsonMessageConverter provided by RabbitCommonConfig.

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    FanoutExchange invoiceExchange() {
        return new FanoutExchange(INVOICE_EXCHANGE, true, false);
    }

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    FanoutExchange invoicePaymentsDlx() {
        return new FanoutExchange(INVOICE_PAYMENTS_DLX, true, false);
    }

    // STATEMENT OF QUEUES AND BINDINGS
    // Invoice Queues and Bindings
    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue invoicePaymentsQueue() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", INVOICE_PAYMENTS_DLX)
                .build();
    }

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue invoicePaymentsDlq() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_DLQ).build();
    }

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Queue invoicePaymentsRetryQueue() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_RETRY_QUEUE)
                .withArgument("x-message-ttl", 60000) // 1 minuto de espera para reintento
                .withArgument("x-dead-letter-exchange", INVOICE_PAYMENTS_DLX) // Si falla después de reintento, va al DLX
                .build();
    }

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding invoicePaymentsBinding() {
        return BindingBuilder.bind(invoicePaymentsQueue()).to(invoiceExchange());
    }

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding invoicePaymentsDlqBinding() {
        return BindingBuilder.bind(invoicePaymentsDlq()).to(invoicePaymentsDlx());
    }

    @Bean
    @Deprecated
    @ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
    Binding invoicePaymentsRetryBinding() {
        return BindingBuilder.bind(invoicePaymentsRetryQueue()).to(invoicePaymentsDlx());
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setMandatory(true);
        
        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}", returned.getMessage());
        });

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message not delivered to exchange. Cause: {}", cause);
            }
        });
        return template;
    }

    //rabbitListenerContainerFactory provided by RabbitCommonConfig.
}
