package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external;

/**
 * @brief Output port for publishing product events
 * 
 * Provides interface for notifying external systems about
 * product usage or inventory changes.
 */
public interface IProductEventPort {
    /**
     * @brief Publishes an event indicating product usage
     * @param productId Product identifier
     * @param quantityUsed Quantity of product used
     */
    void publishUsedProductEvent(Long productId, Integer quantityUsed);

}
