package co.unicauca.edu.co.contables.audit.application.usecases.commands;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogSessionRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditSessionCommand;
import co.unicauca.edu.co.contables.audit.domain.model.AuditSession;
import co.unicauca.edu.co.contables.audit.domain.port.output.AuditSessionRepositoryPort;

/**
 * @brief Use case implementation for logging audit sessions (login/logoutevents)
 * 
 */
@Service
public class LogAuditSessionCommandImpl implements LogAuditSessionCommand {
    
    private final AuditSessionRepositoryPort auditSessionRepository;

    public LogAuditSessionCommandImpl(AuditSessionRepositoryPort auditSessionRepository) {
        this.auditSessionRepository = auditSessionRepository;
    }

    @Override
    @Transactional
    public void execute(LogSessionRequest request) {
        AuditSession session = AuditSession.create(
                request.getSessionId(),
                request.getUserId(),
                request.getUserName(),
                request.getUserRole(),
                request.getAction(),
                request.getActionAt(),
                request.getIpAddress());

        auditSessionRepository.save(session);
    }
}
