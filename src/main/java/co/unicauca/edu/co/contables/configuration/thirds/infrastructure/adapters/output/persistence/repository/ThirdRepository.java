package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @brief Repositorio JPA principal para terceros con operaciones complejas de búsqueda y actualización masiva
 */
@Repository
public interface ThirdRepository extends JpaRepository<ThirdEntity, Long> {

    @Query("SELECT t FROM ThirdEntity t LEFT JOIN FETCH t.typeId WHERE t.entId = :entId")
    Page<ThirdEntity> getThirdsBy(String entId, Pageable page);

    @Query("SELECT t FROM ThirdEntity t LEFT JOIN FETCH t.typeId WHERE t.entId = :entId AND t.state = :state")
    Page<ThirdEntity> getThirdsByEntIdAndState(@Param("entId") String entId, @Param("state") Boolean state, Pageable page);

    /**
     * @brief Busca terceros por empresa y término de búsqueda
     * @details Busca en: nombres, apellidos, razón social, número de identificación con case insensitive
     * @param entId ID de la empresa
     * @param search Término de búsqueda
     * @param page Paginación con ordenamiento
     * @return Página de terceros que coinciden con la búsqueda
     */
    @Query("SELECT t FROM ThirdEntity t LEFT JOIN FETCH t.typeId WHERE t.entId = :entId AND " +
           "(LOWER(t.names) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.lastNames) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.socialReason) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(t.idNumber AS string) LIKE CONCAT('%', :search, '%'))")
    Page<ThirdEntity> findByEntIdAndSearch(@Param("entId") String entId, @Param("search") String search, Pageable page);

    @Query("SELECT COUNT(t) FROM ThirdEntity t WHERE t.entId = :entId AND " +
           "(LOWER(t.names) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.lastNames) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.socialReason) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(t.idNumber AS string) LIKE CONCAT('%', :search, '%'))")
    long countByEntIdAndSearch(@Param("entId") String entId, @Param("search") String search);

    @Query("SELECT COUNT(t) > 0 FROM ThirdEntity t WHERE t.idNumber = :idNumber AND t.entId = :entId")
    boolean existThirdBy(@Param("idNumber") Long idNumber, @Param("entId") String entId);

    @Query("SELECT COUNT(t) > 0 FROM ThirdEntity t WHERE t.thId = :thId AND t.entId = :entId")
    boolean existThirdByThIdAndEntId(@Param("thId") Long thId, @Param("entId") String entId);

    @Query("SELECT t FROM ThirdEntity t LEFT JOIN FETCH t.typeId WHERE t.thId = :thId AND t.entId = :entId")
    Optional<ThirdEntity> findByThIdAndEntId(@Param("thId") Long thId, @Param("entId") String entId);

    @Query("SELECT COUNT(t) > 0 FROM ThirdEntity t WHERE t.typeId.tiId = :typeIdCode AND t.entId = :entId")
    boolean existsByTypeIdTiIdAndEntId(@Param("typeIdCode") String typeIdCode, @Param("entId") String entId);

    @Query("SELECT COUNT(t) FROM ThirdEntity t WHERE t.entId = :entId")
    long countByEntId(@Param("entId") String entId);

    /**
     * @brief Actualiza el estado de todos los terceros de una empresa de forma masiva
     * @details Query optimizada para operaciones bulk con @Modifying
     * @param entId ID de la empresa
     * @param newState Nuevo estado (true para activo, false para inactivo)
     * @return Cantidad de registros actualizados
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ThirdEntity t SET t.state = :newState WHERE t.entId = :entId")
    int bulkUpdateStateByEntId(@Param("entId") String entId, @Param("newState") Boolean newState);

    /**
     * @brief Encuentra números de identificación que ya existen para importaciones masivas
     * @details Optimizado para validación de duplicados en procesos batch
     * @param idNumbers lista de números de identificación a verificar
     * @param entId ID de la empresa
     * @return lista de números de identificación que ya existen
     */
    @Query("SELECT t.idNumber FROM ThirdEntity t WHERE t.idNumber IN :idNumbers AND t.entId = :entId")
    List<Long> findExistingIdNumbers(@Param("idNumbers") Set<Long> idNumbers, @Param("entId") String entId);

    @Query("SELECT t FROM ThirdEntity t LEFT JOIN FETCH t.typeId WHERE t.entId = :entId AND t.state = true")
    Page<ThirdEntity> getActiveThirdsBy(@Param("entId") String entId, Pageable page);

    @Query("SELECT COUNT(t) FROM ThirdEntity t WHERE t.entId = :entId AND t.state = true")
    long countActiveByEntId(@Param("entId") String entId);

    /**
     * @brief Busca terceros por empresa y nombre de tipo de tercero activo con paginación
     * @details Filtra terceros que tienen al menos un tipo de tercero específico y activo.
     * La búsqueda por nombre es case insensitive.
     * El ordenamiento se aplica desde el Pageable proporcionado por el servicio.
     * @param entId ID de la empresa
     * @param thirdTypeName Nombre del tipo de tercero activo (case insensitive)
     * @param page Paginación con ordenamiento ASC por defecto en "names"
     * @return Página de terceros que tienen el tipo especificado y activo
     */
    @Query("SELECT DISTINCT t FROM ThirdEntity t " +
           "LEFT JOIN FETCH t.typeId " +
           "JOIN ThirdsAndTypesEntity tat ON t.thId = tat.thId " +
           "JOIN ThirdTypeEntity tt ON tat.ttId = tt.ttId " +
           "WHERE t.entId = :entId AND LOWER(tt.ttName) = LOWER(:thirdTypeName) AND tt.status = true")
    Page<ThirdEntity> findByEntIdAndThirdTypeName(@Param("entId") String entId, @Param("thirdTypeName") String thirdTypeName, Pageable page);

