package co.unicauca.edu.co.contables.audit.application.port.input.commands;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogSessionRequest;

/** 
 * @brief input port for session event logger
 */
public interface LogAuditSessionCommand {

    void execute(LogSessionRequest request);
}
