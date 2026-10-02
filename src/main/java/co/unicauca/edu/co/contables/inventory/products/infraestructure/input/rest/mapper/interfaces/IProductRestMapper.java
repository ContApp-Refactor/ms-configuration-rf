package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ProductResponse;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para transformación de entidades de producto
 *
 * Define contratos de mapeo entre entidades de dominio Product
 * y DTOs de request/response para operaciones REST.
 */
@Mapper(componentModel = "spring")
public interface IProductRestMapper {

    /**
     * @brief Convierte DTO de creación a entidad de producto
     * @param productCreateRequest solicitud de creación de producto
     * @return entidad de producto convertida
     */
    Product toProduct(ProductCreateRequest productCreateRequest);

    /**
     * @brief Convierte entidad de producto a DTO de respuesta
     * @param product entidad de producto
     * @return respuesta de producto convertida
     */
    ProductResponse toProductResponse(Product product);

    /**
     * @brief Convierte lista de entidades a lista de DTOs de respuesta
     * @param productList lista de entidades de producto
     * @return lista de respuestas de producto convertidas
     */
    List<ProductResponse> toProductResponseList(List<Product> productList);
}
