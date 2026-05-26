package co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.domain.models.Tag;

@Service
public class TestNoCommercialTagAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "NO_COMMERCIAL_TAG", moduleName = "NO_COMMERCIAL_TAGS")
    public Tag create(Tag tag) {
        return tag;
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "NO_COMMERCIAL_TAG", moduleName = "NO_COMMERCIAL_TAGS")
    public Tag update(Long id, Tag tag) {
        return tag;
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "NO_COMMERCIAL_TAG", moduleName = "NO_COMMERCIAL_TAGS")
    public boolean delete(Long id) {
        return true;
    }
}
