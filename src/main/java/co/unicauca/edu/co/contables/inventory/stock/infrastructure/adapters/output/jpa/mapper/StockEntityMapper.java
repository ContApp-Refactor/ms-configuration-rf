package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

@Mapper(componentModel = "spring")
public interface StockEntityMapper {
    
    Stock toDomain(StockEntity entity);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    StockEntity toEntity(Stock domain);
}
