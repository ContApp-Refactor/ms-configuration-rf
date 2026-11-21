package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductUsagePort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductUsageEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsProductUsageListener {

    private final IProductUsagePort productUsagePort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProductUsed(ProductUsageEventDto event) {
        log.info("Received product usage event");

        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid product usage event received");
                return;
            }

            log.info("Processing usage for productId: {}, quantity: {}",
                    event.getProductId(), event.getQuantityUsed());
            productUsagePort.incrementUsageCount(event.getProductId());
            log.info("Product usage event processed successfully for productId: {}", event.getProductId());
        } catch (Exception e) {

            log.error("Error processing product usage event: {}", e.getMessage(), e);
        }
    }

    private boolean isValidEvent(ProductUsageEventDto data) {
        if (data == null) {
            log.warn("Event is null");
            return false;
        }

        if (data.getProductId() == null) {
            log.warn("ProductId is null - required field");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed is null or invalid - required field");
            return false;
        }

        return true;
    }
}
