package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.recovery;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @brief Recovery action implementation for product message processing failures
 * 
 * When asynchronous product processing fails, executes a full synchronization
 * for the affected enterprise as a recovery measure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRecoveryActionPort implements IEventRecoveryActionPort<EventDto<ProductAsyncDto, EventProductType>> {
    
    @Qualifier("pepsProductCommandService")
    private final IProductSyncCommandPort productSyncCommandPort;

    /**
     * @brief Executes recovery action by triggering full product sync
     * @param event The failed product event
     * @return True if recovery was successful, false otherwise
     */
    @Override
    public boolean executeRecoveryAction(EventDto<ProductAsyncDto, EventProductType> event) {
        try {
            
            if (event == null || event.getData() == null) {
                log.error("Cannot execute recovery: event or event data is null");
                return false;
            }
            
            ProductAsyncDto productData = event.getData();
            String enterpriseId = productData.getEnterpriseId();
            
            if (enterpriseId == null || enterpriseId.trim().isEmpty()) {
                log.error("Cannot execute recovery: enterpriseId is null or empty");
                return false;
            }
            
            log.info("Executing product sync recovery for enterpriseId: {}", enterpriseId);
            
            String result = productSyncCommandPort.syncProductsByEnterpriseId(enterpriseId);
            
            log.info("Product sync recovery completed for enterpriseId: {}. Result: {}", enterpriseId, result);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to execute product sync recovery: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * @brief Determines if this recovery action can handle the given event
     * @param event The event to check
     * @return True if this action can handle the event, false otherwise
     */
    @Override
    public boolean canHandle(EventDto<ProductAsyncDto, EventProductType> event) {
        if (event == null || event.getData() == null) {
            log.debug("Cannot handle recovery: event or event data is null");
            return false;
        }
        
        String enterpriseId = event.getData().getEnterpriseId();
        if (enterpriseId == null || enterpriseId.trim().isEmpty()) {
            log.debug("Cannot handle recovery: enterpriseId is null or empty");
            return false;
        }
        
        log.debug("Can handle product recovery for enterpriseId: {}", enterpriseId);
        return true;
    }
}
