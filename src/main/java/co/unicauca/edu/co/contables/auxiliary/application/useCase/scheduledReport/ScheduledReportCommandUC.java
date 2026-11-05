package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IScheduledReportCommandPort;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.ScheduledReportCommandFactory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IClockPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

/**
 * @brief Caso de uso de escritura para reportes programados.
 *
 * Implementa el puerto de entrada {@link IScheduledReportCommandPort} y
 * coordina la creación, actualización y cancelación de jobs de reportes
 * programados. Aplica validaciones de negocio (campos requeridos, rangos
 * de fechas, frecuencia) y normaliza el {@link ScheduleSpec} calculando
 * la próxima ejecución antes de persistir el job.
 */
@Service
@RequiredArgsConstructor
public class ScheduledReportCommandUC implements IScheduledReportCommandPort {

    private final IScheduledReportCommandRepositoryPort scheduledReportCommandRepositoryPort;
    private final IScheduledReportQueryRepositoryPort scheduledReportQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;
    private final IClockPort clockPort;
    private final ScheduledReportCommandFactory scheduledReportCommandFactory;

    @Override
    public ScheduledAuxiliaryBookJob createScheduledReport(ScheduledAuxiliaryBookJob job) {
        validateJob(job);

        Instant now = clockPort.now();
        if (job.getPublicId() == null || job.getPublicId().isBlank()) {
            job.setPublicId(UUID.randomUUID().toString());
        }
        job.setCreatedAt(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
        job.setStatus(EJobStatus.ACTIVE);

        ScheduleSpec normalizedSpec = normalizeScheduleSpec(job.getScheduleSpec(), now);
        job.setScheduleSpec(normalizedSpec);

        return this.scheduledReportCommandRepositoryPort.save(job);
    }

    @Override
    public ScheduledAuxiliaryBookJob updateScheduledReport(String publicId, ScheduledAuxiliaryBookJob job) {
        if (publicId == null || publicId.isBlank()) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "publicId"));
        }
        validateJob(job);

        ScheduledAuxiliaryBookJob current = this.scheduledReportQueryRepositoryPort.findByPublicId(publicId)
                .orElseGet(() -> {
                    this.formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404,
                            this.messageServicePort.getMessage(MessageKeys.ERROR_NOT_FOUND, "ScheduledReport"));
                    return null;
                });

        Instant now = clockPort.now();

        current.setBookType(job.getBookType());
        current.setCriteria(job.getCriteria());
        current.setScheduleSpec(normalizeScheduleSpec(job.getScheduleSpec(), now));
        current.setCreatedBy(job.getCreatedBy());
        current.setEntId(job.getEntId());
        current.setUserId(job.getUserId());
        current.setDeliveryConfig(job.getDeliveryConfig());

        return this.scheduledReportCommandRepositoryPort.save(current);
    }

    @Override
    public void cancelScheduledReport(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "publicId"));
        }

        ScheduledAuxiliaryBookJob current = this.scheduledReportQueryRepositoryPort.findByPublicId(publicId)
                .orElseGet(() -> {
                    this.formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404,
                            this.messageServicePort.getMessage(MessageKeys.ERROR_NOT_FOUND, "ScheduledReport"));
                    return null;
                });

        this.scheduledReportCommandRepositoryPort.updateStatus(current.getJobId(), EJobStatus.CANCELLED);
    }

    private void validateJob(ScheduledAuxiliaryBookJob job) {
        if (job == null) {
            this.formatterResultOutputPort.returnErrorGenericResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_NULL_VALUE, "ScheduledReport"));
        }

        if (job.getBookType() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_INVALID_TYPE, "bookType", "null"));
        }

        if (job.getEntId() == null || job.getEntId().isBlank()) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "entId"));
        }

        if (job.getUserId() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "userId"));
        }

        if (job.getCriteria() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_NULL_VALUE, "criteria"));
        }

        if (job.getCriteria().getCriteriaType() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_INVALID_VALUE, "criteriaType"));
        }

        if (job.getScheduleSpec() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_NULL_VALUE, "scheduleSpec"));
        }

        if (job.getDeliveryConfig() == null || job.getDeliveryConfig().getDeliveryWay() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "deliveryWay"));
        }

        if (job.getScheduleSpec().getFrequency() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.ERROR_INVALID_TYPE, "frequency", "null"));
        }

        if (job.getScheduleSpec().getStartAt() == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_FIELD_REQUIRED, "startAt"));
        }

        Optional<Instant> endAt = job.getScheduleSpec().getEndAt();
        if (endAt != null && endAt.isPresent()) {
            if (endAt.get().isBefore(job.getScheduleSpec().getStartAt())) {
                this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                        this.messageServicePort.getMessage(MessageKeys.VALIDATION_DATE_RANGE_INVALID));
            }
        }

        validateCriteriaDates(job.getCriteria());
    }

    private void validateCriteriaDates(AuxiliaryBookCriteria criteria) {
        LocalDate startDate = criteria.getStartDate();
        LocalDate endDate = criteria.getEndDate();

        if (startDate == null || endDate == null) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_DATE_RANGE_INCOMPLETE));
        }

        if (endDate.isBefore(startDate)) {
            this.formatterResultOutputPort.returnBusinessRuleErrorResponse(400,
                    this.messageServicePort.getMessage(MessageKeys.VALIDATION_DATE_RANGE_INVALID));
        }
    }

    private ScheduleSpec normalizeScheduleSpec(ScheduleSpec scheduleSpec, Instant now) {
        Instant startAt = scheduleSpec.getStartAt();
        Instant endAt = scheduleSpec.getEndAt() != null ? scheduleSpec.getEndAt().orElse(null) : null;
        Instant nextRunAt = scheduledReportCommandFactory.computeFirstRunAfter(startAt, scheduleSpec.getFrequency(), now);
        return new ScheduleSpec(scheduleSpec.getFrequency(), startAt, Optional.ofNullable(endAt), nextRunAt);
    }
}
