package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @brief Repositorio JPA para relación muchos a muchos terceros-tipos con multi-tenancy
 */
@Repository
public interface ThirdsAndTypesRepository extends JpaRepository<ThirdsAndTypesEntity, ThirdsAndTypesId> {

    boolean existsByThIdAndTtId(Long thId, Long ttId);

    @Query("SELECT tat FROM ThirdsAndTypesEntity tat WHERE tat.thId = :thId")
    List<ThirdsAndTypesEntity> findByThId(@Param("thId") Long thId);

    @Query("SELECT tat FROM ThirdsAndTypesEntity tat WHERE tat.ttId = :ttId")
    List<ThirdsAndTypesEntity> findByTtId(@Param("ttId") Long ttId);

    void deleteByThId(Long thId);

    void deleteByTtId(Long ttId);

    boolean existsByTtId(Long ttId);

    /**
     * @brief Obtiene todas las relaciones tercero-tipo para múltiples terceros (batch)
     * @details Optimizado para exportación masiva, carga todas las relaciones con sus ThirdTypes
     * en una sola consulta usando JOIN FETCH. Elimina el problema N+1 queries.
     * @param thirdIds lista de IDs de terceros
     * @return lista de relaciones con ThirdTypes cargados
     */
    @Query("SELECT tat FROM ThirdsAndTypesEntity tat " +
           "JOIN FETCH tat.thirdType " +
           "WHERE tat.thId IN :thirdIds")
    List<ThirdsAndTypesEntity> findByThIdInWithThirdType(@Param("thirdIds") List<Long> thirdIds);
}