package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAveragePurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAverageSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("kardexSpringEventsFactureListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsFactureListener {

    private static final String ENTITY_TYPE = "Kardex";

    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRabbitMQMapper kardexRabbitMQRestMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWeightedAveragePurchaseEvent(WeightedAveragePurchaseEvent event) {
        handleFactureEvent(toKardexEvent(event));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWeightedAverageSalesEvent(WeightedAverageSalesEvent event) {
        handleFactureEvent(toKardexEvent(event));
    }

    private EventDto<KardexRabbitDto, EventFactureType> toKardexEvent(WeightedAveragePurchaseEvent event) {
        if (event == null) {
            return null;
        }

        KardexPurchaseDtoRequest source = event.getData();
        KardexRabbitDto data = source == null ? null : new KardexRabbitDto(
                (long) source.getQuantity(),
                source.getFactCode(),
                source.getUnitPrice(),
                source.getProductId(),
                source.getDetails());

        return new EventDto<>(data, event.getType());
    }

    private EventDto<KardexRabbitDto, EventFactureType> toKardexEvent(WeightedAverageSalesEvent event) {
        if (event == null) {
            return null;
        }

        KardexSalesDtoRequest source = event.getData();
        KardexRabbitDto data = source == null ? null : new KardexRabbitDto(
                (long) source.getQuantity(),
                source.getFactCode(),
                null,
                source.getProductId(),
                source.getDetails());

        return new EventDto<>(data, event.getType());
    }

    private void handleFactureEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        try {
            log.info("Received {} message from in-process bus", ENTITY_TYPE);

            String validationError = validateEvent(event);
            if (validationError != null) {
                log.warn("Invalid {} event received, saving error to database", ENTITY_TYPE);
                handleValidationError(event, validationError);
                return;
            }

            processEvent(event);
            log.info("{} message processed successfully", ENTITY_TYPE);
        } catch (Exception e) {
            handleProcessingError(e, event);
        }
    }

    private String validateEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            log.warn("Event is null");
            return "Event is null";
        }

        if (event.getType() == null) {
            log.warn("Event type is null");
            return "Event type is null";
        }

        if (event.getData() == null) {
            log.warn("Event data is null");
            return "Event data is null";
        }

        KardexRabbitDto data = event.getData();

        if (data.getQuantity() == null || data.getQuantity() <= 0) {
            log.warn("Quantity is null or invalid - required field");
            return "Missing or invalid required field: quantity";
        }

        if (data.getFactCode() == null) {
            log.warn("FactCode is null - required field");
            return "Missing required field: factCode";
        }

        if (data.getProductId() == null) {
            log.warn("ProductId is null - required field");
            return "Missing required field: productId";
        }

        if ((event.getType() == EventFactureType.PURCHASE || event.getType() == EventFactureType.NONCOMMERCIALENTRY)
                && (data.getUnitPrice() == null || data.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)) {
            log.warn("UnitPrice is null or invalid for purchase operation - required field");
            return "Missing or invalid required field for purchase operation: unitPrice";
        }

        return null;
    }

    private void processEvent(EventDto<KardexRabbitDto, EventFactureType> event) {
        switch (event.getType()) {
            case PURCHASE:
                Kardex kardex = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardex.setType(MovementType.PURCHASE);
                kardexCommandPort.registerPurchase(kardex);
                log.info("Registering purchase in Kardex for product ID: {}", kardex.getProductId());
                break;
            case SALE:
                Kardex kardexSale = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexSale.setType(MovementType.SALE);
                kardexCommandPort.registerSale(kardexSale);
                log.info("Registering sale in Kardex for product ID: {}", kardexSale.getProductId());
                break;
            case RETURNONSALE:
                Kardex kardexReturnOnSale = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexReturnOnSale.setType(MovementType.SALESRETURN);
                kardexCommandPort.registerReturnOnSale(kardexReturnOnSale);
                log.info("Registering return on sale in Kardex for product ID: {}", kardexReturnOnSale.getProductId());
                break;
            case RETURNONPURCHASE:
                Kardex kardexReturnOnPurchase = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexReturnOnPurchase.setType(MovementType.PURCHASERETURN);
                kardexCommandPort.registerReturnOnPurchase(kardexReturnOnPurchase);
                log.info("Registering return on purchase in Kardex for product ID: {}", kardexReturnOnPurchase.getProductId());
                break;
            case NONCOMMERCIALEXIT:
                Kardex kardexNonCommercialExit = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexNonCommercialExit.setType(MovementType.NONCOMMERCIALEXIT);
                kardexCommandPort.registerSale(kardexNonCommercialExit);
                log.info("Registering non-commercial exit in Kardex for product ID: {}", kardexNonCommercialExit.getProductId());
                break;
            case NONCOMMERCIALENTRY:
                Kardex kardexNonCommercialEntry = kardexRabbitMQRestMapper.toDomain(event.getData());
                kardexNonCommercialEntry.setType(MovementType.NONCOMMERCIALENTRY);
                kardexCommandPort.registerPurchase(kardexNonCommercialEntry);
                log.info("Registering non-commercial entry in Kardex for product ID: {}", kardexNonCommercialEntry.getProductId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + event.getType());
        }
    }

    private void handleValidationError(EventDto<KardexRabbitDto, EventFactureType> event, String validationError) {
        try {
            if (messageErrorHandlingPort != null) {
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        "Validation failed: " + validationError,
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception e) {
            log.error("Error saving validation error to database: {}", e.getMessage());
        }
    }

    private void handleProcessingError(Exception e, EventDto<KardexRabbitDto, EventFactureType> event) {
        try {
            log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage(), e);

            if (messageErrorHandlingPort != null) {
                String errorDescription = String.format("Processing error: %s", e.getMessage());
                messageErrorHandlingPort.saveProcessingError(
                        extractEventType(event),
                        errorDescription,
                        convertEventToJson(event),
                        ENTITY_TYPE);
            }
        } catch (Exception handlerException) {
            log.error("Error handling processing error: {}", handlerException.getMessage());
        }
    }

    private String extractEventType(EventDto<KardexRabbitDto, EventFactureType> event) {
        if (event == null) {
            return null;
        }

        return event.getType() != null ? event.getType().toString() : null;
    }

    private String convertEventToJson(EventDto<KardexRabbitDto, EventFactureType> event) {
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
