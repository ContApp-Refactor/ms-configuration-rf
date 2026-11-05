package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

@Mapper(componentModel = "spring")
public interface IProductClientMapper {

    @Mapping(target = "presentation", ignore = true)
    @Mapping(target = "reference", ignore = true)
    ProductSyncDto toDto(Stock product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "price", constant = "0")
    @Mapping(target = "quantity", constant = "0")
    Stock toDomain(ProductSyncDto dto);
}
