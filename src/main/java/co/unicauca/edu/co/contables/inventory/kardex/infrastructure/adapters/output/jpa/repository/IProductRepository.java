package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

@Repository("kardexProductRepository")
public interface IProductRepository extends JpaRepository<ProductEntity, Long> {

    // Find all products by enterprise ID
    Collection<ProductEntity> findAllByEnterpriseId(String enterpriseId);

    // Find all products by ID
    List<ProductEntity> findByProductIdIn(List<Long> list);

    // Return a list of product IDs
    @Query("SELECT p.productId FROM WeightedAverageProductEntity p WHERE p.productId IN :ids")
    List<Long> findProductsIdByProductIdIn(@Param("ids") List<Long> ids);

    ProductEntity getReferenceByProductId(Long productId);

    boolean existsByProductId(Long id);

    // Delete methods
    @Modifying
    int deleteByProductId(Long productId);
    
    @Modifying
    int deleteByEnterpriseId(String enterpriseId);

}
