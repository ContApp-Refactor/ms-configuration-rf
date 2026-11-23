package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external;

import java.time.Instant;
import java.util.List;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;

/**
 * @brief Output port for product service client
 * 
 * Provides interface for retrieving product information from
 * external product management services.
 */
public interface IProductClientPort {
    /**
     * @brief Retrieves all products for an enterprise modified since a given timestamp
     * @param enterpriseId Enterprise identifier
     * @param since Timestamp to filter modified products
     * @return List of products
     */
    List<Product> findAllProductsByEnterpriseId(String enterpriseId, Instant since);
}
