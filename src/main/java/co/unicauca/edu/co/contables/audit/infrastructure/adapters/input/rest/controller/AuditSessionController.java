package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.request.GetSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.SessionAuditResponse;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetAuditSessionsQuery;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.export.ISessionExportUseCase;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.mapper.SessionRestMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/audit/sessions")
@RequiredArgsConstructor
@Slf4j
public class AuditSessionController {

    private final GetAuditSessionsQuery getAuditSessionsQuery;
    private final SessionRestMapper sessionRestMapper;
    private final ISessionExportUseCase sessionExportUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<PageResponse<SessionAuditResponse>> getAuditSessions(
            @Valid @ModelAttribute GetSessionsRestRequest restRequest) {
        GetSessionsRequest request = sessionRestMapper.toGetSessionsRequest(restRequest);
        PageResponse<SessionAuditResponse> response = getAuditSessionsQuery.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<Map<String, String>> exportSessions(
            @Valid @RequestBody ExportSessionsRestRequest restRequest) {
        ExportSessionsRequest request = sessionRestMapper.toExportSessionsRequest(restRequest);
        String jobId = sessionExportUseCase.execute(request);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

}
