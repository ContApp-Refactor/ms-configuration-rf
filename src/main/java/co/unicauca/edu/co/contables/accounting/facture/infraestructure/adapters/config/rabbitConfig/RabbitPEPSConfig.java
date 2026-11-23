package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;


@Configuration
@Slf4j
@Profile("!test")
public class RabbitPEPSConfig {
    public static final String PEPS_EXCHANGE = "peps.exchange";
    public static final String PEPS_QUEUE = "peps.queue";

   @Bean
    Queue pepsQueue() {
        return QueueBuilder.durable(PEPS_QUEUE).build();
    }

    @Bean
    FanoutExchange pepsExchange() {
        return new FanoutExchange(PEPS_EXCHANGE, true, false);
    }

    @Bean
    Binding pepsBinding() {
        return BindingBuilder.bind(pepsQueue()).to(pepsExchange());
    }

}
