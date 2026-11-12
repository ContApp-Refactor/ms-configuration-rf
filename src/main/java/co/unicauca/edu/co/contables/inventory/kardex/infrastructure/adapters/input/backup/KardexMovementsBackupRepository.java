package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

/**
 * @brief Consultas exclusivas de la copia de seguridad para los movimientos del kardex de
 * promedio ponderado. La tabla {@code kardex} no tiene columna de empresa: la empresa se resuelve por
 * las réplicas {@code kardex_products} ({@code enterprise_id}). El filtro {@code @TenantId} aplica solo.
 */
public interface KardexMovementsBackupRepository extends JpaRepository<KardexEntity, Long> {

    @Query("""
            select k from WeightedAverageKardexEntity k
            where k.id > :lastId
              and k.productId in (select p.productId from WeightedAverageProductEntity p
                                  where p.enterpriseId = :enterpriseId)
            order by k.id asc
            """)
    List<KardexEntity> findBackupBatch(@Param("enterpriseId") String enterpriseId,
            @Param("lastId") long lastId, Pageable pageable);

    /**
     * Movimientos huérfanos del origen: productos de la empresa que tienen movimientos pero NO réplica en
     * {@code kardex_products}. Columnas: productId, reference, name, cantidad de movimientos. No viajan (no se
     * podrían restaurar) y el export los avisa.
     */
    @Query("""
            select p.id, p.reference, p.name, count(k)
            from InventoryProductEntity p, WeightedAverageKardexEntity k
            where k.productId = p.id and p.enterpriseId = :enterpriseId
              and p.id not in (select r.productId from WeightedAverageProductEntity r
                               where r.enterpriseId = :enterpriseId)
            group by p.id, p.reference, p.name
            order by p.id
            """)
    List<Object[]> findOrphanMovements(@Param("enterpriseId") String enterpriseId);

    @Query("""
            select count(k) from WeightedAverageKardexEntity k
            where k.productId in (select p.productId from WeightedAverageProductEntity p
                                  where p.enterpriseId = :enterpriseId)
            """)
    long countByEnterprise(@Param("enterpriseId") String enterpriseId);

    @Query("""
            select count(k) from WeightedAverageKardexEntity k
            where k.balanceQuantity < 0
              and k.productId in (select p.productId from WeightedAverageProductEntity p
                                  where p.enterpriseId = :enterpriseId)
            """)
    long countWithNegativeBalance(@Param("enterpriseId") String enterpriseId);

    /**
     * Restore en el lugar: borra (bulk) los movimientos de los productos de la empresa (por el producto
     * original o por su réplica kardex_products; la tabla no tiene columna de empresa).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from WeightedAverageKardexEntity k
            where k.productId in (select i.id from InventoryProductEntity i where i.enterpriseId = :enterpriseId)
               or k.productId in (select p.productId from WeightedAverageProductEntity p
                                  where p.enterpriseId = :enterpriseId)
            """)
    int deleteByEnterprise(@Param("enterpriseId") String enterpriseId);
}
