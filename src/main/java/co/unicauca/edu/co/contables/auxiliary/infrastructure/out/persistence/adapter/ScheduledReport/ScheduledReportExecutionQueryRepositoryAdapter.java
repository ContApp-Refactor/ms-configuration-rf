package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.ScheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportExecutionEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.scheduledReport.IScheduledReportExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @brief Adaptador de lectura para las ejecuciones de reportes programados.
 *
 * Implementa {@link IScheduledReportExecutionQueryRepositoryPort}
 * consultando la base de datos y mapeando las entidades JPA al dominio
 * {@link ReportExecution}, con búsquedas por identificador de ejecución,
 * por trabajo y por instante planificado.
 */
@Component
@RequiredArgsConstructor
public class ScheduledReportExecutionQueryRepositoryAdapter implements IScheduledReportExecutionQueryRepositoryPort {

    private final IScheduledReportExecutionRepository scheduledReportExecutionRepository;

    /**
     * @brief Busca una ejecución por su identificador.
     * @param executionId Identificador de la ejecución.
     * @return Ejecución si existe.
     */
    @Override
    public Optional<ReportExecution> findById(UUID executionId) {
        return scheduledReportExecutionRepository.findById(executionId).map(this::toDomain);
    }

    /**
     * @brief Obtiene las ejecuciones asociadas a un trabajo.
     * @param jobId Identificador del trabajo.
     * @param filtros Filtros opcionales (actualmente no aplicados).
     * @return Lista de ejecuciones del trabajo indicado.
     */
    @Override
    public List<ReportExecution> findByJobId(UUID jobId, String filtros) {
        return scheduledReportExecutionRepository.findByJobId(jobId).stream().map(this::toDomain).toList();
    }

    /**
     * @brief Busca una ejecución por trabajo e instante planificado.
     * @param jobId Identificador del trabajo.
     * @param scheduledAt Instante planificado de ejecución.
     * @return Ejecución correspondiente si existe.
     */
    @Override
    public Optional<ReportExecution> findByJobIdAndScheduledAt(UUID jobId, Instant scheduledAt) {
        return scheduledReportExecutionRepository.findByJobIdAndScheduledAt(jobId, scheduledAt).map(this::toDomain);
    }

    private ReportExecution toDomain(ScheduledReportExecutionEntity entity) {
        ReportExecution execution = new ReportExecution();
        execution.setExecutionId(entity.getExecutionId());
        execution.setJobId(entity.getJobId());
        execution.setScheduledAt(entity.getScheduledAt());
        execution.setStartedAt(entity.getStartedAt());
        execution.setFinishedAt(entity.getFinishedAt());
        execution.setStatusExecution(entity.getStatusExecution());
        execution.setDeliveryStatus(entity.getDeliveryStatus());
        execution.setErrorCode(entity.getErrorCode());
        execution.setErrorMessage(entity.getErrorMessage());
        execution.setCorrelationId(entity.getCorrelationId());
        execution.setRetryCount(entity.getRetryCount());
        return execution;
    }
}
