package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper para transformación entre dominio y persistencia de productos
 *
 * Define contratos de mapeo bidireccional entre entidades JPA ProductEntity
 * y objetos de dominio Product para operaciones de persistencia.
 */
@Mapper(componentModel = "spring")
public interface IProductPersistenceMapper {

    /**
     * @brief Convierte objeto de dominio a entidad JPA
     * @param product objeto Product del dominio
     * @return ProductEntity correspondiente para persistencia
     */
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    ProductEntity toProductEntity(Product product);

    /**
     * @brief Convierte entidad JPA a objeto de dominio
     * @param productEntity ProductEntity de persistencia
     * @return objeto Product del dominio
     */
    Product toProduct(ProductEntity productEntity);

    /**
     * @brief Convierte lista de entidades JPA a lista de objetos de dominio
     * @param productList lista de ProductEntity de persistencia
     * @return lista de objetos Product del dominio
     */
    List<Product> toProductList(List<ProductEntity> productList);
}
