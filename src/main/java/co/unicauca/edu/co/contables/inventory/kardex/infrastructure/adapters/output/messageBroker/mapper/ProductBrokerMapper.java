package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface ProductBrokerMapper {
    @Mapping(target = "id", ignore = true)
    Product toDomain(ProductAsyncDto productSyncDto);
}
