package co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;

/**
 * @brief Mapeador de datos para clases de documento
 *
 * Interfaz que utiliza MapStruct para convertir entre entidades de base de datos
 * (DocumentClassEntity) y modelos de dominio (DocumentClass).
 * Gestiona la transformación automática de datos entre capas.
 */
@Mapper(componentModel = "spring")
public interface DocumentClassDataMapper {
    @Mappings({
        @Mapping(target = "tenantId", ignore = true)
    })
    DocumentClassEntity toEntity(DocumentClass domain);
    DocumentClass toDomain(DocumentClassEntity entity);
}


