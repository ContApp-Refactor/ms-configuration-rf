package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

/**
 * @brief Mapper for Product synchronization objects
 *
 * implementationName desambigua el nombre del bean generado: stock tiene otro
 * mapper con la misma interfaz y Spring rechaza dos beans con el mismo nombre.
 * 
 * Handles conversion between Product domain models and ProductSyncDto
 * used for synchronization with external services.
 */
@Mapper(componentModel = "spring", implementationName = "PepsProductClientMapperImpl")
public interface IProductClientMapper {

    /**
     * @brief Converts Product domain model to Sync DTO
     * @param product The product domain object
     * @return The synchronization DTO
     */
    ProductSyncDto toDto(Product product);
    
    /**
     * @brief Converts Sync DTO to Product domain model
     * @param dto The synchronization DTO
     * @return The product domain object
     */
    @Mapping(target = "id", ignore = true)
    Product toDomain(ProductSyncDto dto);
}
