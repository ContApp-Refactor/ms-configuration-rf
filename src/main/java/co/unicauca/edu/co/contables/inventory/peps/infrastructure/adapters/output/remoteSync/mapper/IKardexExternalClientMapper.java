package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexBatchDTORequest;

/**
 * @brief Mapper for converting external kardex DTOs to domain models
 */
@Mapper(componentModel = "spring")
public interface IKardexExternalClientMapper {
    
     /**
     * @brief Converts external DTO to domain model
     * @param dto External kardex DTO
     * @return Domain kardex model
     * 
     */
    @Mapping(target = "product", source = "productId", qualifiedByName = "idToProduct")
    @Mapping(target = "idKardex", ignore = true)
    @Mapping(target = "date", ignore = true)
    Kardex toDomain(KardexBatchDTORequest dto);

    /**
     * @brief Maps a product ID to a Product domain object
     * @param productId The product identifier
     * @return Product object with the ID set, or null if ID is null
     */
    @Named("idToProduct")
    default Product mapIdToProduct(Long productId) {
        if (productId == null) {
            return null;
        }
        Product product = new Product();
        product.setProductId(productId); // Asignamos el ID al campo 'id' del producto
        return product;
    }
}
