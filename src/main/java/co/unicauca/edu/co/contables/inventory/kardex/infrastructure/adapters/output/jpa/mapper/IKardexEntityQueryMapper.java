package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IKardexEntityQueryMapper {

    Kardex toDomain(KardexEntity kardexEntity);

    List<Kardex> toDomainList(List<KardexEntity> kardexEntities);
}