    /**
     * @brief Cuenta terceros por empresa y nombre de tipo de tercero activo
     * @details Cuenta terceros que tienen al menos un tipo de tercero específico y activo.
     * La búsqueda por nombre es case insensitive.
     * @param entId ID de la empresa
     * @param thirdTypeName Nombre del tipo de tercero activo (case insensitive)
     * @return Cantidad de terceros que tienen el tipo especificado y activo
     */
    /**
     * @brief Obtiene terceros con todas sus relaciones cargadas para exportación
     * @details Carga todas las relaciones necesarias en una sola consulta usando JOIN FETCH,
     * eliminando el problema N+1. Específicamente diseñado para operaciones de exportación masiva.
     * @param entId ID de la empresa
     * @param pageable paginación
     * @return Página de terceros con todas sus relaciones cargadas
     */
    @Query("SELECT DISTINCT t FROM ThirdEntity t " +
           "LEFT JOIN FETCH t.typeId " +
           "WHERE t.entId = :entId")
    Page<ThirdEntity> findAllForExport(@Param("entId") String entId, Pageable pageable);

    /**
     * @brief Obtiene terceros filtrados por estado con todas sus relaciones para exportación
     * @details Similar a findAllForExport pero con filtro de estado
     * @param entId ID de la empresa
     * @param state Estado de los terceros (true=activos, false=inactivos)
     * @param pageable paginación
     * @return Página de terceros con todas sus relaciones cargadas
     */
    @Query("SELECT DISTINCT t FROM ThirdEntity t " +
           "LEFT JOIN FETCH t.typeId " +
           "WHERE t.entId = :entId AND t.state = :state")
    Page<ThirdEntity> findAllByStateForExport(@Param("entId") String entId, @Param("state") Boolean state, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT t) FROM ThirdEntity t " +
           "JOIN ThirdsAndTypesEntity tat ON t.thId = tat.thId " +
           "JOIN ThirdTypeEntity tt ON tat.ttId = tt.ttId " +
           "WHERE t.entId = :entId AND LOWER(tt.ttName) = LOWER(:thirdTypeName) AND tt.status = true")
    long countByEntIdAndThirdTypeName(@Param("entId") String entId, @Param("thirdTypeName") String thirdTypeName);

    /**
     * @brief Busca tercero por ID con typeId cargado
     * @details Método personalizado para cargar tercero con su typeId inicializado,
     * usado en servicios que requieren acceso completo a los datos del tercero
     * @param id ID del tercero
     * @return Tercero con typeId cargado o vacío si no existe
     */
    @Query("SELECT t FROM ThirdEntity t LEFT JOIN FETCH t.typeId WHERE t.thId = :id")
    Optional<ThirdEntity> findByIdWithTypeId(@Param("id") Long id);

    /**
     * @brief Verifica si existe tercero con tipo de identificación específico, empresa y movimientos contables
     * @param typeIdCode Código del tipo de identificación
     * @param entId ID de la empresa
     * @param usageCount Límite mínimo de movimientos contables
     * @return true si existe al menos un tercero que cumple las condiciones
     */
    @Query("SELECT COUNT(t) > 0 FROM ThirdEntity t WHERE t.typeId.tiId = :typeIdCode AND t.entId = :entId AND t.usageCount > :usageCount")
    boolean existsByTypeIdTiIdAndEntIdAndUsageCountGreaterThan(@Param("typeIdCode") String typeIdCode, @Param("entId") String entId, @Param("usageCount") int usageCount);

    /**
     * @brief Verifica si existe tercero asociado a tipo de tercero específico, empresa y movimientos contables
     * @param thirdTypeId ID del tipo de tercero
     * @param entId ID de la empresa
     * @param usageCount Límite mínimo de movimientos contables
     * @return true si existe al menos un tercero que cumple las condiciones
     */
    @Query("SELECT COUNT(t) > 0 FROM ThirdEntity t " +
           "JOIN ThirdsAndTypesEntity tat ON t.thId = tat.thId " +
           "WHERE tat.ttId = :thirdTypeId AND t.entId = :entId AND t.usageCount > :usageCount")
    boolean existsByThirdTypeIdAndEntIdAndUsageCountGreaterThan(@Param("thirdTypeId") Long thirdTypeId, @Param("entId") String entId, @Param("usageCount") int usageCount);

    /**
     * @brief Incrementa el contador de uso de un tercero de forma optimizada
     * @details Query optimizada para incrementar el usageCount sin cargar la entidad completa.
     * Usa COALESCE para manejar valores null correctamente.
     * @param thirdId ID del tercero
     * @return Cantidad de registros actualizados (debe ser 1 si existe, 0 si no existe)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ThirdEntity t SET t.usageCount = COALESCE(t.usageCount, 0) + 1 WHERE t.thId = :thirdId")
    int incrementUsageCountByThirdId(@Param("thirdId") Long thirdId);
}
