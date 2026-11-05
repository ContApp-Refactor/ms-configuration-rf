package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig.RabbitProductConfig;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Publicador de eventos de productos via message broker
 *
 * Publica eventos de productos a través de RabbitMQ para sincronización
 * entre sistemas, incluyendo autenticación JWT en headers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher implements IProductEventPort{

    private final RabbitTemplate rabbitTemplate;
    private final IJwtUtils jwtUtils;

    @Override
    public void publishCreatedStockEvent(ProductSyncDto productSyncDto) {
        EventDto<ProductSyncDto, EventType> event = new EventDto<>(productSyncDto, EventType.CREATED);
        log.info("Publishing stock created event for product: {}", productSyncDto.getName());

        rabbitTemplate.convertAndSend(RabbitProductConfig.PRODUCT_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeader("x-jwt-token", jwtUtils.getToken());
            return message;
        });
    }

    @Override
    public void publishUpdatedStockEvent(ProductSyncDto productSyncDto) {
        EventDto<ProductSyncDto, EventType> event = new EventDto<>(productSyncDto, EventType.UPDATED);
        log.info("Publishing stock updated event for product: {}", productSyncDto.getName());

        rabbitTemplate.convertAndSend(RabbitProductConfig.PRODUCT_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeader("x-jwt-token", jwtUtils.getToken());
            return message;
        });
    }

    @Override
    public void publishDeletedStockEvent(ProductSyncDto productSyncDto) {
        EventDto<ProductSyncDto, EventType> event = new EventDto<>(productSyncDto, EventType.DELETED);
        log.info("Publishing stock deleted event for product ID: {}", productSyncDto.getName());

        rabbitTemplate.convertAndSend(RabbitProductConfig.PRODUCT_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeader("x-jwt-token", jwtUtils.getToken());
            return message;
        });
    }
    
}