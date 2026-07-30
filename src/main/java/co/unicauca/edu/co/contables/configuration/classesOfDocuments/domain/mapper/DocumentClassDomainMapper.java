package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.response.DocumentClassRes;

/**
 * @brief Mapeador de dominio para clases de documento
 *
 * Interfaz que utiliza MapStruct para convertir entre modelos de dominio
 * (DocumentClass) y DTOs de presentación (request/response).
 * Gestiona la transformación de datos entre la capa de dominio y la presentación.
 */
@Mapper(componentModel = "spring")
public interface DocumentClassDomainMapper {
    @Mapping(target = "id", ignore = true)
    DocumentClass toDomain(DocumentClassCreateReq req);

    DocumentClass toDomain(DocumentClassUpdateReq req);

    DocumentClassRes toRes(DocumentClass domain);
}


