package co.unicauca.edu.co.contables.audit.application.port.output;

import java.util.List;

import co.unicauca.edu.co.contables.audit.application.internal.query.AuditOperationCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;
import co.unicauca.edu.co.contables.audit.domain.model.ModuleTable;

public interface AuditOperationQueryPort {

    PageResult<AuditOperation> findPageByCriteria(AuditOperationCriteria criteria, QueryOptions options);

    long countByCriteria(AuditOperationCriteria criteria);

    List<AuditOperation> findAllForExport(AuditOperationCriteria criteria);

    List<ModuleTable> findDistinctModulesAndTables(String enterpriseId);
}
