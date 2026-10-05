package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

@Mapper(componentModel = "spring")
public interface IProductEntityCommandMapper {

    @Mapping(target = "tenantId", ignore = true)
    ProductEntity toEntity(Product product);

    List<ProductEntity> toEntity(List<Product> products);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void updateEntityFromProduct(Product product, @MappingTarget ProductEntity entity);
}
