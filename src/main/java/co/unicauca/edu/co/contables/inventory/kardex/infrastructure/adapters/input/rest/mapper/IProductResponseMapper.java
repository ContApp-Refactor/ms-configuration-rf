package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.ProductDtoResponse;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IProductResponseMapper {
    ProductDtoResponse toDtoResponse(Product product);
}
