package co.unicauca.edu.co.contables.auxiliary;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.ScheduledReportCommandFactory;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.ScheduledReportJobRunner;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.IJobCommand;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EExecutionStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @brief Pruebas unitarias para {@link ScheduledReportJobRunner}.
 */
@ExtendWith(MockitoExtension.class)
class AuxiliaryBookApplicationTests {

    @Mock
    private IScheduledReportQueryRepositoryPort scheduledReportQueryRepositoryPort;

    @Mock
    private IScheduledReportCommandRepositoryPort scheduledReportCommandRepositoryPort;

    @Mock
    private IScheduledReportExecutionCommandRepositoryPort scheduledReportExecutionCommandRepositoryPort;

    @Mock
    private IScheduledReportExecutionQueryRepositoryPort scheduledReportExecutionQueryRepositoryPort;

    @Mock
    private ScheduledReportCommandFactory scheduledReportCommandFactory;

    @Mock
    private IClockPort clockPort;

    @Test
    @DisplayName("runDueScheduledReports debe cancelar jobs expirados")
    void runDueScheduledReportsCancelsExpiredJobs() {
        Instant now = Instant.parse("2025-01-20T08:00:00Z");
        ScheduledAuxiliaryBookJob expiredJob = job(
                UUID.randomUUID().toString(),
                10L,
                deliveryConfig(EDeliveryWay.DOWNLOAD, EAuxiliaryBookFormat.PDF),
                now.minusSeconds(30),
                now.minusSeconds(1)
        );

        Mockito.when(clockPort.now()).thenReturn(now);
        Mockito.when(scheduledReportQueryRepositoryPort.findDueJobs(now, 50)).thenReturn(List.of(expiredJob));

        runner(List.of()).runDueScheduledReports();

        Mockito.verify(scheduledReportCommandRepositoryPort).updateStatus(10L, EJobStatus.CANCELLED);
        Mockito.verifyNoInteractions(scheduledReportExecutionCommandRepositoryPort);
    }

    @Test
    @DisplayName("runDueScheduledReports debe cancelar jobs con publicId inválido")
    void runDueScheduledReportsCancelsJobsWithInvalidPublicId() {
        Instant now = Instant.parse("2025-01-20T08:00:00Z");
        ScheduledAuxiliaryBookJob invalidPublicIdJob = job(
                "not-a-uuid",
                11L,
                deliveryConfig(EDeliveryWay.DOWNLOAD, EAuxiliaryBookFormat.PDF),
                now.minusSeconds(30),
                null
        );

        Mockito.when(clockPort.now()).thenReturn(now);
        Mockito.when(scheduledReportQueryRepositoryPort.findDueJobs(now, 50)).thenReturn(List.of(invalidPublicIdJob));

        runner(List.of()).runDueScheduledReports();

        Mockito.verify(scheduledReportCommandRepositoryPort).updateStatus(11L, EJobStatus.CANCELLED);
        Mockito.verifyNoInteractions(scheduledReportExecutionCommandRepositoryPort);
    }

