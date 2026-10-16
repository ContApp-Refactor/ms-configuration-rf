package co.unicauca.edu.co.contables.audit.application.usecases.commands;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.audit.application.dto.request.LogOperationRequest;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.LogAuditOperationCommand;
import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;
import co.unicauca.edu.co.contables.audit.domain.port.output.AuditOperationRepositoryPort;

@Service
public class LogAuditOperationCommandImpl implements LogAuditOperationCommand {

    private final AuditOperationRepositoryPort auditOperationRepository;

    public LogAuditOperationCommandImpl(AuditOperationRepositoryPort auditOperationRepository) {
        this.auditOperationRepository = auditOperationRepository;
    }

    @Override
    @Transactional
    public void execute(LogOperationRequest request) {
        AuditOperation operation = AuditOperation.create(
                request.getUserId(),
                request.getUserName(),
                request.getUserRole(),
                request.getOperationType(),
                request.getOperationAt(),
                request.getModuleName(),
                request.getAffectedTable(),
                request.getRegisterId(),
                request.getEnterpriseId(),
                request.getDataObject());
        auditOperationRepository.save(operation);
    }
}
