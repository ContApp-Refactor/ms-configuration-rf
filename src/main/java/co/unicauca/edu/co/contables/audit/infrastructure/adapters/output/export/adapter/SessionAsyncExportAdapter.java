package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.adapter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.internal.CombinedSession;
import co.unicauca.edu.co.contables.audit.application.internal.ExportJobTracker;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditSessionCriteria;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditSessionQueryPort;
import co.unicauca.edu.co.contables.audit.application.port.output.ISessionAsyncExportPort;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;
import co.unicauca.edu.co.contables.audit.domain.model.ExportJob;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.generator.SessionExcelGenerator;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.generator.SessionPdfGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionAsyncExportAdapter implements ISessionAsyncExportPort {

    private final AuditSessionQueryPort sessionQueryPort;
    private final ExportJobTracker jobTracker;
    private final SessionExcelGenerator excelGenerator;
    private final SessionPdfGenerator pdfGenerator;

    @Override
    @Async("exportTaskExecutor")
    public void process(ExportSessionsRequest request, String jobId) {
        try {
            ExportJob job = jobTracker.getJob(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

            job.markAsProcessing();

            // Construir criteria desde el request
            AuditSessionCriteria criteria = AuditSessionCriteria.create(
                    request.getDateFrom(),
                    request.getDateTo(),
                    request.getUserName(),
                    request.getUserRole(),
                    request.getAction());

            List<CombinedSession> sessions = sessionQueryPort.findAllForExport(criteria);

            if (sessions.isEmpty()) {
                job.fail("No se encontraron registros para los filtros aplicados");
                return;
            }

            job.updateProgress(50);
            String reportDate = DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("America/Bogota"))
                    .format(Instant.now());

            byte[] fileData = request.getExportFormat() == ExportFormat.PDF
                    ? pdfGenerator.generate(sessions, request.getRequestedBy(), reportDate,
                            buildAppliedFilters(request))
                    : excelGenerator.generate(sessions, request.getRequestedBy(), reportDate,
                            buildAppliedFilters(request));

            job.updateProgress(90);

            job.complete(fileData, sessions.size());

        } catch (Exception e) {
            log.error("Error exportando sesiones para job {}", jobId, e);
            jobTracker.getJob(jobId).ifPresent(j -> j.fail("Error inesperado: " + e.getMessage()));
        }
    }

    private String buildAppliedFilters(ExportSessionsRequest request) {
        StringBuilder filters = new StringBuilder();

        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            filters.append("Usuario: ").append(request.getUserName()).append("; ");
        }
        if (request.getUserRole() != null) {
            filters.append("Rol: ").append(request.getUserRole()).append("; ");
        }
        if (request.getDateFrom() != null) {
            filters.append("Desde: ").append(formatDateTime(request.getDateFrom())).append("; ");
        }
        if (request.getDateTo() != null) {
            filters.append("Hasta: ").append(formatDateTime(request.getDateTo())).append("; ");
        }

        if (filters.length() > 2) {
            filters.setLength(filters.length() - 2);
        }

        return filters.toString();
    }

    private String formatDateTime(Instant instant) {
        if (instant == null)
            return "";
        return instant.atZone(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

}
