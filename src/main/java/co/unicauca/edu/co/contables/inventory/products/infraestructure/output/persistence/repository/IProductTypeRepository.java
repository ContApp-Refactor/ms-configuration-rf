package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @brief Repositorio JPA para operaciones de persistencia de tipos de producto
 *
 * Proporciona métodos de consulta con soporte para multitenancy por empresa,
 * incluyendo búsquedas filtradas y paginadas en tipos de producto.
 */
@Repository
public interface IProductTypeRepository extends JpaRepository<ProductTypeEntity, Long> {
    Optional<ProductTypeEntity> findByIdAndEnterpriseId(Long id, String enterpriseId);

    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);
    
    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);

    @Query("SELECT p FROM ProductTypeEntity p WHERE p.enterpriseId = :enterpriseId")
    Page<ProductTypeEntity> getProductTypesBy(String enterpriseId, Pageable page);

    /**
     * @brief Busca tipos de producto por término de búsqueda en nombres y descripciones
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @param page configuración de paginación
     * @return página de tipos de producto que coinciden con la búsqueda
     */
    @Query("SELECT p FROM ProductTypeEntity p WHERE p.enterpriseId = :enterpriseId AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductTypeEntity> findByEnterpriseIdAndSearch(@Param("enterpriseId") String enterpriseId,
            @Param("search") String search, Pageable page);

    /**
     * @brief Cuenta tipos de producto que coinciden con búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de tipos de producto que coinciden
     */
    @Query("SELECT COUNT(p) FROM ProductTypeEntity p WHERE p.enterpriseId = :enterpriseId AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countByEnterpriseIdAndSearch(@Param("enterpriseId") String enterpriseId, @Param("search") String search);

    @Query("SELECT COUNT(p) FROM ProductTypeEntity p WHERE p.enterpriseId = :enterpriseId")
    long countByEnterpriseId(@Param("enterpriseId") String enterpriseId);

    /**
     * @brief Obtiene tipos de producto activos de una empresa
     * @param enterpriseId ID de la empresa
     * @param pageable configuración de paginación
     * @return página de tipos de producto activos
     */
    @Query("SELECT p FROM ProductTypeEntity p WHERE p.enterpriseId = :enterpriseId AND p.state = true")
    Page<ProductTypeEntity> findActivatedByEnterpriseId(@Param("enterpriseId") String enterpriseId, Pageable pageable);

    /**
     * @brief Cuenta tipos de producto activos por empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad total de tipos de producto activos
     */
    @Query("SELECT COUNT(p) FROM ProductTypeEntity p WHERE p.enterpriseId = :enterpriseId AND p.state = true")
    long countActivatedByEnterpriseId(@Param("enterpriseId") String enterpriseId);
}
