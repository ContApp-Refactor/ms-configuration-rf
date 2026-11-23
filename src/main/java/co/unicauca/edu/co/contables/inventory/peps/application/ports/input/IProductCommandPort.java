package co.unicauca.edu.co.contables.inventory.peps.application.ports.input;

/**
 * @brief Input port for Product command operations
 * 
 * Provides command capabilities for managing product lifecycle
 * including deletion operations.
 */
public interface IProductCommandPort {
    /**
     * @brief Deletes a product by its ID
     * @param productId Product ID to delete
     * @return Status message indicating operation result
     */
    String deleteById(Long productId);
    
    /**
     * @brief Deletes all products for a specific enterprise
     * @param enterpriseId Enterprise ID
     * @return Status message indicating operation result
     */
    String deleteAllByEnterpriseId(String enterpriseId);
}
