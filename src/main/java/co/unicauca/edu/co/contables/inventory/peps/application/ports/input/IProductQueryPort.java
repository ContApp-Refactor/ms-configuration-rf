package co.unicauca.edu.co.contables.inventory.peps.application.ports.input;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;

/**
 * @brief Input port for Product query operations
 * 
 * Provides query capabilities for retrieving product information
 * filtered by enterprise.
 */
public interface IProductQueryPort {
    /**
     * @brief Retrieves all products associated with a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of products belonging to the enterprise
     */
    List<Product> findAll(String enterpriseId);
}
