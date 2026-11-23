package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IDownloadScheduledReportPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportQueryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief Caso de uso de descarga on-demand de una ejecucion programada.
 *
 * Localiza el job y la ejecucion, regenera el reporte usando el snapshot
 * (criteria/format del job al momento de la programacion) y marca la
 * ejecucion como descargada.
 */
@Service
@RequiredArgsConstructor
public class DownloadScheduledReportUC implements IDownloadScheduledReportPort {

    private final IScheduledReportQueryRepositoryPort scheduledReportQueryRepositoryPort;
    private final IScheduledReportExecutionQueryRepositoryPort scheduledReportExecutionQueryRepositoryPort;
    private final IScheduledReportExecutionCommandRepositoryPort scheduledReportExecutionCommandRepositoryPort;
    private final AuxiliaryBookReportGenerator auxiliaryBookReportGenerator;

    @Override
    public DownloadedReport download(String publicId, UUID executionId) {
        ScheduledAuxiliaryBookJob job = scheduledReportQueryRepositoryPort.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled report not found: " + publicId));

        ReportExecution execution = scheduledReportExecutionQueryRepositoryPort.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        UUID jobUuid = UUID.fromString(job.getPublicId());
        if (!jobUuid.equals(execution.getJobId())) {
            throw new IllegalArgumentException("Execution does not belong to scheduled report.");
        }

        EAuxiliaryBookFormat format = resolveFormat(job.getDeliveryConfig());
        AuxiliaryBookReportGenerator.GenerationResult result = auxiliaryBookReportGenerator.generate(job, format);

        execution.setDeliveryStatus(EDeliveryStatus.DOWNLOADED);
        execution.setFinishedAt(Instant.now());
        scheduledReportExecutionCommandRepositoryPort.save(execution);

        String filename = buildFilename(job, format);
        return new DownloadedReport(result.getReportBytes(), filename, format);
    }

    private EAuxiliaryBookFormat resolveFormat(DeliveryConfig deliveryConfig) {
        if (deliveryConfig == null || deliveryConfig.getFormat() == null) {
            return EAuxiliaryBookFormat.PDF;
        }
        return deliveryConfig.getFormat();
    }

    private String buildFilename(ScheduledAuxiliaryBookJob job, EAuxiliaryBookFormat format) {
        String baseName = job.getBookType() != null ? job.getBookType().name() : "AuxiliaryBook";
        String extension = format == EAuxiliaryBookFormat.EXCEL ? ".xlsx" : ".pdf";
        return baseName + "_scheduled_report" + extension;
    }
}
