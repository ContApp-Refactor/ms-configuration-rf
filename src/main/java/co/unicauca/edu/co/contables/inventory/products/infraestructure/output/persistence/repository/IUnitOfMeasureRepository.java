package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * @brief Repositorio JPA para operaciones de persistencia de unidades de medida
 *
 * Proporciona métodos de consulta con soporte para multitenancy por empresa,
 * incluyendo búsquedas filtradas y paginadas.
 */
public interface IUnitOfMeasureRepository extends JpaRepository<UnitOfMeasureEntity, Long> {
    
    Optional<UnitOfMeasureEntity> findByIdAndEnterpriseId(Long id, String enterpriseId);

    boolean existsByNameAndEnterpriseId(String name, String enterpriseId);

    boolean existsByAbbreviationAndEnterpriseId(String abbreviation, String enterpriseId);

    boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id);
    
    boolean existsByAbbreviationAndEnterpriseIdAndIdNot(String abbreviation, String enterpriseId, Long id);

    @Query("SELECT u FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId")
    Page<UnitOfMeasureEntity> getUnitOfMeasuresBy(String enterpriseId, Pageable page);

    /**
     * @brief Obtiene unidades de medida filtradas por estado
     * @param enterpriseId ID de la empresa
     * @param state estado de filtrado (activo/inactivo)
     * @param page configuración de paginación
     * @return página de unidades de medida filtradas
     */
    @Query("SELECT u FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId AND u.state = :state")
    Page<UnitOfMeasureEntity> getUnitOfMeasuresByEnterpriseIdAndState(@Param("enterpriseId") String enterpriseId, @Param("state") Boolean state, Pageable page);

    /**
     * @brief Busca unidades de medida por término de búsqueda en nombres y abreviaturas
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @param page configuración de paginación
     * @return página de unidades de medida que coinciden con la búsqueda
     */
    @Query("SELECT u FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.abbreviation) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UnitOfMeasureEntity> findByEnterpriseIdAndSearch(@Param("enterpriseId") String enterpriseId, @Param("search") String search, Pageable page);

    /**
     * @brief Cuenta unidades de medida que coinciden con búsqueda
     * @param enterpriseId ID de la empresa
     * @param search término de búsqueda
     * @return cantidad de unidades que coinciden
     */
    @Query("SELECT COUNT(u) FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.abbreviation) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countByEnterpriseIdAndSearch(@Param("enterpriseId") String enterpriseId, @Param("search") String search);

    @Query("SELECT COUNT(u) FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId")
    long countByEnterpriseId(@Param("enterpriseId") String enterpriseId);

    /**
     * @brief Obtiene unidades de medida activas de una empresa
     * @param enterpriseId ID de la empresa
     * @param page configuración de paginación
     * @return página de unidades de medida activas
     */
    @Query("SELECT u FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId AND u.state = true")
    Page<UnitOfMeasureEntity> getActiveUnitOfMeasuresBy(@Param("enterpriseId") String enterpriseId, Pageable page);

    /**
     * @brief Cuenta unidades de medida activas por empresa
     * @param enterpriseId ID de la empresa
     * @return cantidad total de unidades activas
     */
    @Query("SELECT COUNT(u) FROM UnitOfMeasureEntity u WHERE u.enterpriseId = :enterpriseId AND u.state = true")
    long countActiveByEnterpriseId(@Param("enterpriseId") String enterpriseId);

}
