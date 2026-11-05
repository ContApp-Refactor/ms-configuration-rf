package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.adapter;

import co.unicauca.edu.co.contables.audit.application.internal.query.AuditOperationCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditOperationQueryPort;
import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;
import co.unicauca.edu.co.contables.audit.domain.model.ModuleTable;
import co.unicauca.edu.co.contables.audit.domain.port.output.AuditOperationRepositoryPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.mapper.AuditOperationJpaMapper;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository.IAuditOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditOperationRepositoryAdapter implements AuditOperationQueryPort,
                AuditOperationRepositoryPort {

        private final IAuditOperationRepository auditOperationRepository;
        private final AuditOperationJpaMapper mapper;

        @Override
        public AuditOperation save(AuditOperation auditOperation) {
                AuditOperationEntity entity = mapper.toEntity(auditOperation);
                AuditOperationEntity saved = auditOperationRepository.save(entity);
                return mapper.toDomain(saved);
        }

        @Override
        public PageResult<AuditOperation> findPageByCriteria(AuditOperationCriteria criteria, QueryOptions options) {
                Sort sort = buildSort(options);
                PageRequest pageRequest = PageRequest.of(options.getPage(), options.getSize(), sort);

                Page<AuditOperationEntity> page = auditOperationRepository.findByFilters(
                                criteria.getEnterpriseId(),
                                criteria.getDateFrom(),
                                criteria.getDateTo(),
                                criteria.getModuleName(),
                                criteria.getAffectedTable(),
                                criteria.getUserName(),
                                criteria.getOperationType() != null ? criteria.getOperationType().name() : null,
                                criteria.getUserRole(),
                                pageRequest);

                List<AuditOperation> content = page.getContent().stream()
                                .map(mapper::toDomain)
                                .toList();

                return new PageResult<>(content, page.getTotalElements());
        }

        @Override
        public Optional<AuditOperation> findById(Long id) {
                return auditOperationRepository.findById(id)
                                .map(mapper::toDomain);
        }

        @Override
        public List<ModuleTable> findDistinctModulesAndTables(String enterpriseId) {
                return auditOperationRepository.findDistinctModulesAndTables(enterpriseId)
                                .stream()
                                .map(p -> new ModuleTable(
                                                p.getModuleName(),
                                                p.getAffectedTable()))
                                .toList();
        }

        @Override
        public Optional<AuditOperation> findByRegisterId(String registerId, String affectedTable) {
                return auditOperationRepository.findByRegisterIdAndAffectedTable(registerId, affectedTable)
                                .map(mapper::toDomain);
        }

        @Override
        public long countByCriteria(AuditOperationCriteria filter) {
                return auditOperationRepository.findByFilters(
                                filter.getEnterpriseId(),
                                filter.getDateFrom(),
                                filter.getDateTo(),
                                filter.getModuleName(),
                                filter.getAffectedTable(),
                                filter.getUserName(),
                                filter.getOperationType() != null ? filter.getOperationType().name() : null,
                                filter.getUserRole(),
                                PageRequest.of(0, 1))
                                .getTotalElements();
        }

        @Override
        public List<AuditOperation> findAllForExport(AuditOperationCriteria criteria) {
                return auditOperationRepository.findAllForExport(
                                criteria.getEnterpriseId(),
                                criteria.getDateFrom(),
                                criteria.getDateTo(),
                                criteria.getModuleName(),
                                criteria.getAffectedTable(),
                                criteria.getUserName(),
                                criteria.getOperationType() != null ? criteria.getOperationType().name() : null,
                                criteria.getUserRole())
                                .stream()
                                .map(mapper::toDomain)
                                .toList();
        }

        private Sort buildSort(QueryOptions options) {
                String sortField = options.getSortField() != null ? options.getSortField() : "operationAt";
                String sortDirection = options.getSortDirection() != null ? options.getSortDirection() : "DESC";

                Set<String> allowedFields = Set.of(
                                "operationAt", "userName",
                                "operationType", "moduleName", "affectedTable");

                if (!allowedFields.contains(sortField)) {
                        sortField = "operationAt";
                }

                Map<String, String> fieldToColumn = Map.of(
                                "operationAt", "operation_at",
                                "userName", "user_name",
                                "operationType", "operation_type",
                                "moduleName", "module_name",
                                "affectedTable", "affected_table");

                String columnName = fieldToColumn.getOrDefault(sortField, "operation_at");

                Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                return JpaSort.unsafe(direction, columnName);
        }
}
