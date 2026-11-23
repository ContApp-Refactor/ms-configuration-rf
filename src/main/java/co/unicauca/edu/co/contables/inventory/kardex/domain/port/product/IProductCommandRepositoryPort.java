package co.unicauca.edu.co.contables.inventory.kardex.domain.port.product;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

/**
 * @brief Output port for Product write operations
 * 
 * Defines contract for persisting product data with
 * batch processing capabilities for efficient synchronization.
 */
public interface IProductCommandRepositoryPort {
     /**
      * @brief Saves multiple products in batch operation
      * @param products List of products to save or update
      * @return Status message indicating operation result
      */
     String saveAll(List<Product> products);
     
     /**
      * @brief Saves a single product
      * @param product Product to save
      * @return Status message indicating operation result
      */
     String save(Product product);
     
     /**
      * @brief Updates an existing product
      * @param product Product to update
      * @return Status message indicating operation result
      */
     String update(Product product);
     
     /**
      * @brief Deletes a product by its ID
      * @param productId Product ID to delete
      * @return Status message indicating operation result
      */
     String delete(Long productId);
     
     /**
      * @brief Deletes all products for a specific enterprise
      * @param enterpriseId Enterprise ID
      * @return Status message indicating operation result
      */
     String deleteAllByEnterpriseId(String enterpriseId);
}
