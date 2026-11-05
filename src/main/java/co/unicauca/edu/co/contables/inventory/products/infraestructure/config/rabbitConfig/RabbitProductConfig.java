package co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

/**
 * @brief Configuración RabbitMQ específica para productos
 *
 * Define infraestructura de mensajería para productos: colas para kardex y stock,
 * exchange de fanout y bindings correspondientes para comunicación asíncrona.
 */
@Configuration
@Slf4j
@Profile("!test")
public class RabbitProductConfig {
    // Configuración para eventos de productos (Products como productor)
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_KARDEX_QUEUE = "product.kardex.queue";
    public static final String PRODUCT_STOCK_QUEUE = "product.stock.queue";

    @Bean
    Queue productKardexQueue() {
        return QueueBuilder.durable(PRODUCT_KARDEX_QUEUE).build();
    }

    /**
     * @brief Crea cola durable para mensajes de stock de productos     *
     * @return Cola configurada para mensajes de stock
     */
    @Bean
    Queue productStockQueue() {
        return QueueBuilder.durable(PRODUCT_STOCK_QUEUE).build();
    }

    /**
     * @brief Crea exchange de fanout para broadcast de productos
     * @return Exchange de fanout durable configurado
     */
    @Bean
    FanoutExchange productFanoutExchange() {
        return new FanoutExchange(PRODUCT_EXCHANGE, true, false);
    }

    /**
     * @brief Vincula cola de kardex al exchange de productos
     * @return Binding configurado entre cola y exchange
     */
    @Bean
    Binding productKardexQueueBinding() {
        return BindingBuilder.bind(productKardexQueue()).to(productFanoutExchange());
    }

    /**
     * @brief Vincula cola de stock al exchange de productos
     * @return Binding configurado entre cola y exchange
     */
    @Bean
    Binding productStockQueueBinding() {
        return BindingBuilder.bind(productStockQueue()).to(productFanoutExchange());
    }

}