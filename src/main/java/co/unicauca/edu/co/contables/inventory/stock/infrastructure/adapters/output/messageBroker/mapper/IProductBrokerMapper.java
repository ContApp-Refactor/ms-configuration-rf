package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;

@Mapper(componentModel = "spring")
public interface IProductBrokerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quantity", constant = "0")
    @Mapping(target = "price", constant = "0.0")
    @Mapping(target = "state", constant = "true")
    Stock toDomain(ProductAsyncDto productAsyncDto);
    
}
