package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @brief Repositorio JPA para la entidad de ejecución de reportes programados.
 *
 * Provee operaciones CRUD, búsqueda de una ejecución concreta por
 * {@code jobId} y {@code scheduledAt} (para idempotencia), y listado
 * de todas las ejecuciones asociadas a un job dado.
 */
public interface IScheduledReportExecutionRepository extends JpaRepository<ScheduledReportExecutionEntity, UUID> {
    Optional<ScheduledReportExecutionEntity> findByJobIdAndScheduledAt(UUID jobId, Instant scheduledAt);

    List<ScheduledReportExecutionEntity> findByJobId(UUID jobId);
}
