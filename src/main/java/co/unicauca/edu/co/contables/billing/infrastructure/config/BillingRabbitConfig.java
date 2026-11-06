package co.unicauca.edu.co.contables.billing.infrastructure.config;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.BillingMessagingTopology;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Topologia RabbitMQ de facturacion (EVT-5, D14). Un unico exchange topic durable
 * {@code billing.documents.exchange} con exchange alterno (fanout) hacia una cola de no enrutados, de
 * modo que un evento sin consumidor nunca se pierde ni se devuelve; y un DLX/DLQ que las colas de los
 * consumidores pueden usar como {@code x-dead-letter-exchange}. Las colas de consumo NO se declaran
 * aqui: son responsabilidad de cada consumidor.
 *
 * <p>Solo nombres {@code billing.*}: no toca {@code invoice.exchange} ni
 * {@code purchase.invoice.exchange} (redeclararlos sin los argumentos originales provoca
 * PRECONDITION_FAILED). Excluida del perfil {@code test}, igual que el resto de configuraciones
 * Rabbit del monolito. {@code RabbitAdmin} de Spring Boot declara estos beans al abrir la conexion.
 */
@Configuration("billingRabbitConfig")
@Profile("!test")
public class BillingRabbitConfig {

    private static final String ALTERNATE_EXCHANGE_ARGUMENT = "alternate-exchange";

    @Bean
    TopicExchange billingDocumentsExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(ALTERNATE_EXCHANGE_ARGUMENT, BillingMessagingTopology.UNROUTED_EXCHANGE);
        return new TopicExchange(BillingMessagingTopology.DOCUMENTS_EXCHANGE, true, false, arguments);
    }

    @Bean
    FanoutExchange billingUnroutedExchange() {
        return new FanoutExchange(BillingMessagingTopology.UNROUTED_EXCHANGE, true, false);
    }

    @Bean
    Queue billingUnroutedQueue() {
        return QueueBuilder.durable(BillingMessagingTopology.UNROUTED_QUEUE).build();
    }

    @Bean
    Binding billingUnroutedBinding() {
        return BindingBuilder.bind(billingUnroutedQueue()).to(billingUnroutedExchange());
    }

    @Bean
    FanoutExchange billingDeadLetterExchange() {
        return new FanoutExchange(BillingMessagingTopology.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue billingDeadLetterQueue() {
        return QueueBuilder.durable(BillingMessagingTopology.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding billingDeadLetterBinding() {
        return BindingBuilder.bind(billingDeadLetterQueue()).to(billingDeadLetterExchange());
    }
}
