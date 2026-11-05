package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.ProductDTOResponse;


@Mapper(componentModel = "spring")
public interface IProductResponseMapper {
    
   //@Mapping(target = "recordsKardex", ignore=true)
    ProductDTOResponse toDtoResponse(Product product);
}
