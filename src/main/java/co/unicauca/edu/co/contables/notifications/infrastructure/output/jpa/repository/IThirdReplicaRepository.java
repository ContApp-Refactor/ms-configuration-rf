package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;

@Repository
public interface IThirdReplicaRepository extends JpaRepository<ThirdReplicaEntity, Long> {
    Optional<ThirdReplicaEntity> findByThirdId(Long thirdId);

    /**
     * Réplicas de una empresa para la copia de seguridad. Solo las que tienen su tercero vivo: la réplica de un
     * tercero eliminado es basura (la eliminación no la borra) y no tiene a qué remapearse en el destino.
     */
    @Query("SELECT r FROM ThirdReplicaEntity r WHERE r.enterpriseId = :entId AND EXISTS "
            + "(SELECT 1 FROM ThirdEntity t WHERE t.thId = r.thirdId AND t.entId = :entId) ORDER BY r.id")
    Slice<ThirdReplicaEntity> findBackupReplicas(@Param("entId") String entId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM ThirdReplicaEntity r WHERE r.enterpriseId = :entId AND EXISTS "
            + "(SELECT 1 FROM ThirdEntity t WHERE t.thId = r.thirdId AND t.entId = :entId)")
    long countBackupReplicas(@Param("entId") String entId);
}
