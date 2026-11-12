package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;

/**
 * @brief Consultas exclusivas de la copia de seguridad para las capas PEPS ({@code peps_kardex}) y su detalle
 * de salidas ({@code DetailOutputFIFO}). Ninguna de las dos tablas tiene empresa ni {@code @TenantId}: la
 * empresa se resuelve por la réplica {@code peps_product.enterprise_id}. Las lecturas son proyecciones
 * ({@code Object[]}) para no arrastrar los grafos JPA de las entidades.
 */
public interface PepsMovementsBackupRepository extends JpaRepository<KardexEntity, Long> {

    /** Columnas: idKardex, factCode, date, details, quantity, unitPrice, type, availableQuantity, productId. */
    @Query("""
            select k.idKardex, k.factCode, k.date, k.details, k.quantity, k.unitPrice, k.type,
                   k.availableQuantity, p.productId
            from KardexEntity k join k.product p
            where p.enterpriseId = :enterpriseId and k.idKardex > :lastId
            order by k.idKardex asc
            """)
    List<Object[]> findKardexBatch(@Param("enterpriseId") String enterpriseId,
            @Param("lastId") long lastId, Pageable pageable);

    /** Columnas: idDetailOutput, quantityUsed, unitPrice, idVenta, idOrigen (nullable). */
    @Query("""
            select d.idDetailOutput, d.quantityUsed, d.unitPrice, s.idKardex, o.idKardex
            from DetailOutputEntity d
              join d.movementSale s join s.product p
              left join d.movementOrigin o
            where p.enterpriseId = :enterpriseId and d.idDetailOutput > :lastId
            order by d.idDetailOutput asc
            """)
    List<Object[]> findDetailBatch(@Param("enterpriseId") String enterpriseId,
            @Param("lastId") long lastId, Pageable pageable);

    @Query("select count(k) from KardexEntity k where k.product.enterpriseId = :enterpriseId")
    long countKardex(@Param("enterpriseId") String enterpriseId);

    @Query("select count(d) from DetailOutputEntity d where d.movementSale.product.enterpriseId = :enterpriseId")
    long countDetails(@Param("enterpriseId") String enterpriseId);

    @Query("""
            select count(k) from KardexEntity k
            where k.product.enterpriseId = :enterpriseId and k.availableQuantity < 0
            """)
    long countNegativeAvailable(@Param("enterpriseId") String enterpriseId);

    /** Salidas sin capa de origen o cuya capa pertenece a otro producto que la venta. */
    @Query("""
            select count(d) from DetailOutputEntity d
              join d.movementSale s join s.product sp
              left join d.movementOrigin o left join o.product op
            where sp.enterpriseId = :enterpriseId
              and (o is null or op.productId <> sp.productId)
            """)
    long countDetailsWithBadOrigin(@Param("enterpriseId") String enterpriseId);

    /**
     * Restore en el lugar: borra (bulk) el detalle de salidas cuyas capas (venta u origen) son de la empresa.
     * Va ANTES que {@link #deleteLayersByEnterprise} (FK de DetailOutputFIFO a peps_kardex).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from DetailOutputEntity d
            where d.movementSale.idKardex in (select k.idKardex from KardexEntity k join k.product p
                                              where p.enterpriseId = :enterpriseId)
               or d.movementOrigin.idKardex in (select k2.idKardex from KardexEntity k2 join k2.product p2
                                                where p2.enterpriseId = :enterpriseId)
            """)
    int deleteDetailsByEnterprise(@Param("enterpriseId") String enterpriseId);

    /** Restore en el lugar: borra (bulk) las capas peps_kardex de las réplicas de la empresa. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from KardexEntity k
            where k.idKardex in (select k2.idKardex from KardexEntity k2 join k2.product p
                                 where p.enterpriseId = :enterpriseId)
            """)
    int deleteLayersByEnterprise(@Param("enterpriseId") String enterpriseId);
}
