package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.ScheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.EmailConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;

import java.net.MalformedURLException;
import java.net.URL;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookCriteriaCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.scheduledReport.IScheduledReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @brief Adaptador de escritura para los trabajos de reportes programados.
 *
 * Implementa {@link IScheduledReportCommandRepositoryPort} persistiendo
 * {@link ScheduledAuxiliaryBookJob} en la base de datos mediante
 * {@link IScheduledReportRepository}, traduciendo entre el dominio y la
 * entidad JPA, incluyendo los criterios del libro auxiliar y la
 * configuración de entrega (correo electrónico).
 */
@Component
@RequiredArgsConstructor
public class ScheduledReportCommandRepositoryAdapter implements IScheduledReportCommandRepositoryPort {

    private final IScheduledReportRepository scheduledReportRepository;
    private final IAuxiliaryBookCriteriaCommandEntityMapper criteriaCommandEntityMapper;
    private final IAuxiliaryBookCommandEntityMapper auxiliaryBookCommandEntityMapper;

    /**
     * @brief Guarda o actualiza un trabajo programado.
     * @param job Trabajo a persistir.
     * @return Trabajo persistido con los identificadores generados.
     */
    @Override
    public ScheduledAuxiliaryBookJob save(ScheduledAuxiliaryBookJob job) {
        ScheduledReportEntity entity = toEntity(job);
        ScheduledReportEntity saved = scheduledReportRepository.save(entity);
        return toDomain(saved);
    }

    /**
     * @brief Actualiza el estado de un trabajo programado.
     * @param jobId Identificador interno del trabajo.
     * @param status Nuevo estado a asignar.
     *
     * Si alguno de los parámetros es {@code null} o el trabajo no existe,
     * la operación no realiza cambios.
     */
    @Override
    public void updateStatus(Long jobId, EJobStatus status) {
        if (jobId == null || status == null) {
            return;
        }
        Optional<ScheduledReportEntity> entityOptional = scheduledReportRepository.findById(jobId);
        if (entityOptional.isEmpty()) {
            return;
        }
        ScheduledReportEntity entity = entityOptional.get();
        entity.setStatus(status);
        scheduledReportRepository.save(entity);
    }

    private ScheduledReportEntity toEntity(ScheduledAuxiliaryBookJob job) {
        ScheduledReportEntity entity = new ScheduledReportEntity();
        entity.setId(job.getJobId());
        entity.setPublicId(job.getPublicId());
        entity.setBookType(job.getBookType());
        entity.setFrequency(job.getScheduleSpec().getFrequency());
        entity.setStartAt(job.getScheduleSpec().getStartAt());
        entity.setEndAt(job.getScheduleSpec().getEndAt() != null ? job.getScheduleSpec().getEndAt().orElse(null) : null);
        entity.setNextRunAt(job.getScheduleSpec().getNextRunAt());
        entity.setCreatedAt(job.getCreatedAt());
        entity.setCreatedBy(job.getCreatedBy());
        entity.setEntId(job.getEntId());
        entity.setUserId(job.getUserId());
        entity.setOwnerSub(job.getOwnerSub());
        entity.setStatus(job.getStatus());

        DeliveryConfig deliveryConfig = job.getDeliveryConfig();
        if (deliveryConfig != null) {
            entity.setDeliveryWay(deliveryConfig.getDeliveryWay());
            EmailConfig emailConfig = deliveryConfig.getEmailConfig();
            if (emailConfig != null) {
                entity.setEmailTo(emailConfig.getTo());
                entity.setEmailSubject(emailConfig.getSubjectTemplate());
                entity.setEmailBody(emailConfig.getBodyTemplate());
            }
        }

        AuxiliaryBookCriteriaEntity criteriaEntity = criteriaCommandEntityMapper.toCriteriaEntity(job.getCriteria());
        entity.setCriteria(criteriaEntity);

        AuxiliaryBookTemplate template = job.getTemplate();
        if (template != null) {
            entity.setTemplateName(template.getName());
            entity.setTemplatePathLogotype(template.getPathLogotype() != null
                    ? template.getPathLogotype().toString() : null);
            entity.setTemplateAlignment(template.getAlienation() != null
                    ? template.getAlienation().name() : null);
            entity.setTemplateFont(template.getFont());
            entity.setTemplateFontSize(template.getFontSize());
            entity.setTemplateMainColor(template.getMainColor());
        }
        return entity;
    }

    private ScheduledAuxiliaryBookJob toDomain(ScheduledReportEntity entity) {
        ScheduleSpec scheduleSpec = new ScheduleSpec(
                entity.getFrequency(),
                entity.getStartAt(),
                Optional.ofNullable(entity.getEndAt()),
                entity.getNextRunAt()
        );

        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setJobId(entity.getId());
        job.setPublicId(entity.getPublicId());
        job.setBookType(entity.getBookType());
        job.setCriteria(auxiliaryBookCommandEntityMapper.toCriteria(entity.getCriteria()));
        job.setScheduleSpec(scheduleSpec);
        job.setCreatedAt(entity.getCreatedAt());
        job.setCreatedBy(entity.getCreatedBy());
        job.setEntId(entity.getEntId());
        job.setUserId(entity.getUserId());
        job.setOwnerSub(entity.getOwnerSub());
        job.setStatus(entity.getStatus());
        job.setDeliveryConfig(toDeliveryConfig(entity));
        job.setTemplate(toTemplate(entity));
        return job;
    }

    private AuxiliaryBookTemplate toTemplate(ScheduledReportEntity entity) {
        if (entity.getTemplateName() == null
                && entity.getTemplatePathLogotype() == null
                && entity.getTemplateAlignment() == null
                && entity.getTemplateFont() == null
                && entity.getTemplateFontSize() == null
                && entity.getTemplateMainColor() == null) {
            return null;
        }
        AuxiliaryBookTemplate template = new AuxiliaryBookTemplate();
        template.setName(entity.getTemplateName());
        template.setPathLogotype(toUrl(entity.getTemplatePathLogotype()));
        template.setAlienation(toAlignment(entity.getTemplateAlignment()));
        template.setFont(entity.getTemplateFont());
        template.setFontSize(entity.getTemplateFontSize());
        template.setMainColor(entity.getTemplateMainColor());
        return template;
    }

    private URL toUrl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new URL(value.trim());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    private EAlignment toAlignment(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return EAlignment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private DeliveryConfig toDeliveryConfig(ScheduledReportEntity entity) {
        if (entity.getDeliveryWay() == null
                && entity.getEmailTo() == null
                && entity.getEmailSubject() == null
                && entity.getEmailBody() == null) {
            return null;
        }
        EmailConfig emailConfig = null;
        if (entity.getEmailTo() != null || entity.getEmailSubject() != null || entity.getEmailBody() != null) {
            emailConfig = new EmailConfig(
                    entity.getEmailTo(),
                    entity.getEmailSubject(),
                    entity.getEmailBody()
            );
        }
        return new DeliveryConfig(entity.getDeliveryWay(), null, emailConfig);
    }
}
