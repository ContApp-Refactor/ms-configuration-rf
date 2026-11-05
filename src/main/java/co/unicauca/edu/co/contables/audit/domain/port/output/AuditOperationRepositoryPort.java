package co.unicauca.edu.co.contables.audit.domain.port.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;

public interface AuditOperationRepositoryPort {

    AuditOperation save(AuditOperation auditOperation);

    Optional<AuditOperation> findById(Long id);

    Optional<AuditOperation> findByRegisterId(String registerId, String affectedTable);
}
