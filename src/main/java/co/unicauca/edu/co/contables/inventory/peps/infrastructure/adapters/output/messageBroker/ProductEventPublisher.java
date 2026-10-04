package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig.RabbitProductUsedConfig;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.ProductUsageEventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Publisher for Product events to RabbitMQ
 * 
 * Implements the output port to publish events when a product is used/consumed.
 */
@Component("pepsProductEventPublisher")
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher implements IProductEventPort{

    private final RabbitTemplate rabbitTemplate;
    private final JwtTokenService jwtTokenService;

    /**
     * @brief Publishes a product usage event
     * 
     * Sends a message to the exchange indicating that a specific quantity of a product has been used.
     * Attaches the current JWT token to the message headers.
     * 
     * @param productId The ID of the product used
     * @param quantityUsed The quantity consumed
     */
    @Override
    public void publishUsedProductEvent(Long productId, Integer quantityUsed) {
        ProductUsageEventDto event = new ProductUsageEventDto();
        event.setProductId(productId);
        event.setQuantityUsed(quantityUsed);

        EventDto<ProductUsageEventDto, EventUsageType> eventDto = new EventDto<>(event, EventUsageType.USED);
        log.info("Publishing product created event, productId: {}, quantityUsed: {}", productId, quantityUsed);

        rabbitTemplate.convertAndSend(RabbitProductUsedConfig.PRODUCT_USED_EXCHANGE, "", eventDto, message -> {
            message.getMessageProperties().setHeader("x-jwt-token", jwtTokenService.getToken());
            return message;
        });
    }
}