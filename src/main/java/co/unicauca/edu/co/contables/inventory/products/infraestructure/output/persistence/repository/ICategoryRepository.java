package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @brief Repositorio JPA para operaciones de persistencia de categorías
 *
 * Proporciona métodos de consulta con soporte para multitenancy por empresa,
 * incluyendo búsquedas filtradas y paginadas en categorías.
 */
public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {
    
    List<CategoryEntity> findByEnterpriseId(String enterpriseId);
    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);
    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);
    Optional<CategoryEntity> findByIdAndEnterpriseId(Long id, String enterpriseId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId")
    Page<CategoryEntity> getCategoriesBy(String enterpriseId, Pageable page);

    /**
     * @brief Obtiene categorías filtradas por estado
     * @param enterpriseId ID de la empresa
     * @param state estado de filtrado (activo/inactivo)
     * @param page configuración de paginación
     * @return página de categorías filtradas
     */
    @Query("SELECT c FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId AND c.state = :state")
    Page<CategoryEntity> getCategoriesByEnterpriseIdAndState(@Param("enterpriseId") String enterpriseId, @Param("state") Boolean state, Pageable page);

    /**
     * @brief Busca categorías por término de búsqueda en nombres y descripciones
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @param page configuración de paginación
     * @return página de categorías que coinciden con la búsqueda
     */
    @Query("SELECT c FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CategoryEntity> findByEnterpriseIdAndSearch(@Param("enterpriseId") String enterpriseId, @Param("search") String search, Pageable page);

    /**
     * @brief Cuenta categorías que coinciden con búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de categorías que coinciden
     */
    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countByEnterpriseIdAndSearch(@Param("enterpriseId") String enterpriseId, @Param("search") String search);

    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId")
    long countByEnterpriseId(@Param("enterpriseId") String enterpriseId);


    @Query("SELECT c FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId AND c.state = true")
    Page<CategoryEntity> getActiveCategoriesBy(@Param("enterpriseId") String enterpriseId, Pageable page);

    @Query("SELECT COUNT(c) FROM CategoryEntity c WHERE c.enterpriseId = :enterpriseId AND c.state = true")
    long countActiveByEnterpriseId(@Param("enterpriseId") String enterpriseId);

}
