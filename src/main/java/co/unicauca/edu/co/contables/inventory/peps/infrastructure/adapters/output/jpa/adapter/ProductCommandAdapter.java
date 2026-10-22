package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import java.util.function.Function;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IProductCommandOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IProductEntityMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.record.ProductPartition;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @brief JPA adapter for Product command operations
 * 
 * Handles product creation and update operations with batch processing
 * capabilities for efficient synchronization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCommandAdapter implements IProductCommandOutPutPort {

    private final IProductRepository productRepository;
    private final IProductEntityMapper productEntityMapper;


    /**
     * @brief Saves or updates multiple products in batch
     * @param products List of products to process
     * @return Result message indicating processing outcome
     */
    @Override
    @Transactional
    public String saveAll(List<Product> products) {
        
        try{

            if (products.isEmpty()) {
                return "No products to process.";
            }
             // 1. Separate new products from existing ones
            ProductPartition partition = separateNewAndExistingProducts(products);

            // 2. Process both types of products
            int newCount = saveNewProducts(partition.newProducts());
            int updatedCount = updateExistingProducts(partition.existingProducts());

             // 3. Return result
            String message = String.format("Products processed: %d new, %d updated", newCount, updatedCount);
            log.info(message);
            return message;

        }catch (Exception e) {
            log.error("Error processing products", e);
            return "Error: " + e.getMessage();
        }
    }

     /**
     * @brief Saves a single product
     * @param product The product to save
     * @return Result message
     */
    @Override
    public String save(Product product) {
        try {
            ProductEntity productEntity = productEntityMapper.toEntity(product);
            productRepository.save(productEntity);
            return "Product saved successfully.";
        } catch (Exception e) {
            return "An error occurred while saving the product: " + e.getMessage();
        }
    }
    /**
     * @brief Updates an existing product
     * @param product The product to update
     * @return Result message
     */
    @Override
    @Transactional
    public String update(Product product) {
        try {
            product.validateRequiredFields();
             // Verificar si el producto existe
            if (!productRepository.existsByProductId(product.getProductId())) {
                log.warn("Product with ID {} not found for update", product.getProductId());
                return "Product not found for update.";
            }

             // Obtener la entidad existente
            ProductEntity existingEntity = productRepository.getReferenceByProductId(product.getProductId());
            
            // Normalize before update
            product.normalize();
            
            // Actualizar la entidad con los nuevos datos
            productEntityMapper.updateEntityFromProduct(product, existingEntity);
            
            // Guardar los cambios
            productRepository.save(existingEntity);
            
            log.info("Product with ID {} updated successfully", product.getProductId());
            return "Product updated successfully.";

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating product: {}", e.getMessage());
            return "Validation error: " + e.getMessage();
        }catch(Exception e){
            log.error("Error updating product with ID {}: {}", product.getProductId(), e.getMessage());
            return "An error occurred while updating the product: " + e.getMessage();

        }
    }


    /**
     * @brief Deletes all products for a specific enterprise
     * @param enterpriseId Enterprise ID
     * @return Result message
     */
    @Override
    @Transactional
    public String delete(Long productId) {
        try {
            int deletedCount = productRepository.deleteByProductId(productId);
            if (deletedCount > 0) {
                log.info("Product with ID {} deleted successfully", productId);
                return "Product deleted successfully.";
            } else {
                log.warn("Product with ID {} not found", productId);
                 return "Product not found.";
            }
                
        } catch (Exception e) {
            log.error("Error deleting product with ID {}: {}", productId, e.getMessage());
            return "An error occurred while deleting the product: " + e.getMessage();
        }
    }


    /**
     * @brief Deletes all products for a specific enterprise
     * @param enterpriseId Enterprise ID
     * @return Result message
     */
    @Override
    @Transactional
    public String deleteAllByEnterpriseId(String enterpriseId) {
        try {
            int deletedCount = productRepository.deleteByEnterpriseId(enterpriseId);
            log.info("Deleted {} products for enterprise {}", deletedCount, enterpriseId);
            return String.format("Deleted %d products successfully.", deletedCount);
        } catch (Exception e) {
            log.error("Error deleting all products for enterprise {}: {}", enterpriseId, e.getMessage());
            return "An error occurred while deleting products: " + e.getMessage();
        }

    }

    /**
     * @brief Separates products into new and existing based on their IDs
     * @param products List of products to partition
     * @return ProductPartition with separated new and existing products
     */
    private ProductPartition separateNewAndExistingProducts(List<Product> products) {
        List<Long> productIds = products.stream().map(Product::getProductId).toList();
        Set<Long> existingIds = new HashSet<>(productRepository.findProductsIdByProductIdIn(productIds));
        

        Map<Boolean, List<Product>> partitionedProducts = products.stream()
            .collect(Collectors.partitioningBy(p -> existingIds.contains(p.getProductId())));
    
        return new ProductPartition(
            partitionedProducts.get(false), 
            partitionedProducts.get(true)   
        );
    }

    /**
     * @brief Saves new products to database
     * @param newProducts List of new products to save
     * @return Number of products saved
     */
    private int saveNewProducts(List<Product> newProducts) {
        if (newProducts.isEmpty()) {
            return 0;
        } 

         List<ProductEntity> newEntities = productEntityMapper.toEntity(newProducts);
        productRepository.saveAll(newEntities);
        
        log.info("Saved {} new products", newProducts.size());
        return newProducts.size();

    }

     /**
     * @brief Updates existing products in database
     * @param existingProducts List of existing products to update
     * @return Number of products updated
     */
    private int updateExistingProducts(List<Product> existingProducts) {
        if (existingProducts.isEmpty()) {
            return 0;
        }

        List<Long> existingIds = existingProducts.stream().map(Product::getProductId).toList();
        List<ProductEntity> existingEntities = productRepository.findByProductIdIn(existingIds);

        Map<Long, Product> updateMap = existingProducts.stream()
            .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        
        existingEntities.forEach(entity -> {
            Product product = updateMap.get(entity.getProductId());
            if (product != null) {
                productEntityMapper.updateEntityFromProduct(product, entity);
            }     
            
        });

        productRepository.saveAll(existingEntities);
        log.info("Updated {} existing products", existingProducts.size());
        return existingProducts.size();
    }

   
    
}
