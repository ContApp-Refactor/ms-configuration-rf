package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.mapper;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.request.GetSessionsRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.ExportSessionsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.GetSessionsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.security.SecurityContextService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionRestMapper {

    private final SecurityContextService securityContextService;

    public GetSessionsRequest toGetSessionsRequest(
            GetSessionsRestRequest restRequest) {

        String requestingUserRole = securityContextService.getCurrentUserRole();

        return GetSessionsRequest.builder()
                .dateFrom(restRequest.getDateFrom().toInstant())
                .dateTo(restRequest.getDateTo().toInstant())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .action(restRequest.getAction())
                .page(restRequest.getPage())
                .size(restRequest.getSize())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .requestingUserRole(requestingUserRole)
                .build();
    }

    public ExportSessionsRequest toExportSessionsRequest(
            ExportSessionsRestRequest restRequest) {

        String requestedBy = securityContextService.getCurrentUsername();

        return ExportSessionsRequest.builder()
                .dateFrom(restRequest.getDateFrom().toInstant())
                .dateTo(restRequest.getDateTo().toInstant())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .action(restRequest.getAction())
                .exportFormat(restRequest.getExportFormat())
                .requestedBy(requestedBy)
                .build();
    }
}
