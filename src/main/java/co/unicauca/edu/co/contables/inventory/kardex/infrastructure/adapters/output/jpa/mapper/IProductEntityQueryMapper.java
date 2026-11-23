package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface IProductEntityQueryMapper {
    Product toDomain(ProductEntity productEntity);
}
