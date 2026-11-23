package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.adapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockStatusRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.mapper.IStockEntityCommandMapper;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.mapper.StockEntityMapper;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.record.StockPartition;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockCommandAdapter implements IStockCommandRepositoryPort, IStockStatusRepositoryPort {

    private final StockRepository stockRepository;
    private final StockEntityMapper stockEntityMapper;
    private final IStockEntityCommandMapper productEntityCommandMapper;

    @Override
    public Stock save(Stock stock) {
        boolean exists = stockRepository.existsByProductId(stock.getProductId());
        if (exists) {
            throw new IllegalArgumentException("Stock with productId " + stock.getProductId() + " already exists.");
        }
        StockEntity stockEntity = stockEntityMapper.toEntity(stock);
        StockEntity savedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Stock update(Long productId, String name) {
        StockEntity stockEntity = stockRepository.findByProductId(productId);
        if (stockEntity == null) {
            throw new IllegalArgumentException("Stock with productId " + productId + " not found.");
        }
        stockEntity.setName(name);
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }

    @Override
    public void inactivate(Long productId) {
        StockEntity stock = stockRepository.findByProductId(productId);
        if(stock.isState() == true) {
            stock.setState(false);
            stockRepository.save(stock);
        } 
    }

    @Override
    public void activate(Long productId) {
        StockEntity stock = stockRepository.findByProductId(productId);
        if(stock.isState() == false) {
            stock.setState(true);
            stockRepository.save(stock);
        }
    }

    @Override
    public Stock registerSale(Stock stock) {
        StockEntity stockEntity = stockRepository.findByProductId(stock.getProductId());
        stockEntity.setQuantity(stock.getQuantity());
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }


    @Override
    public Stock registerPurchase(Stock stock) {
        StockEntity stockEntity = stockRepository.findByProductId(stock.getProductId());
        stockEntity.setQuantity(stock.getQuantity());
        stockEntity.setPrice(stock.getPrice());
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }

    @Override
    public String saveAll(List<Stock> stocks) {
        try {
            if (stocks.isEmpty()) {
                return "No products to process.";
            }

            // 1. Separate new products from existing ones
            StockPartition partition = separateNewAndExistingProducts(stocks);

            // 2. Process both types of products
            int newCount = saveNewProducts(partition.newProducts());
            int updatedCount = updateExistingProducts(partition.existingProducts());

            // 3. Return result
            String message = String.format("Products processed: %d new, %d updated", newCount, updatedCount);
            log.info(message);
            return message;

        } catch (Exception e) {
            log.error("Error processing products", e);
            return "Error: " + e.getMessage();
        }
    }

     /**
     * @brief Separates products into new and existing based on their IDs
     * @param products List of products to partition
     * @return StockPartition with separated new and existing products
     */
    private StockPartition separateNewAndExistingProducts(List<Stock> products) {
        List<Long> productIds = products.stream().map(Stock::getProductId).toList();
        Set<Long> existingIds = new HashSet<>(stockRepository.findProductsIdByProductIdIn(productIds));

        Map<Boolean, List<Stock>> partitionedProducts = products.stream()
            .collect(Collectors.partitioningBy(p -> existingIds.contains(p.getProductId())));

        return new StockPartition(
            partitionedProducts.get(false), 
            partitionedProducts.get(true)   
        );
    }

    /**
     * @brief Saves new products to database
     * @param newProducts List of new products to save
     * @return Number of products saved
     */
    private int saveNewProducts(List<Stock> newProducts) {
        if (newProducts.isEmpty()) {
            return 0;
        }

        

        List<StockEntity> newEntities = productEntityCommandMapper.toEntity(newProducts);
        stockRepository.saveAll(newEntities);
        
        log.info("Saved {} new products", newProducts.size());
        return newProducts.size();
    }

    /**
     * @brief Updates existing products in database
     * @param existingProducts List of existing products to update
     * @return Number of products updated
     */
    private int updateExistingProducts(List<Stock> existingProducts) {
        if (existingProducts.isEmpty()) {
            return 0;
        }

        List<Long> existingIds = existingProducts.stream().map(Stock::getProductId).toList();
        List<StockEntity> existingEntities = stockRepository.findByProductIdIn(existingIds);

        Map<Long, Stock> updateMap = existingProducts.stream()
            .collect(Collectors.toMap(Stock::getProductId, Function.identity()));

        existingEntities.forEach(entity -> {
            Stock stock = updateMap.get(entity.getProductId());
            if (stock != null) {
                productEntityCommandMapper.updateEntityFromProduct(stock, entity);
            }     
            
        });
        
        stockRepository.saveAll(existingEntities);
        log.info("Updated {} existing products", existingProducts.size());
        return existingProducts.size();
    }

    @Override
    public String deleteAllByEnterpriseId(String enterpriseId) {
        try {
            long count = stockRepository.countByEnterpriseId(enterpriseId);
            if (count == 0) {
                log.info("No stock records found to delete for enterprise: {}", enterpriseId);
                return String.format("No stock records found to delete for enterprise: %s", enterpriseId);
            }
            
            stockRepository.deleteAllByEnterpriseId(enterpriseId);
            log.info("Successfully deleted {} stock records for enterprise: {}", count, enterpriseId);
            return String.format("Successfully deleted %d stock records for enterprise: %s", count, enterpriseId);
            
        } catch (Exception e) {
            log.error("Error deleting stock records for enterprise: {}", enterpriseId, e);
            return String.format("Error deleting stock records for enterprise %s: %s", enterpriseId, e.getMessage());
        }
    }

    @Override
    public int deleteByProductId(Long productId) {
        return stockRepository.deleteByProductId(productId);
    }


    
}
