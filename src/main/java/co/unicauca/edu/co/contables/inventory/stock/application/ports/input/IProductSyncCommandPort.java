package co.unicauca.edu.co.contables.inventory.stock.application.ports.input;

/**
 * @brief Input port for product synchronization operations
 * 
 * Handles synchronization of product data from external sources
 * to maintain data consistency across systems.
 * 
 * Important: Products is equal to Stock in this context.
 */
public interface IProductSyncCommandPort {
    /**
     * @brief Synchronizes products for a specific enterprise
     * @param enterpriseId Enterprise identifier to sync products for
     * @return Status message indicating sync result
     */
    String syncProductsByEnterpriseId(String enterpriseId);
}
