package co.unicauca.edu.co.contables.inventory.peps.application.service.command;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.transaction.Transactional;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IProductCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IProductCommandOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductClientPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.ISyncStateRepositoryPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for Product command operations
 * 
 * Handles product synchronization with external systems and manages
 * product lifecycle operations including deletion.
 */
@Service("pepsProductCommandService")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductCommandService implements IProductSyncCommandPort,IProductCommandPort {
    private final IProductCommandOutPutPort productCommandOutPutPort;
    @Qualifier("pepsProductClientAdapter")
    private final IProductClientPort productClient;
    private final ISyncStateRepositoryPort syncStateRepository;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;
    
    private static final String SYNC_TYPE_PRODUCTS = "products";

    /**
     * @brief Synchronizes products for an enterprise from external API
     * @param enterpriseId Enterprise identifier to sync products for
     * @return Status message with sync results
     */
    @Override
    public String syncProductsByEnterpriseId(String enterpriseId) {
        // 1. Obtain the last synchronization date
        Optional<Instant> lastSync=syncStateRepository.findLastSyncFor(enterpriseId, enterpriseId);

        if(lastSync.isEmpty()){
            // If no previous sync exists, create a new sync state with the current time
            Instant oldDate = Instant.parse("2000-01-01T00:00:00Z");
            lastSync = createSyncStateIfNotExists(enterpriseId, oldDate); 
            log.info("Created initial sync state for enterpriseId={} with date={}", enterpriseId, oldDate);
        }
    
        log.info("Syncing products for enterpriseId={} since lastSync={}", enterpriseId, lastSync.get());

        // 2. Moment before the call (this will be the new date if all goes well)
        Instant syncStartedAt = Instant.now();

        try {
            
            // 3. Call the API
            List<Product> updatedProducts = productClient
                .findAllProductsByEnterpriseId(enterpriseId, lastSync.get());

            // 4. Process the received products
            processUpdatedProducts(updatedProducts, enterpriseId);

            // 5. Only if everything was successful, update the date
            updateSyncState(enterpriseId, syncStartedAt);

            log.info("Syncing products for enterpriseId={} completed", enterpriseId);

            return "Successful synchronization: " + updatedProducts.size() + " products processed.";
            
        } catch (Exception e) {
            log.error(messageService.getMessage(MessageKeys.LOG_SYNC_ERROR, enterpriseId), e);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(500, 
                messageService.getMessage(MessageKeys.ERROR_SYNC_PRODUCTS, e.getMessage()));
            throw e;
        }
    }

    /**
     * @brief Converts product DTOs to domain entities
     * @param products List of product DTOs
     * @param enterpriseId Enterprise identifier
     * @return List of Product domain entities
     */
    private List<Product> getProductsFromDto(List<Product> products, String enterpriseId) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        return products.stream()
            .map(dto -> Product.create(
                    dto.getProductId(),
                    dto.getName(),
                    dto.getReference(),
                    dto.getPresentation(),
                    enterpriseId
            ))
            .toList();
    }

    /**
     * @brief Updates the synchronization state
     * @param enterpriseId Enterprise identifier
     * @param syncDate Timestamp of successful synchronization
     */
    private void updateSyncState(String enterpriseId, Instant syncDate) {
        Optional<SyncState> existingState = syncStateRepository
            .findBySyncTypeAndEnterpriseId(SYNC_TYPE_PRODUCTS, enterpriseId);
        
        if (existingState.isPresent()) {
            // Update existing record
            SyncState state = existingState.get();
            state.setLastSyncDate(syncDate);
            syncStateRepository.save(state);
        } else {
            // Create new record
            SyncState newState = new SyncState();
            newState.setSyncType(SYNC_TYPE_PRODUCTS);
            newState.setEnterpriseId(enterpriseId);
            newState.setLastSyncDate(syncDate);
            syncStateRepository.save(newState);
        }
    }

   
    
    /**
     * @brief Processes and persists updated products
     * @param products List of updated products
     * @param enterpriseId Enterprise identifier
     */
    private void processUpdatedProducts(List<Product> products, String enterpriseId) {
        try {
            if (products == null || products.isEmpty()) {
                log.info("No products to update for enterpriseId={}", enterpriseId);
                return;
            }
            
            List<Product> productList = getProductsFromDto(products, enterpriseId);

            // Save all products to the database
            productCommandOutPutPort.saveAll(productList);
            log.info("Process updated products completed for enterpriseId={}", enterpriseId);
        } catch (Exception e) {
            log.error("Process updated products error for enterpriseId={}", enterpriseId, e);
        }
    }

    


    /**
     * @brief Creates initial sync state if none exists
     * @param enterpriseId Enterprise identifier
     * @param syncDate Initial sync date
     * @return Optional containing the sync date
     */
    private Optional<Instant> createSyncStateIfNotExists(String enterpriseId, Instant syncDate) {
        SyncState newState = new SyncState();
        newState.setSyncType(SYNC_TYPE_PRODUCTS);
        newState.setEnterpriseId(enterpriseId);
        newState.setLastSyncDate(syncDate);
        syncStateRepository.save(newState);
        log.info("Created new sync state for enterpriseId={} with date={}", enterpriseId, syncDate);
        return Optional.of(syncDate);
    }

    


    /**
     * @brief Deletes a product by its ID
     * @param productId Product identifier
     * @return Status message
     */
    @Override
    public String deleteById(Long productId) {
        log.info("Deleting product with ID {}", productId);
        return productCommandOutPutPort.delete(productId); 
    }


    /**
     * @brief Deletes all products for an enterprise
     * @param enterpriseId Enterprise identifier
     * @return Status message
     */
    @Override
    public String deleteAllByEnterpriseId(String enterpriseId) {
        log.info("Deleting all products for enterprise {}", enterpriseId);
        return productCommandOutPutPort.deleteAllByEnterpriseId(enterpriseId);
    }


    
}
