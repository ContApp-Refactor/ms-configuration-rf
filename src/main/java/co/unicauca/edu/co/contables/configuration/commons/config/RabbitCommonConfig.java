package co.unicauca.edu.co.contables.configuration.commons.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

/**
 * @brief Common RabbitMQ configuration for message handling
 * 
 * Provides shared RabbitMQ infrastructure including JSON message
 * conversion and listener container factory setup.
 */
@Configuration
@Slf4j
@Profile("!test")
public class RabbitCommonConfig {
    
    /**
     * @brief Configures JSON message converter for RabbitMQ
     * @return Jackson2JsonMessageConverter for automatic JSON serialization/deserialization
     */
    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * @brief Creates custom listener container factory with JSON conversion
     * @param connectionFactory RabbitMQ connection factory
     * @param configurer Auto-configurer for listener container factory
     * @return Configured listener container factory with JSON message converter
     */
    @Bean
    RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
