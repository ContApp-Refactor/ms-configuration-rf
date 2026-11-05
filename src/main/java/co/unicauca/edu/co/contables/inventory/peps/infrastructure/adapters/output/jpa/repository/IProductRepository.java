package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;


/**
 * @brief Repository for Product entities
 * 
 * Handles database operations for product management, including
 * retrieval by ID, enterprise, and batch operations.
 */
@Repository("pepsProductRepository")
public interface IProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * @brief Finds a product by its business ID
     * @param productId The product business ID
     * @return Optional containing the product if found
     */
    Optional<ProductEntity> findByProductId(Long productId);

    /**
     * @brief Finds existing product IDs from a list of candidates
     * @param ids List of product IDs to check
     * @return List of IDs that exist in the database
     */
    @Query("SELECT p.productId FROM PepsProductEntity p WHERE p.productId IN :ids")
    List<Long> findProductsIdByProductIdIn(@Param("ids") List<Long> ids);

    /**
     * @brief Finds all products matching a list of IDs
     * @param list List of product IDs
     * @return List of matching product entities
     */
    List<ProductEntity> findByProductIdIn(List<Long> list);


    /**
     * @brief Finds all products for a specific enterprise
     * @param enterpriseId The enterprise ID
     * @return Collection of products
     */
    Collection<ProductEntity> findAllByEnterpriseId(String enterpriseId);



    /**
     * @brief Deletes all products for an enterprise
     * @param enterpriseId The enterprise ID
     * @return Number of deleted records
     */
    @Modifying
    int deleteByEnterpriseId(String enterpriseId);


    /**
     * @brief Deletes a product by its business ID
     * @param productId The product business ID
     * @return Number of deleted records
     */
    @Modifying
    int deleteByProductId(Long productId);


    /**
     * @brief Checks if a product exists by its business ID
     * @param id The product business ID
     * @return True if exists, false otherwise
     */
    boolean existsByProductId(Long id);

    /**
     * @brief Gets a reference to a product entity by its business ID
     * @param productId The product business ID
     * @return The product entity reference
     */
    ProductEntity getReferenceByProductId(Long productId);
    
}
