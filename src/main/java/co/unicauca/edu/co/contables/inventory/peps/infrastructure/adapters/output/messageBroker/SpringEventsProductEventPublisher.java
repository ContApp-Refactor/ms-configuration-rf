package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductUsageEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("pepsProductEventPublisher")
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsProductEventPublisher implements IProductEventPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishUsedProductEvent(Long productId, Integer quantityUsed) {
        ProductUsageEventDto event = new ProductUsageEventDto();
        event.setProductId(productId);
        event.setQuantityUsed(quantityUsed);
        log.info("Publishing product used event, productId: {}, quantityUsed: {}", productId, quantityUsed);

        eventPublisher.publishEvent(event);
    }
}
