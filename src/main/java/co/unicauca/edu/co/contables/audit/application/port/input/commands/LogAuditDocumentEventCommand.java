package co.unicauca.edu.co.contables.audit.application.port.input.commands;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogDocumentEventRequest;

public interface LogAuditDocumentEventCommand {
    void execute(LogDocumentEventRequest request);
}
