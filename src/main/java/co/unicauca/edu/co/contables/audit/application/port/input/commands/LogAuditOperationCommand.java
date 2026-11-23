package co.unicauca.edu.co.contables.audit.application.port.input.commands;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;

public interface LogAuditOperationCommand {
    void execute(LogOperationRequest request);
}
