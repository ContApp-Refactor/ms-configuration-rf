package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {
    ProductEntity toProductEntity(Product product);
    
    Product toProduct(ProductEntity productEntity);
}
