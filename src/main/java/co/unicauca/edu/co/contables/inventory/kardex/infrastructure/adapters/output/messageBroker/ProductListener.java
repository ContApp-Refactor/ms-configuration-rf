package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig.RabbitProductConfig;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.mapper.ProductBrokerMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.util.JsonUtils;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief RabbitMQ listener for product synchronization events
 * 
 * Handles product lifecycle events (create, update, delete) from message broker
 * with error handling and recovery mechanisms for reliable data synchronization.
 */
@Component("kardexProductListener")@RequiredArgsConstructor
@Slf4j
public class ProductListener extends AbstractMessageListener<EventDto<ProductAsyncDto, EventProductType>> {
    private final IProductCommandRepositoryPort productCommandPort;
    private final ProductBrokerMapper productBrokerMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;
    private final IEventRecoveryActionPort<EventDto<ProductAsyncDto, EventProductType>> productRecoveryActionPort;

    private String validationErrorMessage = null;
    
    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
        this.eventRecoveryActionPort = productRecoveryActionPort;
    }

    /**
     * @brief Handles product events from RabbitMQ queue
     * @param event Product event with data and type information
     * @param message RabbitMQ message metadata
     * @param channel RabbitMQ channel for acknowledgments
     * @param deliveryTag Message delivery tag for acknowledgment
     */
    @RabbitListener(queues = RabbitProductConfig.PRODUCT_KARDEX_QUEUE)
    public void handleProductEvent(
            EventDto<ProductAsyncDto, EventProductType> event, 
            Message message, 
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        
        handleMessage(event, channel, deliveryTag);
    }
    
    /**
     * @brief Processes product events based on event type
     * @param event Product event to process
     */
    @Override
    protected void processEvent(EventDto<ProductAsyncDto, EventProductType> event) {
        ProductAsyncDto data = event.getData();
        String productName = data.getName() != null ? data.getName() : "unnamed";
        
        try {
            switch (event.getType()) {
                case CREATED:
                    log.info("Creating new product: {}", productName);
                    Product product = productBrokerMapper.toDomain(data);
                    productCommandPort.save(product);
                    log.info("Product created successfully: {}", product.getName());
                    break;
                    
                case UPDATED:
                    log.info("Updating product: {}", productName);
                    Product updatedProduct = productBrokerMapper.toDomain(data);
                    String updateResult = productCommandPort.update(updatedProduct);
                    log.info("Product update result: {} - {}", updatedProduct.getName(), updateResult);
                    break;
                    
                case DELETED:
                    log.info("Deleting product: {}", productName);
                    String deleteResult = productCommandPort.delete(data.getProductId());
                    log.info("Product deletion result: {}", deleteResult);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unsupported event type: " + event.getType());
            }
        } catch (Exception e) {
            // Re-throw for parent class error handling if persistence fails (e.g., duplicate reference)
            log.error("Database operation failed for product {}: {}", productName, e.getMessage());
            throw e;
        }
    }

    /**
     * @brief Validates product event data integrity
     * @param event Product event to validate
     * @return True if event is valid, false otherwise
     */
    @Override
    protected boolean isValidEvent(EventDto<ProductAsyncDto, EventProductType> event) {
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
        
        // Validar campos obligatorios (todos menos presentation)
        if (data.getProductId() == null) {
            validationErrorMessage = "Missing required field: productId";
            log.warn(validationErrorMessage);
            return false;
        }
        
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            validationErrorMessage = "Name is null or empty - required field";
            log.warn(validationErrorMessage);
            return false;
        }
        
        if (data.getReference() == null || data.getReference().trim().isEmpty()) {
            validationErrorMessage = "Reference is null or empty - required field";
            log.warn(validationErrorMessage);
            return false;
        }
        
        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            validationErrorMessage = "EnterpriseId is null or empty - required field";
            log.warn(validationErrorMessage);
            return false;
        }
        
        return true;
    }

    @Override
    protected String getEntityType() {
        return "Product";
    }

    @Override
    protected String extractEventType(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null) {
            return null;
        }
        
        return event.getType() != null ? event.getType().toString() : null;
    }

    @Override
    protected String convertEventToJson(EventDto<ProductAsyncDto, EventProductType> event) {
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
