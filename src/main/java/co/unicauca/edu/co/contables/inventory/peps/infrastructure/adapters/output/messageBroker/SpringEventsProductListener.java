package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IProductCommandOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.BaseException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper.ProductBrokerMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.utils.JsonUtils;
import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("pepsSpringEventsProductListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsProductListener {

    private static final String ENTITY_TYPE = "Product";

    private final IProductCommandOutPutPort productCommandOutPutPort;
    private final ProductBrokerMapper productBrokerMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;
    private final IEventRecoveryActionPort<EventDto<ProductAsyncDto, EventProductType>> productRecoveryActionPort;

    private String validationErrorMessage = null;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductEvent(
            co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<ProductSyncDto, EventType> event) {

        EventDto<ProductAsyncDto, EventProductType> pepsEvent = toPepsEvent(event);

        try {
            log.info("Received {} message from in-process bus", ENTITY_TYPE);

            if (!isValidEvent(pepsEvent)) {
                log.warn("Invalid {} event received, saving error to database", ENTITY_TYPE);
                handleValidationError(pepsEvent);
                return;
            }

            processEvent(pepsEvent);
            log.info("{} message processed successfully", ENTITY_TYPE);

        } catch (Exception e) {
            handleProcessingError(e, pepsEvent);
        }
    }

    private EventDto<ProductAsyncDto, EventProductType> toPepsEvent(
            co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<ProductSyncDto, EventType> event) {

        if (event == null) {
            return null;
        }

        ProductAsyncDto data = null;
        if (event.getData() != null) {
            ProductSyncDto source = event.getData();
            data = new ProductAsyncDto(
                    source.getProductId(),
                    source.getName(),
                    source.getReference(),
                    source.getEnterpriseId(),
                    source.getPresentation(),
                    source.getQuantity(),
                    source.getCost(),
                    source.isState());
        }

        EventProductType type = event.getType() != null
                ? EventProductType.valueOf(event.getType().name())
                : null;

        return new EventDto<>(data, type);
    }

    private void processEvent(EventDto<ProductAsyncDto, EventProductType> event) {
        ProductAsyncDto data = event.getData();
        String productName = data.getName() != null ? data.getName() : "unnamed";

        try {
            switch (event.getType()) {
                case CREATED:
                    log.info("Creating new product: {}", productName);
                    Product product = productBrokerMapper.toDomain(data);
                    productCommandOutPutPort.save(product);
                    log.info("Product created successfully: {}", product.getName());
                    break;

                case UPDATED:
                    log.info("Updating product: {}", productName);
                    Product updatedProduct = productBrokerMapper.toDomain(data);
                    productCommandOutPutPort.save(updatedProduct);
                    log.info("Product updated successfully: {}", updatedProduct.getName());
                    break;

                case DELETED:
                    log.info("Deleting product: {}", productName);
                    String deleteResult = productCommandOutPutPort.delete(data.getProductId());
                    log.info("Product deletion result for: {}", deleteResult);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported event type: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Database operation failed for product {}: {}", productName, e.getMessage());
            throw e;
        }
    }

    private boolean isValidEvent(EventDto<ProductAsyncDto, EventProductType> event) {
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

    private void handleValidationError(EventDto<ProductAsyncDto, EventProductType> event) {
        try {
            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String specificError = validationErrorMessage;
                String errorDescription = specificError != null
                        ? "Validation failed: " + specificError
                        : "Validation failed: Required fields are missing or invalid";

                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, ENTITY_TYPE);
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    private void handleProcessingError(Exception e, EventDto<ProductAsyncDto, EventProductType> event) {
        try {
            if (e instanceof BaseException) {
                log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage());
            } else {
                log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage(), e);
            }

            boolean recoveryExecuted = attemptRecovery(event);

            if (messageErrorHandlingPort != null) {
                String eventType = extractEventType(event);
                String messageData = convertEventToJson(event);
                String errorDescription = String.format("Processing error: %s%s",
                        e.getMessage(),
                        recoveryExecuted ? " (Recovery action executed)" : "");

                messageErrorHandlingPort.saveProcessingError(eventType, errorDescription, messageData, ENTITY_TYPE);
            }
        } catch (Exception handlerException) {
            log.error("Error handling processing error: {}", handlerException.getMessage());
        }
    }

    private boolean attemptRecovery(EventDto<ProductAsyncDto, EventProductType> event) {
        if (productRecoveryActionPort == null) {
            log.debug("No recovery action port configured for {}", ENTITY_TYPE);
            return false;
        }

        try {
            if (productRecoveryActionPort.canHandle(event)) {
                log.info("Attempting recovery action for failed {} event", ENTITY_TYPE);
                boolean success = productRecoveryActionPort.executeRecoveryAction(event);

                if (success) {
                    log.info("Recovery action executed successfully for {} event", ENTITY_TYPE);
                } else {
                    log.warn("Recovery action failed for {} event", ENTITY_TYPE);
                }

                return success;
            }

            log.debug("Recovery action cannot handle this {} event", ENTITY_TYPE);
            return false;
        } catch (Exception recoveryException) {
            log.error("Error executing recovery action for {} event: {}",
                    ENTITY_TYPE, recoveryException.getMessage(), recoveryException);
            return false;
        }
    }

    private String extractEventType(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null) {
            return null;
        }

        return event.getType() != null ? event.getType().toString() : null;
    }

    private String convertEventToJson(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null) {
            return "{\"error\": \"Event is null\"}";
        }

        if (event.getData() == null) {
            return "{\"error\": \"Event data is null\", \"eventType\": \""
                    + (event.getType() != null ? event.getType().toString() : "null") + "\"}";
        }

        return JsonUtils.toJsonWithNullHandling(event.getData());
    }
}
