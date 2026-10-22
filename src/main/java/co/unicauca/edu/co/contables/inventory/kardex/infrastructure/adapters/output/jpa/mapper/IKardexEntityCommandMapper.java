package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IKardexEntityCommandMapper {
    @Mapping(target = "tenantId", ignore = true)
    KardexEntity toEntity(Kardex kardex);
    
    Kardex toDomain(KardexEntity kardexEntity);
}
