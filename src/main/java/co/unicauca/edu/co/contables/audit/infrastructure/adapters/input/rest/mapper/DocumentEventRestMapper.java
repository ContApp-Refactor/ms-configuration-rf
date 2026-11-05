package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.mapper;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.dto.request.ExportDocumentsEventsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.request.GetDocumentsEventsRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.ExportDocumentsEventsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.GetDocumentsEventsRestRequest;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.security.SecurityContextService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentEventRestMapper {

    private final SecurityContextService securityContextService;

    public GetDocumentsEventsRequest toGetDocumentsEventsRequest(
            GetDocumentsEventsRestRequest restRequest) {

        return GetDocumentsEventsRequest.builder()
                .dateFrom(restRequest.getDateFrom().toInstant())
                .dateTo(restRequest.getDateTo().toInstant())
                .dateType(restRequest.getDateType())
                .enterpriseId(restRequest.getEnterpriseId())
                .documentCode(restRequest.getDocumentCode())
                .documentType(restRequest.getDocumentType())
                .createdBy(restRequest.getCreatedBy())
                .thirdPartyName(restRequest.getThirdPartyName())
                .page(restRequest.getPage())
                .size(restRequest.getSize())
                .sortField(restRequest.getSortField())
                .sortDirection(restRequest.getSortDirection())
                .build();
    }

    public ExportDocumentsEventsRequest toExportDocumentsEventsRequest(
            ExportDocumentsEventsRestRequest restRequest) {

        String requestedBy = securityContextService.getCurrentUsername();

        return ExportDocumentsEventsRequest.builder()
                .dateFrom(restRequest.getDateFrom().toInstant())
                .dateTo(restRequest.getDateTo().toInstant())
                .dateType(restRequest.getDateType())
                .enterpriseId(restRequest.getEnterpriseId())
                .enterpriseName(restRequest.getEnterpriseName())
                .documentCode(restRequest.getDocumentCode())
                .documentType(restRequest.getDocumentType())
                .thirdPartyName(restRequest.getThirdPartyName())
                .operationType(restRequest.getOperationType())
                .userName(restRequest.getUserName())
                .requestedBy(requestedBy)
                .exportFormat(restRequest.getExportFormat())
                .build();
    }
}
