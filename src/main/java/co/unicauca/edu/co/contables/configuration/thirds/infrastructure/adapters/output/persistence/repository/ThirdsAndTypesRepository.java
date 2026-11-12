package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    /** Proyección liviana (sin cargar terceros ni tipos) de una fila de la relación. */
    interface LinkRow {
        Long getThId();

        Long getTtId();
    }

    /**
     * @brief Relaciones tercero-tipo de UNA empresa, para la copia de seguridad
     * @details La tabla no tiene columna de empresa: se resuelve por join con sus terceros y se exige
     * que el tipo también sea de la empresa. Orden estable para paginar por Slice.
     */
    @Query("SELECT tat.thId AS thId, tat.ttId AS ttId FROM ThirdsAndTypesEntity tat, ThirdEntity t, ThirdTypeEntity tt " +
           "WHERE tat.thId = t.thId AND tat.ttId = tt.ttId AND t.entId = :entId AND tt.ttentId = :entId " +
           "ORDER BY tat.thId, tat.ttId")
    Slice<LinkRow> findBackupLinksByEntId(@Param("entId") String entId, Pageable pageable);

    @Query("SELECT COUNT(tat) FROM ThirdsAndTypesEntity tat, ThirdEntity t, ThirdTypeEntity tt " +
           "WHERE tat.thId = t.thId AND tat.ttId = tt.ttId AND t.entId = :entId AND tt.ttentId = :entId")
    long countBackupLinksByEntId(@Param("entId") String entId);

    /** Relaciones de terceros de la empresa cuyo tipo NO es de la empresa (huérfanas). */
    @Query("SELECT tat.thId AS thId, tat.ttId AS ttId FROM ThirdsAndTypesEntity tat, ThirdEntity t " +
           "WHERE tat.thId = t.thId AND t.entId = :entId AND NOT EXISTS " +
           "(SELECT 1 FROM ThirdTypeEntity tt WHERE tt.ttId = tat.ttId AND tt.ttentId = :entId) " +
           "ORDER BY tat.thId, tat.ttId")
    List<LinkRow> findOrphanLinksByEntId(@Param("entId") String entId, Pageable pageable);
}