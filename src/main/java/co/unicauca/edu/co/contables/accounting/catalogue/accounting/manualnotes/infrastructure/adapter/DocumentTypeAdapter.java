package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.adapter;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port.DocumentTypePort;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
import org.springframework.stereotype.Component;

/** Adapter local de solo lectura a tipos de documento configurados. */
@Component
public class DocumentTypeAdapter implements DocumentTypePort {
    private final IDocumentTypeService documentTypes;
    public DocumentTypeAdapter(IDocumentTypeService documentTypes) { this.documentTypes = documentTypes; }

    @Override
    public java.util.Optional<DocumentTypePort.DocumentType> findActiveByPrefix(String prefix, String enterpriseId) {
        return documentTypes.findAllByEnterprise(enterpriseId, 0, 1000).getContent().stream()
                .filter(type -> Boolean.TRUE.equals(type.getStatus()))
                .filter(type -> type.getPrefix() != null && type.getPrefix().equalsIgnoreCase(prefix))
                .map(type -> new DocumentTypePort.DocumentType(type.getPrefix(), type.getName(), type.getModuleId()))
                .findFirst();
    }
}
