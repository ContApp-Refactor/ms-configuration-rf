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
import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IScheduledReportQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.scheduledReport.IScheduledReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @brief Adaptador de lectura para los trabajos de reportes programados.
 *
 * Implementa {@link IScheduledReportQueryRepositoryPort} consultando la
 * base de datos a través de {@link IScheduledReportRepository} y mapeando
 * las entidades JPA al dominio {@link ScheduledAuxiliaryBookJob},
 * incluyendo los criterios del libro auxiliar y la configuración de
 * entrega asociada.
 */
@Component
@RequiredArgsConstructor
public class ScheduledReportQueryRepositoryAdapter implements IScheduledReportQueryRepositoryPort {

    private final IScheduledReportRepository scheduledReportRepository;
    private final IAuxiliaryBookCommandEntityMapper auxiliaryBookCommandEntityMapper;

    /**
     * @brief Busca un trabajo programado por su ID interno.
     * @param jobId Identificador interno.
     * @return Trabajo programado si existe.
     */
    @Override
    public Optional<ScheduledAuxiliaryBookJob> findById(Long jobId) {
        return scheduledReportRepository.findById(jobId).map(this::toDomain);
    }

    /**
     * @brief Busca un trabajo programado por su identificador público.
     * @param publicId Identificador público (UUID).
     * @return Trabajo programado si existe.
     */
    @Override
    public Optional<ScheduledAuxiliaryBookJob> findByPublicId(String publicId) {
        return scheduledReportRepository.findByPublicId(publicId).map(this::toDomain);
    }

    /**
     * @brief Obtiene todos los trabajos programados de una empresa.
     * @param entId Identificador de la empresa.
     * @return Lista de trabajos programados asociados.
     */
    @Override
    public List<ScheduledAuxiliaryBookJob> findByEntId(String entId) {
        return scheduledReportRepository.findByEntId(entId).stream().map(this::toDomain).toList();
    }

    /**
     * @brief Obtiene los trabajos activos cuya próxima ejecución ya venció.
     * @param now Instante de referencia para comparar {@code nextRunAt}.
     * @param limit Tamaño máximo del lote a retornar (por defecto 50 si no es positivo).
     * @return Lista de trabajos elegibles para ejecución ordenados por {@code nextRunAt} ascendente.
     */
    @Override
    public List<ScheduledAuxiliaryBookJob> findDueJobs(Instant now, int limit) {
        int resolvedLimit = limit > 0 ? limit : 50;
        return scheduledReportRepository
                .findByStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(EJobStatus.ACTIVE, now, PageRequest.of(0, resolvedLimit))
                .stream()
                .map(this::toDomain)
                .toList();
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
