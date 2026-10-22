package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;

@Component
public class ProductMapper {
    
    public Product toDomain(ProductEntity entity) {
        if (entity == null) return null;
        
        return Product.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .reference(entity.getReference())
                .name(entity.getName())
                .presentation(entity.getPresentation())
                .enterpriseId(entity.getEnterpriseId())
                .state(entity.isState())
                .build();
    }

    public ProductEntity toEntity(Product domain) {
        if (domain == null) return null;
        
        ProductEntity entity = new ProductEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setReference(domain.getReference());
        entity.setName(domain.getName());
        entity.setPresentation(domain.getPresentation());
        entity.setEnterpriseId(domain.getEnterpriseId());
        entity.setState(domain.isState());
        return entity;
    }
}