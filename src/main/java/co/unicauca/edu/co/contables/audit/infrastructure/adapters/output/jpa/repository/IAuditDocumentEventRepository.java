package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditDocumentEventEntity;

public interface IAuditDocumentEventRepository extends JpaRepository<AuditDocumentEventEntity, Long>,
                JpaSpecificationExecutor<AuditDocumentEventEntity> {
        List<AuditDocumentEventEntity> findByEnterpriseIdAndDocumentCodeOrderByOperationAtAsc(
                        String enterpriseId, String documentCode);

}
