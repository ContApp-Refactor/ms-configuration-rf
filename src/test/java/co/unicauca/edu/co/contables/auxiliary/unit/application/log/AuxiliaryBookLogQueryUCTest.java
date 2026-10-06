package co.unicauca.edu.co.contables.auxiliary.unit.application.log;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IEmailSenderPort;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.INotificationPusherPort;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.log.AuxiliaryBookLogQueryUC;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.jobSteps.PrepareDownloadStep;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.jobSteps.SendEmailStep;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ENotificationType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.EmailConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.notification.INotificationCommandRepositoryPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @brief Pruebas unitarias para {@link AuxiliaryBookLogQueryUC},
 * {@link PrepareDownloadStep} y {@link SendEmailStep}.
 */
@ExtendWith(MockitoExtension.class)
class AuxiliaryBookLogQueryUCTest {

    @Mock
    private IAuxiliaryBookLogQueryRepositoryPort repositoryPort;

    @Mock
    private IEmailSenderPort emailSenderPort;

    @Mock
    private INotificationCommandRepositoryPort notificationCommandRepositoryPort;

    @Mock
    private INotificationPusherPort notificationPusherPort;

    @InjectMocks
    private AuxiliaryBookLogQueryUC useCase;

    @Test
    @DisplayName("findAllByAuxiliaryBookPublicId delega en el repositorio y retorna la lista")
    void findAllByAuxiliaryBookPublicIdDelegates() {
        String publicId = "uuid-123";
        List<AuxiliaryBookLog> logs = List.of(
                AuxiliaryBookLog.builder().id(1L).build(),
                AuxiliaryBookLog.builder().id(2L).build()
        );
        Mockito.when(repositoryPort.findAllByAuxiliaryBookPublicId(publicId)).thenReturn(logs);

        List<AuxiliaryBookLog> result = useCase.findAllByAuxiliaryBookPublicId(publicId);

        Assertions.assertThat(result).hasSize(2).isSameAs(logs);
        Mockito.verify(repositoryPort).findAllByAuxiliaryBookPublicId(publicId);
        Mockito.verifyNoMoreInteractions(repositoryPort);
    }

