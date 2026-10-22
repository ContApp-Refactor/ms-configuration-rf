package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

@Mapper(componentModel = "spring")
public interface IStockEntityCommandMapper {

    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    StockEntity toEntity(Stock product);

    List<StockEntity> toEntity(List<Stock> products);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void updateEntityFromProduct(Stock product, @MappingTarget StockEntity entity);
}
