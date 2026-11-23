package co.unicauca.edu.co.contables.audit.application.usecases.queries;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetModulesTablesRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.ModuleTableResponse;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetModulesAndTablesQuery;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditOperationQueryPort;

@Service
public class GetModulesAndTablesQueryImpl implements GetModulesAndTablesQuery {

    private final AuditOperationQueryPort queryPort;

    public GetModulesAndTablesQueryImpl(AuditOperationQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleTableResponse> execute(GetModulesTablesRequest request) {
        return queryPort.findDistinctModulesAndTables(request.enterpriseId())
                .stream()
                .map(mt -> new ModuleTableResponse(
                        mt.moduleName(),
                        mt.affectedTable()))
                .toList();
    }

}
