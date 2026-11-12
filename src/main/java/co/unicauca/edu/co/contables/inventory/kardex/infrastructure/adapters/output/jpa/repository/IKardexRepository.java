package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository;

import java.time.ZonedDateTime;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Repository("kardexKardexRepository")
public interface IKardexRepository extends JpaRepository<KardexEntity, Long> {

    // Find the latest Kardex entry by product ID
    KardexEntity findTopByProductIdOrderByDateDesc(Long productId);

    // Find the latest Kardex entry of a product restricted to the given movement types (ties: highest id)
    Optional<KardexEntity> findTopByProductIdAndTypeInOrderByDateDescIdDesc(Long productId, Collection<MovementType> types);

    // Latest Kardex row of a product with balance unit price greater than the given value (null is excluded)
    Optional<KardexEntity> findTopByProductIdAndBalanceUnitPriceGreaterThanOrderByDateDescIdDesc(Long productId, BigDecimal min);

    // Find all Kardex entries by product ID
    Page<KardexEntity> findByProductId(Long productId, Pageable pageable);

    // Find all Kardex entries by product ID and date range
    Page<KardexEntity> findByProductIdAndDateBetween(
        Long productId,
        ZonedDateTime startDate,
        ZonedDateTime endDate,
        Pageable pageable
    );

    // Find all Kardex entries by fact code, product ID and movement type
    List<KardexEntity> findByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type);

    // Check if exists by fact code, product ID and movement type
    boolean existsByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type);

    // Find last kardex for all products of an enterprise
    @Query("SELECT k FROM WeightedAverageKardexEntity k " +
           "INNER JOIN WeightedAverageProductEntity p ON k.productId = p.productId " +
           "WHERE p.enterpriseId = :enterpriseId " +
           "AND k.id IN (SELECT MAX(k2.id) FROM WeightedAverageKardexEntity k2 GROUP BY k2.productId)")
    List<KardexEntity> findLastKardexForAllProductsByEnterpriseId(String enterpriseId);

}
