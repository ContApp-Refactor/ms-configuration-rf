package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.schedulerReport;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IScheduledReportCommandPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.CreateScheduledReportRequest;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.UpdateScheduledReportRequest;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport.ScheduledReportResponse;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.IScheduledReportRestMapper;

import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST de comandos para reportes programados.
 *
 * Expone endpoints para crear, actualizar y cancelar reportes
 * programados de libros auxiliares. Delega la lógica de negocio al
 * puerto de aplicación correspondiente y utiliza el mapper REST para
 * convertir entre DTOs y el dominio.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auxiliary-books/scheduled-reports")
public class ScheduledReportCommandController {

    private final IScheduledReportCommandPort scheduledReportCommandPort;
    private final IScheduledReportRestMapper scheduledReportRestMapper;

    @PostMapping
    public ResponseEntity<ResponseDTO<ScheduledReportResponse>> create(
            @Validated @RequestBody CreateScheduledReportRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ScheduledAuxiliaryBookJob job = scheduledReportRestMapper.toDomain(request);
        if (jwt != null) {
            job.setOwnerSub(jwt.getSubject());
        }
        ScheduledAuxiliaryBookJob created = scheduledReportCommandPort.createScheduledReport(job);

        ResponseDTO<ScheduledReportResponse> responseDTO = ResponseDTO.<ScheduledReportResponse>builder()
                .data(scheduledReportRestMapper.toResponse(created))
                .statusCode(201)
                .message("Scheduled report created successfully.")
                .build();
        return responseDTO.of();
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<ResponseDTO<ScheduledReportResponse>> update(
            @PathVariable String publicId,
            @Validated @RequestBody UpdateScheduledReportRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ScheduledAuxiliaryBookJob job = scheduledReportRestMapper.toDomain(request);
        if (jwt != null) {
            job.setOwnerSub(jwt.getSubject());
        }
        ScheduledAuxiliaryBookJob updated = scheduledReportCommandPort.updateScheduledReport(publicId, job);

        ResponseDTO<ScheduledReportResponse> responseDTO = ResponseDTO.<ScheduledReportResponse>builder()
                .data(scheduledReportRestMapper.toResponse(updated))
                .statusCode(200)
                .message("Scheduled report updated successfully.")
                .build();
        return responseDTO.of();
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ResponseDTO<Void>> cancel(@PathVariable String publicId) {
        scheduledReportCommandPort.cancelScheduledReport(publicId);
        ResponseDTO<Void> responseDTO = ResponseDTO.<Void>builder()
                .statusCode(200)
                .message("Scheduled report cancelled successfully.")
                .build();
        return responseDTO.of();
    }
}
