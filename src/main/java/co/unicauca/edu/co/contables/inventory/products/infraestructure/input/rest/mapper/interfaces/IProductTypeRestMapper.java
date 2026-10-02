package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductTypeRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ProductTypeResponse;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper para transformación de entidades de tipo de producto
 *
 * Define contratos de mapeo entre entidades de dominio ProductType
 * y DTOs de request/response para operaciones REST.
 */
@Mapper(componentModel = "spring")
public interface IProductTypeRestMapper {

    /**
     * @brief Convierte DTO de solicitud a entidad de tipo de producto
     * @param productTypeRequest objeto de solicitud
     * @return entidad ProductType correspondiente
     */
    @Mapping(target = "id", ignore = true)
    ProductType toProductType(ProductTypeRequest productTypeRequest);

    /**
     * @brief Convierte entidad de tipo de producto a DTO de respuesta
     * @param productType entidad de tipo de producto
     * @return respuesta convertida
     */
    ProductTypeResponse toProductTypeResponse(ProductType productType);

    /**
     * @brief Convierte lista de DTOs a lista de entidades
     * @param productTypeRequestList lista de objetos de solicitud
     * @return lista de entidades ProductType correspondientes
     */
    List<ProductType> toProductTypeList(List<ProductTypeRequest> productTypeRequestList);
}
