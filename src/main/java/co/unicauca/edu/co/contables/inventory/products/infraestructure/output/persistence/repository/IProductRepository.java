package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @brief Repositorio JPA para operaciones de persistencia de productos
 *
 * Proporciona métodos de consulta con soporte para multitenancy por empresa,
 * incluyendo búsquedas filtradas y paginadas en productos.
 */
public interface IProductRepository extends JpaRepository<ProductEntity, Long> {
    
    List<ProductEntity> findByEnterpriseIdAndLastModifiedDateAfter(String enterpriseId, Instant lastSyncDate);
    
    Optional<ProductEntity> findByIdAndEnterpriseId(Long id, String enterpriseId);

    Page<ProductEntity> findByEnterpriseIdAndState(String enterpriseId, boolean state, Pageable pageable);

    long countByEnterpriseIdAndState(String enterpriseId, boolean state);

    List<ProductEntity> findByCategoryId(Long categoryId);

    List<ProductEntity> findByUnitOfMeasureId(Long unitOfMeasureId);

    List<ProductEntity> findByProductTypeId(Long productTypeId);

    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);

    boolean existsByReferenceAndEnterpriseId(String reference, String enterpriseId);

    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);

    boolean existsByReferenceAndEnterpriseIdAndIdNot(String reference, String enterpriseId, Long id);
    
    /**
     * @brief Busca productos por filtros de búsqueda en código, nombre y referencia
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda (opcional)
     * @param pageable configuración de paginación
     * @return página de productos que coinciden con los filtros
     */
    @Query("SELECT p FROM InventoryProductEntity p WHERE p.enterpriseId = :enterpriseId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductEntity> findByEnterpriseIdWithFilters(String enterpriseId, String search, Pageable pageable);
    
    /**
     * @brief Cuenta productos que coinciden con filtros de búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda (opcional)
     * @return cantidad de productos que coinciden con los filtros
     */
    @Query("SELECT COUNT(p) FROM InventoryProductEntity p WHERE p.enterpriseId = :enterpriseId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countByEnterpriseIdWithFilters(String enterpriseId, String search);
    
    long countByEnterpriseId(String enterpriseId);

    /**
     * @brief Página de productos de la empresa (copia de seguridad: lectura por lotes)
     * @param enterpriseId ID de la empresa
     * @param pageable página (ordenada por id para recorrer sin repetir)
     * @return porción de productos
     */
    Slice<ProductEntity> findByEnterpriseId(String enterpriseId, Pageable pageable);

    /**
     * @brief Ids de productos cuya categoría no existe en la empresa (copia de seguridad)
     */
    @Query("SELECT p.id FROM InventoryProductEntity p WHERE p.enterpriseId = :enterpriseId "
            + "AND p.categoryId IS NOT NULL AND p.categoryId NOT IN "
            + "(SELECT c.id FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId)")
    List<Long> findIdsWithMissingCategory(@Param("enterpriseId") String enterpriseId, Pageable pageable);

    /**
     * @brief Ids de productos cuya unidad de medida no existe en la empresa (copia de seguridad)
     */
    @Query("SELECT p.id FROM InventoryProductEntity p WHERE p.enterpriseId = :enterpriseId "
            + "AND p.unitOfMeasureId IS NOT NULL AND p.unitOfMeasureId NOT IN "
            + "(SELECT u.id FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId)")
    List<Long> findIdsWithMissingUnitOfMeasure(@Param("enterpriseId") String enterpriseId, Pageable pageable);

    /**
     * @brief Ids de productos cuyo tipo no existe en la empresa (copia de seguridad)
     */
    @Query("SELECT p.id FROM InventoryProductEntity p WHERE p.enterpriseId = :enterpriseId "
            + "AND p.productTypeId IS NOT NULL AND p.productTypeId NOT IN "
            + "(SELECT t.id FROM ProductTypeEntity t WHERE t.enterpriseId = :enterpriseId)")
    List<Long> findIdsWithMissingProductType(@Param("enterpriseId") String enterpriseId, Pageable pageable);

    /** Restore en el lugar: borra (bulk) los productos de la empresa. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM InventoryProductEntity p WHERE p.enterpriseId = :enterpriseId")
    int deleteAllByEnterpriseIdBulk(@Param("enterpriseId") String enterpriseId);

}
