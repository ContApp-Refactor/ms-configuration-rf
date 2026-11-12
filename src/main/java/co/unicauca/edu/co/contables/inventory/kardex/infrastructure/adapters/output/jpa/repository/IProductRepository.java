package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository;

import java.util.Collection;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    // Copia de seguridad: conteo y lectura por lotes de las réplicas de la empresa
    long countByEnterpriseId(String enterpriseId);

    Slice<ProductEntity> findByEnterpriseId(String enterpriseId, Pageable pageable);

    // Copia de seguridad: réplicas cuyo producto no existe en la empresa
    @Query("SELECT p.productId FROM WeightedAverageProductEntity p WHERE p.enterpriseId = :enterpriseId "
            + "AND p.productId NOT IN (SELECT i.id FROM InventoryProductEntity i WHERE i.enterpriseId = :enterpriseId)")
    List<Long> findProductIdsWithoutProduct(@Param("enterpriseId") String enterpriseId, Pageable pageable);

    /**
     * Referencias que YA existen en kardex_products, de cualquier empresa y de cualquier tenant.
     * Es SQL nativo a propósito: {@code @TenantId} no filtra consultas nativas y la restricción
     * UNIQUE de {@code reference} es global en la base (no por tenant).
     */
    @Query(value = "SELECT reference FROM kardex_products WHERE reference IN (:references)", nativeQuery = true)
    List<String> findExistingReferences(@Param("references") Collection<String> references);

    /**
     * Igual que {@link #findExistingReferences} pero ignora las réplicas de la empresa indicada (restore en el
     * lugar: esas filas se borran antes de restaurar, así que no pueden chocar).
     */
    @Query(value = "SELECT reference FROM kardex_products WHERE reference IN (:references) "
            + "AND (enterprise_id IS NULL OR enterprise_id <> :enterpriseId)", nativeQuery = true)
    List<String> findExistingReferencesExcludingEnterprise(@Param("references") Collection<String> references,
            @Param("enterpriseId") String enterpriseId);

    /** Restore en el lugar: borra (bulk) las réplicas kardex_products de la empresa. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WeightedAverageProductEntity p WHERE p.enterpriseId = :enterpriseId")
    int deleteAllByEnterpriseIdBulk(@Param("enterpriseId") String enterpriseId);

}
