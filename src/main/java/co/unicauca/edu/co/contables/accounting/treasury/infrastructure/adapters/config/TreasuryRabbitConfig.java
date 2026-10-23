package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.*;

@Configuration
public class TreasuryRabbitConfig {
    public static final String INVOICE_EXCHANGE="purchase.invoice.exchange";
    public static final String PURCHASE_QUEUE="invoice.treasury.queue";
    public static final String ACCOUNTING_EXCHANGE="treasury.accounting.exchange";
    public static final String RESULT_EXCHANGE="accounting.result.exchange";
    public static final String RESULT_QUEUE="accounting.treasury.result.queue";
    public static final String NOTIFICATION_EXCHANGE="payment.notification.exchange";
    public static final String DLX="treasury.dlx";
    public static final String DLQ="treasury.dlq";

    @Bean FanoutExchange treasuryInvoiceExchange(){return new FanoutExchange(INVOICE_EXCHANGE,true,false);}
    @Bean FanoutExchange accountingExchange(){return new FanoutExchange(ACCOUNTING_EXCHANGE,true,false);}
    @Bean FanoutExchange resultExchange(){return new FanoutExchange(RESULT_EXCHANGE,true,false);}
    @Bean FanoutExchange notificationExchange(){return new FanoutExchange(NOTIFICATION_EXCHANGE,true,false);}
    @Bean FanoutExchange treasuryDlx(){return new FanoutExchange(DLX,true,false);}
    @Bean Queue purchaseQueue(){return QueueBuilder.durable(PURCHASE_QUEUE).deadLetterExchange(DLX).build();}
    @Bean Queue resultQueue(){return QueueBuilder.durable(RESULT_QUEUE).deadLetterExchange(DLX).build();}
    @Bean Queue treasuryDlq(){return QueueBuilder.durable(DLQ).build();}
    @Bean Binding purchaseBinding(){return BindingBuilder.bind(purchaseQueue()).to(treasuryInvoiceExchange());}
    @Bean Binding resultBinding(){return BindingBuilder.bind(resultQueue()).to(resultExchange());}
    @Bean Binding dlqBinding(){return BindingBuilder.bind(treasuryDlq()).to(treasuryDlx());}
    /**
     * Contenedor de los listeners de tesoreria.
     *
     * <p>Sin {@code Jackson2JsonMessageConverter} el contenedor cae al
     * {@code SimpleMessageConverter} por defecto, que deja el payload como
     * {@code byte[]} y no puede convertirlo a los DTO tipados de
     * {@code TreasuryEventListener} ni al envelope del consumidor contable. El
     * converter se resuelve con {@code ObjectProvider} porque en el perfil
     * {@code test} ese bean no se declara.</p>
     */
    @Bean SimpleRabbitListenerContainerFactory treasuryRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ObjectProvider<Jackson2JsonMessageConverter> jsonMessageConverter) {
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        Jackson2JsonMessageConverter converter = jsonMessageConverter.getIfAvailable();
        if (converter != null) {
            factory.setMessageConverter(converter);
        }
        factory.setAdviceChain(RetryInterceptorBuilder.stateless().maxAttempts(3)
                .backOffOptions(500, 2.0, 5000)
                .recoverer(new RejectAndDontRequeueRecoverer()).build());
        return factory;
    }
}