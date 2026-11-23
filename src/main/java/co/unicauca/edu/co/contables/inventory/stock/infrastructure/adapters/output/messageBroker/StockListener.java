package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig.RabbitProductConfig;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.enums.EventStockType;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.mapper.IProductBrokerMapper;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.util.JsonUtils;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockListener extends AbstractMessageListener<EventDto<ProductAsyncDto, EventStockType>> {
    private final IStockCommandRepositoryPort stockCommandPort;
    private final IProductBrokerMapper productBrokerMapper;

    private String validationErrorMessage = null;

    @RabbitListener(queues = RabbitProductConfig.PRODUCT_STOCK_QUEUE)
    public void handleStockEvent(
            EventDto<ProductAsyncDto, EventStockType> event, 
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, channel, deliveryTag);
    }

    @Override
    public void processEvent(EventDto<ProductAsyncDto, EventStockType> event) {
        try{
            switch (event.getType()) {
                case CREATED:
                    log.info("Creating new stock for product: {}", event.getData().getName());
                    Stock stock = productBrokerMapper.toDomain(event.getData());
                    stockCommandPort.save(stock);
                    log.info("Stock created successfully for product: {}", event.getData().getName());
                    break;
                    
                case UPDATED:
                    log.info("Updating stock for product: {}", event.getData().getName());
                    Stock updatedStock = productBrokerMapper.toDomain(event.getData());
                    stockCommandPort.update(updatedStock.getProductId(), updatedStock.getName());
                    log.info("Stock updated successfully for product: {}", event.getData().getName());
                    break;
                    
                case DELETED:
                    log.info("Deleting stock for product: {}", event.getData().getName());
                    stockCommandPort.deleteByProductId(event.getData().getProductId());
                    log.info("Stock deletion processed for product: {}", event.getData().getProductId());
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported event type: " + event.getType());
            }
        } catch (Exception e) {
            // Re-throw to be handled by the parent class if persistence fails
            log.error("Database operation failed for kardex operation: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    protected boolean isValidEvent(EventDto<ProductAsyncDto, EventStockType> event) {
        validationErrorMessage = null; // Reset error message

        if (event == null) {
            validationErrorMessage = "Event is null";
            log.warn(validationErrorMessage);
            return false;
        }

        if (event.getType() == null) {
            validationErrorMessage = "Event type is null";
            log.warn(validationErrorMessage);
            return false;
        }
        
        if (event.getData() == null) {
            validationErrorMessage = "Event data is null";
            log.warn(validationErrorMessage);
            return false;
        }

        ProductAsyncDto data = event.getData();

        if (data.getProductId() == null) {
            validationErrorMessage = "Missing or invalid required field: productId";
            log.warn("ProductId is null - required field");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().isEmpty()) {
            validationErrorMessage = "Missing or invalid required field: enterpriseId";
            log.warn(validationErrorMessage);
            return false;
        }

        if (data.getName() == null || data.getName().isEmpty()) {
            validationErrorMessage = "Missing or invalid required field: name";
            log.warn(validationErrorMessage);
            return false;
        }

        return true;

    }

    @Override
    protected String getEntityType() {
        return "Stock";
    }

    @Override
    protected String extractEventType(EventDto<ProductAsyncDto, EventStockType> event) {
        if (event == null) {
            return null;
        }
        
        return event.getType() != null ? event.getType().toString() : null;
    }

    @Override
    protected String convertEventToJson(EventDto<ProductAsyncDto, EventStockType> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }
        
        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \"" + 
                   (event.getType() != null ? event.getType().toString() : "null") + "\"}";
        }
        
        return JsonUtils.toJsonWithNullHandling(event.getData());
    }

    @Override
    protected String getValidationErrorMessage() {
        return validationErrorMessage;
    }   
}
