package co.unicauca.edu.co.contables.audit.domain.port.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;

public interface AuditDocumentEventRepositoryPort {
    AuditDocumentEvent save(AuditDocumentEvent event);

    Optional<AuditDocumentEvent> findById(Long id);
}
