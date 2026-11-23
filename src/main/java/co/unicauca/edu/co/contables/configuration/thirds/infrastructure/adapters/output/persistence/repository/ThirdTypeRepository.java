package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @brief Repositorio JPA para tipos de tercero con consultas avanzadas de búsqueda y paginación
 */
@Repository
public interface ThirdTypeRepository extends JpaRepository<ThirdTypeEntity,Long>{
       
    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttName LIKE %:name%")
    List<ThirdTypeEntity> findByName(String name);

    List<ThirdTypeEntity> findByTtNameContaining(String name);

    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId")
    List<ThirdTypeEntity> findAllByTtentId(@Param("ttentId") String ttentId);

    @Query("SELECT COUNT(tt) > 0 FROM ThirdTypeEntity tt WHERE LOWER(tt.ttName) = LOWER(:ttName) AND tt.ttentId = :ttentId")
    boolean existsByTtNameIgnoreCaseAndTtentId(@Param("ttName") String ttName, @Param("ttentId") String ttentId);

    @Query("SELECT COUNT(tt) > 0 FROM ThirdTypeEntity tt WHERE tt.ttName = :ttName AND tt.ttentId = :ttentId")
    boolean existsByTtNameAndTtentId(@Param("ttName") String ttName, @Param("ttentId") String ttentId);

    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttId = :ttId AND tt.ttentId = :ttentId")
    Optional<ThirdTypeEntity> findByTtIdAndTtentId(@Param("ttId") Long ttId, @Param("ttentId") String ttentId);

    void deleteByTtentId(String ttentId);

    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId")
    Page<ThirdTypeEntity> findAllByTtentIdPageable(@Param("ttentId") String ttentId, Pageable pageable);

    /**
     * @brief Busca tipos de tercero por empresa y término de búsqueda
     * @details Busca en: nombre (ttName) con case insensitive
     * @param ttentId ID de la empresa
     * @param search Término de búsqueda
     * @param pageable Paginación con ordenamiento
     * @return Página de tipos de tercero que coinciden con la búsqueda
     */
    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId AND " +
           "LOWER(tt.ttName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ThirdTypeEntity> findByTtentIdAndSearch(@Param("ttentId") String ttentId, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(tt) FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId")
    long countByTtentId(@Param("ttentId") String ttentId);

    /**
     * @brief Cuenta tipos de tercero por empresa y término de búsqueda
     * @param ttentId ID de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de tercero que coinciden
     */
    @Query("SELECT COUNT(tt) FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId AND " +
           "LOWER(tt.ttName) LIKE LOWER(CONCAT('%', :search, '%'))")
    long countByTtentIdAndSearch(@Param("ttentId") String ttentId, @Param("search") String search);

    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId AND tt.status = true")
    Page<ThirdTypeEntity> findActiveByTtentIdPageable(@Param("ttentId") String ttentId, Pageable pageable);

    /**
     * @brief Busca tipos de tercero activos por empresa y término de búsqueda
     * @details Busca en: nombre (ttName) con case insensitive, solo registros activos
     * @param ttentId ID de la empresa
     * @param search Término de búsqueda
     * @param pageable Paginación con ordenamiento
     * @return Página de tipos de tercero activos que coinciden con la búsqueda
     */
    @Query("SELECT tt FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId AND tt.status = true AND " +
           "LOWER(tt.ttName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ThirdTypeEntity> findActiveByTtentIdAndSearch(@Param("ttentId") String ttentId, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(tt) FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId AND tt.status = true")
    long countActiveByTtentId(@Param("ttentId") String ttentId);

    /**
     * @brief Cuenta tipos de tercero activos por empresa y término de búsqueda
     * @param ttentId ID de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de tercero activos que coinciden
     */
    @Query("SELECT COUNT(tt) FROM ThirdTypeEntity tt WHERE tt.ttentId = :ttentId AND tt.status = true AND " +
           "LOWER(tt.ttName) LIKE LOWER(CONCAT('%', :search, '%'))")
    long countActiveByTtentIdAndSearch(@Param("ttentId") String ttentId, @Param("search") String search);
}
