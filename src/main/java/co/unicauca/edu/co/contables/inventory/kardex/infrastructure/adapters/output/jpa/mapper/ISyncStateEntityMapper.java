package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface ISyncStateEntityMapper {
    SyncState toDomain(SyncStateEntity entity);
    SyncStateEntity toEntity(SyncState domain);
}
