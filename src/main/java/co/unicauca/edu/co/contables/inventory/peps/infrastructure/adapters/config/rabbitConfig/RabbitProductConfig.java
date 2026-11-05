package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.rabbitConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Configuration for Product-related RabbitMQ messaging
 *
 * Defines the exchanges, queues, and bindings required for
 * integrating product events into the Kardex system.
 *
 * El exchange {@code product.exchange} lo declara una unica vez
 * {@code inventory.products.RabbitProductConfig} (paso 6 del contrato de
 * migracion): aca solo se declara la cola propia de peps y su binding, que
 * se resuelve contra el bean {@code productFanoutExchange} de products.
 * Los nombres de bean llevan prefijo {@code peps} porque products declara
 * {@code productKardexQueue}/{@code productKardexQueueBinding} y
 * facture declara {@code productExchange}.
 */
@Configuration("pepsRabbitProductConfig")
@Slf4j
@Profile("!test")
public class RabbitProductConfig {
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_KARDEX_QUEUE = "product.peps.queue";

    /**
     * @brief Creates the durable queue for product-kardex integration
     * @return Configured durable Queue instance
     */
    @Bean
    Queue pepsProductKardexQueue() {
        return QueueBuilder.durable(PRODUCT_KARDEX_QUEUE).build();
    }

    /**
     * @brief Binds the product-kardex queue to the product exchange
     * @return Binding configuration between queue and exchange
     */
    @Bean
    Binding pepsProductKardexQueueBinding(
            @Qualifier("productFanoutExchange") FanoutExchange productExchange) {
        return BindingBuilder.bind(pepsProductKardexQueue()).to(productExchange);
    }

}
