package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.request.StockBuyDtoRequest;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.request.StockSellDtoRequest;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;

@Mapper(componentModel = "spring")
public interface IStockRestMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "enterpriseId", ignore = true)
    @Mapping(target = "state", ignore = true)
    Stock toDomain(StockBuyDtoRequest stockDtoRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "enterpriseId", ignore = true)
    @Mapping(target = "state", ignore = true)
    Stock toDomain(StockSellDtoRequest stockDtoRequest);

    StockDtoResponse toDtoResponse(Stock stock);
}