    @Test
    @DisplayName("findAllByAuxiliaryBookPublicId retorna lista vacía si no hay registros")
    void findAllByAuxiliaryBookPublicIdEmpty() {
        Mockito.when(repositoryPort.findAllByAuxiliaryBookPublicId("missing")).thenReturn(List.of());

        List<AuxiliaryBookLog> result = useCase.findAllByAuxiliaryBookPublicId("missing");

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("PrepareDownloadStep debe marcar FAILED cuando no hay canal configurado")
    void prepareDownloadStepFailsWhenNoDeliveryIsEnabled() {
        PrepareDownloadStep step = new PrepareDownloadStep(notificationCommandRepositoryPort, notificationPusherPort);
        ReportExecution execution = newExecution();
        JobCommandContext context = context(execution, false, false, null, null);

        step.execute(context);

        Assertions.assertThat(execution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.FAILED);
        Assertions.assertThat(execution.getErrorMessage()).contains("No delivery method configured");
        Mockito.verifyNoInteractions(notificationCommandRepositoryPort, notificationPusherPort);
    }

    @Test
    @DisplayName("PrepareDownloadStep debe dejar READY_FOR_DOWNLOAD y emitir notificación cuando descarga está habilitada")
    void prepareDownloadStepMarksReadyForDownloadAndNotifies() {
        PrepareDownloadStep step = new PrepareDownloadStep(notificationCommandRepositoryPort, notificationPusherPort);
        ReportExecution execution = newExecution();
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.ACCOUNT);
        job.setOwnerSub("sub-99");
        JobCommandContext context = context(execution, true, false, null, job);

        Mockito.when(notificationCommandRepositoryPort.save(Mockito.any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        step.execute(context);

        Assertions.assertThat(execution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.READY_FOR_DOWNLOAD);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        Mockito.verify(notificationCommandRepositoryPort).save(notificationCaptor.capture());
        Notification persisted = notificationCaptor.getValue();
        Assertions.assertThat(persisted.getUserId()).isEqualTo("sub-99");
        Assertions.assertThat(persisted.getType()).isEqualTo(ENotificationType.SCHEDULED_REPORT_READY);
        Assertions.assertThat(persisted.getReferenceId()).isEqualTo(execution.getExecutionId());
        Assertions.assertThat(persisted.getReferencePublicId()).isEqualTo("job-public-id");
        Assertions.assertThat(persisted.isRead()).isFalse();
        Mockito.verify(notificationPusherPort).push(Mockito.eq("sub-99"), Mockito.any(Notification.class));
    }

    @Test
    @DisplayName("PrepareDownloadStep no debe alterar el estado cuando solo el email está habilitado")
    void prepareDownloadStepKeepsStatusWhenOnlyEmailIsEnabled() {
        PrepareDownloadStep step = new PrepareDownloadStep(notificationCommandRepositoryPort, notificationPusherPort);
        ReportExecution execution = newExecution();
        execution.setDeliveryStatus(EDeliveryStatus.NONE);
        JobCommandContext context = context(execution, false, true, null, null);

        step.execute(context);

        Assertions.assertThat(execution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.NONE);
        Assertions.assertThat(execution.getErrorMessage()).isNull();
        Mockito.verifyNoInteractions(notificationCommandRepositoryPort, notificationPusherPort);
    }

    @Test
    @DisplayName("PrepareDownloadStep marca READY_FOR_DOWNLOAD pero omite la notificación si el job no tiene userId")
    void prepareDownloadStepSkipsNotificationWhenUserIdIsNull() {
        PrepareDownloadStep step = new PrepareDownloadStep(notificationCommandRepositoryPort, notificationPusherPort);
        ReportExecution execution = newExecution();
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.ACCOUNT);
        job.setOwnerSub(null);
        JobCommandContext context = context(execution, true, false, null, job);

        step.execute(context);

        Assertions.assertThat(execution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.READY_FOR_DOWNLOAD);
        Mockito.verifyNoInteractions(notificationCommandRepositoryPort, notificationPusherPort);
    }

    @Test
    @DisplayName("SendEmailStep no debe hacer nada cuando el envío por email está deshabilitado")
    void sendEmailStepDoesNothingWhenEmailDisabled() {
        SendEmailStep step = new SendEmailStep(emailSenderPort);
        ReportExecution execution = newExecution();
        JobCommandContext context = context(execution, true, false, null, null);

        step.execute(context);

        Assertions.assertThat(execution.getDeliveryStatus()).isNull();
        Mockito.verifyNoInteractions(emailSenderPort);
    }

    @Test
    @DisplayName("SendEmailStep debe enviar correo con asunto, cuerpo y adjunto esperados")
    void sendEmailStepSendsEmailAndMarksExecution() {
        SendEmailStep step = new SendEmailStep(emailSenderPort);
        ReportExecution execution = newExecution();
        byte[] attachment = new byte[]{4, 5, 6};
        DeliveryConfig deliveryConfig = new DeliveryConfig(
                co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay.EMAIL,
                EAuxiliaryBookFormat.EXCEL,
                new EmailConfig("destinatario@example.com", "Asunto custom", "Cuerpo custom")
        );
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.ACCOUNTING_MOVEMENT);
        JobCommandContext context = context(execution, false, true, deliveryConfig, job);
        context.putAttribute(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, EAuxiliaryBookFormat.EXCEL);
        context.putAttribute(JobCommandContext.ATTRIBUTE_REPORT_BYTES, attachment);

        step.execute(context);

        Mockito.verify(emailSenderPort).sendReport(
                "destinatario@example.com",
                "Asunto custom",
                "Cuerpo custom",
                attachment,
                "ACCOUNTING_MOVEMENT_scheduled_report.xlsx"
        );
        Assertions.assertThat(execution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.EMAIL_SENT);
        Assertions.assertThat(execution.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("SendEmailStep debe marcar EMAIL_FAILED cuando la configuración de email es inválida")
    void sendEmailStepMarksFailureWhenEmailConfigIsInvalid() {
        SendEmailStep step = new SendEmailStep(emailSenderPort);
        ReportExecution execution = newExecution();
        DeliveryConfig deliveryConfig = new DeliveryConfig(
                co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay.EMAIL,
                EAuxiliaryBookFormat.PDF,
                new EmailConfig(" ", null, null)
        );
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.THIRD_PARTY);
        JobCommandContext context = context(execution, false, true, deliveryConfig, job);
        context.putAttribute(JobCommandContext.ATTRIBUTE_REPORT_BYTES, new byte[]{1});

        step.execute(context);

        Assertions.assertThat(execution.getDeliveryStatus()).isEqualTo(EDeliveryStatus.EMAIL_FAILED);
        Assertions.assertThat(execution.getErrorMessage()).contains("Email delivery failed");
        Mockito.verifyNoInteractions(emailSenderPort);
    }

    private ReportExecution newExecution() {
        ReportExecution execution = new ReportExecution();
        execution.setExecutionId(UUID.randomUUID());
        return execution;
    }

    private JobCommandContext context(
            ReportExecution execution,
            boolean downloadEnabled,
            boolean emailEnabled,
            DeliveryConfig deliveryConfig,
            ScheduledAuxiliaryBookJob job
    ) {
        ScheduledAuxiliaryBookJob resolvedJob = job != null ? job : scheduledJob(EAuxiliaryBookType.ACCOUNT);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JobCommandContext.ATTRIBUTE_EXECUTION, execution);
        attributes.put(JobCommandContext.ATTRIBUTE_DOWNLOAD_ENABLED, downloadEnabled);
        attributes.put(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, emailEnabled);
        attributes.put(JobCommandContext.ATTRIBUTE_JOB, resolvedJob);
        if (deliveryConfig != null) {
            attributes.put(JobCommandContext.ATTRIBUTE_DELIVERY_CONFIG, deliveryConfig);
        }
        return new JobCommandContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-20T08:00:00Z"),
                "corr-id",
                "scheduler",
                attributes
        );
    }

    private ScheduledAuxiliaryBookJob scheduledJob(EAuxiliaryBookType bookType) {
        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setPublicId("job-public-id");
        job.setBookType(bookType);
        job.setOwnerSub("sub-99");
        job.setCriteria(AuxiliaryBookCriteria.builder()
                .criteriaType(ECriteriaType.ACCOUNT)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build());
        job.setScheduleSpec(new ScheduleSpec(EFrequency.DAILY, Instant.parse("2025-01-20T08:00:00Z"), Optional.empty(), Instant.parse("2025-01-20T08:00:00Z")));
        return job;
    }
}
