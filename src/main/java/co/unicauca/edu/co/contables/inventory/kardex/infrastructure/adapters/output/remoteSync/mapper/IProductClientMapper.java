package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IProductClientMapper {

    ProductSyncDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    Product toDomain(ProductSyncDto dto);
}
