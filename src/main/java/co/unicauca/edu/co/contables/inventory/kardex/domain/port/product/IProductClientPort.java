package co.unicauca.edu.co.contables.inventory.kardex.domain.port.product;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

/**
 * @brief Output port for external product system integration
 * 
 * Provides interface for retrieving product information
 * from external product management systems.
 */
public interface IProductClientPort {
    /**
     * @brief Retrieves products updated since a specific timestamp
     * @param enterpriseId Enterprise identifier
     * @param since Timestamp for incremental sync
     * @return List of products updated since the given timestamp
     */
    List<Product> findAllProductsByEnterpriseId(String enterpriseId, Instant since);
}
