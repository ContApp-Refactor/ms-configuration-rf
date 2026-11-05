package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

public interface StockRepository extends JpaRepository <StockEntity, Long> {

    StockEntity findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    // Find all products by enterprise ID
    Collection<StockEntity> findAllByEnterpriseId(String enterpriseId);

    // Find all products by ID
    List<StockEntity> findByProductIdIn(List<Long> list);

    // Return a list of product IDs
    @Query("SELECT p.productId FROM StockEntity p WHERE p.productId IN :ids")
    List<Long> findProductsIdByProductIdIn(@Param("ids") List<Long> ids);

    // Delete all products by enterprise ID
    @Modifying
    @Transactional
    void deleteAllByEnterpriseId(String enterpriseId);

    // Count products by enterprise ID
    long countByEnterpriseId(String enterpriseId);

    // Delete methods
    @Modifying
    int deleteByProductId(Long productId);

}
