package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IScheduledReportExecutionsQueryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @brief Caso de uso de consulta para ejecuciones de reportes programados.
 *
 * Implementa el puerto de entrada {@link IScheduledReportExecutionsQueryPort}
 * y recupera las ejecuciones asociadas a un job programado, validando el
 * identificador público y aplicando los filtros opcionales recibidos.
 */
@Service
@RequiredArgsConstructor
public class ScheduledReportExecutionQueryUC implements IScheduledReportExecutionsQueryPort {

    private final IScheduledReportExecutionQueryRepositoryPort scheduledReportExecutionQueryRepositoryPort;
    private final IScheduledReportQueryRepositoryPort scheduledReportQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;

    @Override
    public List<ReportExecution> listExecutionsByJob(String scheduledReportPublicId, String filters) {
        if (scheduledReportPublicId == null || scheduledReportPublicId.isBlank()) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "scheduledReportPublicId"));
        }

        ScheduledAuxiliaryBookJob job = this.scheduledReportQueryRepositoryPort.findByPublicId(scheduledReportPublicId)
                .orElseGet(() -> {
                    this.formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404,
                            this.messageServicePort.getMessage(MessageKeys.ERROR_NOT_FOUND, "ScheduledReport"));
                    return null;
                });

        try {
            UUID jobUuid = UUID.fromString(job.getPublicId());
            return this.scheduledReportExecutionQueryRepositoryPort.findByJobId(jobUuid, filters);
        } catch (IllegalArgumentException ex) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_INVALID_VALUE, "publicId"));
            return List.of();
        }
    }
}