    @Test
    @DisplayName("runDueScheduledReports debe deduplicar ejecuciones existentes y solo recalcular la próxima corrida")
    void runDueScheduledReportsSkipsWhenExecutionAlreadyExists() {
        Instant now = Instant.parse("2025-01-20T08:00:00Z");
        Instant scheduledAt = Instant.parse("2025-01-20T07:30:00Z");
        Instant next = Instant.parse("2025-01-21T07:30:00Z");
        String publicId = UUID.randomUUID().toString();
        ScheduledAuxiliaryBookJob job = job(publicId, 12L, deliveryConfig(EDeliveryWay.DOWNLOAD, EAuxiliaryBookFormat.PDF), scheduledAt, null);
        UUID jobUuid = UUID.fromString(publicId);
        IJobCommand step = Mockito.mock(IJobCommand.class);

        Mockito.when(clockPort.now()).thenReturn(now);
        Mockito.when(scheduledReportQueryRepositoryPort.findDueJobs(now, 50)).thenReturn(List.of(job));
        Mockito.when(scheduledReportExecutionQueryRepositoryPort.findByJobIdAndScheduledAt(jobUuid, scheduledAt))
                .thenReturn(Optional.of(new ReportExecution()));
        Mockito.when(scheduledReportCommandFactory.computeNextRunAt(scheduledAt, EFrequency.DAILY)).thenReturn(next);
        Mockito.when(scheduledReportCommandFactory.computeFirstRunAfter(next, EFrequency.DAILY, now)).thenReturn(next);
        Mockito.when(scheduledReportCommandRepositoryPort.save(Mockito.any(ScheduledAuxiliaryBookJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        runner(List.of(step)).runDueScheduledReports();

        Mockito.verifyNoInteractions(step);
        Mockito.verifyNoInteractions(scheduledReportExecutionCommandRepositoryPort);
        Mockito.verify(scheduledReportCommandRepositoryPort).save(job);
        Assertions.assertThat(job.getScheduleSpec().getNextRunAt()).isEqualTo(next);
    }

    @Test
    @DisplayName("runDueScheduledReports debe ejecutar pasos, persistir RUNNING y luego SUCCESS")
    void runDueScheduledReportsExecutesStepsAndMarksSuccess() {
        Instant now = Instant.parse("2025-01-20T08:00:00Z");
        Instant finishedAt = Instant.parse("2025-01-20T08:10:00Z");
        Instant updateNow = Instant.parse("2025-01-20T08:11:00Z");
        Instant scheduledAt = Instant.parse("2025-01-20T07:30:00Z");
        Instant next = Instant.parse("2025-01-21T07:30:00Z");
        String publicId = UUID.randomUUID().toString();
        ScheduledAuxiliaryBookJob job = job(publicId, 13L, null, scheduledAt, null);
        UUID jobUuid = UUID.fromString(publicId);

        IJobCommand step = Mockito.mock(IJobCommand.class);
        Mockito.doAnswer(invocation -> {
            JobCommandContext context = invocation.getArgument(0);
            Assertions.assertThat(context.getAttribute(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, EAuxiliaryBookFormat.class))
                    .isEqualTo(EAuxiliaryBookFormat.PDF);
            Assertions.assertThat(context.getAttribute(JobCommandContext.ATTRIBUTE_DOWNLOAD_ENABLED, Boolean.class)).isTrue();
            Assertions.assertThat(context.getAttribute(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, Boolean.class)).isFalse();
            return null;
        }).when(step).execute(Mockito.any(JobCommandContext.class));

        List<ReportExecution> savedExecutions = new ArrayList<>();
        Mockito.when(clockPort.now()).thenReturn(now, finishedAt, updateNow);
        Mockito.when(scheduledReportQueryRepositoryPort.findDueJobs(now, 50)).thenReturn(List.of(job));
        Mockito.when(scheduledReportExecutionQueryRepositoryPort.findByJobIdAndScheduledAt(jobUuid, scheduledAt))
                .thenReturn(Optional.empty());
        Mockito.when(scheduledReportExecutionCommandRepositoryPort.save(Mockito.any(ReportExecution.class)))
                .thenAnswer(invocation -> {
                    ReportExecution execution = invocation.getArgument(0);
                    savedExecutions.add(copyExecution(execution));
                    return execution;
                });
        Mockito.when(scheduledReportCommandFactory.computeNextRunAt(scheduledAt, EFrequency.DAILY)).thenReturn(next);
        Mockito.when(scheduledReportCommandFactory.computeFirstRunAfter(next, EFrequency.DAILY, updateNow)).thenReturn(next);
        Mockito.when(scheduledReportCommandRepositoryPort.save(Mockito.any(ScheduledAuxiliaryBookJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        runner(List.of(step)).runDueScheduledReports();

        Assertions.assertThat(savedExecutions).hasSize(2);
        Assertions.assertThat(savedExecutions.get(0).getStatusExecution()).isEqualTo(EExecutionStatus.RUNNING);
        Assertions.assertThat(savedExecutions.get(1).getStatusExecution()).isEqualTo(EExecutionStatus.SUCCESS);
        Assertions.assertThat(savedExecutions.get(1).getFinishedAt()).isEqualTo(finishedAt);
        Mockito.verify(step).execute(Mockito.any(JobCommandContext.class));
        Mockito.verify(scheduledReportCommandRepositoryPort).save(job);
        Assertions.assertThat(job.getScheduleSpec().getNextRunAt()).isEqualTo(next);
    }

    @Test
    @DisplayName("runDueScheduledReports debe marcar FAILED y conservar el mensaje de error ya definido")
    void runDueScheduledReportsMarksFailureAndPreservesErrorMessage() {
        Instant now = Instant.parse("2025-01-20T08:00:00Z");
        Instant finishedAt = Instant.parse("2025-01-20T08:05:00Z");
        Instant updateNow = Instant.parse("2025-01-20T08:06:00Z");
        Instant scheduledAt = Instant.parse("2025-01-20T07:30:00Z");
        Instant next = Instant.parse("2025-01-21T07:30:00Z");
        String publicId = UUID.randomUUID().toString();
        ScheduledAuxiliaryBookJob job = job(publicId, 14L, deliveryConfig(EDeliveryWay.EMAIL, EAuxiliaryBookFormat.PDF), scheduledAt, null);
        UUID jobUuid = UUID.fromString(publicId);

        IJobCommand failingStep = Mockito.mock(IJobCommand.class);
        Mockito.doAnswer(invocation -> {
            JobCommandContext context = invocation.getArgument(0);
            ReportExecution execution = context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_EXECUTION, ReportExecution.class);
            execution.setErrorMessage("detalle controlado");
            throw new IllegalStateException("boom");
        }).when(failingStep).execute(Mockito.any(JobCommandContext.class));

        List<ReportExecution> savedExecutions = new ArrayList<>();
        Mockito.when(clockPort.now()).thenReturn(now, finishedAt, updateNow);
        Mockito.when(scheduledReportQueryRepositoryPort.findDueJobs(now, 50)).thenReturn(List.of(job));
        Mockito.when(scheduledReportExecutionQueryRepositoryPort.findByJobIdAndScheduledAt(jobUuid, scheduledAt))
                .thenReturn(Optional.empty());
        Mockito.when(scheduledReportExecutionCommandRepositoryPort.save(Mockito.any(ReportExecution.class)))
                .thenAnswer(invocation -> {
                    ReportExecution execution = invocation.getArgument(0);
                    savedExecutions.add(copyExecution(execution));
                    return execution;
                });
        Mockito.when(scheduledReportCommandFactory.computeNextRunAt(scheduledAt, EFrequency.DAILY)).thenReturn(next);
        Mockito.when(scheduledReportCommandFactory.computeFirstRunAfter(next, EFrequency.DAILY, updateNow)).thenReturn(next);
        Mockito.when(scheduledReportCommandRepositoryPort.save(Mockito.any(ScheduledAuxiliaryBookJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        runner(List.of(failingStep)).runDueScheduledReports();

        Assertions.assertThat(savedExecutions).hasSize(2);
        ReportExecution finalExecution = savedExecutions.get(1);

        Assertions.assertThat(finalExecution.getStatusExecution()).isEqualTo(EExecutionStatus.FAILED);
        Assertions.assertThat(finalExecution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.FAILED);
        Assertions.assertThat(finalExecution.getErrorMessage()).isEqualTo("detalle controlado");
    }

    private ScheduledReportJobRunner runner(List<IJobCommand> steps) {
        ScheduledReportJobRunner runner = new ScheduledReportJobRunner(
                scheduledReportQueryRepositoryPort,
                scheduledReportCommandRepositoryPort,
                scheduledReportExecutionCommandRepositoryPort,
                scheduledReportExecutionQueryRepositoryPort,
                scheduledReportCommandFactory,
                clockPort,
                steps
        );
        ReflectionTestUtils.setField(runner, "batchSize", 0);
        return runner;
    }

    private ScheduledAuxiliaryBookJob job(
            String publicId,
            Long jobId,
            DeliveryConfig deliveryConfig,
            Instant nextRunAt,
            Instant endAt
    ) {
        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setJobId(jobId);
        job.setPublicId(publicId);
        job.setBookType(co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType.ACCOUNT);
        job.setCriteria(AuxiliaryBookCriteria.builder()
                .criteriaType(ECriteriaType.ACCOUNT)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build());
        job.setScheduleSpec(new ScheduleSpec(EFrequency.DAILY, Instant.parse("2025-01-01T08:00:00Z"), Optional.ofNullable(endAt), nextRunAt));
        job.setDeliveryConfig(deliveryConfig);
        job.setEntId("ENT1");
        job.setUserId(40L);
        job.setStatus(EJobStatus.ACTIVE);
        return job;
    }

    private DeliveryConfig deliveryConfig(EDeliveryWay way, EAuxiliaryBookFormat format) {
        return new DeliveryConfig(way, format, null);
    }

    private ReportExecution copyExecution(ReportExecution source) {
        ReportExecution copy = new ReportExecution();
        copy.setExecutionId(source.getExecutionId());
        copy.setJobId(source.getJobId());
        copy.setScheduledAt(source.getScheduledAt());
        copy.setStartedAt(source.getStartedAt());
        copy.setFinishedAt(source.getFinishedAt());
        copy.setStatusExecution(source.getStatusExecution());
        copy.setDeliveryStatus(source.getDeliveryStatus());
        copy.setRetryCount(source.getRetryCount());
        copy.setCorrelationId(source.getCorrelationId());
        copy.setErrorMessage(source.getErrorMessage());
        return copy;
    }
}
