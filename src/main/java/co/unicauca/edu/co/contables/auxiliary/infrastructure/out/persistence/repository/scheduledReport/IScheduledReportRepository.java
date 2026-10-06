package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @brief Repositorio JPA para la entidad de reporte programado.
 *
 * Expone operaciones CRUD y búsquedas: por ID público, por empresa
 * ({@code entId}) y un método especializado para listar los reportes
 * vencidos (estado activo con {@code nextRunAt} ya cumplido) de forma
 * paginada y ordenada, usado por el poller del scheduler.
 */
public interface IScheduledReportRepository extends JpaRepository<ScheduledReportEntity, Long> {
    Optional<ScheduledReportEntity> findByPublicId(String publicId);

    List<ScheduledReportEntity> findByEntId(String entId);

    List<ScheduledReportEntity> findByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(EJobStatus status, Instant now, Pageable pageable);
}
