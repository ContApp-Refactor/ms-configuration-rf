package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.mapper;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @brief Mapper para conversión entre entidades JPA de IDs y modelos del dominio
 */
@Mapper(componentModel = "spring")
public interface IdPersistenceMapper {

    @Mapping(source = "ttId", target = "thirdTypeId" )
    @Mapping(source = "ttName", target = "thirdTypeName" )
    @Mapping(source = "ttentId", target = "entId" )
    ThirdType toThirdType(ThirdTypeEntity thirdTypeEntity);

    @Mapping(target =  "ttId", source = "thirdTypeId" )
    @Mapping(target =  "ttName", source = "thirdTypeName" )
    @Mapping(target =  "ttentId", source = "entId" )
    @Mapping(target = "tenantId", ignore = true)
    ThirdTypeEntity toThirdTypeEntity(ThirdType thirdType);

    List<ThirdTypeEntity> toThirdTypeEntityList(List<ThirdType> thirdTypes);

    List<ThirdType> toThirdTypeList(List<ThirdTypeEntity> thirdTypeEntities);

    @Mapping(target =  "id", source = "id" )
    @Mapping(target =  "tiId", source = "typeId" )
    @Mapping(target =  "tiName", source = "typeIdname")
    @Mapping(target =  "tientId", source = "entId" )
    @Mapping(target =  "classification", source = "classification")
    @Mapping(target = "tenantId", ignore = true)
    TypeIdEntity toTypeIdEntity(TypeId typeId);

    @Mapping(source =  "id", target = "id" )
    @Mapping(source =  "tiId", target = "typeId" )
    @Mapping(source =  "tiName", target = "typeIdname")
    @Mapping(source =  "tientId", target = "entId" )
    @Mapping(source =  "classification", target = "classification")
    TypeId toTypeId(TypeIdEntity typeIdEntity);

    List<TypeIdEntity> toTypeIdEntityList(List<TypeId> typeIds);

    List<TypeId> toTypeIdList(List<TypeIdEntity> typeIdEntities);
}
