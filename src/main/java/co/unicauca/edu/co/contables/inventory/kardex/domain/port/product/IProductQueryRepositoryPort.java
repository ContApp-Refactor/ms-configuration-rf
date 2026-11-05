package co.unicauca.edu.co.contables.inventory.kardex.domain.port.product;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

/**
 * @brief Output port for Product read operations
 * 
 * Defines contract for querying product data with
 * enterprise-scoped access and existence validation.
 */
public interface IProductQueryRepositoryPort {
    /**
     * @brief Retrieves all products for a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of products belonging to the enterprise
     */
    List<Product> findAll(String enterpriseId);
    
    /**
     * @brief Checks if a product exists by its ID
     * @param id Product identifier
     * @return True if product exists, false otherwise
     */
    boolean existsByProductId(Long id);
}
