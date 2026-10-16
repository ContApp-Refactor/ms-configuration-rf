package co.unicauca.edu.co.contables.auxiliary.unit.application.history;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.history.HistoryAuxiliaryBookQueryUC;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.ScheduledReportCommandUC;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.ScheduledReportCommandFactory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.EmailConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IClockPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.BusinessRuleException;

/**
 * @brief Pruebas unitarias para {@link HistoryAuxiliaryBookQueryUC},
 * {@link ScheduledReportCommandFactory} y {@link ScheduledReportCommandUC}.
 */
@ExtendWith(MockitoExtension.class)
class HistoryAuxiliaryBookQueryUCTest {

    @Mock
    private IAuxiliaryBookHistoryQueryRepositoryPort historyRepositoryPort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @Mock
    private IMessageServicePort messageServicePort;

    @Mock
    private IScheduledReportCommandRepositoryPort scheduledReportCommandRepositoryPort;

    @Mock
    private IScheduledReportQueryRepositoryPort scheduledReportQueryRepositoryPort;

    @Mock
    private IClockPort clockPort;

    @Mock
    private ScheduledReportCommandFactory scheduledReportCommandFactory;

    @InjectMocks
    private HistoryAuxiliaryBookQueryUC useCase;

    @Test
    @DisplayName("findPageByEntId delega en el repositorio cuando entId es válido")
    void findPageByEntIdWithValidId() {
        String entId = "ent-001";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuxiliaryBookHistory> expected = new PageImpl<>(List.of(new AuxiliaryBookHistory()));

        Mockito.when(historyRepositoryPort.findPageByEntId(entId, pageable)).thenReturn(expected);

        Page<AuxiliaryBookHistory> result = useCase.findPageByEntId(entId, pageable);

        Assertions.assertThat(result).isSameAs(expected);
        Mockito.verify(historyRepositoryPort).findPageByEntId(entId, pageable);
        Mockito.verifyNoInteractions(formatterResultOutputPort);
    }

