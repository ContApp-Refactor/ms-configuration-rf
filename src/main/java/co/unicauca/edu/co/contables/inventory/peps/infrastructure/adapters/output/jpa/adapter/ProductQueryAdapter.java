package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IProductEntityMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
/**
 * @brief Adapter for Product query operations
 * 
 * Implements the output port for retrieving product information
 * from the database using JPA repository.
 */
public class ProductQueryAdapter implements IProductQueryOutputPort{

    private final IProductRepository productRepository;
    private final IProductEntityMapper productEntityMapper;

    /**
     * @brief Retrieves a product by its ID
     * @param productId The product ID
     * @return Optional containing the product if found
     */
    @Override
    public Optional<Product> getProductByProductId(Long productId) {
          return productRepository.findByProductId(productId)
                .map(productEntityMapper::toDomain); 

                
    }

    /**
     * @brief Retrieves all products for an enterprise
     * @param enterpriseId The enterprise ID
     * @return List of products
     */
    @Override
    public List<Product> findAll(String enterpriseId) {
         return productRepository.findAllByEnterpriseId(enterpriseId).stream()
                .map(productEntityMapper::toDomain)
                .toList(); 
    }
    
}
