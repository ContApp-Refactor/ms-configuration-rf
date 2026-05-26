package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;

@Service
public class TestTypesDocumentsAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "DOCUMENT_TYPE", moduleName = "TYPE_OF_DOCUMENTS")
    public DocumentType create() {

        return DocumentType.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("FACTURA")
                .prefix("FAC")
                .documentClassId(10L)
                .moduleId(1)
                .status(true)
                .build();
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "DOCUMENT_TYPE", moduleName = "TYPE_OF_DOCUMENTS")
    public DocumentType update(DocumentTypeUpdateReq req) {

        return DocumentType.builder()
                .id(req.getId())
                .idEnterprise(req.getIdEnterprise())
                .name(req.getName())
                .prefix(req.getPrefix())
                .documentClassId(req.getDocumentClassId())
                .moduleId(req.getModuleId())
                .status(true)
                .build();
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "DOCUMENT_TYPE", moduleName = "TYPE_OF_DOCUMENTS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public DocumentType changeState(Long id, String enterpriseId, Boolean status) {

        return DocumentType.builder()
                .id(id)
                .idEnterprise(enterpriseId)
                .status(status)
                .build();
    }
}
