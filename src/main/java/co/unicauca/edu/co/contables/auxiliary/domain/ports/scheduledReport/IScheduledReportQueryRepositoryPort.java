package co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @brief Puerto de salida para operaciones de consulta de reportes programados.
 *
 * Define el contrato para buscar jobs por id interno, por identificador
 * público, por entidad y para localizar aquellos vencidos
 * ({@code findDueJobs}) que deben ejecutarse en el ciclo actual.
 */
public interface IScheduledReportQueryRepositoryPort {
    Optional<ScheduledAuxiliaryBookJob> findById(Long jobId);
    Optional<ScheduledAuxiliaryBookJob> findByPublicId(String publicId);
    List<ScheduledAuxiliaryBookJob> findByEntId(String entId);
    List<ScheduledAuxiliaryBookJob> findDueJobs(Instant now, int limit);
    //List<ScheduledAuxiliaryBookJob> list();
}
