package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.adapter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportDocumentsEventsRequest;
import co.unicauca.edu.co.contables.audit.application.internal.ExportJobTracker;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditDocumentExportCriteria;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditDocumentEventQueryPort;
import co.unicauca.edu.co.contables.audit.application.port.output.IDocumentAsyncExportPort;
import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;
import co.unicauca.edu.co.contables.audit.domain.model.ExportJob;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.generator.DocumentExcelGenerator;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper.DocumentAuditTranslationHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentAsyncExportAdapter implements IDocumentAsyncExportPort {

    private final AuditDocumentEventQueryPort documentQueryPort;
    private final ExportJobTracker jobTracker;
    private final DocumentExcelGenerator excelGenerator;

    @Override
    @Async("exportTaskExecutor")
    public void process(ExportDocumentsEventsRequest request, String jobId) {
        try {
            ExportJob job = jobTracker.getJob(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

            job.markAsProcessing();

            AuditDocumentExportCriteria criteria = AuditDocumentExportCriteria.create(
                    request.getEnterpriseId(),
                    request.getDateFrom(),
                    request.getDateTo(),
                    request.getDateType(),
                    request.getDocumentCode(),
                    request.getDocumentType(),
                    request.getThirdPartyName(),
                    request.getOperationType(),
                    request.getUserName());

            List<AuditDocumentEvent> events = documentQueryPort.findAllEventsForExport(criteria);

            if (events.isEmpty()) {
                job.fail("No se encontraron eventos para los filtros aplicados");
                return;
            }

            job.updateProgress(50);

            String appliedFilters = buildAppliedFilters(request);
            String reportDate = DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("America/Bogota"))
                    .format(Instant.now());
            byte[] fileData = excelGenerator.generate(
                    events,
                    request.getEnterpriseName(),
                    request.getRequestedBy(),
                    reportDate,
                    appliedFilters);

            job.updateProgress(90);
            job.complete(fileData, events.size());

        } catch (Exception e) {
            log.error("Error exportando documentos para job {}", jobId, e);
            jobTracker.getJob(jobId).ifPresent(j -> j.fail("Error inesperado: " + e.getMessage()));
        }
    }

    private String buildAppliedFilters(ExportDocumentsEventsRequest request) {
        StringBuilder filters = new StringBuilder();
        if (request.getUserName() != null && !request.getUserName().isBlank())
            filters.append("Usuario: ").append(request.getUserName()).append("; ");
        if (request.getOperationType() != null)
            filters.append("Tipo operación: ")
                    .append(DocumentAuditTranslationHelper
                            .translateValue(request.getOperationType().name()))
                    .append("; ");
        if (request.getDocumentType() != null && !request.getDocumentType().isBlank())
            filters.append("Tipo documento: ")
                    .append(DocumentAuditTranslationHelper
                            .translateValue(request.getDocumentType()))
                    .append("; ");
        if (request.getDocumentCode() != null && !request.getDocumentCode().isBlank())
            filters.append("Código: ").append(request.getDocumentCode()).append("; ");
        if (request.getThirdPartyName() != null && !request.getThirdPartyName().isBlank())
            filters.append("Tercero: ").append(request.getThirdPartyName()).append("; ");
        if (request.getDateFrom() != null)
            filters.append("Desde: ").append(formatInstant(request.getDateFrom())).append("; ");
        if (request.getDateTo() != null)
            filters.append("Hasta: ").append(formatInstant(request.getDateTo())).append("; ");
        if (filters.length() > 2)
            filters.setLength(filters.length() - 2);
        return filters.toString();
    }

    private String formatInstant(Instant instant) {
        return instant.atZone(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
