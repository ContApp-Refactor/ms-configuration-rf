package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.schedulerReport;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IDownloadScheduledReportPort;
import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IDownloadScheduledReportPort.DownloadedReport;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @brief Endpoint de descarga on-demand de un reporte programado en modo DOWNLOAD.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auxiliary-books/scheduled-reports")
public class ScheduledReportDownloadController {

    private final IDownloadScheduledReportPort downloadScheduledReportPort;

    @GetMapping("/{publicId}/executions/{executionId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable String publicId,
            @PathVariable UUID executionId
    ) {
        DownloadedReport result = downloadScheduledReportPort.download(publicId, executionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resolveMediaType(result.getFormat()));
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename(result.getFilename())
                        .build()
        );
        headers.setContentLength(result.getContent().length);
        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    private MediaType resolveMediaType(EAuxiliaryBookFormat format) {
        if (format == EAuxiliaryBookFormat.EXCEL) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return MediaType.APPLICATION_PDF;
    }
}
