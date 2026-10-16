package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper;



import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;

@Mapper(componentModel = "spring")
/**
 * @brief Mapper for Product entity conversions
 * 
 * Handles transformation between Product domain model and ProductEntity.
 */
public interface  IProductEntityMapper {

    /**
     * @brief Converts entity to domain model
     * @param productEntity The ProductEntity
     * @return Product domain object
     */
    @Mapping(target = "recordsKardex", ignore = true)
    Product toDomain(ProductEntity productEntity);

    /**
     * @brief Converts domain model to entity
     * @param product The Product domain object
     * @return ProductEntity
     */
    @Mapping(target = "recordsKardex", ignore = true)
    ProductEntity toEntity(Product product);

    /**
     * @brief Converts list of domain models to list of entities
     * @param products List of Product domain objects
     * @return List of ProductEntity
     */
    List<ProductEntity> toEntity(List<Product> products);


    /**
     * @brief Updates an existing entity from a domain model
     * @param product Source Product domain object
     * @param entity Target ProductEntity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recordsKardex", ignore = true)
    void updateEntityFromProduct(Product product, @MappingTarget ProductEntity entity);


}
