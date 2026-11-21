package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.enums.EventStockType;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.mapper.IProductBrokerMapper;
import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("stockSpringEventsProductListener")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsProductListener {

    private static final String ENTITY_TYPE = "Stock";

    private final IStockCommandRepositoryPort stockCommandPort;
    private final IProductBrokerMapper productBrokerMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductEvent(
            co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<ProductSyncDto, EventType> event) {

        EventDto<ProductAsyncDto, EventStockType> stockEvent = toStockEvent(event);

        try {
            log.info("Received {} message from in-process bus", ENTITY_TYPE);

            if (!isValidEvent(stockEvent)) {
                log.warn("Invalid {} event received, message discarded", ENTITY_TYPE);
                return;
            }

            processEvent(stockEvent);
            log.info("{} message processed successfully", ENTITY_TYPE);

        } catch (Exception e) {
            log.error("Error processing {} message: {}", ENTITY_TYPE, e.getMessage(), e);
        }
    }

    private EventDto<ProductAsyncDto, EventStockType> toStockEvent(
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
                    source.getEnterpriseId());
        }

        EventStockType type = event.getType() != null
                ? EventStockType.valueOf(event.getType().name())
                : null;

        return new EventDto<>(type, data);
    }

    private void processEvent(EventDto<ProductAsyncDto, EventStockType> event) {
        ProductAsyncDto data = event.getData();
        try {
            switch (event.getType()) {
                case CREATED:
                    log.info("Creating new stock for product: {}", data.getName());
                    Stock stock = productBrokerMapper.toDomain(data);
                    stockCommandPort.save(stock);
                    log.info("Stock created successfully for product: {}", data.getName());
                    break;

                case UPDATED:
                    log.info("Updating stock for product: {}", data.getName());
                    Stock updatedStock = productBrokerMapper.toDomain(data);
                    stockCommandPort.update(updatedStock.getProductId(), updatedStock.getName());
                    log.info("Stock updated successfully for product: {}", data.getName());
                    break;

                case DELETED:
                    log.info("Deleting stock for product: {}", data.getName());
                    stockCommandPort.deleteByProductId(data.getProductId());
                    log.info("Stock deletion processed for product: {}", data.getProductId());
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported event type: " + event.getType());
            }
        } catch (Exception e) {
            log.error("Database operation failed for stock product: {}", e.getMessage());
            throw e;
        }
    }

    private boolean isValidEvent(EventDto<ProductAsyncDto, EventStockType> event) {
        if (event == null) {
            log.warn("Event is null");
            return false;
        }

        if (event.getType() == null) {
            log.warn("Event type is null");
            return false;
        }

        if (event.getData() == null) {
            log.warn("Event data is null");
            return false;
        }

        ProductAsyncDto data = event.getData();

        if (data.getProductId() == null) {
            log.warn("ProductId is null - required field");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().isEmpty()) {
            log.warn("Missing or invalid required field: enterpriseId");
            return false;
        }

        if (data.getName() == null || data.getName().isEmpty()) {
            log.warn("Missing or invalid required field: name");
            return false;
        }

        return true;
    }
}
