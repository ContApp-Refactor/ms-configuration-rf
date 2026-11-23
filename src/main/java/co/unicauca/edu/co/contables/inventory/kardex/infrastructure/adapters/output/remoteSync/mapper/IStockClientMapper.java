package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.dto.StockBuyDtoRequest;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.dto.StockSellDtoRequest;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IStockClientMapper {

    StockBuyDtoRequest toDtoRequest(Stock stock);

    Stock toDomain(StockDtoResponse dto);

    StockSellDtoRequest toSellDtoRequest(Stock stock);

}
