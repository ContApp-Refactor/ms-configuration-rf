package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductSyncEvent;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;

import lombok.extern.slf4j.Slf4j;

@Component("productEventPublisher")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class SpringEventsProductEventPublisher implements IProductEventPort {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventsProductEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publishCreatedStockEvent(ProductSyncDto productSyncDto) {
        publishSpringEvent(productSyncDto, EventType.CREATED);
    }

    @Override
    public void publishUpdatedStockEvent(ProductSyncDto productSyncDto) {
        publishSpringEvent(productSyncDto, EventType.UPDATED);
    }

    @Override
    public void publishDeletedStockEvent(ProductSyncDto productSyncDto) {
        publishSpringEvent(productSyncDto, EventType.DELETED);
    }

    private void publishSpringEvent(ProductSyncDto productSyncDto, EventType type) {
        EventDto<ProductSyncDto, EventType> event = new ProductSyncEvent(productSyncDto, type);
        log.info("Publishing in-process product {} event for product: {}", type, productSyncDto.getName());

        eventPublisher.publishEvent(event);
    }
}
