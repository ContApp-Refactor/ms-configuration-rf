package co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @brief Puerto de salida para operaciones de consulta de ejecuciones de reportes programados.
 *
 * Define el contrato para recuperar ejecuciones por id, por job (con
 * filtros) y por combinación job + instante programado, útil para evitar
 * duplicados y presentar el historial en la UI.
 */
public interface IScheduledReportExecutionQueryRepositoryPort {
    Optional<ReportExecution> findById(UUID executionId);
    List<ReportExecution> findByJobId(UUID jobId, String filtros/* filtros */);
    Optional<ReportExecution> findByJobIdAndScheduledAt(UUID jobId, Instant scheduledAt);
}
