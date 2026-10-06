package co.unicauca.edu.co.contables.auxiliary.unit.application.log;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.AuxiliaryBookCommandUC;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AuxiliaryBookProcessor;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.log.AuxiliaryBookLogCommandUC;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.jobSteps.GenerateReportStep;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.AuxiliaryBookReportGenerator;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.AuxiliaryBookReportGenerator.GenerationResult;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.commands.JobCommandContext;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBook.IAuxiliaryBookCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.GenericErrorException;

/**
 * @brief Pruebas unitarias para {@link AuxiliaryBookLogCommandUC},
 * {@link AuxiliaryBookCommandUC} y {@link GenerateReportStep}.
 */
@ExtendWith(MockitoExtension.class)
class AuxiliaryBookLogCommandUCTest {

    @Mock
    private IAuxiliaryBookLogCommandRepositoryPort repositoryPort;

    @Mock
    private IAuxiliaryBookCommandRepositoryPort auxiliaryBookCommandRepositoryPort;

    @Mock
    private IAuxiliaryBookHistoryCommandRepositoryPort auxiliaryBookHistoryCommandRepositoryPort;

    @Mock
    private IAccountingInfoClient accountingInfoClient;

    @Mock
    private AuxiliaryBookProcessor auxiliaryBookProcessor;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @Mock
    private IMessageServicePort messageServicePort;

    @Mock
    private AuxiliaryBookReportGenerator auxiliaryBookReportGenerator;

    @InjectMocks
    private AuxiliaryBookLogCommandUC useCase;

    @Test
    @DisplayName("registerAuxiliaryBookLog delega en el repositorio y retorna su resultado")
    void registerAuxiliaryBookLogDelegates() {
        AuxiliaryBookLog input = AuxiliaryBookLog.builder().message("evento").build();
        AuxiliaryBookLog persisted = AuxiliaryBookLog.builder().id(10L).message("evento").build();

        Mockito.when(repositoryPort.registerAuxiliaryBookLog(input)).thenReturn(persisted);

        AuxiliaryBookLog result = useCase.registerAuxiliaryBookLog(input);

        Assertions.assertThat(result).isSameAs(persisted);
        Mockito.verify(repositoryPort).registerAuxiliaryBookLog(input);
        Mockito.verifyNoMoreInteractions(repositoryPort);
    }

