package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query;

import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;

/**
 * @brief Output port for Product query operations
 * 
 * Provides interface for retrieving product information from the persistence layer.
 */
public interface IProductQueryOutputPort {

    /**
     * @brief Retrieves a product by its ID
     * @param productId Product identifier
     * @return Optional containing the product if found
     */
    Optional<Product> getProductByProductId(Long productId);

    /**
     * @brief Retrieves all products for a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of products
     */
    List<Product> findAll(String enterpriseId);
}
