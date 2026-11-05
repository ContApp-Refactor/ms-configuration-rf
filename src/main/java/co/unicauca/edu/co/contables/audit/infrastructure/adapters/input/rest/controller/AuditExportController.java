package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.unicauca.edu.co.contables.audit.application.dto.response.ExportFileResult;
import co.unicauca.edu.co.contables.audit.application.dto.response.ExportJobResponse;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.export.IExportJobQuery;
import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;

import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/audit/export")
@RequiredArgsConstructor
@Slf4j
public class AuditExportController {

        private final IExportJobQuery exportJobQuery;

        /**
         * Consulta el estado y progreso de un job de exportación.
         * El cliente hace polling sobre este endpoint hasta que status = COMPLETED o
         * FAILED.
         */
        @GetMapping("/{jobId}/status")
        @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
        public ResponseEntity<ExportJobResponse> getExportStatus(@PathVariable String jobId) {
                ExportJobResponse response = exportJobQuery.getJobStatus(jobId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Job no encontrado: " + jobId));
                return ResponseEntity.ok(response);
        }

        /**
         * Descarga el archivo generado. Solo funciona si el job está COMPLETED.
         * Después de la descarga, el cliente debe llamar a DELETE para liberar memoria.
         */
        @GetMapping("/{jobId}/download")
        @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
        public ResponseEntity<byte[]> downloadExport(@PathVariable String jobId) {

                ExportFileResult file = exportJobQuery.getJobFileData(jobId);

                MediaType mediaType = file.getFormat() == ExportFormat.PDF
                                ? MediaType.APPLICATION_PDF
                                : MediaType.parseMediaType(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + file.getFileName() + "\"")
                                .contentType(mediaType)
                                .contentLength(file.getData().length)
                                .body(file.getData());
        }

        /**
         * Libera la memoria del job después de la descarga.
         * El cliente debe llamar a este endpoint inmediatamente después de descargar.
         */
        @DeleteMapping("/{jobId}")
        @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
        public ResponseEntity<Void> removeExportJob(@PathVariable String jobId) {
                exportJobQuery.removeJob(jobId);
                return ResponseEntity.noContent().build();
        }
}