    @Test
    @DisplayName("registerAuxiliaryBook debe generar publicId, historial GENERATED y log REGISTERED")
    void registerAuxiliaryBookCreatesHistoryAndLog() {
        AuxiliaryBookCommandUC commandUC = new AuxiliaryBookCommandUC(
                auxiliaryBookCommandRepositoryPort,
                repositoryPort,
                auxiliaryBookHistoryCommandRepositoryPort,
                accountingInfoClient,
                auxiliaryBookProcessor,
                formatterResultOutputPort,
                messageServicePort
        );
        AuxiliaryBook input = AuxiliaryBook.builder()
                .type(EAuxiliaryBookType.ACCOUNT)
                .entId("ENT1")
                .userId(30L)
                .criteria(criteria())
                .format(EAuxiliaryBookFormat.PDF)
                .build();

        Mockito.when(auxiliaryBookCommandRepositoryPort.registerAuxiliaryBook(Mockito.any(AuxiliaryBook.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuxiliaryBook result = commandUC.registerAuxiliaryBook(input);

        ArgumentCaptor<AuxiliaryBookHistory> historyCaptor = ArgumentCaptor.forClass(AuxiliaryBookHistory.class);
        ArgumentCaptor<AuxiliaryBookLog> logCaptor = ArgumentCaptor.forClass(AuxiliaryBookLog.class);

        Mockito.verify(auxiliaryBookHistoryCommandRepositoryPort).registerAuxiliaryBookHistory(historyCaptor.capture());
        Mockito.verify(repositoryPort).registerAuxiliaryBookLog(logCaptor.capture());

        Assertions.assertThat(result.getPublicId()).isNotBlank();
        Assertions.assertThat(historyCaptor.getValue().getState()).isEqualTo(EState.GENERATED);
        Assertions.assertThat(historyCaptor.getValue().getAuxiliaryBook()).isSameAs(result);
        Assertions.assertThat(logCaptor.getValue().getETypeEvent()).isEqualTo(ETypeEvent.REGISTERED);
        Assertions.assertThat(logCaptor.getValue().getMessage()).contains("registered successfully");
    }

    @Test
    @DisplayName("registerAuxiliaryBook debe delegar al formatter cuando el libro es nulo")
    void registerAuxiliaryBookDelegatesToFormatterForNullBook() {
        AuxiliaryBookCommandUC commandUC = new AuxiliaryBookCommandUC(
                auxiliaryBookCommandRepositoryPort,
                repositoryPort,
                auxiliaryBookHistoryCommandRepositoryPort,
                accountingInfoClient,
                auxiliaryBookProcessor,
                formatterResultOutputPort,
                messageServicePort
        );

        Mockito.when(messageServicePort.getMessage(Mockito.anyString(), Mockito.anyString())).thenReturn("AuxiliaryBook");
        Mockito.doThrow(new GenericErrorException(400, "AuxiliaryBook"))
                .when(formatterResultOutputPort)
                .returnErrorGenericResponse(Mockito.eq(400), Mockito.anyString());

        Assertions.assertThatThrownBy(() -> commandUC.registerAuxiliaryBook(null))
                .isInstanceOf(GenericErrorException.class)
                .hasMessageContaining("AuxiliaryBook");

        Mockito.verifyNoInteractions(auxiliaryBookCommandRepositoryPort, auxiliaryBookHistoryCommandRepositoryPort, repositoryPort);
    }

    @Test
    @DisplayName("genereteAuxiliaryBookInfo debe registrar logs de inicio y éxito")
    void generateAuxiliaryBookInfoLogsStartAndSuccess() {
        AuxiliaryBookCommandUC commandUC = new AuxiliaryBookCommandUC(
                auxiliaryBookCommandRepositoryPort,
                repositoryPort,
                auxiliaryBookHistoryCommandRepositoryPort,
                accountingInfoClient,
                auxiliaryBookProcessor,
                formatterResultOutputPort,
                messageServicePort
        );
        AuxiliaryBook book = AuxiliaryBook.builder()
                .publicId("book-public-id")
                .criteria(criteria())
                .build();
        List<String> processedData = List.of("a", "b");

        Mockito.doReturn(processedData)
                .when(auxiliaryBookProcessor)
                .processAuxiliaryBookData(accountingInfoClient, book);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) commandUC.genereteAuxiliaryBookInfo(book);

        ArgumentCaptor<AuxiliaryBookLog> logCaptor = ArgumentCaptor.forClass(AuxiliaryBookLog.class);
        Mockito.verify(repositoryPort, Mockito.times(2)).registerAuxiliaryBookLog(logCaptor.capture());

        List<AuxiliaryBookLog> logs = logCaptor.getAllValues();
        Assertions.assertThat(result).containsExactly("a", "b");
        Assertions.assertThat(logs.get(0).getETypeEvent()).isEqualTo(ETypeEvent.GENERATING);
        Assertions.assertThat(logs.get(1).getETypeEvent()).isEqualTo(ETypeEvent.SUCCESS_GENERATION);
        Assertions.assertThat(logs.get(1).getMessage()).contains("2 items processed");
    }

    @Test
    @DisplayName("genereteAuxiliaryBookInfo debe registrar error y delegar al formatter cuando falla el procesamiento")
    void generateAuxiliaryBookInfoLogsErrorAndDelegatesToFormatter() {
        AuxiliaryBookCommandUC commandUC = new AuxiliaryBookCommandUC(
                auxiliaryBookCommandRepositoryPort,
                repositoryPort,
                auxiliaryBookHistoryCommandRepositoryPort,
                accountingInfoClient,
                auxiliaryBookProcessor,
                formatterResultOutputPort,
                messageServicePort
        );
        AuxiliaryBook book = AuxiliaryBook.builder()
                .publicId("book-public-id")
                .criteria(criteria())
                .build();

        Mockito.when(auxiliaryBookProcessor.processAuxiliaryBookData(accountingInfoClient, book))
                .thenThrow(new IllegalStateException("boom"));
        Mockito.when(messageServicePort.getMessage(Mockito.anyString(), Mockito.anyString())).thenReturn("formatted");
        Mockito.doThrow(new GenericErrorException(500, "formatted"))
                .when(formatterResultOutputPort)
                .returnErrorGenericResponse(Mockito.eq(500), Mockito.anyString());

        Assertions.assertThatThrownBy(() -> commandUC.genereteAuxiliaryBookInfo(book))
                .isInstanceOf(GenericErrorException.class)
                .hasMessageContaining("formatted");

        ArgumentCaptor<AuxiliaryBookLog> logCaptor = ArgumentCaptor.forClass(AuxiliaryBookLog.class);
        Mockito.verify(repositoryPort, Mockito.times(2)).registerAuxiliaryBookLog(logCaptor.capture());
        Assertions.assertThat(logCaptor.getAllValues().get(1).getETypeEvent()).isEqualTo(ETypeEvent.ERROR_GENERATION);
        Assertions.assertThat(logCaptor.getAllValues().get(1).getMessage()).contains("boom");
    }

    @Test
    @DisplayName("GenerateReportStep debe ejecutar el generador y guardar atributos cuando el email está habilitado")
    void generateReportStepStoresArtifactsInContext() {
        GenerateReportStep step = new GenerateReportStep(auxiliaryBookReportGenerator);
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.ACCOUNT);
        AuxiliaryBook registeredBook = AuxiliaryBook.builder()
                .publicId("book-public-id")
                .type(EAuxiliaryBookType.ACCOUNT)
                .entId("ENT1")
                .userId(90L)
                .criteria(criteria())
                .format(EAuxiliaryBookFormat.PDF)
                .build();
        List<String> reportData = List.of("fila-1", "fila-2");
        byte[] bytes = new byte[]{1, 2, 3};

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JobCommandContext.ATTRIBUTE_JOB, job);
        attributes.put(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, true);
        attributes.put(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, EAuxiliaryBookFormat.PDF);
        JobCommandContext context = new JobCommandContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-20T08:00:00Z"),
                "corr-1",
                "scheduler",
                attributes
        );

