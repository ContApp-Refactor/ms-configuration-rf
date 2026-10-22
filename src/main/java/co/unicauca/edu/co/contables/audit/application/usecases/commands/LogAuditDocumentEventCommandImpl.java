package co.unicauca.edu.co.contables.audit.application.usecases.commands;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogDocumentEventRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditDocumentEventCommand;
import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;
import co.unicauca.edu.co.contables.audit.domain.port.output.AuditDocumentEventRepositoryPort;

import org.springframework.transaction.annotation.Transactional;

@Service
public class LogAuditDocumentEventCommandImpl implements LogAuditDocumentEventCommand {

    private final AuditDocumentEventRepositoryPort auditDocumentEventRepository;

    public LogAuditDocumentEventCommandImpl(AuditDocumentEventRepositoryPort auditDocumentEventRepository) {
        this.auditDocumentEventRepository = auditDocumentEventRepository;
    }

    @Override
    @Transactional
    public void execute(LogDocumentEventRequest request) {
        AuditDocumentEvent documentEvent = AuditDocumentEvent.create(
                request.getEnterpriseId(),
                request.getDocumentCode(),
                request.getDocumentType(),
                request.getDocumentId(),
                request.getUserId(),
                request.getUserName(),
                request.getUserRoles(),
                request.getOperationType(),
                request.getThirdPartyId(),
                request.getThirdPartyName(),
                request.getModuleName(),
                request.getOperationAt(),
                request.getDocumentDate(),
                request.getDocumentData());
        auditDocumentEventRepository.save(documentEvent);
    }

}
