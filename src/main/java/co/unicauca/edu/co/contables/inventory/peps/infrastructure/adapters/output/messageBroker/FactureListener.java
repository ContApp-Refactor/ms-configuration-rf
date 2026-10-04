package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig.RabbitPEPSConfig;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.base.AbstractMessageListener;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;

/**
 * @brief Listener for Facture (Invoice) events from RabbitMQ
 * 
 * Handles various invoice-related events such as PURCHASE, SALE, RETURNS, etc.,
 * and triggers corresponding commands in the Kardex domain.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FactureListener extends AbstractMessageListener<EventDto<KardexRabbitDto,EventFactureType>,EventFactureType> {
    
    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRabbitMQMapper kardexRabbitMQMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPortImpl;

    private String validationErrorMessage = null;
      
    /**
     * @brief Initializes the listener configuration
     */
    @PostConstruct
    private void init() {
        this.messageErrorHandlingPort = messageErrorHandlingPortImpl;
        // Implement specific recovery strategy for kardex events if needed
    }

    /**
     * @brief Handles incoming facture events from the queue
     * 
     * @param event The event data containing Kardex information and event type
     * @param message The raw AMQP message
     * @param channel The AMQP channel
     * @param deliveryTag The delivery tag for manual acknowledgement
     */
    @RabbitListener(queues = RabbitPEPSConfig.PEPS_QUEUE)
    public void handleFactureEvent( EventDto<KardexRabbitDto, EventFactureType> event,
        Message message, 
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        
        handleMessage(event, channel, deliveryTag);
    }

    /**
     * @brief Processes the specific business logic for the event
     * 
     * Maps the DTO to the domain model and executes the appropriate command
     * based on the event type (PURCHASE, SALE, etc.).
     * 
     * @param event The event to process
     */
    @Override
    protected void processEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        switch (event.getType()) {
            case PURCHASE:
                Kardex kardex=kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerPurchase(kardex);
                log.info("Registering purchase in Kardex for product ID: {}", kardex.getProduct().getProductId());
            break;
            case SALE:
                Kardex kardexSale=kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerSale(kardexSale);
                log.info("Registering sale in Kardex for product ID: {}", kardexSale.getProduct().getProductId());
            break;
            case RETURNONSALE:
                Kardex kardexReturnOnSale=kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerSaleReturn(kardexReturnOnSale);
                 log.info("Registering sale-return in Kardex for product ID: {}", kardexReturnOnSale.getProduct().getProductId());
            break;
            case RETURNONPURCHASE:
                Kardex kardexReturnOnPurchase=kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerPurchaseReturn(kardexReturnOnPurchase);
                log.info("Registering purchase-return in Kardex for product ID: {}", kardexReturnOnPurchase.getProduct().getProductId());        
            break;
            case NONCOMMERCIALENTRY:
                Kardex nonCommercialEntry=kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerNonCommercialEntry(nonCommercialEntry);
                log.info("Registering Non-commercial-Entry in Kardex for product ID: {}",nonCommercialEntry.getProduct().getProductId());

            break;
            case NONCOMMERCIALEXIT:
                Kardex nonCommercialExit=kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerNonCommercialExit(nonCommercialExit);
                log.info("Registering Non-commercial-Exit in Kardex for product ID: {}");

            break;

            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }

    }

    /**
     * @brief Validates the incoming event structure and data
     * 
     * Checks for nulls and required fields like quantity, factCode, productId.
     * Also validates unitPrice for PURCHASE events.
     * 
     * @param event The event to validate
     * @return true if valid, false otherwise
     */
    @Override
    protected boolean isValidEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
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
        
        KardexRabbitDto data = event.getData();
        
        // Validate required fields
        if (data.getQuantity() == null || data.getQuantity() <= 0) {
            validationErrorMessage = "Missing or invalid required field: quantity";
            log.warn("Quantity is null or invalid - required field");
            return false;
        }
        
        if (data.getFactCode() == null) {
            validationErrorMessage = "Missing required field: factCode";
            log.warn("FactCode is null - required field");
            return false;
        }
        
        if (data.getProductId() == null) {
            validationErrorMessage = "Missing required field: productId";
            log.warn("ProductId is null - required field");
            return false;
        }
        
        // For purchase operations, unitPrice is required
        if ((event.getType() == EventFactureType.PURCHASE || event.getType() == EventFactureType.NONCOMMERCIALENTRY)
            && (data.getUnitPrice() == null || data.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)) {
            validationErrorMessage = "Missing or invalid required field for purchase operation: unitPrice";
            log.warn("UnitPrice is null or invalid for purchase operation - required field");
            return false;
        }
        
        return true;

    }

   

    /**
     * @brief Gets the entity type name for logging
     * @return "Kardex"
     */
    @Override
    protected String getEntityType() {
        return "Kardex";
    }


    /**
     * @brief Extracts the event type as a string
     * @param event The event object
     * @return The string representation of the event type
     */
     @Override
    protected String extractEventType(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            return null;
        }
        
        return event.getType() != null ? event.getType().toString() : null;
    }

    /**
     * @brief Converts the event data to JSON for error logging
     * @param event The event object
     * @return JSON string of the event data
     */
    @Override
    protected String convertEventToJson(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }
        
        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \"" + 
                   (event.getType() != null ? event.getType().toString() : "null") + "\"}";
        }
        
        return JsonUtils.toJsonWithNullHandling(event.getData());
    }
    
    /**
     * @brief Gets the last validation error message
     * @return The error message string
     */
     @Override
    protected String getValidationErrorMessage() {
        return validationErrorMessage;
    }

}
