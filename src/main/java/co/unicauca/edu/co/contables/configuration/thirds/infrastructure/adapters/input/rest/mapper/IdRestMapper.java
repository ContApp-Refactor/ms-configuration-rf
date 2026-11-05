package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdTypeCreateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdTypeUpdateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.TypeIdCreateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.TypeIdUpdateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ThirdTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @brief Mapper para conversión entre DTOs de tipos de tercero e identificación
 */
@Mapper(componentModel = "spring")
public interface IdRestMapper {

    ThirdTypeCreateRequest toThirdTypeCreateRequest(ThirdType thirdType);

    @Mapping(target = "status", defaultValue = "true")
    ThirdType toThirdType(ThirdTypeCreateRequest thirdTypeCreateRequest);

    TypeIdCreateRequest toTypeIdCreateRequest(TypeId typeId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "typeIdname", source = "typeIdname")
    @Mapping(target = "entId", source = "entId")
    @Mapping(target = "typeId", source = "typeId")
    @Mapping(target = "classification", source = "classification")
    TypeId toTypeId(TypeIdCreateRequest typeIdCreateRequest);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "typeIdname", source = "typeIdname")
    @Mapping(target = "entId", source = "entId")
    @Mapping(target = "typeId", source = "typeId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "classification", source = "classification")
    TypeId toTypeId(TypeIdUpdateRequest typeIdUpdateRequest);

    @Mapping(target = "thirdTypeName", source = "thirdTypeName")
    @Mapping(target = "entId", source = "entId")
    @Mapping(target = "thirdTypeId", source = "thirdTypeId")
    @Mapping(target = "status", source = "status")
    ThirdType toThirdType(ThirdTypeUpdateRequest thirdTypeUpdateRequest);

    ThirdTypeResponse toThirdTypeResponse(ThirdType thirdType);

    List<ThirdTypeResponse> toThirdTypeResponseList(List<ThirdType> thirdTypes);
} 