        Mockito.when(auxiliaryBookReportGenerator.generate(job, EAuxiliaryBookFormat.PDF))
                .thenReturn(new GenerationResult(registeredBook, reportData, bytes, EAuxiliaryBookFormat.PDF));

        step.execute(context);

        Assertions.assertThat(context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_REGISTERED_BOOK, AuxiliaryBook.class))
                .isSameAs(registeredBook);
        @SuppressWarnings("unchecked")
        List<String> reportDataResult = (List<String>) context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_REPORT_DATA, List.class);
        Assertions.assertThat(reportDataResult).isEqualTo(reportData);
        Assertions.assertThat(context.getRequiredAttribute(JobCommandContext.ATTRIBUTE_REPORT_BYTES, byte[].class))
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("GenerateReportStep debe ser no-op cuando el email no está habilitado (modo DOWNLOAD puro)")
    void generateReportStepIsNoOpWhenEmailDisabled() {
        GenerateReportStep step = new GenerateReportStep(auxiliaryBookReportGenerator);
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.THIRD_PARTY);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JobCommandContext.ATTRIBUTE_JOB, job);
        attributes.put(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, false);
        attributes.put(JobCommandContext.ATTRIBUTE_DOWNLOAD_ENABLED, true);
        JobCommandContext context = new JobCommandContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-20T08:00:00Z"),
                "corr-2",
                "scheduler",
                attributes
        );

        step.execute(context);

        Mockito.verifyNoInteractions(auxiliaryBookReportGenerator);
        Assertions.assertThat(context.getAttribute(JobCommandContext.ATTRIBUTE_REPORT_BYTES, byte[].class)).isNull();
        Assertions.assertThat(context.getAttribute(JobCommandContext.ATTRIBUTE_REGISTERED_BOOK, AuxiliaryBook.class)).isNull();
    }

    @Test
    @DisplayName("GenerateReportStep debe propagar excepción si el generador falla")
    void generateReportStepPropagatesGeneratorFailure() {
        GenerateReportStep step = new GenerateReportStep(auxiliaryBookReportGenerator);
        ScheduledAuxiliaryBookJob job = scheduledJob(EAuxiliaryBookType.THIRD_PARTY);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JobCommandContext.ATTRIBUTE_JOB, job);
        attributes.put(JobCommandContext.ATTRIBUTE_EMAIL_ENABLED, true);
        attributes.put(JobCommandContext.ATTRIBUTE_REPORT_FORMAT, EAuxiliaryBookFormat.EXCEL);
        JobCommandContext context = new JobCommandContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2025-01-20T08:00:00Z"),
                "corr-3",
                "scheduler",
                attributes
        );

        Mockito.when(auxiliaryBookReportGenerator.generate(job, EAuxiliaryBookFormat.EXCEL))
                .thenThrow(new IllegalStateException("Exported report content is empty"));
        Assertions.assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("empty");
    }

    private AuxiliaryBookCriteria criteria() {
        return AuxiliaryBookCriteria.builder()
                .criteriaType(ECriteriaType.ACCOUNT)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build();
    }

    private ScheduledAuxiliaryBookJob scheduledJob(EAuxiliaryBookType bookType) {
        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setBookType(bookType);
        job.setEntId("ENT1");
        job.setUserId(90L);
        job.setCriteria(criteria());
        job.setScheduleSpec(new ScheduleSpec(
                co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency.DAILY,
                Instant.parse("2025-01-20T08:00:00Z"),
                Optional.empty(),
                Instant.parse("2025-01-20T08:00:00Z")
        ));
        return job;
    }
}