    @Test
    @DisplayName("findPageByEntId invoca el formatter cuando entId está vacío")
    void findPageByEntIdWithEmptyIdInvokesFormatter() {
        String entId = "";
        Pageable pageable = PageRequest.of(0, 10);

        Mockito.when(messageServicePort.getMessage(Mockito.anyString())).thenReturn("campo requerido");
        Mockito.when(historyRepositoryPort.findPageByEntId(entId, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        useCase.findPageByEntId(entId, pageable);

        Mockito.verify(formatterResultOutputPort)
                .returnBusinessRuleErrorResponse(Mockito.eq(404), Mockito.anyString());
        Mockito.verify(messageServicePort).getMessage(Mockito.anyString());
    }

    @Test
    @DisplayName("findPageByEntId lanza NullPointerException cuando entId es null")
    void findPageByEntIdWithNullThrows() {
        Pageable pageable = PageRequest.of(0, 10);

        Assertions.assertThatThrownBy(() -> useCase.findPageByEntId(null, pageable))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("ScheduledReportCommandFactory debe calcular la siguiente ejecución según la frecuencia")
    void scheduledReportCommandFactoryComputesNextRunAt() {
        ScheduledReportCommandFactory factory = new ScheduledReportCommandFactory();
        Instant base = Instant.parse("2025-01-15T10:00:00Z");

        Assertions.assertThat(factory.computeNextRunAt(base, EFrequency.DAILY))
                .isEqualTo(base.atZone(ZoneId.systemDefault()).plusDays(1).toInstant());
        Assertions.assertThat(factory.computeNextRunAt(base, EFrequency.WEEKLY))
                .isEqualTo(base.atZone(ZoneId.systemDefault()).plusWeeks(1).toInstant());
        Assertions.assertThat(factory.computeNextRunAt(base, EFrequency.MONTHLY))
                .isEqualTo(base.atZone(ZoneId.systemDefault()).plusMonths(1).toInstant());
    }

    @Test
    @DisplayName("ScheduledReportCommandFactory debe avanzar ciclos hasta encontrar la primera corrida futura")
    void scheduledReportCommandFactoryComputesFirstRunAfterNow() {
        ScheduledReportCommandFactory factory = new ScheduledReportCommandFactory();
        Instant startAt = Instant.parse("2025-01-01T10:00:00Z");
        Instant now = Instant.parse("2025-01-03T12:00:00Z");

        Instant result = factory.computeFirstRunAfter(startAt, EFrequency.DAILY, now);

        Assertions.assertThat(result).isEqualTo(Instant.parse("2025-01-04T10:00:00Z"));
    }

    @Test
    @DisplayName("ScheduledReportCommandFactory debe retornar el inicio original cuando faltan datos para recalcular")
    void scheduledReportCommandFactoryReturnsStartAtWhenFrequencyOrNowMissing() {
        ScheduledReportCommandFactory factory = new ScheduledReportCommandFactory();
        Instant startAt = Instant.parse("2025-01-01T10:00:00Z");

        Assertions.assertThat(factory.computeFirstRunAfter(startAt, null, Instant.now())).isEqualTo(startAt);
        Assertions.assertThat(factory.computeFirstRunAfter(startAt, EFrequency.DAILY, null)).isEqualTo(startAt);
        Assertions.assertThat(factory.computeFirstRunAfter(null, EFrequency.DAILY, Instant.now())).isNull();
    }

    @Test
    @DisplayName("createScheduledReport debe asignar publicId, createdAt, status y nextRunAt normalizado")
    void createScheduledReportSetsDefaultsAndPersists() {
        ScheduledReportCommandUC scheduledReportCommandUC = new ScheduledReportCommandUC(
                scheduledReportCommandRepositoryPort,
                scheduledReportQueryRepositoryPort,
                formatterResultOutputPort,
                messageServicePort,
                clockPort,
                scheduledReportCommandFactory
        );
        Instant now = Instant.parse("2025-01-20T15:00:00Z");
        Instant startAt = Instant.parse("2025-01-20T18:00:00Z");
        Instant nextRunAt = Instant.parse("2025-01-20T18:00:00Z");
        ScheduledAuxiliaryBookJob job = scheduledJob(null, "ENT1", 99L, startAt, null);

        Mockito.when(clockPort.now()).thenReturn(now);
        Mockito.when(scheduledReportCommandFactory.computeFirstRunAfter(startAt, EFrequency.DAILY, now)).thenReturn(nextRunAt);
        Mockito.when(scheduledReportCommandRepositoryPort.save(Mockito.any(ScheduledAuxiliaryBookJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScheduledAuxiliaryBookJob result = scheduledReportCommandUC.createScheduledReport(job);

        Assertions.assertThat(result.getPublicId()).isNotBlank();
        Assertions.assertThat(result.getStatus()).isEqualTo(EJobStatus.ACTIVE);
        Assertions.assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
        Assertions.assertThat(result.getScheduleSpec().getNextRunAt()).isEqualTo(nextRunAt);
        Mockito.verify(scheduledReportCommandRepositoryPort).save(job);
    }

    @Test
    @DisplayName("updateScheduledReport debe sobreescribir los campos editables y persistir el job actual")
    void updateScheduledReportUpdatesCurrentJob() {
        ScheduledReportCommandUC scheduledReportCommandUC = new ScheduledReportCommandUC(
                scheduledReportCommandRepositoryPort,
                scheduledReportQueryRepositoryPort,
                formatterResultOutputPort,
                messageServicePort,
                clockPort,
                scheduledReportCommandFactory
        );
        Instant now = Instant.parse("2025-01-20T15:00:00Z");
        Instant startAt = Instant.parse("2025-01-21T08:00:00Z");
        Instant nextRunAt = Instant.parse("2025-01-21T08:00:00Z");
        ScheduledAuxiliaryBookJob current = scheduledJob("job-public-id", "ENT0", 1L, Instant.parse("2025-01-19T08:00:00Z"), null);
        current.setJobId(40L);
        current.setCreatedAt(LocalDateTime.of(2025, 1, 10, 9, 0));

        ScheduledAuxiliaryBookJob updatedData = scheduledJob("ignored", "ENT9", 200L, startAt, Instant.parse("2025-02-01T08:00:00Z"));
        updatedData.setBookType(EAuxiliaryBookType.THIRD_PARTY);
        updatedData.setCreatedBy("editor");

        Mockito.when(scheduledReportQueryRepositoryPort.findByPublicId("job-public-id")).thenReturn(Optional.of(current));
        Mockito.when(clockPort.now()).thenReturn(now);
        Mockito.when(scheduledReportCommandFactory.computeFirstRunAfter(startAt, EFrequency.DAILY, now)).thenReturn(nextRunAt);
        Mockito.when(scheduledReportCommandRepositoryPort.save(Mockito.any(ScheduledAuxiliaryBookJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScheduledAuxiliaryBookJob result = scheduledReportCommandUC.updateScheduledReport("job-public-id", updatedData);

        Assertions.assertThat(result).isSameAs(current);
        Assertions.assertThat(current.getEntId()).isEqualTo("ENT9");
        Assertions.assertThat(current.getUserId()).isEqualTo(200L);
        Assertions.assertThat(current.getBookType()).isEqualTo(EAuxiliaryBookType.THIRD_PARTY);
        Assertions.assertThat(current.getCreatedBy()).isEqualTo("editor");
        Assertions.assertThat(current.getScheduleSpec().getNextRunAt()).isEqualTo(nextRunAt);
        Mockito.verify(scheduledReportCommandRepositoryPort).save(current);
    }

    @Test
    @DisplayName("cancelScheduledReport debe actualizar el estado a CANCELLED")
    void cancelScheduledReportUpdatesStatusToCancelled() {
        ScheduledReportCommandUC scheduledReportCommandUC = new ScheduledReportCommandUC(
                scheduledReportCommandRepositoryPort,
                scheduledReportQueryRepositoryPort,
                formatterResultOutputPort,
                messageServicePort,
                clockPort,
                scheduledReportCommandFactory
        );
        ScheduledAuxiliaryBookJob current = scheduledJob("job-public-id", "ENT1", 88L, Instant.parse("2025-01-20T08:00:00Z"), null);
        current.setJobId(55L);

        Mockito.when(scheduledReportQueryRepositoryPort.findByPublicId("job-public-id")).thenReturn(Optional.of(current));

        scheduledReportCommandUC.cancelScheduledReport("job-public-id");

        Mockito.verify(scheduledReportCommandRepositoryPort).updateStatus(55L, EJobStatus.CANCELLED);
    }

    @Test
    @DisplayName("createScheduledReport debe delegar al formatter cuando faltan campos requeridos")
    void createScheduledReportDelegatesToFormatterOnValidationError() {
        ScheduledReportCommandUC scheduledReportCommandUC = new ScheduledReportCommandUC(
                scheduledReportCommandRepositoryPort,
                scheduledReportQueryRepositoryPort,
                formatterResultOutputPort,
                messageServicePort,
                clockPort,
                scheduledReportCommandFactory
        );
        ScheduledAuxiliaryBookJob invalidJob = scheduledJob(null, "", 99L, Instant.parse("2025-01-20T18:00:00Z"), null);

        Mockito.when(messageServicePort.getMessage(Mockito.anyString(), Mockito.anyString())).thenReturn("validation");
        Mockito.doThrow(new BusinessRuleException(400, "validation"))
                .when(formatterResultOutputPort)
                .returnBusinessRuleErrorResponse(Mockito.eq(400), Mockito.anyString());

        Assertions.assertThatThrownBy(() -> scheduledReportCommandUC.createScheduledReport(invalidJob))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("validation");

        Mockito.verifyNoInteractions(scheduledReportCommandRepositoryPort);
    }

    private ScheduledAuxiliaryBookJob scheduledJob(
            String publicId,
            String entId,
            Long userId,
            Instant startAt,
            Instant endAt
    ) {
        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setPublicId(publicId);
        job.setBookType(EAuxiliaryBookType.ACCOUNT);
        job.setCriteria(AuxiliaryBookCriteria.builder()
                .criteriaType(ECriteriaType.ACCOUNT)
                .criteriaRange(new CriteriaRange(1105L, 1105L))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build());
        job.setScheduleSpec(new ScheduleSpec(EFrequency.DAILY, startAt, Optional.ofNullable(endAt), startAt));
        job.setDeliveryConfig(new DeliveryConfig(
                EDeliveryWay.EMAIL,
                null,
                new EmailConfig("test@example.com", "subject", "body")
        ));
        job.setCreatedBy("tester");
        job.setEntId(entId);
        job.setUserId(userId);
        return job;
    }
}
