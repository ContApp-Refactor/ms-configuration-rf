package co.unicauca.edu.co.contables.audit.application.usecases.queries;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetOperationsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.OperationAuditResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditOperationCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetAuditOperationsQuery;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditOperationQueryPort;
import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;

import java.util.List;

@Service
public class GetAuditOperationsQueryImpl implements GetAuditOperationsQuery {

        private final AuditOperationQueryPort queryPort;

        public GetAuditOperationsQueryImpl(AuditOperationQueryPort queryPort) {
                this.queryPort = queryPort;
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<OperationAuditResponse> execute(GetOperationsRequest request) {

                AuditOperationCriteria criteria = AuditOperationCriteria.create(
                                request.getDateFrom(),
                                request.getDateTo(),
                                request.getModuleName(),
                                request.getAffectedTable(),
                                request.getUserName(),
                                request.getUserRole(),
                                request.getOperationType(),
                                request.getRegisterId(),
                                request.getEnterpriseId());

                QueryOptions options = QueryOptions.builder()
                                .page(request.getPage())
                                .size(request.getSize())
                                .sortField(request.getSortField())
                                .sortDirection(request.getSortDirection())
                                .build();

                PageResult<AuditOperation> pageResult = queryPort.findPageByCriteria(criteria,
                                options);

                List<OperationAuditResponse> operations = pageResult.getContent().stream()
                                .map(this::toResponse)
                                .toList();

                return buildPageResponse(operations, pageResult.getTotalElements(), request.getPage(),
                                request.getSize());
        }

        private OperationAuditResponse toResponse(AuditOperation operation) {
                return OperationAuditResponse.builder()
                                .userName(operation.getUserName())
                                .userRole(operation.getUserRole())
                                .operationType(operation.getOperationType().name())
                                .operationAt(operation.getOperationAt())
                                .moduleName(operation.getModuleName())
                                .affectedTable(operation.getAffectedTable())
                                .registerId(operation.getRegisterId())
                                .dataObject(operation.getDataObject())
                                .build();
        }

        private PageResponse<OperationAuditResponse> buildPageResponse(
                        List<OperationAuditResponse> data, long totalElements, int page, int size) {
                int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
                return PageResponse.<OperationAuditResponse>builder()
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
