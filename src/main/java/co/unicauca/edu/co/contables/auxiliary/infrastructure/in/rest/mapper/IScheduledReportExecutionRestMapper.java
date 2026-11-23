package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport.ScheduledReportExecutionListItemResponse;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport.ScheduledReportExecutionResponse;
import org.mapstruct.Mapper;

/**
 * @brief Mapper REST entre el dominio de ejecuciones y sus DTOs de respuesta.
 *
 * Convierte una {@link ReportExecution} del dominio a
 * {@link ScheduledReportExecutionResponse} (detalle completo) o a
 * {@link ScheduledReportExecutionListItemResponse} (vista resumida
 * para listados), según lo requiera el controlador.
 */
@Mapper(componentModel = "spring")
public interface IScheduledReportExecutionRestMapper {

    default ScheduledReportExecutionResponse toResponse(ReportExecution execution) {
        if (execution == null) {
            return null;
        }
        ScheduledReportExecutionResponse response = new ScheduledReportExecutionResponse();
        response.setExecutionId(execution.getExecutionId());
        response.setJobId(execution.getJobId());
        response.setScheduledAt(execution.getScheduledAt());
        response.setStartedAt(execution.getStartedAt());
        response.setFinishedAt(execution.getFinishedAt());
        response.setStatusExecution(execution.getStatusExecution());
        response.setDeliveryStatus(execution.getDeliveryStatus());
        response.setErrorMessage(execution.getErrorMessage());
        return response;
    }

    default ScheduledReportExecutionListItemResponse toListItemResponse(ReportExecution execution) {
        if (execution == null) {
            return null;
        }
        ScheduledReportExecutionListItemResponse response = new ScheduledReportExecutionListItemResponse();
        response.setExecutionId(execution.getExecutionId());
        response.setScheduledAt(execution.getScheduledAt());
        response.setStatusExecution(execution.getStatusExecution());
        response.setDeliveryStatus(execution.getDeliveryStatus());
        response.setErrorMessage(execution.getErrorMessage());
        return response;
    }
}
