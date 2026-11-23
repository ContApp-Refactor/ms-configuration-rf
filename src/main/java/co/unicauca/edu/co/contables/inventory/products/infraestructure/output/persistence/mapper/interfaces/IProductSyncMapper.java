package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;

/**
 * @brief Mapper para transformación de productos a DTOs de sincronización
 *
 * Define contratos de mapeo de entidades ProductEntity a DTOs ProductSyncDto
 * utilizados en operaciones de sincronización entre sistemas.
 */
@Mapper(componentModel = "spring")
public interface IProductSyncMapper {
    /**
     * @brief Convierte entidad de producto a DTO de sincronización
     * @param productEntity entidad ProductEntity de persistencia
     * @return ProductSyncDto para operaciones de sincronización
     */
    @Mapping(target = "productId", source = "id")
    ProductSyncDto toDto(ProductEntity productEntity);

    /**
     * @brief Convierte lista de entidades a lista de DTOs de sincronización
     * @param productEntities lista de entidades ProductEntity
     * @return lista de ProductSyncDto para sincronización
     */
    List<ProductSyncDto> toDto(List<ProductEntity> productEntities);
}
