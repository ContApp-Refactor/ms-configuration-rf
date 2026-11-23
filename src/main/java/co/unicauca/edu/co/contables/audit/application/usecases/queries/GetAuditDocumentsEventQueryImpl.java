package co.unicauca.edu.co.contables.audit.application.usecases.queries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetDocumentsEventsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.DocumentEventAuditResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;
import co.unicauca.edu.co.contables.audit.application.internal.DocumentSummaryProjection;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditDocumentCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetAuditDocumentEventQuery;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditDocumentEventQueryPort;

@Service
public class GetAuditDocumentsEventQueryImpl implements GetAuditDocumentEventQuery {

        private final AuditDocumentEventQueryPort queryPort;

        public GetAuditDocumentsEventQueryImpl(AuditDocumentEventQueryPort queryPort) {
                this.queryPort = queryPort;
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<DocumentEventAuditResponse> execute(GetDocumentsEventsRequest request) {

                AuditDocumentCriteria criteria = AuditDocumentCriteria.create(
                                request.getDateFrom(),
                                request.getDateTo(),
                                request.getDateType(),
                                request.getDocumentType(),
                                request.getDocumentCode(),
                                request.getEnterpriseId(),
                                request.getThirdPartyName(),
                                request.getCreatedBy());

                QueryOptions options = QueryOptions.builder()
                                .page(request.getPage())
                                .size(request.getSize())
                                .sortField(request.getSortField())
                                .sortDirection(request.getSortDirection())
                                .build();

                PageResult<DocumentSummaryProjection> pageResult = queryPort.findDocumentSummaries(criteria, options);

                List<DocumentEventAuditResponse> data = pageResult.getContent().stream()
                                .map(this::toResponse)
                                .toList();

                return buildPageResponse(data, pageResult.getTotalElements(), request.getPage(), request.getSize());
        }

        private DocumentEventAuditResponse toResponse(DocumentSummaryProjection summary) {
                return DocumentEventAuditResponse.builder()
                                .documentCode(summary.getDocumentCode())
                                .documentType(summary.getDocumentType())
                                .thirdPartyName(summary.getThirdPartyName())
                                .documentDate(summary.getDocumentDate())
                                .createdBy(summary.getCreatedBy())
                                .lastModifiedBy(summary.getLastModifiedBy())
                                .lastModifiedAt(summary.getLastModifiedAt())
                                .build();
        }

        private PageResponse<DocumentEventAuditResponse> buildPageResponse(
                        List<DocumentEventAuditResponse> data, long totalElements, int page, int size) {
                int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
                return PageResponse.<DocumentEventAuditResponse>builder()
                                .data(data)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .currentPage(page)
                                .pageSize(size)
                                .hasNext(page < totalPages - 1)
                                .hasPrevious(page > 0)
                                .build();
        }

}
