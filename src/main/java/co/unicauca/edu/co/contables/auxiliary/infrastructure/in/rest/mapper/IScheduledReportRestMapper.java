package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.DeliveryConfig;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduleSpec;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.CreateScheduledReportRequest;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.ScheduledReportTemplateRequest;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.UpdateScheduledReportRequest;

import java.net.MalformedURLException;
import java.net.URL;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport.ScheduledReportListItemResponse;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport.ScheduledReportResponse;

/**
 * @brief Mapper REST entre las solicitudes/respuestas y el dominio de reportes programados.
 *
 * Provee métodos por defecto para convertir entre los DTOs de la capa
 * REST ({@link CreateScheduledReportRequest}, {@link UpdateScheduledReportRequest},
 * {@link ScheduledReportResponse} y {@link ScheduledReportListItemResponse}) y la
 * entidad de dominio {@link ScheduledAuxiliaryBookJob}, armando las
 * estructuras internas {@link ScheduleSpec} y {@link DeliveryConfig}.
 */
@Mapper(componentModel = "spring")
public interface IScheduledReportRestMapper {

    default ScheduledAuxiliaryBookJob toDomain(CreateScheduledReportRequest request) {
        if (request == null) {
            return null;
        }
        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setEntId(request.getEntId());
        job.setUserId(request.getUserId());
        job.setBookType(request.getBookType());
        job.setCriteria(request.getCriteria());
        job.setCreatedBy(request.getCreatedBy());
        job.setScheduleSpec(new ScheduleSpec(
                request.getFrequency(),
                request.getStartAt(),
                Optional.ofNullable(request.getEndAt()),
                request.getStartAt()
        ));
        job.setDeliveryConfig(new DeliveryConfig(
                request.getDeliveryWay(),
                request.getFormat(),
                request.getEmailConfig()
        ));
        job.setTemplate(toTemplate(request.getInfoReportTemplate()));
        return job;
    }

    default ScheduledAuxiliaryBookJob toDomain(UpdateScheduledReportRequest request) {
        if (request == null) {
            return null;
        }
        ScheduledAuxiliaryBookJob job = new ScheduledAuxiliaryBookJob();
        job.setEntId(request.getEntId());
        job.setUserId(request.getUserId());
        job.setBookType(request.getBookType());
        job.setCriteria(request.getCriteria());
        job.setCreatedBy(request.getCreatedBy());
        job.setScheduleSpec(new ScheduleSpec(
                request.getFrequency(),
                request.getStartAt(),
                Optional.ofNullable(request.getEndAt()),
                request.getStartAt()
        ));
        job.setDeliveryConfig(new DeliveryConfig(
                request.getDeliveryWay(),
                request.getFormat(),
                request.getEmailConfig()
        ));
        job.setTemplate(toTemplate(request.getInfoReportTemplate()));
        return job;
    }

    default AuxiliaryBookTemplate toTemplate(ScheduledReportTemplateRequest request) {
        if (request == null) {
            return null;
        }
        AuxiliaryBookTemplate template = new AuxiliaryBookTemplate();
        template.setId(request.getId());
        template.setName(request.getName());
        template.setPathLogotype(parseUrl(request.getPathLogotype()));
        template.setAlienation(parseAlignment(request.getAlienation()));
        template.setFont(request.getFont());
        template.setFontSize(request.getFontSize());
        template.setMainColor(request.getMainColor());
        return template;
    }

    default URL parseUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new URL(value.trim());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    default EAlignment parseAlignment(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EAlignment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    default ScheduledReportResponse toResponse(ScheduledAuxiliaryBookJob job) {
        if (job == null) {
            return null;
        }
        ScheduledReportResponse response = new ScheduledReportResponse();
        response.setPublicId(job.getPublicId());
        response.setBookType(job.getBookType());
        response.setCriteria(job.getCriteria());
        response.setFrequency(job.getScheduleSpec() != null ? job.getScheduleSpec().getFrequency() : null);
        response.setStartAt(job.getScheduleSpec() != null ? job.getScheduleSpec().getStartAt() : null);
        response.setEndAt(job.getScheduleSpec() != null && job.getScheduleSpec().getEndAt() != null
                ? job.getScheduleSpec().getEndAt().orElse(null)
                : null);
        response.setNextRunAt(job.getScheduleSpec() != null ? job.getScheduleSpec().getNextRunAt() : null);
        response.setStatus(job.getStatus());
        response.setEntId(job.getEntId());
        response.setUserId(job.getUserId());
        response.setCreatedBy(job.getCreatedBy());
        response.setCreatedAt(job.getCreatedAt());
        response.setDeliveryWay(job.getDeliveryConfig() != null ? job.getDeliveryConfig().getDeliveryWay() : null);
        response.setEmailConfig(job.getDeliveryConfig() != null ? job.getDeliveryConfig().getEmailConfig() : null);
        return response;
    }

    default ScheduledReportListItemResponse toListItemResponse(ScheduledAuxiliaryBookJob job) {
        if (job == null) {
            return null;
        }
        ScheduledReportListItemResponse response = new ScheduledReportListItemResponse();
        response.setPublicId(job.getPublicId());
        response.setBookType(job.getBookType());
        response.setFrequency(job.getScheduleSpec() != null ? job.getScheduleSpec().getFrequency() : null);
        response.setNextRunAt(job.getScheduleSpec() != null ? job.getScheduleSpec().getNextRunAt() : null);
        response.setStatus(job.getStatus());
        return response;
    }
}
