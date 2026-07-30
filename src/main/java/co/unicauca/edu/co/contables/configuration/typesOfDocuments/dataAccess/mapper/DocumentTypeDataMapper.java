package co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import org.mapstruct.*;

/** @brief Mapper de datos para convertir entre entidades y modelos de dominio de tipos de documentos */
@Mapper(componentModel = "spring")
public interface DocumentTypeDataMapper {

    @Mappings({
            @Mapping(target = "documentClass", expression = "java(mapDocumentClass(domain.getDocumentClassId()))"),
            @Mapping(target = "module", expression = "java(mapModuleIdToName(domain.getModuleId()))"),
            @Mapping(target = "tenantId", ignore = true)
    })
    DocumentTypeEntity toEntity(DocumentType domain);

    @Mappings({
            @Mapping(target = "documentClassId", expression = "java(entity.getDocumentClass() != null ? entity.getDocumentClass().getId() : null)"),
            @Mapping(target = "moduleId", expression = "java(mapModuleNameToId(entity.getModule()))")
    })
    DocumentType toDomain(DocumentTypeEntity entity);

    default DocumentClassEntity mapDocumentClass(Long id) {
        if (id == null) return null;
        DocumentClassEntity ref = new DocumentClassEntity();
        ref.setId(id);
        return ref;
    }

    default String mapModuleIdToName(Integer moduleId) {
        if (moduleId == null) return null;
        try {
            DocumentModule module = DocumentModule.fromId(moduleId);
            return module.getName();
        } catch (Exception e) {
            return null;
        }
    }

    default Integer mapModuleNameToId(String moduleName) {
        if (moduleName == null || moduleName.trim().isEmpty()) return null;
        try {
            DocumentModule module = DocumentModule.fromName(moduleName);
            return module.getId();
        } catch (Exception e) {
            return null;
        }
    }
}


