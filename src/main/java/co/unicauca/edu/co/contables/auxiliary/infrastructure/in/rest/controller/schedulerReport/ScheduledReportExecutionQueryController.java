package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.schedulerReport;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IScheduledReportExecutionsQueryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport.ScheduledReportExecutionListItemResponse;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.IScheduledReportExecutionRestMapper;

import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST de consulta de ejecuciones de reportes programados.
 *
 * Expone endpoints para listar las ejecuciones asociadas a un reporte
 * programado, con soporte para filtros dinámicos por query string.
 * Convierte las entidades de dominio a DTOs de respuesta.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auxiliary-books/scheduled-reports")
public class ScheduledReportExecutionQueryController {

    private final IScheduledReportExecutionsQueryPort scheduledReportExecutionsQueryPort;
    private final IScheduledReportExecutionRestMapper scheduledReportExecutionRestMapper;

    @GetMapping("/{publicId}/executions")
    public ResponseEntity<ResponseDTO<List<ScheduledReportExecutionListItemResponse>>> listExecutions(
            @PathVariable String publicId,
            @RequestParam(required = false) String filters
    ) {
        List<ReportExecution> executions = scheduledReportExecutionsQueryPort.listExecutionsByJob(publicId, filters);
        List<ScheduledReportExecutionListItemResponse> responseItems = executions.stream()
                .map(scheduledReportExecutionRestMapper::toListItemResponse)
                .toList();

        ResponseDTO<List<ScheduledReportExecutionListItemResponse>> responseDTO = ResponseDTO.<List<ScheduledReportExecutionListItemResponse>>builder()
                .data(responseItems)
                .statusCode(200)
                .message("Scheduled report executions retrieved successfully.")
                .build();
        return responseDTO.of();
    }
}
