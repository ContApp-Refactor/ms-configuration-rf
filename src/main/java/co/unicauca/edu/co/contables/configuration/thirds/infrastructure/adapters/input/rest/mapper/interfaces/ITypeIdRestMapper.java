package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.TypeIdCreateRequest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @brief Interfaz mapper para tipos de identificación
 */
@Mapper(componentModel = "spring")
public interface ITypeIdRestMapper {

    TypeId toTypeId(TypeIdCreateRequest typeIdCreateRequest);

    TypeId toTypeIdResponse(TypeId typeId);

    List<TypeId> toTypeIdResponseList(List<TypeId> typeIdList);
}
