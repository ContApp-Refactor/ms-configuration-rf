package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductUsagePort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig.RabbitProductUsedConfig;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductUsageEventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventUsageType;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Listener para eventos de uso de productos desde PEPS
 *
 * Escucha eventos de RabbitMQ cuando PEPS notifica que ha utilizado un producto,
 * actualizando el contador de uso correspondiente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductUsageListener extends AbstractMessageListener<EventDto<ProductUsageEventDto, EventUsageType>> {

    private final IProductUsagePort productUsagePort;

    /**
     * @brief Maneja eventos de uso de productos desde la cola
     * @param event Evento con información del producto usado
     */
    @RabbitListener(queues = RabbitProductUsedConfig.PRODUCT_USED_QUEUE)
    public void handleProductEvent(
            EventDto<ProductUsageEventDto, EventUsageType> event,
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Valida la integridad del evento recibido
     * @param event Evento a validar
     * @return true si el evento es válido
     */
    protected boolean isValidEvent(EventDto<ProductUsageEventDto, EventUsageType> event) {
        if (event == null) {
            log.warn("Event is null");
            return false;
        }
        
        if (event.getData() == null) {
            log.warn("Event data is null");
            return false;
        }
        
        ProductUsageEventDto data = event.getData();
        
        if (data.getProductId() == null) {
            log.warn("ProductId is null - required field");
            return false;
        }
        
        // if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
        //     log.warn("EnterpriseId is null or empty - required field");
        //     return false;
        // }
        
        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed is null or invalid - required field");
            return false;
        }
        
        return true;
    }

    @Override
    protected void processEvent(EventDto<ProductUsageEventDto, EventUsageType> event) {
        log.info("Received product usage event");
        
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid product usage event received");
                return;
            }
            
            ProductUsageEventDto data = event.getData();
            log.info("Processing usage for productId: {}, quantity: {}",
                     data.getProductId(), data.getQuantityUsed());

            productUsagePort.incrementUsageCount(data.getProductId());

            log.info("Product usage event processed successfully for productId: {}", data.getProductId());
            
        } catch (Exception e) {
            log.error("Error processing product usage event: {}", e.getMessage(), e);
            // En caso de error, el mensaje se pierde intencionalmente para no bloquear la cola
            // Se podría implementar DLQ o reintentos según necesidades del negocio
        }
    }

    @Override
    protected String getEntityType() {
        return "ProductUsage";
    }
}

