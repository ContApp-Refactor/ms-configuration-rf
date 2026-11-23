package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;


import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @brief Repositorio JPA para tipos de identificación con consultas avanzadas de búsqueda y paginación
 */
@Repository
public interface TypeIdRepository extends JpaRepository<TypeIdEntity,Long>{
       
    @Query("SELECT t FROM TypeIdEntity t WHERE t.tientId = :tientId")
    List<TypeIdEntity> findAllByTientId(@Param("tientId") String tientId);

    @Query("SELECT COUNT(t) > 0 FROM TypeIdEntity t WHERE LOWER(t.tiName) = LOWER(:tiName) AND t.tientId = :tientId")
    boolean existsByTiNameIgnoreCaseAndTientId(@Param("tiName") String tiName, @Param("tientId") String tientId);

    @Query("SELECT COUNT(t) > 0 FROM TypeIdEntity t WHERE t.tiId = :tiId AND t.tientId = :tientId")
    boolean existsByTiIdAndTientId(@Param("tiId") String tiId, @Param("tientId") String tientId);

    @Query("SELECT t FROM TypeIdEntity t WHERE t.tiId = :tiId")
    Optional<TypeIdEntity> findByTiId(@Param("tiId") String tiId);

    @Query("SELECT t FROM TypeIdEntity t WHERE t.tiId = :tiId AND t.tientId = :tientId")
    Optional<TypeIdEntity> findByTiIdAndTientId(@Param("tiId") String tiId, @Param("tientId") String tientId);

    @Query("SELECT t FROM TypeIdEntity t WHERE t.tientId = :tientId")
    Page<TypeIdEntity> findAllByTientIdPageable(@Param("tientId") String tientId, Pageable pageable);

    /**
     * @brief Busca tipos de identificación por empresa y término de búsqueda
     * @details Busca en: código (tiId) y nombre (tiName) con case insensitive
     * @param tientId ID de la empresa
     * @param search Término de búsqueda
     * @param pageable Paginación con ordenamiento
     * @return Página de tipos de identificación que coinciden con la búsqueda
     */
    @Query("SELECT t FROM TypeIdEntity t WHERE t.tientId = :tientId AND " +
           "(LOWER(t.tiId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.tiName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TypeIdEntity> findByTientIdAndSearch(@Param("tientId") String tientId, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TypeIdEntity t WHERE t.tientId = :tientId")
    long countByTientId(@Param("tientId") String tientId);

    /**
     * @brief Cuenta tipos de identificación por empresa y término de búsqueda
     * @param tientId ID de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de identificación que coinciden
     */
    @Query("SELECT COUNT(t) FROM TypeIdEntity t WHERE t.tientId = :tientId AND " +
           "(LOWER(t.tiId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.tiName) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countByTientIdAndSearch(@Param("tientId") String tientId, @Param("search") String search);

    /**
     * @brief Cuenta tipos de identificación activos por empresa y término de búsqueda
     * @param tientId ID de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de identificación activos que coinciden
     */
    @Query("SELECT COUNT(t) FROM TypeIdEntity t WHERE t.tientId = :tientId AND t.status = true AND " +
           "(LOWER(t.tiId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.tiName) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countActiveByTientIdAndSearch(@Param("tientId") String tientId, @Param("search") String search);

    @Query("SELECT t FROM TypeIdEntity t WHERE t.tientId = :tientId AND t.status = true")
    Page<TypeIdEntity> findActiveByTientIdPageable(@Param("tientId") String tientId, Pageable pageable);

    /**
     * @brief Busca tipos de identificación activos por empresa y término de búsqueda
     * @details Busca en: código (tiId) y nombre (tiName) con case insensitive, solo registros activos
     * @param tientId ID de la empresa
     * @param search Término de búsqueda
     * @param pageable Paginación con ordenamiento
     * @return Página de tipos de identificación activos que coinciden con la búsqueda
     */
    @Query("SELECT t FROM TypeIdEntity t WHERE t.tientId = :tientId AND t.status = true AND " +
           "(LOWER(t.tiId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.tiName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TypeIdEntity> findActiveByTientIdAndSearch(@Param("tientId") String tientId, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TypeIdEntity t WHERE t.tientId = :tientId AND t.status = true")
    long countActiveByTientId(@Param("tientId") String tientId);
}
