package co.unicauca.edu.co.contables.audit.application.port.input.queries;

import java.util.List;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetModulesTablesRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.ModuleTableResponse;

public interface GetModulesAndTablesQuery {
    List<ModuleTableResponse> execute(GetModulesTablesRequest request);
}
