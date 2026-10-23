package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia de RabbitMQ del consumidor contable de tesoreria.
 *
 * <p>Tesoreria publica sus eventos de contabilizacion en
 * {@link TreasuryRabbitConfig#ACCOUNTING_EXCHANGE}. Ese exchange es un fanout que
 * hasta ahora no tenia ninguna cola ligada, por lo que todos esos eventos se
 * descartaban en silencio. Esta configuracion declara la cola que los consume.</p>
 *
 * <p>La respuesta del consumidor se publica en
 * {@link TreasuryRabbitConfig#RESULT_EXCHANGE}, cuya cola
 * {@link TreasuryRabbitConfig#RESULT_QUEUE} ya existia y estaba ligada.</p>
 */
@Configuration
public class TreasuryAccountingConsumerConfig {

    /** Cola que consume los eventos de contabilizacion publicados por tesoreria. */
    public static final String ACCOUNTING_QUEUE = "treasury.accounting.queue";

    @Bean
    public Queue treasuryAccountingQueue() {
        return QueueBuilder.durable(ACCOUNTING_QUEUE)
                .deadLetterExchange(TreasuryRabbitConfig.DLX)
                .build();
    }

    @Bean
    public Binding treasuryAccountingBinding(
            @Qualifier("accountingExchange") FanoutExchange accountingExchange) {
        return BindingBuilder.bind(treasuryAccountingQueue()).to(accountingExchange);
    }
}
