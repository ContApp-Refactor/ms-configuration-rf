package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.mapper;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response.DocumentTypeRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** @brief Mapper de dominio para convertir entre modelos de dominio y DTOs de tipos de documentos */
@Mapper(componentModel = "spring")
public interface DocumentTypeDomainMapper {

    @Mapping(target = "id", ignore = true)
    DocumentType toDomain(DocumentTypeCreateReq req);

    DocumentType toDomain(DocumentTypeUpdateReq req);

    DocumentTypeRes toRes(DocumentType domain);
}


