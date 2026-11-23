package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;

/**
 * @brief Mapper for Product Broker DTOs
 * 
 * Handles conversion between ProductAsyncDto and Product domain model.
 */
@Mapper(componentModel = "spring")
public interface ProductBrokerMapper {  

    /**
     * @brief Converts Async DTO to domain model
     * @param productAsyncDto The DTO received from message broker
     * @return Product domain object
     */
    @Mapping(target = "id", ignore = true)
    Product toDomain(ProductAsyncDto productAsyncDto);
    
}
