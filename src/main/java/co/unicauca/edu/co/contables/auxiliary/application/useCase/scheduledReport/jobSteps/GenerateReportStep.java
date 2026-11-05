package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.jobSteps;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.AuxiliaryBookReportGenerator;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.AuxiliaryBookReportGenerator.GenerationResult;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.IJobCommand;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @brief Paso de generacion del reporte dentro del job programado.
 *
 * Solo genera bytes si la entrega requiere el archivo en el momento de
 * la ejecucion programada (canal EMAIL o BOTH). Cuando el canal es solo
 * DOWNLOAD el archivo se generara on-demand al hacer click en la
 * notificacion, por lo que este paso es no-op.
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class GenerateReportStep implements IJobCommand {

    private final AuxiliaryBookReportGenerator auxiliaryBookReportGenerator;

    @Override
    public void execute(JobCommandContext context) {
        boolean emailEnabled = Boolean.TRUE.equals(
                context.getAttribute(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, Boolean.class)
        );
        if (!emailEnabled) {
            return;
        }

        ScheduledAuxiliaryBookJob job = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_JOB, ScheduledAuxiliaryBookJob.class);
        EAuxiliaryBookFormat reportFormat = context.getAttribute(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, EAuxiliaryBookFormat.class);

        GenerationResult result = auxiliaryBookReportGenerator.generate(job, reportFormat);

        context.putAttribute(JobCommandContext.ATTRIBUTE_REGISTERED_BOOK, result.getRegisteredBook());
        context.putAttribute(JobCommandContext.ATTRIBUTE_REPORT_DATA, result.getReportData());
        context.putAttribute(JobCommandContext.ATTRIBUTE_REPORT_BYTES, result.getReportBytes());
    }
}
