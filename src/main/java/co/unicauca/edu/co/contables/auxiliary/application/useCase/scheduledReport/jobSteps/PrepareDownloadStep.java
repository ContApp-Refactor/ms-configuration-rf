package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.jobSteps;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.INotificationPusherPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.IJobCommand;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ENotificationType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.notification.INotificationCommandRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief Paso de preparacion de la descarga del reporte programado.
 *
 * Si el job esta configurado para descarga, marca la ejecucion como
 * {@link EDeliveryStatus#READY_FOR_DOWNLOAD}, persiste una notificacion
 * para el usuario duenio del job y la empuja por el canal en vivo. Si no
 * hay canal de entrega configurado, marca la ejecucion como fallida.
 */
@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class PrepareDownloadStep implements IJobCommand {

    private final INotificationCommandRepositoryPort notificationCommandRepositoryPort;
    private final INotificationPusherPort notificationPusherPort;

    @Override
    public void execute(JobCommandContext context) {
        ReportExecution execution = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_EXECUTION, ReportExecution.class);
        boolean downloadEnabled = Boolean.TRUE.equals(
                context.getAttribute(JobCommandContext.ATTRIBUTE_DOWNLOAD_ENABLED, Boolean.class)
        );
        boolean emailEnabled = Boolean.TRUE.equals(
                context.getAttribute(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, Boolean.class)
        );

        if (!downloadEnabled && !emailEnabled) {
            execution.setDeliveryStatus(EDeliveryStatus.FAILED);
            execution.setErrorMessage("No delivery method configured for scheduled report.");
            return;
        }

        if (!downloadEnabled) {
            return;
        }

        execution.setDeliveryStatus(EDeliveryStatus.READY_FOR_DOWNLOAD);

        ScheduledAuxiliaryBookJob job = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_JOB, ScheduledAuxiliaryBookJob.class);
        String ownerSub = job.getOwnerSub();
        if (ownerSub == null || ownerSub.isBlank()) {
            log.warn("Scheduled report {} has no ownerSub; skipping notification.", job.getPublicId());
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .notificationId(UUID.randomUUID())
                    .userId(ownerSub)
                    .type(ENotificationType.SCHEDULED_REPORT_READY)
                    .title("Reporte programado listo")
                    .message(buildMessage(job))
                    .referenceId(execution.getExecutionId())
                    .referencePublicId(job.getPublicId())
                    .read(false)
                    .createdAt(Instant.now())
                    .build();

            Notification saved = notificationCommandRepositoryPort.save(notification);
            notificationPusherPort.push(saved.getUserId(), saved);
        } catch (Exception ex) {
            log.warn("Could not deliver download notification for scheduled report {}: {}",
                    job.getPublicId(), ex.getMessage());
        }
    }

    private String buildMessage(ScheduledAuxiliaryBookJob job) {
        String type = job != null && job.getBookType() != null ? job.getBookType().name() : "Auxiliary Book";
        return "Tu reporte programado (" + type + ") esta listo para descarga.";
    }
}
