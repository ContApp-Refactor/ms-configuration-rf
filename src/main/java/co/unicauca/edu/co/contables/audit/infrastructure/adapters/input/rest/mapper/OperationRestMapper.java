package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.mapper;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportOperationsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.request.GetModulesTablesRequest;
import co.unicauca.edu.co.contables.audit.application.dto.request.GetOperationsRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.ExportOperationsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.GetOperationsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.MissingHeaderException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.security.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationRestMapper {

    private final SecurityContextService securityContextService;

    public GetOperationsRequest toGetOperationsRequest(
            GetOperationsRestRequest restRequest,
            HttpServletRequest httpServletRequest) {
        return GetOperationsRequest.builder()
                .dateFrom(restRequest.getDateFrom().toInstant())
                .dateTo(restRequest.getDateTo().toInstant())
                .moduleName(restRequest.getModuleName())
                .affectedTable(restRequest.getAffectedTable())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .operationType(restRequest.getOperationType())
                .registerId(restRequest.getRegisterId())
                .enterpriseId(restRequest.getEnterpriseId())
                .page(restRequest.getPage())
                .size(restRequest.getSize())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .build();
    }

    public ExportOperationsRequest toExportOperationsRequest(
            ExportOperationsRestRequest restRequest,
            HttpServletRequest httpRequest) {

        String requestedBy = securityContextService.getCurrentUsername();

        return ExportOperationsRequest.builder()
                .enterpriseId(restRequest.getEnterpriseId())
                .enterpriseName(restRequest.getEnterpriseName())
                .dateFrom(restRequest.getDateFrom().toInstant())
                .dateTo(restRequest.getDateTo().toInstant())
                .moduleName(restRequest.getModuleName())
                .affectedTable(restRequest.getAffectedTable())
                .userName(restRequest.getUserName())
                .userRole(restRequest.getUserRole())
                .operationType(restRequest.getOperationType())
                .registerId(restRequest.getRegisterId())
                .requestedBy(requestedBy)
                .exportFormat(restRequest.getExportFormat())
                .build();
    }

    public GetModulesTablesRequest toGetModulesTablesRequest(HttpServletRequest httpServletRequest) {
        String enterpriseId = httpServletRequest.getHeader("X-Enterprise-Id");
        if (enterpriseId == null || enterpriseId.isBlank()) {
            throw new MissingHeaderException("X-Enterprise-Id header is required");
        }
        return new GetModulesTablesRequest(enterpriseId);
    }
}
