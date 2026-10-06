package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IRunDueScheduledReportPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.IJobCommand;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EExecutionStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IClockPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportExecutionQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportQueryRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Ejecutor de reportes programados que vencen.
 *
 * Implementa el puerto de entrada {@link IRunDueScheduledReportPort} y
 * orquesta el ciclo completo de ejecución para cada job vencido: detecta
 * vencimiento/expiración, evita duplicados, ejecuta los pasos
 * ({@link IJobCommand}) en orden y actualiza tanto el resultado de la
 * ejecución como la próxima fecha de corrida del job.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportJobRunner implements IRunDueScheduledReportPort {

    private static final int DEFAULT_BATCH_SIZE = 50;

    private final IScheduledReportQueryRepositoryPort scheduledReportQueryRepositoryPort;
    private final IScheduledReportCommandRepositoryPort scheduledReportCommandRepositoryPort;
    private final IScheduledReportExecutionCommandRepositoryPort scheduledReportExecutionCommandRepositoryPort;
    private final IScheduledReportExecutionQueryRepositoryPort scheduledReportExecutionQueryRepositoryPort;
    private final ScheduledReportCommandFactory scheduledReportCommandFactory;
    private final IClockPort clockPort;
    private final List<IJobCommand> scheduledReportJobSteps;

    @Value("${scheduled.reports.poller.batch-size:50}")
    private int batchSize;

    // @Override
    // public void runDueScheduledReports() {
    //     Instant now = clockPort.now();
    //     List<ScheduledAuxiliaryBookJob> dueJobs = scheduledReportQueryRepositoryPort.findDueJobs(now, resolveBatchSize());

    //     for (ScheduledAuxiliaryBookJob job : dueJobs) {
    //         processJobSafely(job, now);
    //     }
    // }

    @Override
    public void runDueScheduledReports() {
        Instant now = clockPort.now();
        log.info("[ScheduledReportJobRunner] Polling due jobs at {} using systemZone={}", now, ZoneId.systemDefault());

        List<ScheduledAuxiliaryBookJob> dueJobs =
                scheduledReportQueryRepositoryPort.findDueJobs(now, resolveBatchSize());

        log.info("[ScheduledReportJobRunner] Found {} due jobs. Steps registered={}",
                dueJobs.size(), scheduledReportJobSteps.size());

        for (ScheduledAuxiliaryBookJob job : dueJobs) {
            log.info("[ScheduledReportJobRunner] Processing job publicId={} nextRunAt={}",
                    resolvePublicId(job),
                    job.getScheduleSpec() != null ? job.getScheduleSpec().getNextRunAt() : null);
            processJobSafely(job, now);
        }
    }

    private void processJobSafely(ScheduledAuxiliaryBookJob job, Instant now) {
        try {
            processDueJob(job, now);
        } catch (Exception ex) {
            log.error("Unexpected error processing scheduled report {}", resolvePublicId(job), ex);
        }
    }

    private void processDueJob(ScheduledAuxiliaryBookJob job, Instant now) {
        if (job == null || job.getStatus() != EJobStatus.ACTIVE) {
            return;
        }
        if (job.getScheduleSpec() == null || job.getScheduleSpec().getNextRunAt() == null) {
            return;
        }
        if (isExpired(job, now)) {
            cancelJob(job);
            return;
        }

        UUID jobUuid = resolveJobUuid(job);
        if (jobUuid == null) {
            cancelJob(job);
            return;
        }

        executeJob(job, jobUuid, now);
    }

    private boolean isExpired(ScheduledAuxiliaryBookJob job, Instant now) {
        ScheduleSpec spec = job.getScheduleSpec();
        return spec.getEndAt() != null
                && spec.getEndAt().isPresent()
                && spec.getEndAt().get().isBefore(now);
    }

    private void executeJob(ScheduledAuxiliaryBookJob job, UUID jobUuid, Instant now) {
        Instant scheduledAt = job.getScheduleSpec().getNextRunAt();
        Optional<ReportExecution> existing = scheduledReportExecutionQueryRepositoryPort.findByJobIdAndScheduledAt(jobUuid, scheduledAt);
        if (existing.isPresent()) {
            updateNextRun(job, now);
            return;
        }

        ReportExecution execution = buildRunningExecution(jobUuid, scheduledAt, now);
        scheduledReportExecutionCommandRepositoryPort.save(execution);

        try {
            JobCommandContext context = buildContext(job, jobUuid, execution);
            executeSteps(context);
            execution.setStatusExecution(EExecutionStatus.SUCCESS);
            execution.setFinishedAt(clockPort.now());
        } catch (Exception ex) {
            log.error("Error executing scheduled report {}", resolvePublicId(job), ex);
            execution.setStatusExecution(EExecutionStatus.FAILED);
            if (execution.getDeliveryStatus() == null || execution.getDeliveryStatus() == EDeliveryStatus.NONE) {
                execution.setDeliveryStatus(EDeliveryStatus.FAILED);
            }
            execution.setErrorMessage(resolveExecutionErrorMessage(execution, ex));
            execution.setFinishedAt(clockPort.now());
        }

        scheduledReportExecutionCommandRepositoryPort.save(execution);
        updateNextRun(job, clockPort.now());
    }

    private ReportExecution buildRunningExecution(UUID jobUuid, Instant scheduledAt, Instant now) {
        ReportExecution execution = new ReportExecution();
        execution.setExecutionId(UUID.randomUUID());
        execution.setJobId(jobUuid);
        execution.setScheduledAt(scheduledAt);
        execution.setStartedAt(now);
        execution.setStatusExecution(EExecutionStatus.RUNNING);
        execution.setDeliveryStatus(EDeliveryStatus.NONE);
        execution.setRetryCount(0);
        execution.setCorrelationId(UUID.randomUUID().toString());
        return execution;
    }

    private JobCommandContext buildContext(ScheduledAuxiliaryBookJob job, UUID jobUuid, ReportExecution execution) {
        DeliveryConfig deliveryConfig = resolveDeliveryConfig(job);
        EAuxiliaryBookFormat reportFormat = resolveReportFormat(deliveryConfig);
        boolean downloadEnabled = isDownloadEnabled(deliveryConfig);
        boolean emailEnabled = isEmailEnabled(deliveryConfig);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JobCommandContext.ATTRIBUTE_JOB, job);
        attributes.put(JobCommandContext.ATTRIBUTE_EXECUTION, execution);
        attributes.put(JobCommandContext.ATTRIBUTE_DELIVERY_CONFIG, deliveryConfig);
        attributes.put(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, reportFormat);
        attributes.put(JobCommandContext.ATTRIBUTE_DOWNLOAD_ENABLED, downloadEnabled);
        attributes.put(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, emailEnabled);

        log.info("[ScheduledReportJobRunner] Initialized mutable job context for publicId={} executionId={} attributes={}",
                resolvePublicId(job),
                execution.getExecutionId(),
                attributes.size());

        return new JobCommandContext(
                jobUuid,
                execution.getExecutionId(),
                execution.getScheduledAt(),
                execution.getCorrelationId(),
                "scheduler",
                attributes
        );
    }

    private void executeSteps(JobCommandContext context) {
        for (IJobCommand step : scheduledReportJobSteps) {
            step.execute(context);
        }
    }

    private DeliveryConfig resolveDeliveryConfig(ScheduledAuxiliaryBookJob job) {
        if (job == null) {
            return null;
        }
        DeliveryConfig deliveryConfig = job.getDeliveryConfig();
        if (deliveryConfig == null || deliveryConfig.getDeliveryWay() == null) {
            return new DeliveryConfig(EDeliveryWay.DOWNLOAD, null, null);
        }
        return deliveryConfig;
    }

    private EAuxiliaryBookFormat resolveReportFormat(DeliveryConfig deliveryConfig) {
        if (deliveryConfig == null || deliveryConfig.getFormat() == null) {
            return EAuxiliaryBookFormat.PDF;
        }
        return deliveryConfig.getFormat();
    }

    private boolean isDownloadEnabled(DeliveryConfig deliveryConfig) {
        return deliveryConfig != null
                && deliveryConfig.getDeliveryWay() != null
                && (deliveryConfig.getDeliveryWay() == EDeliveryWay.DOWNLOAD
                || deliveryConfig.getDeliveryWay() == EDeliveryWay.BOTH);
    }

    private boolean isEmailEnabled(DeliveryConfig deliveryConfig) {
        return deliveryConfig != null
                && deliveryConfig.getDeliveryWay() != null
                && (deliveryConfig.getDeliveryWay() == EDeliveryWay.EMAIL
                || deliveryConfig.getDeliveryWay() == EDeliveryWay.BOTH);
    }

    private void updateNextRun(ScheduledAuxiliaryBookJob job, Instant now) {
        ScheduleSpec spec = job.getScheduleSpec();
        if (spec == null || spec.getNextRunAt() == null) {
            return;
        }
        Instant next = scheduledReportCommandFactory.computeNextRunAt(spec.getNextRunAt(), spec.getFrequency());
        next = scheduledReportCommandFactory.computeFirstRunAfter(next, spec.getFrequency(), now);
        ScheduleSpec updated = new ScheduleSpec(spec.getFrequency(), spec.getStartAt(), spec.getEndAt(), next);
        job.setScheduleSpec(updated);

        if (spec.getEndAt() != null && spec.getEndAt().isPresent() && next.isAfter(spec.getEndAt().get())) {
            scheduledReportCommandRepositoryPort.updateStatus(job.getJobId(), EJobStatus.CANCELLED);
            return;
        }
        scheduledReportCommandRepositoryPort.save(job);
    }

    private UUID resolveJobUuid(ScheduledAuxiliaryBookJob job) {
        try {
            return UUID.fromString(job.getPublicId());
        } catch (IllegalArgumentException ex) {
            log.error("Scheduled report {} has an invalid publicId and will be cancelled.", resolvePublicId(job), ex);
            return null;
        }
    }

    private void cancelJob(ScheduledAuxiliaryBookJob job) {
        if (job != null && job.getJobId() != null) {
            scheduledReportCommandRepositoryPort.updateStatus(job.getJobId(), EJobStatus.CANCELLED);
        }
    }

    private int resolveBatchSize() {
        return batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
    }

    private String resolveExecutionErrorMessage(ReportExecution execution, Exception ex) {
        if (execution.getErrorMessage() != null && !execution.getErrorMessage().isBlank()) {
            return execution.getErrorMessage();
        }
        return ex.getMessage();
    }

    private String resolvePublicId(ScheduledAuxiliaryBookJob job) {
        return job != null ? job.getPublicId() : "unknown";
    }
}
