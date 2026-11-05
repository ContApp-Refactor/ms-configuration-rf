package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.jobSteps;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IEmailSenderPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.IJobCommand;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.EmailConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @brief Paso de envío por correo del reporte programado.
 *
 * Si el job tiene habilitado el canal de email, arma el asunto, el cuerpo
 * y el adjunto del reporte y delega el envío en {@link IEmailSenderPort},
 * actualizando el estado de entrega de la ejecución según el resultado.
 */
@Component
@Order(30)
@RequiredArgsConstructor
@Slf4j
public class SendEmailStep implements IJobCommand {

    private final IEmailSenderPort emailSenderPort;

    @Override
    public void execute(JobCommandContext context) {
        boolean emailEnabled = Boolean.TRUE.equals(
                context.getAttribute(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, Boolean.class)
        );
        if (!emailEnabled) {
            return;
        }

        ScheduledAuxiliaryBookJob job = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_JOB, ScheduledAuxiliaryBookJob.class);
        ReportExecution execution = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_EXECUTION, ReportExecution.class);
        DeliveryConfig deliveryConfig = context.getAttribute(JobCommandContext.ATTRIBUTE_DELIVERY_CONFIG, DeliveryConfig.class);
        byte[] reportBytes = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_REPORT_BYTES, byte[].class);
        boolean downloadEnabled = Boolean.TRUE.equals(
                context.getAttribute(JobCommandContext.ATTRIBUTE_DOWNLOAD_ENABLED, Boolean.class)
        );

        try {
            EmailConfig emailConfig = deliveryConfig != null ? deliveryConfig.getEmailConfig() : null;
            validateEmailConfig(emailConfig);
            emailSenderPort.sendReport(
                    emailConfig.getTo(),
                    resolveEmailSubject(job, emailConfig),
                    resolveEmailBody(emailConfig),
                    reportBytes,
                    buildAttachmentName(job, context.getAttribute(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, EAuxiliaryBookFormat.class))
            );
            if (!downloadEnabled) {
                execution.setDeliveryStatus(EDeliveryStatus.EMAIL_SENT);
            }
        } catch (Exception ex) {
            log.warn("Email delivery failed for scheduled report {}", job.getPublicId(), ex);
            if (!downloadEnabled) {
                execution.setDeliveryStatus(EDeliveryStatus.EMAIL_FAILED);
            }
            execution.setErrorMessage(truncate("Email delivery failed: " + ex.getMessage(), 1000));
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private void validateEmailConfig(EmailConfig emailConfig) {
        if (emailConfig == null || emailConfig.getTo() == null || emailConfig.getTo().isBlank()) {
            throw new IllegalStateException("Email configuration is incomplete.");
        }
    }

    private String resolveEmailSubject(ScheduledAuxiliaryBookJob job, EmailConfig emailConfig) {
        if (emailConfig != null && emailConfig.getSubjectTemplate() != null && !emailConfig.getSubjectTemplate().isBlank()) {
            return emailConfig.getSubjectTemplate();
        }
        return "Scheduled report: " + (job.getBookType() != null ? job.getBookType().name() : "Auxiliary Book");
    }

    private String resolveEmailBody(EmailConfig emailConfig) {
        if (emailConfig != null && emailConfig.getBodyTemplate() != null && !emailConfig.getBodyTemplate().isBlank()) {
            return emailConfig.getBodyTemplate();
        }
        return "Your scheduled report is attached.";
    }

    private String buildAttachmentName(ScheduledAuxiliaryBookJob job, EAuxiliaryBookFormat format) {
        String baseName = job.getBookType() != null ? job.getBookType().name() : "AuxiliaryBook";
        String extension = format == EAuxiliaryBookFormat.EXCEL ? ".xlsx" : ".pdf";
        return baseName + "_scheduled_report" + extension;
    }
}
