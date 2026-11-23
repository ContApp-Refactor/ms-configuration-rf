package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
/**
 * @brief Mapper for Kardex RabbitMQ DTOs
 * 
 * Handles conversion between KardexRabbitDto and Kardex domain model.
 */

@Mapper(componentModel = "spring")
public interface IKardexRabbitMQMapper {

    /**
     * @brief Converts RabbitMQ DTO to domain model
     * @param kardexRabbitDto The DTO received from RabbitMQ
     * @return Kardex domain object
     */
    @Mapping(target = "product.productId", source = "productId")
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "idKardex", ignore = true)
    Kardex toDomain(KardexRabbitDto kardexRabbitDto);


    /**
     * @brief Helper method to map product ID to Product object
     * @param productId The product ID
     * @return Product object with ID set
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
