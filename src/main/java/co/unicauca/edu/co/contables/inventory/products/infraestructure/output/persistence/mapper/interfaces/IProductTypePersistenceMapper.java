package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para transformación entre dominio y persistencia de tipos de producto
 *
 * Define contratos de mapeo bidireccional entre entidades JPA ProductTypeEntity
 * y objetos de dominio ProductType para operaciones de persistencia.
 */
@Mapper(componentModel = "spring")
public interface IProductTypePersistenceMapper {

    /**
     * @brief Convierte objeto de dominio a entidad JPA
     * @param productType objeto ProductType del dominio
     * @return ProductTypeEntity correspondiente para persistencia
     */
    ProductTypeEntity toProductTypeEntity(ProductType productType);

    /**
     * @brief Convierte entidad JPA a objeto de dominio
     * @param productTypeEntity ProductTypeEntity de persistencia
     * @return objeto ProductType del dominio
     */
    ProductType toProductType(ProductTypeEntity productTypeEntity);

    /**
     * @brief Convierte lista de entidades JPA a lista de objetos de dominio
     * @param productTypeEntityList lista de ProductTypeEntity de persistencia
     * @return lista de objetos ProductType del dominio
     */
    List<ProductType> toProductTypeList(List<ProductTypeEntity> productTypeEntityList);
}
