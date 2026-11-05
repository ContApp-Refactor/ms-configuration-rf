package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.ScheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportExecutionEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.scheduledReport.IScheduledReportExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador de escritura para las ejecuciones de reportes programados.
 *
 * Implementa {@link IScheduledReportExecutionCommandRepositoryPort}
 * persistiendo registros de {@link ReportExecution} en la base de datos
 * y traduciendo entre el dominio y la entidad JPA.
 */
@Component
@RequiredArgsConstructor
public class ScheduledReportExecutionCommandRepositoryAdapter implements IScheduledReportExecutionCommandRepositoryPort {

    private final IScheduledReportExecutionRepository scheduledReportExecutionRepository;

    /**
     * @brief Guarda una ejecución de reporte programado.
     * @param execution Ejecución a persistir.
     * @return Ejecución persistida con los identificadores generados.
     */
    @Override
    public ReportExecution save(ReportExecution execution) {
        ScheduledReportExecutionEntity entity = toEntity(execution);
        ScheduledReportExecutionEntity saved = scheduledReportExecutionRepository.save(entity);
        return toDomain(saved);
    }

    private ScheduledReportExecutionEntity toEntity(ReportExecution execution) {
        ScheduledReportExecutionEntity entity = new ScheduledReportExecutionEntity();
        entity.setExecutionId(execution.getExecutionId());
        entity.setJobId(execution.getJobId());
        entity.setScheduledAt(execution.getScheduledAt());
        entity.setStartedAt(execution.getStartedAt());
        entity.setFinishedAt(execution.getFinishedAt());
        entity.setStatusExecution(execution.getStatusExecution());
        entity.setDeliveryStatus(execution.getDeliveryStatus());
        entity.setErrorCode(execution.getErrorCode());
        entity.setErrorMessage(execution.getErrorMessage());
        entity.setCorrelationId(execution.getCorrelationId());
        entity.setRetryCount(execution.getRetryCount());
        return entity;
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
