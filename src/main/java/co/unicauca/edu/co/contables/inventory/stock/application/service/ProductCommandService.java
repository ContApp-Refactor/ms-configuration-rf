package co.unicauca.edu.co.contables.inventory.stock.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IProductClientPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.ISyncStateRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n.MessageKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for synchronizing products from external sources
 * 
 * Manages incremental product synchronization using timestamp-based
 * tracking to maintain data consistency across systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductCommandService implements IProductSyncCommandPort {

    private final IStockCommandRepositoryPort productCommandRepositoryPort;
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
        Optional<Instant> lastSync = syncStateRepository.findLastSyncFor(SYNC_TYPE_PRODUCTS, enterpriseId);

        if (lastSync.isEmpty()) {
            // If no previous sync exists, create a new sync state with the current time
            Instant oldDate = Instant.parse("2000-01-01T00:00:00Z");
            lastSync = createSyncStateIfNotExists(enterpriseId, oldDate); 
            log.info(messageService.getMessage(MessageKeys.ERROR_MISSING_RECORD, enterpriseId));
        }

        log.info("Last sync date for enterpriseId {}", enterpriseId);

        // 2. Moment before the call (this will be the new date if all goes well)
        Instant syncStartedAt = Instant.now();

        try {
            // 3. Call the API
            List<Stock> updatedProducts = productClient
                .findAllProductsByEnterpriseId(enterpriseId, lastSync.get());

            // 4. Process the received products
            processUpdatedProducts(updatedProducts, enterpriseId);

            // 5. Only if everything was successful, update the date
            updateSyncState(enterpriseId, syncStartedAt);

            log.info("Successfully completed synchronization for enterpriseId {}: {} products processed.", enterpriseId, updatedProducts.size());

            return "Successful synchronization: " + updatedProducts.size() + " products processed.";

        } catch (Exception e) {
            log.error("Error during synchronization for enterpriseId {}: {}", enterpriseId, e.getMessage(), e);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(500, 
                messageService.getMessage(MessageKeys.ERROR_GENERIC, "syncProductsByEnterpriseId", e.getMessage()));
            throw e;
        }
    }

    private void processUpdatedProducts(List<Stock> products, String enterpriseId) {
        try {
            if (products == null || products.isEmpty()) {
                log.info(" No products to process for enterpriseId {}", enterpriseId);
                return;
            }
            
            List<Stock> productList = getProductsFromDto(products, enterpriseId);

            // Save all products to the database
            productCommandRepositoryPort.saveAll(productList);
            log.info("Successfully completed processing for enterpriseId {}: {} products saved.", enterpriseId, productList.size());
        } catch (Exception e) {
            log.error("Error during processing for enterpriseId {}: {}", enterpriseId, e.getMessage(), e);
        }
    }

    private List<Stock> getProductsFromDto(List<Stock> products, String enterpriseId) {
        return products.stream().map(dto -> {
            Stock product = new Stock();
            product.setProductId(dto.getProductId());
            product.setName(dto.getName());
            product.setEnterpriseId(enterpriseId);
            product.setQuantity(dto.getQuantity());
            product.setPrice(dto.getPrice());
            product.setState(dto.isActive());
            return product;
        }).toList();
    }

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

     private Optional<Instant> createSyncStateIfNotExists(String enterpriseId, Instant syncDate) {
        SyncState newState = new SyncState();
        newState.setSyncType(SYNC_TYPE_PRODUCTS);
        newState.setEnterpriseId(enterpriseId);
        newState.setLastSyncDate(syncDate);
        syncStateRepository.save(newState);
        log.info("Created initial sync state for enterpriseId={} with date={}", enterpriseId, syncDate);
        return Optional.of(syncDate);
    }
    
}
