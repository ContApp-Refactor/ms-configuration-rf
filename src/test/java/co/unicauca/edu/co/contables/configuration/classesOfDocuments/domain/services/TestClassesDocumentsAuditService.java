package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;

@Service
public class TestClassesDocumentsAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMNETS")
    public DocumentClass create() {
        return DocumentClass.builder()
                .id(1L)
                .idEnterprise("ENT-1")
                .name("Test")
                .status(true)
                .build();
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMENTS")
    public DocumentClass update(DocumentClassUpdateReq req) {

        return DocumentClass.builder()
                .id(req.getId())
                .idEnterprise(req.getIdEnterprise())
                .name(req.getName())
                .status(true)
                .build();
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMENTS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public DocumentClass changeState(Long id, String entId, Boolean status) {

        return DocumentClass.builder()
                .id(id)
                .idEnterprise(entId)
                .name("Test")
                .status(status)
                .build();
    }
}
