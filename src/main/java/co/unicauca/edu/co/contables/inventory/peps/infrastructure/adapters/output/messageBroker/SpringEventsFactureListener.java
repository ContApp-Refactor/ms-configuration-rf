package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsPurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("pepsSpringEventsFactureListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsFactureListener {

    private static final String ENTITY_TYPE = "Kardex";

    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRabbitMQMapper kardexRabbitMQMapper;
    private final IMessageErrorHandlingPort messageErrorHandlingPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPepsPurchaseEvent(PepsPurchaseEvent event) {
        handleFactureEvent(toPepsEvent(event));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPepsSalesEvent(PepsSalesEvent event) {
        handleFactureEvent(toPepsEvent(event));
    }

    private EventDto<KardexRabbitDto, EventFactureType> toPepsEvent(PepsPurchaseEvent event) {
        if (event == null) {
            return null;
        }

        KardexPurchaseDtoRequest source = event.getData();
        KardexRabbitDto data = source == null ? null : new KardexRabbitDto(
                (long) source.getQuantity(),
                source.getFactCode() == null ? null : String.valueOf(source.getFactCode()),
                source.getUnitPrice(),
                source.getProductId(),
                source.getDetails());

        return new EventDto<>(data, event.getType());
    }

    private EventDto<KardexRabbitDto, EventFactureType> toPepsEvent(PepsSalesEvent event) {
        if (event == null) {
            return null;
        }

        KardexSalesDtoRequest source = event.getData();
        KardexRabbitDto data = source == null ? null : new KardexRabbitDto(
                (long) source.getQuantity(),
                source.getFactCode() == null ? null : String.valueOf(source.getFactCode()),
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
                Kardex kardex = kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerPurchase(kardex);
                log.info("Registering purchase in Kardex for product ID: {}", kardex.getProduct().getProductId());
                break;
            case SALE:
                Kardex kardexSale = kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerSale(kardexSale);
                log.info("Registering sale in Kardex for product ID: {}", kardexSale.getProduct().getProductId());
                break;
            case RETURNONSALE:
                Kardex kardexReturnOnSale = kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerSaleReturn(kardexReturnOnSale);
                log.info("Registering sale-return in Kardex for product ID: {}",
                        kardexReturnOnSale.getProduct().getProductId());
                break;
            case RETURNONPURCHASE:
                Kardex kardexReturnOnPurchase = kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerPurchaseReturn(kardexReturnOnPurchase);
                log.info("Registering purchase-return in Kardex for product ID: {}",
                        kardexReturnOnPurchase.getProduct().getProductId());
                break;
            case NONCOMMERCIALENTRY:
                Kardex nonCommercialEntry = kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerNonCommercialEntry(nonCommercialEntry);
                log.info("Registering Non-commercial-Entry in Kardex for product ID: {}",
                        nonCommercialEntry.getProduct().getProductId());
                break;
            case NONCOMMERCIALEXIT:
                Kardex nonCommercialExit = kardexRabbitMQMapper.toDomain(event.getData());
                kardexCommandPort.registerNonCommercialExit(nonCommercialExit);
                log.info("Registering Non-commercial-Exit in Kardex for product ID: {}",
                        nonCommercialExit.getProduct().getProductId());
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
